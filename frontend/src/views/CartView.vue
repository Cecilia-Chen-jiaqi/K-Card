<template>
  <el-container>
    <el-main>
      <h2>购物车</h2>
      <el-table :data="cartList" style="width: 100%">
        <el-table-column prop="goods.title" label="商品"></el-table-column>
        <el-table-column prop="quantity" label="数量"></el-table-column>
        <el-table-column prop="goods.price" label="单价"></el-table-column>
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button type="primary" @click="checkout(row.cart.id)">去结算</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-main>
  </el-container>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';

const router = useRouter();
const cartList = ref([]);

const loadCart = async () => {
  const res = await axios.get('/api/cart/list');
  cartList.value = res.data.data || [];
};

const checkout = async (cartId) => {
  try {
    const res = await axios.post(`/api/cart/checkout/${cartId}`);
    const orderNo = res.data.data.orderNo;
    if (orderNo) {
      router.push(`/checkout/${orderNo}`);
    }
  } catch (err) {
    alert(err.response?.data?.message || '结算失败');
  }
};

onMounted(loadCart);
</script>
