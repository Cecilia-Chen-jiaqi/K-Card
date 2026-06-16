<template>
  <div class="page-content pay-success-page">
    <h2>支付成功</h2>
    <p>支付成功，请等待异步回调确认状态。</p>
    <el-button type="primary" @click="goHome">返回首页</el-button>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';
const router = useRouter();
const goHome = () => router.push('/');
</script>

<style scoped>
.page-content {
  max-width: 640px;
  margin: 0 auto;
  text-align: center;
}
</style>
