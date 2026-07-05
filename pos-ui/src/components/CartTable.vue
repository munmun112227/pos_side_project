<script setup>
/**
 * 購物車清單與促銷明細顯示組件
 */
const props = defineProps({
  items: {
    type: Array,
    required: true
  },
  promotions: {
    type: Array,
    required: true
  },
  orderStatus: {
    type: String,
    required: true
  },
  originalTotal: {
    type: Number,
    required: true
  },
  discountTotal: {
    type: Number,
    required: true
  },
  finalTotal: {
    type: Number,
    required: true
  }
});

const emit = defineEmits(['remove-item', 'abandon-cart', 'prepare-checkout']);
</script>

<template>
  <div>
    <!-- 商品列表表格 -->
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
            <th class="text-center" v-if="props.orderStatus === 'DRAFT'">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="props.items.length === 0">
            <td colspan="7" class="empty-placeholder">
              🛒 目前購物車空空如也，請在上方輸入商品關鍵字搜尋並掃描。
            </td>
          </tr>
          <tr v-for="(item, idx) in props.items" :key="item.itemId">
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
            <td class="text-center" v-if="props.orderStatus === 'DRAFT'">
              <button @click="emit('remove-item', idx)" class="btn-icon-delete">🗑️</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 促銷折扣明細 -->
    <div class="promotions-summary" v-if="props.promotions.length > 0">
      <h4>💡 已套用促銷活動</h4>
      <ul class="promo-list">
        <li v-for="promo in props.promotions" :key="promo.promoId">
          <span class="promo-badge">{{ promo.level }}</span>
          <span class="promo-name">{{ promo.name }}</span>
          <span class="promo-discount">-${{ promo.discountAmount.toFixed(1) }}</span>
        </li>
      </ul>
    </div>

    <!-- 總計區塊 -->
    <div class="totals-block">
      <div class="total-row">
        <span>原始總金額:</span>
        <span>${{ props.originalTotal.toFixed(1) }}</span>
      </div>
      <div class="total-row discount-row">
        <span>累計優惠減免:</span>
        <span>-${{ props.discountTotal.toFixed(1) }}</span>
      </div>
      <div class="total-row final-row">
        <span>結帳應付金額:</span>
        <span>${{ props.finalTotal.toFixed(1) }}</span>
      </div>
    </div>

    <!-- 購物車操作按鈕 -->
    <div class="panel-actions" v-if="props.orderStatus === 'DRAFT'">
      <button @click="emit('abandon-cart')" class="btn-danger">
        ⚠️ 放棄/取消交易
      </button>
      <button @click="emit('prepare-checkout')" class="btn-success btn-lg">
        💸 確認進入付款
      </button>
    </div>
  </div>
</template>
