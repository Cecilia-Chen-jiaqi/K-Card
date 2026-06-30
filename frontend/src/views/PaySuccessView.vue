<template>
  <div class="pay-success-page kc-animate-in page-content">
    <div class="success-wrap kc-surface">
      <div class="success-glow"></div>
      <div class="success-icon">🎉</div>
      <h2 class="kc-gradient-text">支付完成</h2>
      <p v-if="orderNo" class="order-no">订单号：{{ orderNo }}</p>
      <p v-if="syncing" class="sync-tip">正在同步支付状态...</p>
      <el-result v-else-if="paid" icon="success" title="支付已确认" sub-title="订单状态已更新，卖家将尽快发货">
        <template #extra>
          <el-button type="primary" round @click="$router.push('/user?tab=buyer')">查看我的订单</el-button>
          <el-button round @click="$router.push('/')">继续逛小卡</el-button>
        </template>
      </el-result>
      <el-result v-else icon="info" title="等待确认" sub-title="如已完成支付，系统正在同步状态，请稍候刷新">
        <template #extra>
          <el-button type="primary" round :loading="syncing" @click="syncPayStatus">刷新支付状态</el-button>
          <el-button round @click="$router.push('/')">返回首页</el-button>
        </template>
      </el-result>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import axios from 'axios';

const route = useRoute();
const orderNo = ref(route.query.orderNo || route.query.out_trade_no || '');
const paid = ref(false);
const syncing = ref(false);

const syncPayStatus = async () => {
  if (!orderNo.value) return;
  syncing.value = true;
  try {
    const res = await axios.get('/api/pay/sync-status', { params: { orderNo: orderNo.value } });
    if (res.data.code === 0 && res.data.data?.paid) {
      paid.value = true;
    }
  } catch (_) { /* ignore */ }
  finally { syncing.value = false; }
};

onMounted(async () => {
  if (orderNo.value) {
    await syncPayStatus();
  }
});
</script>

<style scoped>
.pay-success-page {
  max-width: 560px;
  margin: 20px auto;
}

.success-wrap {
  position: relative;
  text-align: center;
  padding: 40px 32px 32px;
  overflow: hidden;
}

.success-glow {
  position: absolute;
  top: -60px;
  left: 50%;
  transform: translateX(-50%);
  width: 200px;
  height: 200px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(74, 144, 226, 0.2), transparent 70%);
  pointer-events: none;
}

.success-icon {
  font-size: 56px;
  margin-bottom: 12px;
  animation: kc-float 2s ease-in-out infinite;
  position: relative;
}

.success-wrap h2 { margin: 0 0 8px; position: relative; }
.order-no, .sync-tip { color: var(--kc-text-muted); position: relative; }
</style>
