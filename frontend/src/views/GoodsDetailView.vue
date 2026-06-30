<template>
  <div class="detail-page kc-animate-in page-content">
    <el-card v-if="goodsInfo" class="detail-card kc-tech-card kc-scan-lr">
      <div class="detail-header">
        <div class="detail-image-wrapper kc-img-zoom">
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
            <span class="kc-tag">{{ goodsInfo.tradeType }}</span>
            <span v-if="goodsInfo.reserveSupport === 1" class="kc-tag">支持预留</span>
            <span class="kc-tag">库存 {{ goodsInfo.stock || 0 }}</span>
          </div>
        </div>
        <div class="detail-info">
          <h2 class="kc-tech-title">{{ goodsInfo.title }}</h2>
          <div class="price kc-digital-price">¥{{ goodsInfo.price }}</div>

          <div class="kc-meta-grid">
            <div class="kc-meta-item">
              <label>团体</label>
              <span>{{ goodsInfo.groupName || '暂无' }}</span>
            </div>
            <div class="kc-meta-item">
              <label>成员</label>
              <span>{{ goodsInfo.idolName || '暂无' }}</span>
            </div>
            <div class="kc-meta-item">
              <label>品相</label>
              <span>{{ goodsInfo.quality || '暂无' }}</span>
            </div>
            <div class="kc-meta-item">
              <label>卡种</label>
              <span>{{ goodsInfo.cardType || '未标注' }}</span>
            </div>
            <div class="kc-meta-item">
              <label>专辑/时期</label>
              <span>{{ goodsInfo.albumEra || '未标注' }}</span>
            </div>
            <div class="kc-meta-item seller-row">
              <label>发布者</label>
              <span class="seller-name">{{ goodsInfo.sellerUsername || '匿名' }}</span>
              <el-button
                v-if="!isOwnGoods && goodsInfo.sellerId"
                size="small"
                round
                :type="followingSeller ? 'primary' : 'default'"
                plain
                @click="toggleFollow"
              >{{ followingSeller ? '已关注' : '+ 关注卖家' }}</el-button>
            </div>
            <div class="kc-meta-item">
              <label>校园</label>
              <span>{{ goodsInfo.sellerCampus || '暂无' }}</span>
            </div>
            <div class="kc-meta-item">
              <label>状态</label>
              <span :class="{ 'status-on': goodsInfo.status === 1 }">{{ goodsInfo.status === 1 ? '上架中' : '已下架' }}</span>
            </div>
          </div>

          <div class="actions">
            <template v-if="goodsInfo.status === 1 && !isOwnGoods">
              <el-button type="primary" size="large" round class="kc-btn-ripple kc-btn-glow" @click="buyNow">立即购买</el-button>
              <el-button size="large" round class="kc-btn-outline kc-btn-ripple" @click="addToCart">加入购物车</el-button>
              <el-button
                size="large"
                round
                :type="favorited ? 'warning' : 'default'"
                plain
                @click="toggleFavorite"
              >{{ favorited ? '★ 已收藏' : '☆ 收藏' }}</el-button>
            </template>
            <template v-else-if="!isOwnGoods && goodsInfo.status !== 1">
              <el-button
                size="large"
                round
                :type="favorited ? 'warning' : 'default'"
                plain
                @click="toggleFavorite"
              >{{ favorited ? '★ 已收藏' : '☆ 收藏' }}</el-button>
            </template>
            <div v-else-if="isOwnGoods" class="off-shelf">这是您发布的小卡，无法购买</div>
            <div v-else class="off-shelf">商品已下架，无法加购</div>
          </div>
        </div>
      </div>

      <div class="detail-body">
        <div class="detail-panel">
          <h3>商品信息</h3>
          <p class="panel-text">{{ goodsInfo.description || '卖家没有填写商品描述。' }}</p>
          <p class="detail-field"><strong>瑕疵图</strong></p>
          <div class="defect-images" v-if="defectImages.length">
            <el-image
              v-for="(img, index) in defectImages"
              :key="index"
              :src="img"
              fit="cover"
              class="defect-image kc-img-zoom"
            >
              <template #error>
                <div class="empty-img">加载失败</div>
              </template>
            </el-image>
          </div>
          <p v-else class="panel-muted">暂无瑕疵图</p>
        </div>
        <div class="detail-panel">
          <h3>更多详情</h3>
          <ul class="detail-list">
            <li><span>捆卡说明</span><strong>{{ goodsInfo.cardBundle || '无' }}</strong></li>
            <li><span>换卡说明</span><strong>{{ goodsInfo.exchangeInfo || '无' }}</strong></li>
            <li><span>预留截止</span><strong>{{ formattedDeadline }}</strong></li>
            <li><span>补充信息</span><strong>{{ goodsInfo.extraInfo || '无' }}</strong></li>
          </ul>
        </div>
      </div>
    </el-card>

    <div v-else class="kc-empty detail-error">
      <div class="empty-icon">😔</div>
      <h3>{{ error || '商品不存在或已下架' }}</h3>
      <el-button type="primary" round @click="router.push('/')">返回首页</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';

