<template>
  <el-container>
    <el-main>
      <el-card v-if="goods">
        <h2>{{ goods.title }}</h2>
        <img :src="goods.coverImage || 'https://via.placeholder.com/300'" style="width: 300px; height: 250px; object-fit: cover;" />
        <div>团体：{{ goods.groupName }} / 爱豆：{{ goods.idolName }}</div>
        <div>品相：{{ goods.quality }} / 交易模式：{{ goods.tradeType }}</div>
        <div>价格：¥{{ goods.price }}</div>
        <div>描述：{{ goods.description }}</div>
        <el-button type="primary" @click="addToCart">加入购物车</el-button>
      </el-card>
      <div v-else style="padding: 24px; text-align: center; color: #f56c6c;">
        {{ error || '商品不存在或已下架。' }}
      </div>
    </el-main>
  </el-container>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();
const goods = ref(null);
const error = ref('');

const loadGoods = async () => {
  try {
    const res = await axios.get(`/api/goods/${route.params.id}`);
    goods.value = res.data.data;
  } catch (e) {
    error.value = '商品信息加载失败，请稍后重试。';
  }
};

const addToCart = async () => {
  if (!goods.value) {
    return;
  }
  await axios.post('/api/cart/add', { goodsId: goods.value.id, quantity: 1 });
  alert('已加入购物车');
  router.push('/cart');
};

onMounted(loadGoods);
</script>
