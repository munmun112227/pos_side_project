<script setup>
import { ref, computed, onMounted } from 'vue';

// --- API State ---
const orderId = ref(null);
const orderStatus = ref('NO_TRANSACTION'); // NO_TRANSACTION, DRAFT, CHECKOUT_PENDING, PAID, CANCELLED
const memberId = ref('');
const memberTier = ref('NORMAL');
const cartItems = ref([]); // { itemId, name, quantity, originalUnitPrice, finalUnitPrice, discountAmount, subtotal }
const appliedPromotions = ref([]);
const recentAuditLogs = ref([]); // Local log of completed/cancelled transactions

// --- Scanner Autocomplete ---
const searchInput = ref('');
const searchResults = ref([]);
const showSuggestions = ref(false);
const inputQty = ref(1);

// --- Checkout & Payment State ---
const paymentMethod = ref('CASH'); // CASH, CREDIT_CARD
const cashPayAmount = ref(0);
const cardNo = ref('');
const authCode = ref('');
const paymentStatus = ref({
  receivableAmount: 0,
  totalPaidAmount: 0,
  balanceAmount: 0,
  fullyPaid: false,
  currentPayments: []
});

const alertMessage = ref(null);
const alertType = ref('info'); // info, success, error, warning

// --- Helper: Show Alert ---
const showAlert = (msg, type = 'info') => {
  alertMessage.value = msg;
  alertType.value = type;
  setTimeout(() => {
    if (alertMessage.value === msg) {
      alertMessage.value = null;
    }
  }, 4000);
};

// --- Computed Totals ---
const cartOriginalTotal = computed(() => {
  return cartItems.value.reduce((sum, item) => sum + (item.originalUnitPrice * item.quantity), 0);
});

const cartDiscountTotal = computed(() => {
  return cartItems.value.reduce((sum, item) => sum + (item.discountAmount || 0), 0) + 
         appliedPromotions.value.reduce((sum, promo) => sum + (promo.discountAmount || 0), 0);
});

const cartFinalTotal = computed(() => {
  return Math.max(0, cartOriginalTotal.value - cartDiscountTotal.value);
});

// --- API Calls ---

// 1. 建立新交易 (New Transaction)
const startNewTransaction = async () => {
  try {
    const res = await fetch('/api/checkout/new-transaction?orderType=RETAIL', {
      method: 'POST'
    });
    const data = await res.json();
    if (data.success) {
      orderId.value = data.orderId;
      orderStatus.value = 'DRAFT';
      cartItems.value = [];
      appliedPromotions.value = [];
      memberId.value = '';
      memberTier.value = 'NORMAL';
      paymentStatus.value = {
        receivableAmount: 0,
        totalPaidAmount: 0,
        balanceAmount: 0,
        fullyPaid: false,
        currentPayments: []
      };
      showAlert(`交易已成功建立！單號: ${data.orderId}`, 'success');
    } else {
      showAlert(data.message || '建立新交易失敗', 'error');
    }
  } catch (err) {
    showAlert(`系統連線失敗: ${err.message}`, 'error');
  }
};

