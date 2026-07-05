<script setup>
import { ref, computed } from 'vue';
import { 
  startNewTransaction as apiStartNewTransaction,
  calculateCart as apiCalculateCart,
  prepareCheckout as apiPrepareCheckout,
  cancelTransactionStep,
  abandonCartStep
} from '../api/checkout';

import MemberCard from '../components/MemberCard.vue';
import ScannerCard from '../components/ScannerCard.vue';
import CartTable from '../components/CartTable.vue';
import PaymentOptions from '../components/PaymentOptions.vue';
import AuditLogs from '../components/AuditLogs.vue';

// --- Global Cashier State ---
const orderId = ref(null);
const orderStatus = ref('NO_TRANSACTION'); // NO_TRANSACTION, DRAFT, CHECKOUT_PENDING, PAID, CANCELLED
const memberId = ref('');
const memberTier = ref('NORMAL');
const cartItems = ref([]); // { itemId, name, quantity, originalUnitPrice, finalUnitPrice, discountAmount, subtotal }
const appliedPromotions = ref([]);
const recentAuditLogs = ref([]);
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
  const itemDiscounts = cartItems.value.reduce((sum, item) => sum + (item.discountAmount || 0), 0);
  const promoDiscounts = appliedPromotions.value.reduce((sum, promo) => sum + (promo.discountAmount || 0), 0);
  return itemDiscounts + promoDiscounts;
});

const cartFinalTotal = computed(() => {
  return Math.max(0, cartOriginalTotal.value - cartDiscountTotal.value);
});

// --- Orchestration Handlers (Controller logic) ---

// 1. 建立新交易
const handleStartNewTransaction = async () => {
  try {
    const data = await apiStartNewTransaction('RETAIL');
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
  } catch (err) {
    showAlert(err.message, 'error');
  }
};

// 2. 計算購物車金額與促銷
const handleCalculateCart = async () => {
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
    const data = await apiCalculateCart(payload);
    cartItems.value = data.items;
    appliedPromotions.value = data.appliedPromotions;
  } catch (err) {
    showAlert(err.message, 'error');
  }
};

// 3. 條碼掃描成功觸發
const handleScanSuccess = async ({ product, quantity }) => {
  if (orderStatus.value !== 'DRAFT') {
    showAlert('請先建立新交易或返回交易草稿狀態', 'warning');
    return;
  }

  // 檢查是否已存在購物車
  const existing = cartItems.value.find(i => i.itemId === product.itemId);
  if (existing) {
    existing.quantity += quantity;
  } else {
    cartItems.value.push({
      itemId: product.itemId,
      name: product.name,
      quantity: quantity,
      originalUnitPrice: product.originalPrice,
      finalUnitPrice: product.promotionalPrice || product.originalPrice,
      discountAmount: 0.0,
      subtotal: (product.promotionalPrice || product.originalPrice) * quantity
    });
  }

  // 重新觸發後端促銷計算
  await handleCalculateCart();
  showAlert(`已成功刷入商品: ${product.name} x ${quantity}`, 'success');
};

// 4. 移除商品項目
const handleRemoveItem = async (index) => {
  cartItems.value.splice(index, 1);
  await handleCalculateCart();
};

// 5. 購物車中途取消交易 (Abandon Cart)
const handleAbandonCart = async () => {
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
    const data = await abandonCartStep(orderId.value, payload);
    recentAuditLogs.value.unshift({
      orderId: data.orderId,
      status: 'CANCELLED',
      remark: '購物車階段取消',
      amount: cartFinalTotal.value
    });
    showAlert(`交易已取消，作廢單號: ${data.orderId}`, 'warning');
    resetAppState();
  } catch (err) {
    showAlert(err.message, 'error');
  }
};

// 6. 確認商品明細，進入結帳付款
const handlePrepareCheckout = async () => {
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
    const data = await apiPrepareCheckout(orderId.value, payload);
    orderStatus.value = 'CHECKOUT_PENDING';
    paymentStatus.value = {
      receivableAmount: cartFinalTotal.value,
      totalPaidAmount: 0,
      balanceAmount: cartFinalTotal.value,
      fullyPaid: false,
      currentPayments: []
    };
    showAlert('已鎖定商品明細，進入付款介面', 'success');
  } catch (err) {
    showAlert(err.message, 'error');
  }
};