const route = useRoute();
const router = useRouter();
const goodsInfo = ref(null);
const error = ref('');
const favorited = ref(false);
const followingSeller = ref(false);
const placeholderImage = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='300' height='250'%3E%3Crect width='300' height='250' fill='%23eef6fd'/%3E%3Ctext x='50%25' y='50%25' dominant-baseline='middle' text-anchor='middle' fill='%234A90E2' font-size='18'%3E暂无图片%3C/text%3E%3C/svg%3E";
const isLoggedIn = computed(() => !!localStorage.getItem('authToken'));

const currentUser = computed(() => {
  try {
    return JSON.parse(localStorage.getItem('currentUser') || 'null');
  } catch {
    return null;
  }
});

const isOwnGoods = computed(() => {
  if (!goodsInfo.value?.sellerId || !currentUser.value?.id) return false;
  return String(goodsInfo.value.sellerId) === String(currentUser.value.id);
});

const goodsId = ref(route.query.id || route.params.id || null);

const defectImages = computed(() => {
  if (!goodsInfo.value?.defectImages) return [];
  return goodsInfo.value.defectImages.split(',').map((item) => item.trim()).filter(Boolean);
});

const formattedDeadline = computed(() => {
  if (!goodsInfo.value?.reserveDeadline) return '无';
  const date = new Date(goodsInfo.value.reserveDeadline);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
});

const getDetail = async () => {
  if (!goodsId.value) {
    error.value = '商品ID缺失，无法加载详情';
    return;
  }
  try {
    const res = await axios.get('/api/goods/detail', { params: { id: goodsId.value } });
    if (res.data.code !== 0) {
      goodsInfo.value = null;
      error.value = res.data.message || '商品不存在或已下架';
      return;
    }
    goodsInfo.value = res.data.data;
    favorited.value = !!res.data.data?.favorited;
    followingSeller.value = !!res.data.data?.followingSeller;
    error.value = '';
  } catch {
    goodsInfo.value = null;
    error.value = '商品信息加载失败，请稍后重试。';
  }
};

const requireLogin = () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录后再操作');
    router.push({ path: '/login', query: { redirect: route.fullPath } });
    return false;
  }
  return true;
};

const addToCart = async () => {
  if (!requireLogin() || !goodsInfo.value) return;
  try {
    const res = await axios.post('/api/cart/add', { goodsId: goodsInfo.value.id, quantity: 1 });
    if (res.data.code !== 0) {
      ElMessage.error(res.data.message || '加入购物车失败');
      return;
    }
    ElMessage.success('已加入购物车');
    window.dispatchEvent(new Event('cart-updated'));
    router.push('/cart');
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '加入购物车失败');
  }
};

const buyNow = async () => {
  if (!requireLogin() || !goodsInfo.value) return;
  try {
    const res = await axios.post('/api/orders/create', {
      goodsId: goodsInfo.value.id,
      quantity: 1,
      payType: 1,
    });
    if (res.data.code !== 0) {
      ElMessage.error(res.data.message || '下单失败');
      return;
    }
    router.push(`/checkout/${res.data.data.orderNo}`);
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '下单失败');
  }
};

