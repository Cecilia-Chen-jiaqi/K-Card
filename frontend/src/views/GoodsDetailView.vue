<template>
  <div class="detail-page">
    <el-card v-if="goodsInfo" class="detail-card">
      <div class="detail-header">
        <div class="detail-image-wrapper">
          <el-image
            :src="goodsInfo.coverImage || placeholderImage"
            fit="cover"
            class="detail-image"
          >
            <template #error>
              <div class="empty-img">暂无图片</div>
            </template>
          </el-image>
          <div class="tags">
            <el-tag type="success">{{ goodsInfo.tradeType }}</el-tag>
            <el-tag type="warning" v-if="goodsInfo.reserveSupport === 1">支持预留</el-tag>
            <el-tag type="info">库存 {{ goodsInfo.stock || 0 }}</el-tag>
          </div>
        </div>
        <div class="detail-info">
          <h2>{{ goodsInfo.title }}</h2>
          <div class="price">¥{{ goodsInfo.price }}</div>
          <div class="meta-row">
            <span>团体：{{ goodsInfo.groupName || '暂无' }}</span>
            <span>爱豆：{{ goodsInfo.idolName || '暂无' }}</span>
          </div>
          <div class="meta-row">
            <span>品相：{{ goodsInfo.quality || '暂无' }}</span>
            <span>发布者：{{ goodsInfo.sellerUsername || '匿名' }}</span>
          </div>
          <div class="meta-row last-row">
            <span>校园：{{ goodsInfo.sellerCampus || '暂无' }}</span>
            <span>商品状态：{{ goodsInfo.status === 1 ? '上架中' : '已下架' }}</span>
          </div>
          <div class="actions">
            <div v-if="goodsInfo.status === 1">
              <el-button type="success" size="large" @click="addToCart">加入购物车</el-button>
              <el-button type="danger" size="large" @click="buyNow">立即购买</el-button>
            </div>
            <span v-else style="color:red;">商品已下架，无法加购</span>
          </div>
        </div>
      </div>
      <div class="detail-body">
        <div class="detail-panel">
          <h3>商品信息</h3>
          <p>{{ goodsInfo.description || '卖家没有填写商品描述。' }}</p>
          <p class="detail-field"><strong>瑕疵图：</strong></p>
          <div class="defect-images" v-if="defectImages.length">
            <el-image
              v-for="(img, index) in defectImages"
              :key="index"
              :src="img"
              fit="cover"
              class="defect-image"
            >
              <template #error>
                <div class="empty-img">加载失败</div>
              </template>
            </el-image>
          </div>
          <p v-else>暂无瑕疵图</p>
        </div>
        <div class="detail-panel">
          <h3>更多详情</h3>
          <p><strong>捆卡说明：</strong> {{ goodsInfo.cardBundle || '无' }}</p>
          <p><strong>换卡说明：</strong> {{ goodsInfo.exchangeInfo || '无' }}</p>
          <p><strong>预留截止：</strong> {{ formattedDeadline }}</p>
          <p><strong>补充信息：</strong> {{ goodsInfo.extraInfo || '无' }}</p>
        </div>
      </div>
    </el-card>
    <div v-else class="detail-error">
      {{ error || '商品不存在或已下架' }}
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();
const goodsInfo = ref(null);
const error = ref('');
const placeholderImage = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='300' height='250'%3E%3Crect width='300' height='250' fill='%23fce4ec'/%3E%3Ctext x='50%25' y='50%25' dominant-baseline='middle' text-anchor='middle' fill='%23f48fb1' font-size='18'%3E暂无图片%3C/text%3E%3C/svg%3E";
const isLoggedIn = computed(() => !!localStorage.getItem('authToken'));
const goodsId = ref(route.query.id || route.params.id || null);

const defectImages = computed(() => {
  if (!goodsInfo.value?.defectImages) {
    return [];
  }
  return goodsInfo.value.defectImages.split(',').map((item) => item.trim()).filter(Boolean);
});

