<script setup>
import { ref } from 'vue';
import { searchProducts, scanProduct } from '../api/products';

/**
 * 條碼掃描模擬器組件
 * 負責本機 autocomplete 搜尋的狀態管理，並在成功刷入後發送事件給父組件。
 */
const emit = defineEmits(['scan-success', 'show-alert']);

const searchInput = ref('');
const searchResults = ref([]);
const showSuggestions = ref(false);
const inputQty = ref(1);

// 動態模糊搜尋 H2 商品
const onSearchInput = async () => {
  if (!searchInput.value.trim()) {
    searchResults.value = [];
    showSuggestions.value = false;
    return;
  }
  try {
    const data = await searchProducts(searchInput.value);
    searchResults.value = data;
    showSuggestions.value = searchResults.value.length > 0;
  } catch (err) {
    console.error('搜尋失敗:', err);
  }
};

// 點擊選擇候選商品
const selectProduct = (item) => {
  searchInput.value = item.itemId; // 帶入商品編號
  searchResults.value = [];
  showSuggestions.value = false;
  emit('show-alert', `已選擇商品: ${item.name} (${item.itemId})`, 'info');
};

// 點擊掃描或按 Enter
const handleScan = async () => {
  if (!searchInput.value.trim()) {
    emit('show-alert', '請先輸入商品編號、條碼或透過搜尋選擇商品', 'warning');
    return;
  }

  try {
    const product = await scanProduct(searchInput.value);
    
    // 將刷讀成功的 product 資料包與目前設定的數量傳遞給父層
    emit('scan-success', {
      product,
      quantity: product.isWeightItem ? product.quantity : inputQty.value
    });

    // 清空輸入框
    searchInput.value = '';
    inputQty.value = 1;
  } catch (err) {
    emit('show-alert', err.message, 'error');
  }
};
</script>

<template>
  <div class="glass-card scanner-card">
    <h3>🏷️ 條碼掃描模擬器 (動態查詢 H2 資料庫)</h3>
    <div class="form-row scanner-row">
      <div class="form-group flex-2 relative">
        <label>輸入商品名稱/條碼 (支援模糊搜尋與生鮮條碼)</label>
        <input 
          type="text" 
          v-model="searchInput" 
          placeholder="輸入商品 ID (如 ITEM-001, 生鮮秤重 2800101002507) 或關鍵字搜尋..." 
          @input="onSearchInput"
          @focus="showSuggestions = searchResults.length > 0"
          @blur="setTimeout(() => showSuggestions = false, 250)"
          @keyup.enter="handleScan"
        />
        <!-- 推薦選單下拉選單 -->
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
      <button @click="handleScan" class="btn-secondary btn-scan">
        ➕ 掃描商品
      </button>
    </div>
  </div>
</template>
