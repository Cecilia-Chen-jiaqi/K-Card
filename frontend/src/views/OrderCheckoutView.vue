<template>
  <div class="page-content checkout-page">
    <h2>订单结算</h2>
    <el-card v-if="orderDetail" class="checkout-card">
      <div class="order-line"><span>订单号：</span>{{ orderDetail.order.orderNo }}</div>
      <div class="order-line"><span>商品：</span>{{ orderDetail.goods.title }}</div>
      <div class="order-line"><span>数量：</span>{{ orderDetail.order.quantity }}</div>
      <div class="order-line"><span>总价：</span>¥{{ orderDetail.order.amount }}</div>
      <div class="order-line"><span>状态：</span>{{ statusLabel }}</div>
      <div class="pay-actions" v-if="orderDetail.order.status === 0">
        <el-button type="primary" @click="pay">支付宝支付</el-button>
      </div>
      <div class="pay-tip" v-else>
        {{ orderDetail.order.status === 1 ? '已支付，等待卖家发货' : '请在订单中心查看最新订单状态' }}
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue';
import axios from 'axios';
import { useRoute } from 'vue-router';

const route = useRoute();
const orderDetail = ref(null);

const statusLabel = computed(() => {
  if (!orderDetail.value) {
    return '';
  }
  const status = orderDetail.value.order.status;
  switch (status) {
    case 0:
      return '待支付';
    case 1:
      return '已支付待发货';
    case 2:
      return '已发货待收货';
    case 3:
      return '交易完成';
    case 4:
      return '交易关闭';
    case 5:
      return '退款完成';
    default:
      return '未知状态';
  }
});

const loadOrder = async () => {
  const res = await axios.get(`/api/orders/${route.params.orderNo}`);
  orderDetail.value = res.data.data;
};

const pay = async () => {
  const res = await axios.post('/api/pay/submit', { orderNo: orderDetail.value.order.orderNo });
  const formHtml = res.data.data;
  const container = document.createElement('div');
  container.innerHTML = formHtml;
  document.body.appendChild(container);
  const form = container.querySelector('form');
  form.submit();
};

onMounted(loadOrder);
</script>

<style scoped>
.checkout-page {
  max-width: 720px;
  margin: 0 auto;
}

.checkout-card {
  padding: 24px;
  border-radius: 26px;
  box-shadow: 0 20px 40px rgba(220, 60, 100, 0.12);
  background: linear-gradient(180deg, #fff5f7 0%, #fff9fb 100%);
}

.order-line {
  margin-bottom: 14px;
  font-size: 16px;
}

.order-line span {
  color: #ff4d6d;
  font-weight: 600;
}

.pay-actions {
  margin-top: 22px;
}

.pay-tip {
  margin-top: 22px;
  color: #909399;
}
</style>
