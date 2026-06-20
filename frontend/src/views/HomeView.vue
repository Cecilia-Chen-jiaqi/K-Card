<template>
  <div class="home-page">
    <section class="hero-card">
      <div>
        <h1>欢迎来到 K-CARD 小卡交易站</h1>
        <p>友善交易、轻松买卖，让你的爱豆小卡闪闪发光。</p>
      </div>
      <div class="hero-actions">
        <el-button type="primary" size="large" @click="$router.push('/publish')">发布你的小卡</el-button>
        <el-button type="success" size="large" @click="$router.push('/cart')">查看购物车</el-button>
      </div>
    </section>

    <div class="goods-grid">
      <el-card v-for="item in goodsList" :key="item.id" class="goods-card">
       <el-image
        :src="item.cover_image || placeholderImage"
        style="width:100%;height:180px;background:#f9f0f6"
        fit="cover"
      >
        <template #error>
          <div style="width:100%;height:100%;display:flex;align-items:center;justify-content:center;color:#ff6994;">
            暂无图片
          </div>
        </template>
      </el-image>
        <div class="goods-info">
          <div class="goods-title">{{ item.title }}</div>
          <div class="goods-meta">{{ item.groupName }} · {{ item.idolName }}</div>
          <div class="goods-tags">
            <el-tag size="small">{{ item.quality }}</el-tag>
            <el-tag size="small" type="info">{{ item.tradeType }}</el-tag>
          </div>
          <div class="goods-price">¥{{ item.price }}</div>
          <div class="card-bottom">
            <el-button 
              type="primary" 
              size="small"
              @click="goDetail(item.id)"
            >
              立即查看
            </el-button>
            <el-button 
              type="success" 
              size="small"
              style="margin-left:8px"
              @click="addCart(item.id)"
            >
              加入购物车
            </el-button>
          </div>
        </div>
      </el-card>
    </div>

    <div v-if="goodsList.length === 0" class="empty-state">
      <h3>当前暂无上架商品</h3>
      <p>如果你有可爱的偶像小卡，快来发布吧！</p>
      <el-button type="primary" @click="$router.push('/publish')">去发布小卡</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';

const router = useRouter();
const goodsList = ref([]);
const placeholderImage = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='300' height='200'%3E%3Crect width='300' height='200' fill='%23fff0f6'/%3E%3Ctext x='50%25' y='50%25' dominant-baseline='middle' text-anchor='middle' fill='%23f06292' font-size='18'%3E暂无图片%3C/text%3E%3C/svg%3E";

const addCart = async (goodsId) => {
  const token = localStorage.getItem('authToken');
  if (!token) {
    ElMessage.error('请先登录');
    router.push('/login');
    return;
  }
  try {
    const res = await axios.post('/api/cart/add', { goodsId, quantity: 1 });
    if (res.data.code === 0) {
      ElMessage.success('成功加入购物车！');
    } else {
      ElMessage.warning(res.data.message || '加购失败');
    }
  } catch (err) {
    if (err.response?.status === 401) {
      ElMessage.error('请先登录');
      router.push('/login');
    } else {
      ElMessage.error('加入购物车失败');
    }
  }
};

const loadGoods = async () => {
  try {
    const res = await axios.get('/api/goods/list');
    goodsList.value = res.data.data || [];
  } catch (error) {
    goodsList.value = [];
    console.error('加载商品列表失败', error);
  }
};

const goDetail = (id) => {
  if (!id) {
    console.warn('商品ID为空，无法跳转详情');
    return;
  }
  router.push({ path: '/goods/detail', query: { id } });
};

onMounted(loadGoods);
</script>

<style scoped>
.home-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.hero-card {
  padding: 28px 32px;
  border-radius: 28px;
  background: linear-gradient(135deg, #ffeff7 0%, #fff6f9 100%);
  box-shadow: 0 20px 40px rgba(242, 150, 180, 0.14);
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 30px;
}

.hero-card h1 {
  margin: 0 0 10px;
  font-size: 32px;
  color: #d81b60;
}

.hero-card p {
  margin: 0;
  color: #636266;
}

.hero-actions {
  display: flex;
  gap: 16px;
}

.goods-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
}

.goods-card {
  border-radius: 24px;
  padding: 16px;
  background: linear-gradient(180deg, #fff3f8 0%, #ffffff 100%);
  box-shadow: 0 14px 30px rgba(240, 165, 196, 0.12);
}

.goods-image {
  width: 100%;
  min-height: 150px;
  border-radius: 20px;
  overflow: hidden;
  background: #fff0f6;
}

.goods-info {
  margin-top: 14px;
}

.goods-title {
  font-weight: 700;
  margin-bottom: 6px;
}

.goods-meta {
  color: #909399;
  font-size: 13px;
  margin-bottom: 10px;
}

.goods-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.goods-price {
  font-size: 20px;
  color: #ff4d73;
  font-weight: 700;
  margin-bottom: 16px;
}

.card-bottom {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
}

.empty-state {
  text-align: center;
  padding: 48px 0;
  color: #909399;
}
</style>