const toggleFavorite = async () => {
  if (!requireLogin() || !goodsInfo.value?.id) return;
  try {
    if (favorited.value) {
      const res = await axios.delete(`/api/favorite/remove/${goodsInfo.value.id}`);
      if (res.data.code !== 0) {
        ElMessage.error(res.data.message || '取消收藏失败');
        return;
      }
      favorited.value = false;
      ElMessage.success('已取消收藏');
    } else {
      const res = await axios.post('/api/favorite/add', { goodsId: goodsInfo.value.id });
      if (res.data.code !== 0) {
        ElMessage.error(res.data.message || '收藏失败');
        return;
      }
      favorited.value = true;
      ElMessage.success('已收藏');
    }
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '操作失败');
  }
};

const toggleFollow = async () => {
  if (!requireLogin() || !goodsInfo.value?.sellerId) return;
  try {
    if (followingSeller.value) {
      const res = await axios.delete(`/api/follow/remove/${goodsInfo.value.sellerId}`);
      if (res.data.code !== 0) {
        ElMessage.error(res.data.message || '取消关注失败');
        return;
      }
      followingSeller.value = false;
      ElMessage.success('已取消关注');
    } else {
      const res = await axios.post('/api/follow/add', { sellerId: goodsInfo.value.sellerId });
      if (res.data.code !== 0) {
        ElMessage.error(res.data.message || '关注失败');
        return;
      }
      followingSeller.value = true;
      ElMessage.success('已关注卖家');
    }
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '操作失败');
  }
};

onMounted(getDetail);
</script>

<style scoped>
.detail-card {
  padding: 28px;
  border-radius: var(--kc-radius-lg);
}

.detail-card :deep(.el-card__body) {
  padding: 0;
}

.detail-header {
  display: flex;
  gap: 32px;
  flex-wrap: wrap;
}

.detail-image-wrapper {
  min-width: 300px;
  border-radius: var(--kc-radius-lg);
  overflow: hidden;
  background: var(--kc-bg-soft);
  padding: 14px;
  border: 1px solid var(--kc-border);
}

.detail-image {
  width: 300px;
  height: 280px;
  border-radius: 16px;
  background: #fff;
  display: block;
}

.tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 14px;
}

.detail-info {
  flex: 1;
  min-width: 280px;
}

.detail-info h2 {
  margin: 0 0 12px;
  font-size: 26px;
  color: var(--kc-text);
}

.price {
  font-size: 32px;
  margin-bottom: 8px;
}

.status-on { color: var(--kc-primary-dark); }

.seller-row {
  grid-column: 1 / -1;
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.seller-name { margin-right: 4px; }

.actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 8px;
}

.off-shelf {
  padding: 10px 16px;
  border-radius: 12px;
  background: var(--kc-bg-soft);
  color: var(--kc-text-muted);
  border: 1px solid var(--kc-border);
  font-size: 14px;
}

.detail-body {
  display: flex;
  gap: 20px;
  margin-top: 32px;
  flex-wrap: wrap;
}

.detail-panel {
  flex: 1;
  min-width: 280px;
  padding: 22px;
  border-radius: var(--kc-radius);
  background: var(--kc-bg-soft);
  border: 1px solid var(--kc-border);
}

.detail-panel h3 {
  margin: 0 0 14px;
  font-size: 16px;
  font-weight: 700;
  color: var(--kc-primary-dark);
}

.panel-text {
  margin: 0 0 16px;
  line-height: 1.7;
  color: var(--kc-text-muted);
}

.panel-muted {
  margin: 0;
  color: var(--kc-text-muted);
  font-size: 14px;
}

.detail-field {
  margin: 0 0 10px;
  font-size: 14px;
  color: var(--kc-text);
}

.detail-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-list li {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 12px;
  border-bottom: 1px dashed var(--kc-border);
  font-size: 14px;
}

.detail-list li:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.detail-list span { color: var(--kc-text-muted); flex-shrink: 0; }
.detail-list strong { color: var(--kc-text); font-weight: 600; text-align: right; }

.defect-images {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.defect-image {
  width: 96px;
  height: 96px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid var(--kc-border);
  overflow: hidden;
}

.empty-img {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--kc-text-muted);
  font-size: 13px;
}

.detail-error h3 {
  margin: 0 0 16px;
  color: var(--kc-text);
  font-weight: 600;
}
</style>
