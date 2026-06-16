<template>
  <div class="page-content cart-page">
    <div class="cart-header">
      <div>
        <h2>我的购物车</h2>
        <p>确认商品后直接结算，简洁可爱买卡体验。</p>
      </div>
      <div class="cart-summary">
        <div>总计 {{ totalQuantity }} 件</div>
        <div>应付 ¥{{ totalAmount }}</div>
      </div>
    </div>
    <el-table :data="cartList" border stripe class="cart-table">
      <el-table-column label="商品" width="260">
        <template #default="{ row }">
          <div class="cart-goods">
            <el-image
              :src="row.goods.coverImage || placeholderImage"
              fit="cover"
              class="goods-thumb"
            >
              <template #error>
                <div class="empty-img">暂无图片</div>
              </template>
            </el-image>
            <div>
              <div class="goods-title">{{ row.goods.title }}</div>
              <div class="goods-meta">{{ row.goods.groupName }} / {{ row.goods.idolName }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="cart.quantity" label="数量" width="90" />
      <el-table-column label="单价" width="110">
        <template #default="{ row }">¥{{ row.goods.price }}</template>
      </el-table-column>
      <el-table-column label="小计" width="120">
        <template #default="{ row }">¥{{ (row.goods.price * row.cart.quantity).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="checkout(row.cart.id)">结算</el-button>
          <el-button type="danger" size="small" @click="remove(row.cart.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div v-if="cartList.length === 0" class="empty-tip">购物车空空如也，快去首页挑选你的心仪小卡吧~</div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';

const router = useRouter();
const cartList = ref([]);
const placeholderImage = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='120' height='120'%3E%3Crect width='120' height='120' fill='%23fdf2f8'/%3E%3Ctext x='50%25' y='50%25' dominant-baseline='middle' text-anchor='middle' fill='%23ec407a' font-size='14'%3E暂无图片%3C/text%3E%3C/svg%3E";

const totalQuantity = computed(() => cartList.value.reduce((sum, item) => sum + (item.cart.quantity || 0), 0));
const totalAmount = computed(() => cartList.value.reduce((sum, item) => sum + ((item.goods.price || 0) * (item.cart.quantity || 0)), 0).toFixed(2));

const loadCart = async () => {
  try {
    const res = await axios.get('/api/cart/list');
    cartList.value = res.data.data || [];
  } catch (error) {
    cartList.value = [];
    console.error('加载购物车失败', error);
  }
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

const remove = async (cartId) => {
  try {
    await axios.delete(`/api/cart/remove/${cartId}`);
    loadCart();
  } catch (err) {
    alert(err.response?.data?.message || '删除失败');
  }
};

onMounted(loadCart);
</script>

<style scoped>
.page-content {
  width: 100%;
}

.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
  padding: 22px 26px;
  border-radius: 24px;
  background: linear-gradient(135deg, #ffe4ec 0%, #fff1f5 100%);
  box-shadow: 0 18px 40px rgba(240, 80, 130, 0.12);
}

.cart-header h2 {
  margin: 0 0 8px;
  color: #d81b60;
}

.cart-summary {
  text-align: right;
  color: #ff4d6d;
  font-weight: 600;
}

.cart-table {
  background: #ffffff;
  border-radius: 20px;
  overflow: hidden;
}

.cart-goods {
  display: flex;
  align-items: center;
  gap: 14px;
}

.goods-thumb {
  width: 70px;
  height: 70px;
  border-radius: 18px;
  background: #fff0f6;
}

.goods-title {
  font-weight: 600;
}

.goods-meta {
  color: #909399;
  font-size: 12px;
}

.empty-tip {
  margin-top: 32px;
  color: #909399;
  text-align: center;
}
</style>