// 7. 處理單筆付款成功
const handlePaymentSuccess = (updatedStatus) => {
  paymentStatus.value = updatedStatus;
  if (updatedStatus.fullyPaid) {
    orderStatus.value = 'PAID';
    showAlert('此訂單已完全付清！', 'success');
  }
};

// 8. 處理作廢付款成功
const handleVoidSuccess = (updatedStatus) => {
  paymentStatus.value = updatedStatus;
};

// 9. 付款階段取消整筆交易
const handleCancelTransaction = async () => {
  if (!confirm('確認要取消整筆付款？所有已成功的信用卡退刷與現金付款均會被物理還原作廢。')) return;
  try {
    await cancelTransactionStep(orderId.value);
    recentAuditLogs.value.unshift({
      orderId: orderId.value,
      status: 'CANCELLED',
      remark: '付款階段整筆取消',
      amount: cartFinalTotal.value
    });
    showAlert('付款已全部還原，交易已取消！', 'warning');
    resetAppState();
  } catch (err) {
    showAlert(err.message, 'error');
  }
};

// 10. 完檔開始下一筆
const handleCompleteTransaction = () => {
  recentAuditLogs.value.unshift({
    orderId: orderId.value,
    status: 'PAID',
    remark: '交易完成',
    amount: paymentStatus.value.receivableAmount
  });
  resetAppState();
  handleStartNewTransaction();
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
        <button v-else @click="handleStartNewTransaction" class="btn-primary btn-pulse">
          ⚡ 啟動新交易
        </button>
      </div>
    </header>

    <!-- Global Alert Banner -->
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
      <section 
        class="workspace-panel cart-section" 
        :class="{ 'disabled-overlay': orderStatus === 'CHECKOUT_PENDING' || orderStatus === 'PAID' }"
      >
        <div class="panel-header">
          <h2>1. 購物車商品輸入</h2>
          <span class="step-num">Step 1</span>
        </div>

        <!-- 1. 會員設定組件 -->
        <MemberCard 
          v-model:memberId="memberId"
          v-model:memberTier="memberTier"
          @change="handleCalculateCart"
        />

        <!-- 2. 條碼掃描模擬器組件 -->
        <ScannerCard 
          @scan-success="handleScanSuccess"
          @show-alert="showAlert"
        />

        <!-- 3. 購物車明細與促銷清單組件 -->
        <CartTable 
          :items="cartItems"
          :promotions="appliedPromotions"
          :orderStatus="orderStatus"
          :originalTotal="cartOriginalTotal"
          :discountTotal="cartDiscountTotal"
          :finalTotal="cartFinalTotal"
          @remove-item="handleRemoveItem"
          @abandon-cart="handleAbandonCart"
          @prepare-checkout="handlePrepareCheckout"
        />
      </section>

      <!-- RIGHT PANEL: Payment / Checkout System -->
      <section 
        class="workspace-panel payment-section" 
        v-if="orderStatus === 'CHECKOUT_PENDING' || orderStatus === 'PAID'"
      >
        <div class="panel-header">
          <h2>2. 複合式支付管理</h2>
          <span class="step-num">Step 2</span>
        </div>

        <!-- 4. 付款管道與明細管理組件 -->
        <PaymentOptions 
          :paymentStatus="paymentStatus"
          :orderStatus="orderStatus"
          :orderId="orderId"
          @payment-success="handlePaymentSuccess"
          @void-success="handleVoidSuccess"
          @cancel-transaction="handleCancelTransaction"
          @complete-transaction="handleCompleteTransaction"
          @show-alert="showAlert"
        />
      </section>
    </div>

    <!-- Empty welcome screen -->
    <div class="empty-state-welcome" v-else>
      <div class="welcome-box glass-card">
        <div class="welcome-icon">⚡</div>
        <h2>歡迎使用 Antigravity POS 收銀系統</h2>
        <p>請點擊下方按鈕啟動新交易，開始進行條碼掃描與付款流程。</p>
        <button @click="handleStartNewTransaction" class="btn-primary btn-lg btn-pulse">
          ⚡ 建立新交易編號
        </button>
      </div>
    </div>

    <!-- 5. 底部稽核日誌組件 -->
    <AuditLogs :logs="recentAuditLogs" />
  </div>
</template>
