<script setup>
/**
 * 系統交易稽核日誌顯示器 (Dumb Component)
 */
const props = defineProps({
  logs: {
    type: Array,
    required: true
  }
});
</script>

<template>
  <footer class="audit-logs-section">
    <div class="audit-header">
      <h3>🔍 系統作廢/結帳交易稽核日誌 (H2 持久化紀錄)</h3>
      <span class="audit-desc">即時記錄收銀中途取消及完檔以防帳務弊端</span>
    </div>
    <div class="log-cards-container">
      <div v-if="props.logs.length === 0" class="log-placeholder">
        目前尚無本機作廢或完成交易的紀錄。
      </div>
      <div 
        v-for="log in props.logs" 
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
</template>
