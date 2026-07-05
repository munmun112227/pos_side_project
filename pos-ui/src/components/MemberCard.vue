<script setup>
/**
 * 會員資料與促銷設定組件 (Dumb/Presentational Component)
 * 專門負責渲染會員輸入介面，並將更動往上傳遞給父組件。
 */
const props = defineProps({
  memberId: {
    type: String,
    required: true
  },
  memberTier: {
    type: String,
    required: true
  }
});

const emit = defineEmits(['update:memberId', 'update:memberTier', 'change']);

const onMemberIdChange = (e) => {
  emit('update:memberId', e.target.value);
  emit('change');
};

const onMemberTierChange = (e) => {
  emit('update:memberTier', e.target.value);
  emit('change');
};
</script>

<template>
  <div class="glass-card member-card">
    <h3>👤 會員資料與促銷設定</h3>
    <div class="form-row">
      <div class="form-group">
        <label>會員卡號 ID</label>
        <input 
          type="text" 
          :value="props.memberId" 
          placeholder="例如 MEM-VIP888" 
          @change="onMemberIdChange"
        />
      </div>
      <div class="form-group">
        <label>會員等級 Tier</label>
        <select :value="props.memberTier" @change="onMemberTierChange">
          <option value="NORMAL">一般會員 (NORMAL)</option>
          <option value="VIP">高級會員 (VIP)</option>
        </select>
      </div>
    </div>
  </div>
</template>
