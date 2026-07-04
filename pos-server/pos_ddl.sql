-- ======================================================================
-- 收銀機系統 (POS System) 基礎資料表建置 SQL (PostgreSQL)
-- ======================================================================

-- 1. 品類表 Category (支援無限層級自關聯)
CREATE TABLE category (
    id SERIAL PRIMARY KEY,
    parent_id INT REFERENCES category(id) ON DELETE SET NULL,
    name VARCHAR(100) NOT NULL
);

-- 2. 供應商表 Supplier
CREATE TABLE supplier (
    id SERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL, -- 供應商代碼 (業務鍵)
    name VARCHAR(100) NOT NULL,
    vat_number VARCHAR(20),           -- 統一編號
    address VARCHAR(255),
    phone VARCHAR(50),
    contact_person VARCHAR(100)
);

-- 3. 商品表 Product
CREATE TABLE product (
    id SERIAL PRIMARY KEY,
    sku VARCHAR(50) UNIQUE NOT NULL,  -- 貨號/庫存單位 (業務鍵)
    barcode VARCHAR(100),             -- 條碼
    name VARCHAR(100) NOT NULL,
    specification VARCHAR(255),       -- 規格
    category_id INT REFERENCES category(id) ON DELETE SET NULL,
    supplier_id INT REFERENCES supplier(id) ON DELETE SET NULL
);

-- 4. 商品價格表 Product_Price (紀錄價格歷史)
CREATE TABLE product_price (
    id SERIAL PRIMARY KEY,
    product_id INT REFERENCES product(id) ON DELETE CASCADE,
    price DECIMAL(10, 2) NOT NULL,    -- 使用 DECIMAL 確保金額精確度
    start_date DATE NOT NULL,
    end_date DATE,                    -- 若為最新價格，此欄位可為 NULL
    description TEXT
);

-- 5. 商品狀態/庫存變動紀錄表 Product_Status_Log
CREATE TABLE product_status_log (
    id SERIAL PRIMARY KEY,
    product_id INT REFERENCES product(id) ON DELETE CASCADE,
    status_code VARCHAR(50) NOT NULL, -- 例如: 'SELL', 'RESTOCK', 'DEFECTIVE'
    quantity INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6. 收銀員表 Cashier
CREATE TABLE cashier (
    id SERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL, -- 員工編號
    name VARCHAR(100) NOT NULL
);

-- 7. 收銀機表 POS
CREATE TABLE pos (
    id SERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL, -- 機台編號
    ip VARCHAR(50),
    location VARCHAR(100),
    description TEXT
);

-- 8. 會員表 Member
CREATE TABLE member (
    id SERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL, -- 會員卡號
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(50)
);

-- 9. 付款方式表 Payment_Method
CREATE TABLE payment_method (
    id SERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL, -- 例如: 'CASH', 'CREDIT_CARD', 'LINE_PAY'
    description VARCHAR(100)
);

-- 10. 優惠活動表 Discount
CREATE TABLE discount (
    id SERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL, 
    type VARCHAR(50) NOT NULL,        -- 例如: 'PERCENTAGE' (打折), 'FIXED' (折抵固定金額)
    threshold DECIMAL(10, 2),         -- 門檻 (例如: 滿 1000 元)
    value DECIMAL(10, 2) NOT NULL,    -- 折扣值 (例如: 0.9 或 100)
    description TEXT
);

-- ======================================================================
-- 交易核心資料表
-- ======================================================================

-- 11. 交易主檔 Transaction
CREATE TABLE transaction (
    id SERIAL PRIMARY KEY,
    transaction_no VARCHAR(50) UNIQUE NOT NULL, -- 交易單號 (由程式端生成，防碰撞)
    transaction_type VARCHAR(50) NOT NULL, -- 交易類別
    cashier_id INT REFERENCES cashier(id) ON DELETE RESTRICT,
    pos_id INT REFERENCES pos(id) ON DELETE RESTRICT,
    member_id INT REFERENCES member(id) ON DELETE SET NULL, -- 允許非會員交易
    total_amount DECIMAL(10, 2) NOT NULL,       -- 原價總金額
    discount_amount DECIMAL(10, 2) DEFAULT 0,   -- 折扣總金額
    net_amount DECIMAL(10, 2) NOT NULL,         -- 最終應付金額
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    remark TEXT
);

-- 12. 交易品項明細 Transaction_Item
CREATE TABLE transaction_item (
    id SERIAL PRIMARY KEY,
    transaction_id INT REFERENCES transaction(id) ON DELETE CASCADE,
    product_id INT REFERENCES product(id) ON DELETE RESTRICT,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,         -- 結帳當下的商品單價
    subtotal DECIMAL(10, 2) NOT NULL            -- 小計 (數量 * 單價)
);

-- 13. 交易付款明細 Transaction_Payment
CREATE TABLE transaction_payment (
    id SERIAL PRIMARY KEY,
    transaction_id INT REFERENCES transaction(id) ON DELETE CASCADE,
    payment_method_id INT REFERENCES payment_method(id) ON DELETE RESTRICT,
    amount DECIMAL(10, 2) NOT NULL,             -- 該付款方式支付的金額
    pay_amount DECIMAL(10, 2) NOT NULL,         -- 客戶實際拿出的金額 (例如給1000鈔票)
    change_amount DECIMAL(10, 2) DEFAULT 0      -- 找零金額
);

-- 14. 交易優惠明細 Transaction_Discount
CREATE TABLE transaction_discount (
    id SERIAL PRIMARY KEY,
    transaction_id INT REFERENCES transaction(id) ON DELETE CASCADE,
    discount_id INT REFERENCES discount(id) ON DELETE RESTRICT,
    amount DECIMAL(10, 2) NOT NULL              -- 該活動實際折抵掉的金額
);