// 2. 計算購物車 (Calculate Cart preview)
const calculateCart = async () => {
  if (!orderId.value) return;
  try {
    const payload = {
      memberId: memberId.value,
      memberTier: memberTier.value,
      items: cartItems.value.map(item => ({
        itemId: item.itemId,
        quantity: item.quantity
      }))
    };

    const res = await fetch('/api/cart/calculate', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    const data = await res.json();
    if (data.success) {
      cartItems.value = data.data.items;
      appliedPromotions.value = data.data.appliedPromotions;
    } else {
      showAlert(data.message || '計算失敗', 'error');
    }
  } catch (err) {
    showAlert(`連線失敗: ${err.message}`, 'error');
  }
};

// 3. 模糊搜尋商品名稱或商品 ID (Autocomplete)
const searchProducts = async () => {
  if (!searchInput.value.trim()) {
    searchResults.value = [];
    showSuggestions.value = false;
    return;
  }
  try {
    const res = await fetch(`/api/products/search?query=${encodeURIComponent(searchInput.value)}`);
    const data = await res.json();
    if (data.success) {
      searchResults.value = data.data;
      showSuggestions.value = searchResults.value.length > 0;
    }
  } catch (err) {
    console.error('搜尋失敗:', err);
  }
};

// 選擇搜尋建議商品
const selectProduct = (item) => {
  searchInput.value = item.itemId; // 帶入商品 ID
  searchResults.value = [];
  showSuggestions.value = false;
  showAlert(`已選擇商品: ${item.name} (${item.itemId})`, 'info');
};

// 4. 模擬掃描新增商品 (Add Item by Barcode/ID)
const addItemToCart = async () => {
  if (orderStatus.value !== 'DRAFT') {
    showAlert('請先建立新交易或返回交易草稿狀態', 'warning');
    return;
  }
  if (!searchInput.value.trim()) {
    showAlert('請先輸入商品編號、條碼或透過搜尋選擇商品', 'warning');
    return;
  }

  try {
    // 呼叫後端刷讀 API
    const res = await fetch(`/api/products/scan?barcode=${encodeURIComponent(searchInput.value)}`);
    const data = await res.json();
    if (data.success) {
      const product = data.data; // 包含 itemId, name, originalPrice, promotionalPrice, isWeightItem, quantity, calculatedPrice
      
      // 檢查是否已存在於購物車
      const existing = cartItems.value.find(i => i.itemId === product.itemId);
      // 生鮮秤重商品直接帶入條碼解析出的重量，一般品帶入數量輸入框的數量
      const qtyToAdd = product.isWeightItem ? product.quantity : inputQty.value;
      
      if (existing) {
        existing.quantity += qtyToAdd;
      } else {
        cartItems.value.push({
          itemId: product.itemId,
          name: product.name,
          quantity: qtyToAdd,
          originalUnitPrice: product.originalPrice,
          finalUnitPrice: product.promotionalPrice || product.originalPrice,
          discountAmount: 0.0,
          subtotal: (product.promotionalPrice || product.originalPrice) * qtyToAdd
        });
      }

      // 重新計算購物車
      await calculateCart();
      showAlert(`已成功刷入商品: ${product.name} x ${qtyToAdd}`, 'success');
      
      // 清空輸入
      searchInput.value = '';
      inputQty.value = 1;
    } else {
      showAlert(data.message || '查無此商品條碼/編號', 'error');
    }
  } catch (err) {
    showAlert(`連線失敗: ${err.message}`, 'error');
  }
};

// 移除商品
const removeItem = async (index) => {
  cartItems.value.splice(index, 1);
  await calculateCart();
};

// 5. 購物車輸入商品階段中途取消交易 (Abandon Cart)
const abandonCart = async () => {
  if (!orderId.value) return;
  if (!confirm('確認要放棄此交易？這將會清空畫面並存檔為「已取消交易」供稽核使用。')) return;

  try {
    const payload = {
      memberId: memberId.value,
      memberTier: memberTier.value,
      items: cartItems.value.map(item => ({
        itemId: item.itemId,
        quantity: item.quantity
      }))
    };

    const res = await fetch(`/api/cart/abandon?orderId=${orderId.value}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    const data = await res.json();
    if (data.success) {
      recentAuditLogs.value.unshift({
        orderId: data.orderId,
        status: 'CANCELLED',
        remark: '購物車階段取消',
        amount: cartFinalTotal.value
      });
      showAlert(`交易已取消，作廢單號: ${data.orderId}`, 'warning');
      resetAppState();
    } else {
      showAlert(data.message || '取消交易失敗', 'error');
    }
  } catch (err) {
    showAlert(`連線失敗: ${err.message}`, 'error');
  }
};

// 6. 確認商品，進入付款階段 (Prepare Checkout)
const prepareCheckout = async () => {
  if (!orderId.value) return;
  if (cartItems.value.length === 0) {
    showAlert('購物車內無商品，無法進行結帳', 'warning');
    return;
  }

  try {
    const payload = {
      memberId: memberId.value,
      memberTier: memberTier.value,
      items: cartItems.value.map(item => ({
        itemId: item.itemId,
        quantity: item.quantity
      }))
    };

    const res = await fetch(`/api/checkout/${orderId.value}/prepare`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    const data = await res.json();
    if (data.success) {
      orderStatus.value = 'CHECKOUT_PENDING';
      paymentStatus.value.receivableAmount = cartFinalTotal.value;
      paymentStatus.value.balanceAmount = cartFinalTotal.value;
      paymentStatus.value.totalPaidAmount = 0;
      paymentStatus.value.currentPayments = [];
      
      // 預設現金輸入值為剩餘金額
      cashPayAmount.value = cartFinalTotal.value;
      showAlert('已鎖定商品明細，進入付款介面', 'success');
    } else {
      showAlert(data.message || '進入結帳失敗', 'error');
    }
  } catch (err) {
    showAlert(`連線失敗: ${err.message}`, 'error');
  }
};

// 7. 執行單筆付款 (Submit Payment Step)
const submitPayment = async () => {
  if (!orderId.value) return;
  
  let details = null;
  let targetAmount = paymentStatus.value.balanceAmount;

  if (paymentMethod.value === 'CASH') {
    if (cashPayAmount.value < 0) {
      showAlert('實收現金不可為負數', 'warning');
      return;
    }
    const deductAmount = Math.min(paymentStatus.value.balanceAmount, cashPayAmount.value);
    targetAmount = deductAmount;
    details = {
      payAmount: cashPayAmount.value
    };
  } else if (paymentMethod.value === 'CREDIT_CARD') {
    details = {
      cardNo: cardNo.value || 'xxxx-xxxx-xxxx-8888',
      authCode: authCode.value || 'CC-AUTH-' + Math.floor(Math.random() * 900000 + 100000)
    };
  }

  const payload = {
    orderId: orderId.value,
    amount: targetAmount,
    paymentMethodId: paymentMethod.value,
    details: details
  };

  try {
    const res = await fetch(`/api/checkout/${orderId.value}/payment`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    const data = await res.json();
    if (data.success) {
      paymentStatus.value = data.data;
      showAlert('付款項目成功紀錄！', 'success');
      
      cardNo.value = '';
      authCode.value = '';
      cashPayAmount.value = paymentStatus.value.balanceAmount;

      if (paymentStatus.value.fullyPaid) {
        orderStatus.value = 'PAID';
        showAlert('此訂單已完全付清！', 'success');
      }
    } else {
      showAlert(data.message || '付款失敗', 'error');
    }
  } catch (err) {
    showAlert(`連線失敗: ${err.message}`, 'error');
  }
};

// 8. 作廢單筆已付的款項項目 (Void Payment)
const voidPayment = async (paymentId) => {
  try {
    const res = await fetch(`/api/checkout/${orderId.value}/payment/${paymentId}`, {
      method: 'DELETE'
    });
    const data = await res.json();
    if (data.success) {
      paymentStatus.value = data.data;
      cashPayAmount.value = paymentStatus.value.balanceAmount;
      showAlert('付款項目已作廢退刷', 'warning');
    } else {
      showAlert(data.message || '作廢失敗', 'error');
    }
  } catch (err) {
    showAlert(`連線失敗: ${err.message}`, 'error');
  }
};

// 9. 付款階段取消整筆交易 (Cancel Checkout)
const cancelTransaction = async () => {
  if (!confirm('確認要取消整筆付款？所有已成功的信用卡退刷與現金付款均會被物理還原作廢。')) return;
  try {
    const res = await fetch(`/api/checkout/${orderId.value}/cancel`, {
      method: 'POST'
    });
    const data = await res.json();
    if (data.success) {
      recentAuditLogs.value.unshift({
        orderId: orderId.value,
        status: 'CANCELLED',
        remark: '付款階段整筆取消',
        amount: cartFinalTotal.value
      });
      showAlert('付款已全部還原，交易已取消！', 'warning');
      resetAppState();
    } else {
      showAlert(data.message || '取消交易失敗', 'error');
    }
  } catch (err) {
    showAlert(`連線失敗: ${err.message}`, 'error');
  }
};

// 完檔並開始下一筆交易
const completeOrderAndNext = () => {
  recentAuditLogs.value.unshift({
    orderId: orderId.value,
    status: 'PAID',
    remark: '交易完成',
    amount: paymentStatus.value.receivableAmount
  });
  resetAppState();
  startNewTransaction();
};

const resetAppState = () => {
  orderId.value = null;
  orderStatus.value = 'NO_TRANSACTION';
  cartItems.value = [];
  appliedPromotions.value = [];
  memberId.value = '';
  memberTier.value = 'NORMAL';
  paymentStatus.value = {
    receivableAmount: 0,
    totalPaidAmount: 0,
    balanceAmount: 0,
    fullyPaid: false,
    currentPayments: []
  };
};
</script>

<template>
  <div class="pos-container">
    <!-- Header -->
    <header class="pos-header">
      <div class="logo-area">
        <span class="icon">🛒</span>
        <h1>Antigravity POS <span class="badge">V2.0</span></h1>
      </div>
      <div class="transaction-info">
        <span v-if="orderId" class="order-id">
          目前交易單號: <strong>{{ orderId }}</strong>
          <span :class="['status-pill', orderStatus.toLowerCase()]">
            {{ orderStatus === 'DRAFT' ? '草稿/輸入中' : orderStatus === 'CHECKOUT_PENDING' ? '付款中' : orderStatus }}
          </span>
        </span>
        <button v-else @click="startNewTransaction" class="btn-primary btn-pulse">
          ⚡ 啟動新交易
        </button>
      </div>
    </header>

    <!-- Global Alert Message -->
    <transition name="fade">
      <div v-if="alertMessage" :class="['alert-banner', alertType]">
        <span class="alert-icon">
          {{ alertType === 'success' ? '✅' : alertType === 'error' ? '❌' : alertType === 'warning' ? '⚠️' : 'ℹ️' }}
        </span>
        <span class="alert-text">{{ alertMessage }}</span>
      </div>
    </transition>

    <!-- Main Workspace -->
    <div class="pos-workspace" v-if="orderId">
      
      <!-- LEFT PANEL: Shopping Cart & Member Info -->
      <section class="workspace-panel cart-section" :class="{ 'disabled-overlay': orderStatus === 'CHECKOUT_PENDING' || orderStatus === 'PAID' }">
        <div class="panel-header">
          <h2>1. 購物車商品輸入</h2>
          <span class="step-num">Step 1</span>
        </div>

        <!-- Member Section -->
        <div class="glass-card member-card">
          <h3>👤 會員資料與促銷設定</h3>
          <div class="form-row">
            <div class="form-group">
              <label>會員卡號 ID</label>
              <input 
                type="text" 
                v-model="memberId" 
                placeholder="例如 MEM-VIP888" 
                @change="calculateCart"
              />
            </div>
            <div class="form-group">
              <label>會員等級 Tier</label>
              <select v-model="memberTier" @change="calculateCart">
                <option value="NORMAL">一般會員 (NORMAL)</option>
                <option value="VIP">高級會員 (VIP)</option>
              </select>
            </div>
          </div>
        </div>

        <!-- Scanner Simulator with Autocomplete -->
        <div class="glass-card scanner-card">
          <h3>🏷️ 條碼掃描模擬器 (動態查詢 H2 資料庫)</h3>
          <div class="form-row scanner-row">
            <div class="form-group flex-2 relative">
              <label>輸入商品名稱/條碼 (支援模糊搜尋與生鮮條碼)</label>
              <input 
                type="text" 
                v-model="searchInput" 
                placeholder="輸入商品 ID (如 ITEM-001, 生鮮秤重 2800101002507) 或關鍵字搜尋..." 
                @input="searchProducts"
                @focus="showSuggestions = searchResults.length > 0"
                @blur="setTimeout(() => showSuggestions = false, 250)"
                @keyup.enter="addItemToCart"
              />
              <!-- Autocomplete suggestions dropdown -->
              <transition name="fade">
                <ul class="autocomplete-dropdown" v-if="showSuggestions && searchResults.length > 0">
                  <li 
                    v-for="item in searchResults" 
                    :key="item.itemId"
                    @mousedown="selectProduct(item)"
                  >
                    <div class="suggestion-item">
                      <span class="s-name font-bold">{{ item.name }}</span>
                      <span class="s-id">{{ item.itemId }}</span>
                      <span class="s-price">${{ (item.promotionalPrice || item.originalPrice).toFixed(1) }}</span>
                    </div>
                  </li>
                </ul>
              </transition>
            </div>
            <div class="form-group flex-1">
              <label>一般商品數量</label>
              <input type="number" v-model.number="inputQty" min="1" />
            </div>
            <button @click="addItemToCart" class="btn-secondary btn-scan">
              ➕ 掃描商品
            </button>
          </div>
        </div>

        <!-- Scanned Items Table -->
        <div class="items-table-container">
          <table class="items-table">
            <thead>
              <tr>
                <th>商品名稱</th>
                <th class="text-right">原單價</th>
                <th class="text-right">數量</th>
                <th class="text-right">折後價</th>
                <th class="text-right">折扣</th>
                <th class="text-right">小計</th>
                <th class="text-center" v-if="orderStatus === 'DRAFT'">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="cartItems.length === 0">
                <td colspan="7" class="empty-placeholder">
                  🛒 目前購物車空空如也，請在上方輸入商品關鍵字搜尋並掃描。
                </td>
              </tr>
              <tr v-for="(item, idx) in cartItems" :key="item.itemId">
                <td>
                  <div class="item-name">{{ item.name }}</div>
                  <div class="item-id-sub">{{ item.itemId }}</div>
                </td>
                <td class="text-right">${{ item.originalUnitPrice.toFixed(1) }}</td>
                <td class="text-right">{{ item.quantity }}</td>
                <td class="text-right">${{ item.finalUnitPrice.toFixed(1) }}</td>
                <td class="text-right discount-text">
                  {{ item.discountAmount > 0 ? '-$' + item.discountAmount.toFixed(1) : '$0.0' }}
                </td>
                <td class="text-right font-bold">${{ item.subtotal.toFixed(1) }}</td>
                <td class="text-center" v-if="orderStatus === 'DRAFT'">
                  <button @click="removeItem(idx)" class="btn-icon-delete">🗑️</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Promotions Summary -->
        <div class="promotions-summary" v-if="appliedPromotions.length > 0">
          <h4>💡 已套用促銷活動</h4>
          <ul class="promo-list">
            <li v-for="promo in appliedPromotions" :key="promo.promoId">
              <span class="promo-badge">{{ promo.level }}</span>
              <span class="promo-name">{{ promo.name }}</span>
              <span class="promo-discount">-${{ promo.discountAmount.toFixed(1) }}</span>
            </li>
          </ul>
        </div>

        <!-- Totals summary block -->
        <div class="totals-block">
          <div class="total-row">
            <span>原始總金額:</span>
            <span>${{ cartOriginalTotal.toFixed(1) }}</span>
          </div>
          <div class="total-row discount-row">
            <span>累計優惠減免:</span>
            <span>-${{ cartDiscountTotal.toFixed(1) }}</span>
          </div>
          <div class="total-row final-row">
            <span>結帳應付金額:</span>
            <span>${{ cartFinalTotal.toFixed(1) }}</span>
          </div>
        </div>

        <!-- Left Actions -->
        <div class="panel-actions" v-if="orderStatus === 'DRAFT'">
          <button @click="abandonCart" class="btn-danger">
            ⚠️ 放棄/取消交易
          </button>
          <button @click="prepareCheckout" class="btn-success btn-lg">
            💸 確認進入付款
          </button>
        </div>
      </section>

      <!-- RIGHT PANEL: Payment / Checkout System -->
      <section class="workspace-panel payment-section" v-if="orderStatus === 'CHECKOUT_PENDING' || orderStatus === 'PAID'">
        <div class="panel-header">
          <h2>2. 複合式支付管理</h2>
          <span class="step-num">Step 2</span>
        </div>

        <!-- Split Payment Status Card -->
        <div class="glass-card payment-status-card">
          <div class="status-gauge">
            <div class="gauge-item">
              <span class="label">應收金額</span>
              <span class="value font-bold">${{ paymentStatus.receivableAmount.toFixed(1) }}</span>
            </div>
            <div class="gauge-item text-success">
              <span class="label">已收金額</span>
              <span class="value font-bold">${{ paymentStatus.totalPaidAmount.toFixed(1) }}</span>
            </div>
            <div class="gauge-item text-danger" v-if="!paymentStatus.fullyPaid">
              <span class="label">剩餘應收 (餘額)</span>
              <span class="value font-bold pulse-glow">${{ paymentStatus.balanceAmount.toFixed(1) }}</span>
            </div>
            <div class="gauge-item text-success flex-column-center" v-else>
              <span class="label">狀態</span>
              <span class="status-badge-paid">🎉 完全付清</span>
            </div>
          </div>
        </div>

        <!-- Added Payments Ledger -->
        <div class="glass-card ledger-card">
          <h3>📝 本次交易付款明細</h3>
          <table class="payments-table">
            <thead>
              <tr>
                <th>付款管道</th>
                <th class="text-right">扣抵金額</th>
                <th class="text-right">顧客實付</th>
                <th class="text-right">找零</th>
                <th class="text-center" v-if="orderStatus === 'CHECKOUT_PENDING'">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="paymentStatus.currentPayments.length === 0">
                <td colspan="5" class="empty-placeholder">
                  ⏳ 尚未加入任何付款明細。請在下方選擇支付方式。
                </td>
              </tr>
              <tr v-for="pay in paymentStatus.currentPayments" :key="pay.id">
                <td>
                  <span :class="['pay-method-badge', pay.paymentMethodId.toLowerCase()]">
                    {{ pay.paymentMethodId === 'CASH' ? '現金 CASH' : '信用卡 CARD' }}
                  </span>
                </td>
                <td class="text-right font-bold">${{ pay.amount.toFixed(1) }}</td>
                <td class="text-right">${{ pay.payAmount.toFixed(1) }}</td>
                <td class="text-right text-warning">
                  {{ pay.changeAmount > 0 ? '$' + pay.changeAmount.toFixed(1) : '$0.0' }}
                </td>
                <td class="text-center" v-if="orderStatus === 'CHECKOUT_PENDING'">
                  <button @click="voidPayment(pay.id)" class="btn-icon-delete">🗑️ 作廢</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Payment Method Selection & Inputs -->
        <div class="glass-card payment-options-card" v-if="!paymentStatus.fullyPaid">
          <h3>💰 選擇支付工具</h3>
          
          <div class="payment-tabs">
            <button 
              :class="['tab-btn', { active: paymentMethod === 'CASH' }]"
              @click="paymentMethod = 'CASH'"
            >
              💵 現金支付
            </button>
            <button 
              :class="['tab-btn', { active: paymentMethod === 'CREDIT_CARD' }]"
              @click="paymentMethod = 'CREDIT_CARD'"
            >
              💳 信用卡授權
            </button>
          </div>

          <!-- Cash Payment Inputs -->
          <div class="tab-content" v-if="paymentMethod === 'CASH'">
            <div class="form-group">
              <label>顧客支付現金總額 (若顧客拿整張現鈔)</label>
              <div class="input-with-currency">
                <span class="currency-symbol">$</span>
                <input type="number" v-model.number="cashPayAmount" min="0" />
              </div>
            </div>
            
            <div class="calculator-helpers">
              <button @click="cashPayAmount = paymentStatus.balanceAmount" class="helper-btn">
                剛好付清 (${{ paymentStatus.balanceAmount.toFixed(1) }})
              </button>
              <button @click="cashPayAmount = 100" class="helper-btn">$100</button>
              <button @click="cashPayAmount = 500" class="helper-btn">$500</button>
              <button @click="cashPayAmount = 1000" class="helper-btn">$1000</button>
            </div>

            <!-- Dynamic Change Preview -->
            <div class="change-preview-box" v-if="cashPayAmount > 0">
              <div class="preview-row">
                <span>欲抵扣金額:</span>
                <span>${{ Math.min(paymentStatus.balanceAmount, cashPayAmount).toFixed(1) }}</span>
              </div>
              <div class="preview-row change-row" v-if="cashPayAmount > paymentStatus.balanceAmount">
                <span>應找零金額:</span>
                <strong>${{ (cashPayAmount - paymentStatus.balanceAmount).toFixed(1) }}</strong>
              </div>
            </div>

            <button @click="submitPayment" class="btn-primary btn-submit-payment">
              Confirm Cash Payment 💵
            </button>
          </div>

          <!-- Credit Card Payment Inputs -->
          <div class="tab-content" v-if="paymentMethod === 'CREDIT_CARD'">
            <div class="card-simulator-box">
              <p class="simulator-notice">模擬刷卡機連線中... 點擊送出將自動產生模擬授權碼。</p>
              
              <div class="form-row">
                <div class="form-group flex-2">
                  <label>卡號末四碼 (選填)</label>
                  <input type="text" v-model="cardNo" placeholder="xxxx-xxxx-xxxx-8888" maxlength="19" />
                </div>
                <div class="form-group flex-1">
                  <label>外部授權碼 (選填)</label>
                  <input type="text" v-model="authCode" placeholder="自動產生" />
                </div>
              </div>
            </div>

            <button @click="submitPayment" class="btn-primary btn-submit-payment card-accent">
              Swipe Credit Card (${{ paymentStatus.balanceAmount.toFixed(1) }}) 💳
            </button>
          </div>
        </div>

        <!-- Final Actions -->
        <div class="panel-actions column-actions">
          <button v-if="orderStatus === 'CHECKOUT_PENDING'" @click="cancelTransaction" class="btn-danger btn-block">
            🚨 取消付款 (還原已付金額並關閉交易)
          </button>
          
          <button v-if="orderStatus === 'PAID'" @click="completeOrderAndNext" class="btn-success btn-block btn-lg btn-glow">
            ✅ 完檔開立發票，開始下一筆交易
          </button>
        </div>
      </section>
    </div>

    <!-- Empty state v-else -->
    <div class="empty-state-welcome" v-else>
      <div class="welcome-box glass-card">
        <div class="welcome-icon">⚡</div>
        <h2>歡迎使用 Antigravity POS 收銀系統</h2>
        <p>請點擊下方按鈕啟動新交易，開始進行條碼掃描與付款流程。</p>
        <button @click="startNewTransaction" class="btn-primary btn-lg btn-pulse">
          ⚡ 建立新交易編號
        </button>
      </div>
    </div>

    <!-- Bottom Audit Logs Bar -->
    <footer class="audit-logs-section">
      <div class="audit-header">
        <h3>🔍 系統作廢/結帳交易稽核日誌 (H2 持久化紀錄)</h3>
        <span class="audit-desc">即時記錄收銀中途取消及完檔以防帳務弊端</span>
      </div>
      <div class="log-cards-container">
        <div v-if="recentAuditLogs.length === 0" class="log-placeholder">
          目前尚無本機作廢或完成交易的紀錄。
        </div>
        <div 
          v-for="log in recentAuditLogs" 
          :key="log.orderId"
          :class="['log-card', log.status.toLowerCase()]"
        >
          <div class="log-row">
            <span class="log-order-id font-bold">{{ log.orderId }}</span>
            <span :class="['log-badge', log.status.toLowerCase()]">{{ log.status }}</span>
          </div>
          <div class="log-row sub-info">
            <span>{{ log.remark }}</span>
            <span class="font-bold">${{ log.amount.toFixed(1) }}</span>
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>

<style>
/* --- Core Variables & CSS Reset --- */
:root {
  --bg-gradient: linear-gradient(135deg, #111827 0%, #1f2937 100%);
  --panel-bg: rgba(31, 41, 55, 0.45);
  --card-bg: rgba(55, 65, 81, 0.4);
  --border-color: rgba(255, 255, 255, 0.08);
  --text-primary: #f9fafb;
  --text-secondary: #9ca3af;
  --accent-color: #6366f1;
  --accent-glow: rgba(99, 102, 241, 0.3);
  --success-color: #10b981;
  --success-glow: rgba(16, 185, 129, 0.3);
  --danger-color: #ef4444;
  --danger-glow: rgba(239, 68, 68, 0.3);
  --warning-color: #f59e0b;
}

body {
  margin: 0;
  padding: 0;
  background: #0f172a;
  color: var(--text-primary);
  font-family: 'Outfit', 'Inter', system-ui, sans-serif;
  overflow-x: hidden;
}

#app {
  width: 100% !important;
  max-width: 100% !important;
  min-height: 100vh;
  border-inline: none !important;
}

.pos-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  padding: 20px;
  box-sizing: border-box;
  gap: 20px;
}

/* --- Header --- */
.pos-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: rgba(30, 41, 59, 0.7);
  backdrop-filter: blur(10px);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  box-shadow: 0 4px 30px rgba(0, 0, 0, 0.2);
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-area h1 {
  font-size: 24px;
  margin: 0;
  font-weight: 700;
  letter-spacing: -0.5px;
  color: var(--text-primary);
}

.logo-area .badge {
  font-size: 11px;
  padding: 2px 6px;
  background: var(--accent-color);
  border-radius: 6px;
  font-weight: 600;
  vertical-align: middle;
}

.transaction-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.order-id {
  font-size: 14px;
  color: var(--text-secondary);
}

.order-id strong {
  color: var(--text-primary);
}

.status-pill {
  font-size: 11px;
  padding: 4px 8px;
  border-radius: 20px;
  margin-left: 8px;
  font-weight: 600;
  text-transform: uppercase;
}

.status-pill.draft {
  background: rgba(99, 102, 241, 0.2);
  color: #a5b4fc;
  border: 1px solid rgba(99, 102, 241, 0.4);
}

.status-pill.checkout_pending {
  background: rgba(245, 158, 11, 0.2);
  color: #fde047;
  border: 1px solid rgba(245, 158, 11, 0.4);
}

.status-pill.paid {
  background: rgba(16, 185, 129, 0.2);
  color: #6ee7b7;
  border: 1px solid rgba(16, 185, 129, 0.4);
}

/* --- Global Alert Banner --- */
.alert-banner {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  animation: slideDown 0.3s ease;
}

.alert-banner.info { background: rgba(59, 130, 246, 0.15); border: 1px solid rgba(59, 130, 246, 0.3); color: #93c5fd; }
.alert-banner.success { background: rgba(16, 185, 129, 0.15); border: 1px solid rgba(16, 185, 129, 0.3); color: #6ee7b7; }
.alert-banner.warning { background: rgba(245, 158, 11, 0.15); border: 1px solid rgba(245, 158, 11, 0.3); color: #fde047; }
.alert-banner.error { background: rgba(239, 68, 68, 0.15); border: 1px solid rgba(239, 68, 68, 0.3); color: #fca5a5; }

/* --- Workspace Grid --- */
.pos-workspace {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 20px;
  flex-grow: 1;
}

@media (max-width: 1024px) {
  .pos-workspace {
    grid-template-columns: 1fr;
  }
}

.workspace-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 24px;
  background: var(--panel-bg);
  backdrop-filter: blur(16px);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.25);
  transition: opacity 0.3s ease;
  position: relative;
}

.disabled-overlay {
  opacity: 0.45;
  pointer-events: none;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 12px;
}

.panel-header h2 {
  font-size: 20px;
  margin: 0;
  font-weight: 600;
}

.step-num {
  font-size: 12px;
  padding: 2px 8px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  color: var(--text-secondary);
}

/* --- Cards (Glassmorphic) --- */
.glass-card {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.glass-card h3 {
  font-size: 15px;
  margin: 0 0 12px;
  font-weight: 600;
  color: var(--text-secondary);
}

.form-row {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
}

.relative {
  position: relative;
}

.flex-1 { flex: 1; }
.flex-2 { flex: 2; }

label {
  font-size: 12px;
  font-weight: 500;
  color: var(--text-secondary);
}

input, select {
  background: rgba(17, 24, 39, 0.6);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 10px 14px;
  color: var(--text-primary);
  font-family: inherit;
  font-size: 14px;
  transition: all 0.3s ease;
}

input:focus, select:focus {
  border-color: var(--accent-color);
  outline: none;
  box-shadow: 0 0 0 3px var(--accent-glow);
}

/* --- Autocomplete Dropdown --- */
.autocomplete-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  width: 100%;
  background: #1e293b;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  max-height: 200px;
  overflow-y: auto;
  z-index: 100;
  list-style: none;
  padding: 0;
  margin: 4px 0 0;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.5);
}

.autocomplete-dropdown li {
  padding: 10px 14px;
  cursor: pointer;
  border-bottom: 1px solid rgba(255, 255, 255, 0.03);
}

.autocomplete-dropdown li:hover {
  background: rgba(255, 255, 255, 0.08);
}

.suggestion-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  gap: 8px;
}

.suggestion-item .s-name {
  color: var(--text-primary);
  flex-grow: 1;
  text-align: left;
}

.suggestion-item .s-id {
  color: var(--text-secondary);
  font-size: 11px;
}

.suggestion-item .s-price {
  color: var(--warning-color);
  font-weight: bold;
}

/* --- Scanner Styling --- */
.scanner-row {
  align-items: flex-end;
}

.btn-scan {
  height: 42px;
  align-self: flex-end;
}

/* --- Items Table --- */
.items-table-container {
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid var(--border-color);
  border-radius: 8px;
}

.items-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
  font-size: 14px;
}

.items-table th, .items-table td {
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-color);
}

.items-table th {
  background: rgba(17, 24, 39, 0.4);
  color: var(--text-secondary);
  font-weight: 600;
  font-size: 12px;
  text-transform: uppercase;
}

.items-table tr:hover td {
  background: rgba(255, 255, 255, 0.02);
}

.item-name {
  font-weight: 600;
}

.item-id-sub {
  font-size: 11px;
  color: var(--text-secondary);
}

.discount-text {
  color: var(--warning-color);
  font-weight: 600;
}

.font-bold { font-weight: 700; }
.text-right { text-align: right; }
.text-center { text-align: center; }

.empty-placeholder {
  text-align: center;
  color: var(--text-secondary);
  padding: 40px !important;
}

/* --- Promotions Block --- */
.promotions-summary {
  background: rgba(245, 158, 11, 0.06);
  border: 1px dashed rgba(245, 158, 11, 0.3);
  border-radius: 8px;
  padding: 12px;
}

.promotions-summary h4 {
  margin: 0 0 8px;
  font-size: 13px;
  color: var(--warning-color);
  font-weight: 600;
}

.promo-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.promo-list li {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
}

.promo-badge {
  font-size: 10px;
  padding: 2px 6px;
  background: rgba(245, 158, 11, 0.2);
  color: var(--warning-color);
  border-radius: 4px;
  font-weight: 700;
}

.promo-discount {
  color: var(--warning-color);
  font-weight: 700;
}

/* --- Totals Summary block --- */
.totals-block {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 16px;
  background: rgba(17, 24, 39, 0.4);
  border-radius: 12px;
  margin-top: auto;
}

.total-row {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
}

.discount-row {
  color: var(--warning-color);
}

.final-row {
  border-top: 1px solid var(--border-color);
  padding-top: 8px;
  font-size: 18px;
  font-weight: 800;
  color: var(--text-primary);
}

/* --- Button Styling --- */
button {
  cursor: pointer;
  font-family: inherit;
  font-size: 14px;
  font-weight: 600;
  border-radius: 8px;
  padding: 10px 18px;
  border: none;
  transition: all 0.3s ease;
}

.btn-primary {
  background: var(--accent-color);
  color: white;
  box-shadow: 0 4px 14px var(--accent-glow);
}

.btn-primary:hover {
  background: #4f46e5;
  transform: translateY(-1px);
  box-shadow: 0 6px 20px var(--accent-glow);
}

.btn-secondary {
  background: rgba(255, 255, 255, 0.08);
  color: var(--text-primary);
  border: 1px solid var(--border-color);
}

.btn-secondary:hover {
  background: rgba(255, 255, 255, 0.15);
}

.btn-success {
  background: var(--success-color);
  color: white;
  box-shadow: 0 4px 14px var(--success-glow);
}

.btn-success:hover {
  background: #059669;
  transform: translateY(-1px);
  box-shadow: 0 6px 20px var(--success-glow);
}

.btn-danger {
  background: var(--danger-color);
  color: white;
  box-shadow: 0 4px 14px var(--danger-glow);
}

.btn-danger:hover {
  background: #dc2626;
  transform: translateY(-1px);
}

.btn-lg {
  padding: 12px 24px;
  font-size: 16px;
}

.btn-block {
  width: 100%;
}

.btn-icon-delete {
  padding: 4px 8px;
  background: rgba(239, 68, 68, 0.15);
  color: var(--danger-color);
  border-radius: 4px;
  font-size: 12px;
}

.btn-icon-delete:hover {
  background: var(--danger-color);
  color: white;
}

.btn-pulse {
  animation: pulse 2s infinite;
}

.btn-glow {
  box-shadow: 0 0 15px var(--success-color);
}

/* --- Panel Actions Layout --- */
.panel-actions {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-top: 10px;
}

.column-actions {
  flex-direction: column;
}

/* --- Payment Panel Elements --- */
.payment-status-card {
  background: linear-gradient(135deg, rgba(30, 41, 59, 0.8) 0%, rgba(15, 23, 42, 0.9) 100%);
  border: 1px solid var(--border-color);
}

.status-gauge {
  display: flex;
  justify-content: space-around;
  text-align: center;
}

.gauge-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.gauge-item .label {
  font-size: 11px;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.gauge-item .value {
  font-size: 20px;
}

.flex-column-center {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.status-badge-paid {
  font-size: 14px;
  padding: 6px 12px;
  background: var(--success-color);
  color: white;
  border-radius: 20px;
  font-weight: 700;
  animation: popIn 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

/* --- Payment Ledger --- */
.payments-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.payments-table th, .payments-table td {
  padding: 10px 12px;
  border-bottom: 1px solid var(--border-color);
}

.payments-table th {
  color: var(--text-secondary);
  font-weight: 600;
  font-size: 11px;
  text-align: left;
}

.pay-method-badge {
  font-size: 10px;
  font-weight: 700;
  padding: 3px 6px;
  border-radius: 4px;
}

.pay-method-badge.cash {
  background: rgba(16, 185, 129, 0.15);
  color: #34d399;
  border: 1px solid rgba(16, 185, 129, 0.3);
}

.pay-method-badge.credit_card {
  background: rgba(99, 102, 241, 0.15);
  color: #a5b4fc;
  border: 1px solid rgba(99, 102, 241, 0.3);
}

/* --- Payment Tab & Options --- */
.payment-tabs {
  display: flex;
  gap: 8px;
  background: rgba(17, 24, 39, 0.4);
  padding: 4px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.tab-btn {
  flex: 1;
  background: transparent;
  color: var(--text-secondary);
  padding: 8px;
  font-size: 13px;
  border-radius: 6px;
}

.tab-btn.active {
  background: var(--card-bg);
  color: var(--text-primary);
  box-shadow: var(--shadow);
}

.tab-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
  animation: fadeIn 0.3s ease;
}

.input-with-currency {
  position: relative;
  display: flex;
  align-items: center;
}

.currency-symbol {
  position: absolute;
  left: 14px;
  font-weight: bold;
  color: var(--text-secondary);
}

.input-with-currency input {
  padding-left: 30px;
  width: 100%;
  box-sizing: border-box;
}

.calculator-helpers {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.helper-btn {
  flex: 1;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid var(--border-color);
  color: var(--text-secondary);
  font-size: 11px;
  padding: 6px 10px;
  border-radius: 6px;
}

.helper-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: var(--text-primary);
}

.change-preview-box {
  background: rgba(245, 158, 11, 0.04);
  border: 1px solid rgba(245, 158, 11, 0.2);
  border-radius: 8px;
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 13px;
}

.preview-row {
  display: flex;
  justify-content: space-between;
}

.change-row {
  font-size: 14px;
  color: var(--warning-color);
  border-top: 1px dashed rgba(245, 158, 11, 0.3);
  padding-top: 4px;
}

.btn-submit-payment {
  width: 100%;
  padding: 12px;
}

.card-simulator-box {
  background: rgba(99, 102, 241, 0.05);
  border: 1px dashed rgba(99, 102, 241, 0.3);
  border-radius: 8px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.simulator-notice {
  font-size: 12px;
  color: #a5b4fc;
  margin: 0;
}

.card-accent {
  background: linear-gradient(135deg, #4f46e5 0%, #6366f1 100%);
  box-shadow: 0 4px 14px rgba(99, 102, 241, 0.4);
}

.card-accent:hover {
  background: linear-gradient(135deg, #4338ca 0%, #4f46e5 100%);
}

/* --- Empty state Welcome --- */
.empty-state-welcome {
  display: flex;
  justify-content: center;
  align-items: center;
  flex-grow: 1;
  padding: 40px;
}

.welcome-box {
  max-width: 500px;
  text-align: center;
  padding: 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.welcome-icon {
  font-size: 48px;
  animation: spinSlow 8s linear infinite;
}

/* --- Audit Logs Section --- */
.audit-logs-section {
  background: rgba(17, 24, 39, 0.7);
  backdrop-filter: blur(10px);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 16px 20px;
}

.audit-header h3 {
  font-size: 14px;
  margin: 0 0 4px;
  font-weight: 600;
  color: var(--text-primary);
}

.audit-desc {
  font-size: 12px;
  color: var(--text-secondary);
}

.log-cards-container {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding: 10px 0;
}

.log-placeholder {
  font-size: 12px;
  color: var(--text-secondary);
  padding: 10px;
}

.log-card {
  flex: 0 0 220px;
  background: rgba(31, 41, 55, 0.6);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 10px;
  font-size: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.log-card.cancelled {
  border-left: 4px solid var(--danger-color);
}

.log-card.paid {
  border-left: 4px solid var(--success-color);
}

.log-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.log-row.sub-info {
  color: var(--text-secondary);
}

.log-badge {
  font-size: 9px;
  padding: 2px 4px;
  border-radius: 4px;
  font-weight: 700;
}

.log-badge.cancelled {
  background: rgba(239, 68, 68, 0.2);
  color: var(--danger-color);
}

.log-badge.paid {
  background: rgba(16, 185, 129, 0.2);
  color: var(--success-color);
}

/* --- Animations --- */
@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.03); }
  100% { transform: scale(1); }
}

@keyframes pulseGlow {
  0% { text-shadow: 0 0 4px var(--danger-color); }
  50% { text-shadow: 0 0 12px var(--danger-color); }
  100% { text-shadow: 0 0 4px var(--danger-color); }
}

.pulse-glow {
  animation: pulseGlow 1.5s infinite;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(4px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes spinSlow {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
