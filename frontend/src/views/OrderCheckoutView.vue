<template>
  <el-container>
    <el-main>
      <h2>订单结算</h2>
      <el-card v-if="order">
        <div>订单号：{{ order.orderNo }}</div>
        <div>金额：¥{{ order.amount }}</div>
        <el-button type="primary" @click="pay">支付宝支付</el-button>
      </el-card>
    </el-main>
  </el-container>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { useRoute } from 'vue-router';

const route = useRoute();
const order = ref(null);

const loadOrder = async () => {
  const res = await axios.get(`/api/orders/${route.params.orderNo}`);
  order.value = res.data.data;
};

const pay = async () => {
  const res = await axios.post('/api/pay/submit', { orderNo: order.value.orderNo });
  const formHtml = res.data.data;
  const container = document.createElement('div');
  container.innerHTML = formHtml;
  document.body.appendChild(container);
  const form = container.querySelector('form');
  form.submit();
};

onMounted(loadOrder);
</script>
