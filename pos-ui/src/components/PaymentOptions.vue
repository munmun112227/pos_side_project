<script setup>
import { ref } from 'vue';
import { submitPaymentStep, voidPaymentStep } from '../api/checkout';

/**
 * 複合式收銀支付管理組件
 */
const props = defineProps({
  paymentStatus: {
    type: Object,
    required: true
  },
  orderStatus: {
    type: String,
    required: true
  },
  orderId: {
    type: String,
    required: true
  }
});

const emit = defineEmits([
  'payment-success',
  'void-success',
  'cancel-transaction',
  'complete-transaction',
  'show-alert'
]);

const paymentMethod = ref('CASH');
const cashPayAmount = ref(0);
const cardNo = ref('');
const authCode = ref('');

// 當應收金額變更時，初始化現金輸入預設值
const syncCashAmount = () => {
  cashPayAmount.value = props.paymentStatus.balanceAmount;
};

// 提交一筆付款
const handlePayment = async () => {
  let details = null;
  let targetAmount = props.paymentStatus.balanceAmount;

  if (paymentMethod.value === 'CASH') {
    if (cashPayAmount.value < 0) {
      emit('show-alert', '實收現金不可為負數', 'warning');
      return;
    }
    const deductAmount = Math.min(props.paymentStatus.balanceAmount, cashPayAmount.value);
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
    orderId: props.orderId,
    amount: targetAmount,
    paymentMethodId: paymentMethod.value,
    details: details
  };

  try {
    const updatedStatus = await submitPaymentStep(props.orderId, payload);
    emit('payment-success', updatedStatus);
    
    // 重置欄位
    cardNo.value = '';
    authCode.value = '';
    cashPayAmount.value = updatedStatus.balanceAmount;
    
    emit('show-alert', '付款項目成功紀錄！', 'success');
  } catch (err) {
    emit('show-alert', err.message, 'error');
  }
};

// 作退款/作廢單筆支付
const handleVoid = async (paymentId) => {
  try {
    const updatedStatus = await voidPaymentStep(props.orderId, paymentId);
    emit('void-success', updatedStatus);
    cashPayAmount.value = updatedStatus.balanceAmount;
    emit('show-alert', '付款項目已作廢退刷', 'warning');
  } catch (err) {
    emit('show-alert', err.message, 'error');
  }
};
</script>

<template>
  <div class="payment-card-wrapper">
    <!-- 付款狀態儀表板 -->
    <div class="glass-card payment-status-card">
      <div class="status-gauge">
        <div class="gauge-item">
          <span class="label">應收金額</span>
          <span class="value font-bold">${{ props.paymentStatus.receivableAmount.toFixed(1) }}</span>
        </div>
        <div class="gauge-item text-success">
          <span class="label">已收金額</span>
          <span class="value font-bold">${{ props.paymentStatus.totalPaidAmount.toFixed(1) }}</span>
        </div>
        <div class="gauge-item text-danger" v-if="!props.paymentStatus.fullyPaid">
          <span class="label">剩餘應收 (餘額)</span>
          <span class="value font-bold pulse-glow">${{ props.paymentStatus.balanceAmount.toFixed(1) }}</span>
        </div>
        <div class="gauge-item text-success flex-column-center" v-else>
          <span class="label">狀態</span>
          <span class="status-badge-paid">🎉 完全付清</span>
        </div>
      </div>
    </div>

    <!-- 本次交易付款明細帳本 -->
    <div class="glass-card ledger-card">
      <h3>📝 本次交易付款明細</h3>
      <table class="payments-table">
        <thead>
          <tr>
            <th>付款管道</th>
            <th class="text-right">扣抵金額</th>
            <th class="text-right">顧客實付</th>
            <th class="text-right">找零</th>
            <th class="text-center" v-if="props.orderStatus === 'CHECKOUT_PEND'">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="props.paymentStatus.currentPayments.length === 0">
            <td colspan="5" class="empty-placeholder">
              ⏳ 尚未加入任何付款明細。請在下方選擇支付方式。
            </td>
          </tr>
          <tr v-for="pay in props.paymentStatus.currentPayments" :key="pay.id">
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
            <td class="text-center" v-if="props.orderStatus === 'CHECKOUT_PENDING'">
              <button @click="handleVoid(pay.id)" class="btn-icon-delete">🗑️ 作廢</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 選擇支付工具 -->
    <div class="glass-card payment-options-card" v-if="!props.paymentStatus.fullyPaid">
      <h3>💰 選擇支付工具</h3>
      
      <div class="payment-tabs">
        <button 
          :class="['tab-btn', { active: paymentMethod === 'CASH' }]"
          @click="paymentMethod = 'CASH'; syncCashAmount();"
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

      <!-- 現金支付介面 -->
      <div class="tab-content" v-if="paymentMethod === 'CASH'">
        <div class="form-group">
          <label>顧客支付現金總額 (若顧客拿整張現鈔)</label>
          <div class="input-with-currency">
            <span class="currency-symbol">$</span>
            <input type="number" v-model.number="cashPayAmount" min="0" />
          </div>
        </div>
        
        <div class="calculator-helpers">
          <button @click="cashPayAmount = props.paymentStatus.balanceAmount" class="helper-btn">
            剛好付清 (${{ props.paymentStatus.balanceAmount.toFixed(1) }})
          </button>
          <button @click="cashPayAmount = 100" class="helper-btn">$100</button>
          <button @click="cashPayAmount = 500" class="helper-btn">$500</button>
          <button @click="cashPayAmount = 1000" class="helper-btn">$1000</button>
        </div>

        <div class="change-preview-box" v-if="cashPayAmount > 0">
          <div class="preview-row">
            <span>欲抵扣金額:</span>
            <span>${{ Math.min(props.paymentStatus.balanceAmount, cashPayAmount).toFixed(1) }}</span>
          </div>
          <div class="preview-row change-row" v-if="cashPayAmount > props.paymentStatus.balanceAmount">
            <span>應找零金額:</span>
            <strong>${{ (cashPayAmount - props.paymentStatus.balanceAmount).toFixed(1) }}</strong>
          </div>
        </div>

        <button @click="handlePayment" class="btn-primary btn-submit-payment">
          Confirm Cash Payment 💵
        </button>
      </div>

      <!-- 信用卡支付介面 -->
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

        <button @click="handlePayment" class="btn-primary btn-submit-payment card-accent">
          Swipe Credit Card (${{ props.paymentStatus.balanceAmount.toFixed(1) }}) 💳
        </button>
      </div>
    </div>

    <!-- 付款階段底部的操作按鈕 -->
    <div class="panel-actions column-actions">
      <button v-if="props.orderStatus === 'CHECKOUT_PENDING'" @click="emit('cancel-transaction')" class="btn-danger btn-block">
        🚨 取消付款 (還原已付金額並關閉交易)
      </button>
      
      <button v-if="props.orderStatus === 'PAID'" @click="emit('complete-transaction')" class="btn-success btn-block btn-lg btn-glow">
        ✅ 完檔開立發票，開始下一筆交易
      </button>
    </div>
  </div>
</template>
