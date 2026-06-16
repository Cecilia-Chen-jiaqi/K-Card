<template>
  <div class="page-content user-center-page">
    <el-card class="profile-card">
      <div class="profile-top">
        <div class="avatar-box">
          <img v-if="user?.avatar" :src="user.avatar" alt="头像" class="user-avatar" />
          <div v-else class="avatar-fallback">{{ user?.username?.charAt(0)?.toUpperCase() || 'U' }}</div>
        </div>
        <div class="profile-info">
          <h2>{{ user?.username || '用户' }}</h2>
          <p>校园：{{ user?.campus || '未填写' }}</p>
          <p>手机号：{{ user?.phone || '未填写' }}</p>
        </div>
      </div>
      <div class="profile-actions">
        <el-button type="primary" @click="goPublish">发布商品</el-button>
        <el-button type="success" @click="goCart">我的购物车</el-button>
        <el-button type="warning" @click="logout">退出登录</el-button>
      </div>
    </el-card>

    <el-card class="order-card">
      <el-tabs v-model="activeTab" @tab-click="changeTab">
        <el-tab-pane label="买家订单" name="buyer"></el-tab-pane>
        <el-tab-pane label="卖家订单" name="seller"></el-tab-pane>
      </el-tabs>
      <div v-if="activeTab === 'buyer'">
        <el-table :data="buyerOrders" stripe border class="order-table">
          <el-table-column prop="order.orderNo" label="订单号" width="180"></el-table-column>
          <el-table-column label="商品">
            <template #default="{ row }">
              <div>{{ row.goods.title }}</div>
              <div class="small-text">{{ row.goods.groupName }} / {{ row.goods.idolName }}</div>
            </template>
          </el-table-column>
          <el-table-column prop="order.amount" label="金额" width="100">
            <template #default="{ row }">¥{{ row.order.amount }}</template>
          </el-table-column>
          <el-table-column label="状态" width="140">
            <template #default="{ row }">{{ statusLabel(row.order.status) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button v-if="row.order.status === 0" type="danger" size="small" @click="cancelOrder(row.order.orderNo)">取消订单</el-button>
              <el-button v-if="row.order.status === 2" type="primary" size="small" @click="confirmReceipt(row.order.orderNo)">确认收货</el-button>
              <el-button type="text" size="small" @click="viewOrder(row.order.orderNo)">查看订单</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div v-if="buyerOrders.length === 0" class="order-empty">暂无买家订单，快去下单吧~</div>
      </div>
      <div v-else>
        <el-table :data="sellerOrders" stripe border class="order-table">
          <el-table-column prop="order.orderNo" label="订单号" width="180"></el-table-column>
          <el-table-column label="商品">
            <template #default="{ row }">
              <div>{{ row.goods.title }}</div>
              <div class="small-text">{{ row.goods.groupName }} / {{ row.goods.idolName }}</div>
            </template>
          </el-table-column>
          <el-table-column prop="order.amount" label="金额" width="100">
            <template #default="{ row }">¥{{ row.order.amount }}</template>
          </el-table-column>
          <el-table-column label="状态" width="140">
            <template #default="{ row }">{{ statusLabel(row.order.status) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button v-if="row.order.status === 1" type="primary" size="small" @click="shipOrder(row.order.orderNo)">确认发货</el-button>
              <el-button type="text" size="small" @click="viewOrder(row.order.orderNo)">查看订单</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div v-if="sellerOrders.length === 0" class="order-empty">暂无卖家订单，发布商品后会在这里展示。</div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue';
import axios from 'axios';
import { useRouter, useRoute } from 'vue-router';

const router = useRouter();
const route = useRoute();
const user = ref(null);
const buyerOrders = ref([]);
const sellerOrders = ref([]);
const activeTab = ref(route.query.tab === 'seller' ? 'seller' : 'buyer');

const loadUser = () => {
  try {
    user.value = JSON.parse(localStorage.getItem('currentUser') || 'null');
  } catch (e) {
    user.value = null;
  }
};

const loadOrders = async () => {
  if (activeTab.value === 'seller') {
    const res = await axios.get('/api/orders/seller/list');
    sellerOrders.value = res.data.data || [];
  } else {
    const res = await axios.get('/api/orders/buyer/list');
    buyerOrders.value = res.data.data || [];
  }
};

const changeTab = (tab) => {
  activeTab.value = tab.name;
  router.replace({ query: { ...route.query, tab: tab.name } });
};

const statusLabel = (status) => {
  switch (status) {
    case 0:
      return '待支付';
    case 1:
      return '已支付';
    case 2:
      return '已发货';
    case 3:
      return '交易完成';
    case 4:
      return '交易关闭';
    case 5:
      return '退款完成';
    default:
      return '未知状态';
  }
};

const shipOrder = async (orderNo) => {
  try {
    await axios.post('/api/orders/ship', { orderNo });
    loadOrders();
  } catch (err) {
    alert(err.response?.data?.message || '操作失败');
  }
};

const confirmReceipt = async (orderNo) => {
  try {
    await axios.post('/api/orders/complete', { orderNo });
    loadOrders();
  } catch (err) {
    alert(err.response?.data?.message || '确认收货失败');
  }
};

const cancelOrder = async (orderNo) => {
  try {
    await axios.post('/api/orders/cancel', { orderNo });
    loadOrders();
  } catch (err) {
    alert(err.response?.data?.message || '取消失败');
  }
};

const viewOrder = (orderNo) => {
  router.push(`/checkout/${orderNo}`);
};

const goPublish = () => router.push('/publish');
const goCart = () => router.push('/cart');
const logout = () => {
  localStorage.removeItem('authToken');
  localStorage.removeItem('currentUser');
  router.push('/login');
};

onMounted(() => {
  loadUser();
  loadOrders();
});

watch(activeTab, loadOrders);
watch(
  () => route.query.tab,
  (next) => {
    if (next && next !== activeTab.value) {
      activeTab.value = next;
    }
  }
);
</script>

<style scoped>
.page-content {
  max-width: 960px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.profile-card,
.order-card {
  border-radius: 24px;
  padding: 24px;
  background: linear-gradient(180deg, #fff8f9 0%, #fff1f5 100%);
  box-shadow: 0 18px 36px rgba(242, 101, 146, 0.12);
}

.profile-top {
  display: flex;
  align-items: center;
  gap: 22px;
}

.avatar-box {
  width: 100px;
  height: 100px;
  border-radius: 24px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffe8f2;
}

.user-avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-fallback {
  font-size: 36px;
  color: #d81b60;
  font-weight: 700;
}

.profile-info h2 {
  margin: 0 0 10px;
}

.profile-actions {
  margin-top: 22px;
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
}

.order-table {
  margin-top: 18px;
  border-radius: 18px;
  overflow: hidden;
}

.small-text {
  color: #909399;
  font-size: 12px;
}

.order-empty {
  margin-top: 18px;
  text-align: center;
  color: #909399;
}
</style>