const formattedDeadline = computed(() => {
  if (!goodsInfo.value?.reserveDeadline) {
    return '无';
  }
  const date = new Date(goodsInfo.value.reserveDeadline);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
});

const getDetail = async () => {
  if (!goodsId.value) {
    error.value = '商品ID缺失，无法加载详情';
    return;
  }
  try {
    const res = await axios.get('/api/goods/detail', {
      params: { id: goodsId.value },
    });
    if (res.data.code !== 0) {
      goodsInfo.value = null;
      error.value = res.data.message || '商品不存在或已下架';
      return;
    }
    goodsInfo.value = res.data.data;
    error.value = '';
  } catch (e) {
    goodsInfo.value = null;
    error.value = '商品信息加载失败，请稍后重试。';
  }
};

const requireLogin = () => {
  if (!isLoggedIn.value) {
    alert('请先登录后再操作');
    router.push({ path: '/login', query: { redirect: route.fullPath } });
    return false;
  }
  return true;
};

const addToCart = async () => {
  if (!requireLogin() || !goodsInfo.value) {
    return;
  }
  try {
    const res = await axios.post('/api/cart/add', {
      goodsId: goodsInfo.value.id,
      quantity: 1,
    });
    if (res.data.code !== 0) {
      return alert(res.data.message || '加入购物车失败');
    }
    alert('已加入购物车');
    router.push('/cart');
  } catch (err) {
    alert(err.response?.data?.message || '加入购物车失败');
  }
};

const buyNow = async () => {
  if (!requireLogin() || !goodsInfo.value) {
    return;
  }
  try {
    const res = await axios.post('/api/orders/create', {
      goodsId: goodsInfo.value.id,
      quantity: 1,
      payType: 1,
    });
    if (res.data.code !== 0) {
      return alert(res.data.message || '下单失败');
    }
    router.push(`/checkout/${res.data.data.orderNo}`);
  } catch (err) {
    alert(err.response?.data?.message || '下单失败');
  }
};

onMounted(getDetail);
</script>

<style scoped>
.detail-page {
  width: 100%;
}

.detail-card {
  padding: 24px;
  border-radius: 24px;
  background: linear-gradient(180deg, #fff0f6 0%, #fff9fb 100%);
  box-shadow: 0 20px 40px rgba(229, 115, 147, 0.16);
}

.detail-header {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.detail-image-wrapper {
  min-width: 320px;
  border-radius: 24px;
  overflow: hidden;
  background: #fff;
  padding: 16px;
}

.detail-image {
  width: 320px;
  height: 280px;
  border-radius: 18px;
  background: #fff7f8;
}

.tags {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 14px;
}

.detail-info {
  flex: 1;
}

.detail-info h2 {
  margin: 0 0 18px;
  font-size: 32px;
  color: #d81b60;
}

.price {
  font-size: 32px;
  color: #ff4565;
  margin-bottom: 16px;
}

.meta-row {
  display: flex;
  gap: 24px;
  margin-bottom: 10px;
}

.last-row {
  margin-bottom: 20px;
}

.actions {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.detail-body {
  display: flex;
  gap: 24px;
  margin-top: 32px;
  flex-wrap: wrap;
}

.detail-panel {
  flex: 1;
  min-width: 280px;
  padding: 20px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.88);
  box-shadow: 0 8px 24px rgba(206, 126, 150, 0.14);
}

.detail-panel h3 {
  margin-bottom: 16px;
  color: #c2185b;
}

.detail-field {
  margin-top: 10px;
}

.defect-images {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
}

.defect-image {
  width: 100px;
  height: 100px;
  border-radius: 16px;
  background: #fff5f7;
}

.empty-img {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #c0c4cc;
}

.detail-error {
  padding: 24px;
  text-align: center;
  color: #f56c6c;
}
</style>
