-- 交易發件箱 (準備送給 MQ 的資料)
CREATE TABLE outbox_events (
                               id INT AUTO_INCREMENT PRIMARY KEY,
                               aggregate_id VARCHAR(50),    -- 關聯的 order_id
                               event_type VARCHAR(50),      -- 例如 'ORDER_CREATED'
                               payload TEXT,                -- 將 offline_orders, items, payments 組裝成 JSON 存在這裡
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 本地商品快取表
CREATE TABLE local_products (
                                item_id VARCHAR(50) PRIMARY KEY,
                                name VARCHAR(100) NOT NULL,
                                original_price DOUBLE NOT NULL,  -- 改名為原價
                                promotional_price DOUBLE,        -- 促銷特價 (若無特價則為 NULL)
                                promo_start TIMESTAMP,           -- 特價開始時間
                                promo_end TIMESTAMP,             -- 特價結束時間
                                is_active BOOLEAN DEFAULT TRUE,  -- 是否停售
                                category_name VARCHAR(50)
);

-- 本地訂單明細表
CREATE TABLE local_order_items (
                                   id INT AUTO_INCREMENT PRIMARY KEY,
                                   order_id VARCHAR(50),
                                   item_id VARCHAR(50),
                                   quantity INT NOT NULL,
                                   original_unit_price DOUBLE NOT NULL, -- 記錄原單價
                                   final_unit_price DOUBLE NOT NULL,    -- 記錄折價後的單價
                                   discount_amount DOUBLE DEFAULT 0,    -- 這一列幫客人省了多少錢
                                   applied_promo_id VARCHAR(50),        -- 記錄是因為哪個活動折價的 (追蹤用)
                                   subtotal DOUBLE NOT NULL             -- 最終小計
);

-- 本地訂單主表
CREATE TABLE local_orders (
                              order_id VARCHAR(50) PRIMARY KEY,
                              order_type VARCHAR(50),
                              status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
                              member_id VARCHAR(50),
                              original_total DOUBLE NOT NULL DEFAULT 0,      -- 未打折前的購物車總額
                              cart_discount_amount DOUBLE DEFAULT 0, -- 全館滿減活動扣掉的總額
                              final_amount DOUBLE NOT NULL DEFAULT 0,        -- 客人最終實際付的錢！
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              remarks TEXT
);

-- 本地訂單付款明細表 (支援複合支付)
CREATE TABLE local_order_payments (
                                      id INT AUTO_INCREMENT PRIMARY KEY,
                                      order_id VARCHAR(50),               -- 關聯到 local_orders
                                      payment_method_id VARCHAR(50),      -- 付款方式代碼 (例如：CASH, CREDIT_CARD, LINE_PAY)
                                      amount DOUBLE NOT NULL,             -- 這筆付款分攤的訂單金額
                                      pay_amount DOUBLE NOT NULL,         -- 客人實際拿出的金額 (例如拿 1000 元現鈔)
                                      change_amount DOUBLE DEFAULT 0      -- 找零金額
);

-- 本地訂單優惠明細表 (記錄這筆訂單到底用了哪些活動、折了多少錢)
CREATE TABLE local_order_promotions (
                                        id INT AUTO_INCREMENT PRIMARY KEY,
                                        order_id VARCHAR(50) NOT NULL,        -- 關聯到 local_orders
                                        promo_id VARCHAR(50) NOT NULL,        -- 關聯到 local_promotions (使用的活動代碼)
                                        discount_amount DOUBLE NOT NULL,      -- 該活動在這筆訂單「總共」幫客人折抵了多少錢
                                        applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 本地促銷主檔
CREATE TABLE local_promotions (
                                  promo_id VARCHAR(50) PRIMARY KEY,
                                  name VARCHAR(100) NOT NULL,
                                  priority INT NOT NULL,              -- 優先級 (數字越小越先計算，例：1 最優先)
                                  start_time TIMESTAMP,
                                  end_time TIMESTAMP,
                                  target_member_tier VARCHAR(20),     -- 目標會員等級 (NULL表所有人, 'VIP', 'NORMAL')
                                  is_stackable BOOLEAN DEFAULT FALSE  -- 是否可與其他活動疊加 (非常重要！)
);

-- 本地促銷規則
CREATE TABLE local_promo_rules (
                                   rule_id VARCHAR(50) PRIMARY KEY,
                                   promo_id VARCHAR(50),               -- 關聯到 local_promotions
                                   rule_type VARCHAR(50),              -- 規則類型 (例：BUNDLE, NTH_ITEM, CART_THRESHOLD)
                                   condition_payload TEXT,             -- 觸發條件 (JSON格式)
                                   action_payload TEXT                 -- 折扣結果 (JSON格式)
);

-- 本地款別(付款方式)快取表
CREATE TABLE local_payment_methods (
                                       code VARCHAR(50) PRIMARY KEY,       -- 款別代碼 (例如：CASH, CREDIT_CARD, LINE_PAY)
                                       description VARCHAR(100)
);

-- 建立一個名為 order_seq 的序號產生器
-- 起始值為 1，每次遞增 1
CREATE SEQUENCE IF NOT EXISTS order_seq START WITH 1 INCREMENT BY 1;