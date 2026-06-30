<template>
  <div class="home-page kc-animate-in">
    <section class="hero-card kc-hero-glass kc-scan-lr">
      <div class="kc-grid-bg" aria-hidden="true" />
      <div class="hero-content">
        <p class="hero-badge">WTS出卡 · WTT换卡 · WTB收卡 | 为爱发电，双向奔赴</p>
        <h1 class="kc-gradient-text kc-tech-title">发现你的下一张本命小卡</h1>
        <p class="hero-desc">专辑卡、特典卡、签售卡… 按团体/成员精准筛选，安全支付，安心交易。</p>
      </div>
      <div class="hero-actions">
        <el-button type="primary" size="large" round class="kc-btn-ripple kc-btn-glow" @click="$router.push('/publish')">发布小卡</el-button>
        <el-button size="large" round class="kc-btn-outline kc-btn-ripple" @click="$router.push('/exchange')">换卡专区</el-button>
      </div>
    </section>

    <section class="filter-panel kc-scan-lr">
      <div class="filter-grid-bg" aria-hidden="true" />
      <div class="filter-accent" aria-hidden="true" />

      <div class="filter-inner">
        <div class="filter-header">
          <div class="filter-title">
            <span class="filter-icon-wrap">
              <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.2">
                <circle cx="11" cy="11" r="7" />
                <path d="M20 20l-3.5-3.5" stroke-linecap="round" />
              </svg>
            </span>
            <span class="filter-title-text">筛选小卡</span>
            <span v-if="activeFilterCount" class="filter-count">{{ activeFilterCount }} 项</span>
          </div>
          <p class="filter-hint">精准匹配 · 快速找到心仪小卡</p>
        </div>

        <div class="filter-search-row">
          <span class="field-label">搜索</span>
          <el-input
            v-model="filters.keyword"
            placeholder="输入标题关键词..."
            clearable
            class="filter-control filter-search-input"
            @keyup.enter="loadGoods"
          />
        </div>

        <div class="filter-select-grid">
          <div class="select-cell">
            <span class="field-label">团体</span>
            <el-select v-model="filters.groupName" placeholder="全部团体" clearable filterable class="filter-control" @change="onGroupChange">
              <el-option v-for="g in groupNames" :key="g" :label="g" :value="g" />
            </el-select>
          </div>
          <div class="select-cell" :class="{ 'is-disabled-cell': !filters.groupName }">
            <span class="field-label">成员</span>
            <el-select v-model="filters.idolName" placeholder="全部成员" clearable filterable class="filter-control" :disabled="!filters.groupName">
              <el-option v-for="m in memberOptions" :key="m" :label="m" :value="m" />
            </el-select>
          </div>
          <div class="select-cell">
            <span class="field-label">卡种</span>
            <el-select v-model="filters.cardType" placeholder="全部卡种" clearable class="filter-control">
              <el-option v-for="t in cardTypes" :key="t" :label="t" :value="t" />
            </el-select>
          </div>
          <div class="select-cell">
            <span class="field-label">交易方式</span>
            <el-select v-model="filters.tradeType" placeholder="全部方式" clearable class="filter-control">
              <el-option v-for="t in tradeTypes" :key="t" :label="t" :value="t" />
            </el-select>
          </div>
          <div class="select-cell">
            <span class="field-label">品相</span>
            <el-select v-model="filters.quality" placeholder="全部品相" clearable class="filter-control">
              <el-option v-for="q in qualities" :key="q" :label="q" :value="q" />
            </el-select>
          </div>
          <div class="select-cell price-cell">
            <span class="field-label">价格区间</span>
            <div class="price-range">
              <el-input v-model="filters.minPrice" placeholder="最低" clearable class="filter-control price-input" />
              <span class="price-sep">—</span>
              <el-input v-model="filters.maxPrice" placeholder="最高" clearable class="filter-control price-input" />
            </div>
          </div>
        </div>

        <div class="filter-footer">
          <el-button type="primary" round class="kc-btn-ripple kc-btn-glow btn-filter" @click="loadGoods">
            筛选
          </el-button>
          <el-button round class="kc-btn-outline kc-btn-ripple btn-reset" @click="resetFilters">
            重置
          </el-button>
        </div>
      </div>
    </section>

    <div v-if="!loading && goodsList.length > 0" class="kc-section-head">
      <h3>在售小卡</h3>
      <span>共 {{ goodsList.length }} 件</span>
    </div>

    <div v-loading="loading" class="goods-grid">
      <el-card
        v-for="(item, idx) in goodsList"
        :key="item.id"
        v-scroll-reveal="idx"
        class="goods-card kc-tech-card kc-card-hover"
      >
        <div class="card-image-wrap kc-img-zoom" @click="goDetail(item.id)">
          <el-image :src="coverUrl(item)" fit="cover" class="card-image">
            <template #error>
              <div class="image-placeholder">暂无图片</div>
            </template>
          </el-image>
          <span v-if="item.cardType" class="card-type-badge">{{ item.cardType }}</span>
          <button
            v-if="!isOwnGoods(item)"
            type="button"
            class="favorite-btn"
            :class="{ active: isFavorited(item) }"
            :title="isFavorited(item) ? '取消收藏' : '收藏'"
            @click.stop="toggleFavorite(item)"
          >
            {{ isFavorited(item) ? '★' : '☆' }}
          </button>
        </div>
        <div class="goods-info">
          <div class="goods-title" @click="goDetail(item.id)">{{ item.title }}</div>
          <div class="goods-meta">{{ item.groupName }} · {{ item.idolName }}</div>
          <div class="goods-tags">
            <span class="kc-tag">{{ item.quality }}</span>
            <span class="kc-tag">{{ item.tradeType }}</span>
            <span v-if="item.albumEra" class="kc-tag">{{ item.albumEra }}</span>
          </div>
          <div class="goods-bottom">
            <div class="goods-price kc-digital-price">¥{{ item.price }}</div>
            <div class="card-actions">
              <el-button type="primary" size="small" round @click="goDetail(item.id)">查看</el-button>
              <el-button
                v-if="!isOwnGoods(item)"
                size="small"
                round
                :type="isFavorited(item) ? 'warning' : 'default'"
                plain
                @click.stop="toggleFavorite(item)"
              >{{ isFavorited(item) ? '已收藏' : '收藏' }}</el-button>
              <el-button size="small" round class="kc-btn-outline" @click.stop="addCart(item.id)">加购</el-button>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <div v-if="!loading && goodsList.length === 0" class="kc-empty empty-state">
      <div class="empty-icon">📭</div>
      <h3>暂无匹配的小卡</h3>
      <p>试试调整筛选条件，或成为第一个发布者</p>
      <el-button type="primary" round @click="$router.push('/publish')">发布小卡</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { loadKpopMeta, GROUP_NAMES, GROUP_MEMBERS, CARD_TYPES, QUALITIES, TRADE_TYPES } from '../constants/kpopMeta';

const router = useRouter();
const goodsList = ref([]);
const loading = ref(false);
const favoriteIds = ref(new Set());
const meta = ref({ groups: GROUP_MEMBERS, cardTypes: CARD_TYPES, qualities: QUALITIES, tradeTypes: TRADE_TYPES });

const filters = reactive({
  keyword: '', groupName: '', idolName: '', cardType: '', tradeType: '', quality: '',
  minPrice: '', maxPrice: '',
});

const groupNames = computed(() => Object.keys(meta.value.groups || {}));
const memberOptions = computed(() => {
  if (!filters.groupName) return [];
  return meta.value.groups[filters.groupName] || [];
});
const cardTypes = computed(() => meta.value.cardTypes || CARD_TYPES);
const qualities = computed(() => meta.value.qualities || QUALITIES);
const tradeTypes = computed(() => meta.value.tradeTypes || TRADE_TYPES);

const activeFilterCount = computed(() => Object.values(filters).filter(Boolean).length);

const placeholderImage = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='300' height='200'%3E%3Crect width='300' height='200' fill='%23eef6fd'/%3E%3Ctext x='50%25' y='50%25' dominant-baseline='middle' text-anchor='middle' fill='%234A90E2' font-size='16'%3E暂无图片%3C/text%3E%3C/svg%3E";

const coverUrl = (item) => item.coverImage || item.cover_image || placeholderImage;

const currentUserId = () => {
  try {
    const user = JSON.parse(localStorage.getItem('currentUser') || 'null');
    return user?.id != null ? String(user.id) : null;
  } catch {
    return null;
  }
};

const isOwnGoods = (item) => {
  const uid = currentUserId();
  return uid && item?.sellerId != null && String(item.sellerId) === uid;
};

const isFavorited = (item) => favoriteIds.value.has(String(item.id));

const loadFavoriteIds = async () => {
  if (!localStorage.getItem('authToken')) {
    favoriteIds.value = new Set();
    return;
  }
  try {
    const res = await axios.get('/api/favorite/list');
    if (res.data.code === 0) {
      favoriteIds.value = new Set(
        (res.data.data || [])
          .map((row) => row.goods?.id ?? row.favorite?.goodsId)
          .filter(Boolean)
          .map(String),
      );
    }
  } catch {
    favoriteIds.value = new Set();
  }
};

const toggleFavorite = async (item) => {
  if (!localStorage.getItem('authToken')) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  if (isOwnGoods(item)) {
    ElMessage.warning('不能收藏自己发布的小卡');
    return;
  }
  const goodsId = item.id;
  try {
    if (isFavorited(item)) {
      const res = await axios.delete(`/api/favorite/remove/${goodsId}`);
      if (res.data.code !== 0) {
        ElMessage.error(res.data.message || '取消收藏失败');
        return;
      }
      const next = new Set(favoriteIds.value);
      next.delete(String(goodsId));
      favoriteIds.value = next;
      ElMessage.success('已取消收藏');
    } else {
      const res = await axios.post('/api/favorite/add', { goodsId });
      if (res.data.code !== 0) {
        ElMessage.error(res.data.message || '收藏失败');
        return;
      }
      const next = new Set(favoriteIds.value);
      next.add(String(goodsId));
      favoriteIds.value = next;
      ElMessage.success('已收藏');
    }
  } catch (err) {
    const status = err.response?.status;
    const msg = err.response?.data?.message;
    if (status === 404) {
      ElMessage.error('收藏接口未就绪，请重启后端服务后再试');
    } else {
      ElMessage.error(msg || '操作失败');
    }
  }
};

const onGroupChange = () => { filters.idolName = ''; };

const resetFilters = () => {
  Object.assign(filters, {
    keyword: '', groupName: '', idolName: '', cardType: '', tradeType: '', quality: '',
    minPrice: '', maxPrice: '',
  });
  loadGoods();
};

const buildSearchParams = () => {
  const params = { ...filters };
  if (params.minPrice !== '' && params.minPrice != null) {
    params.minPrice = Number(params.minPrice);
    if (Number.isNaN(params.minPrice)) delete params.minPrice;
  } else {
    delete params.minPrice;
  }
  if (params.maxPrice !== '' && params.maxPrice != null) {
    params.maxPrice = Number(params.maxPrice);
    if (Number.isNaN(params.maxPrice)) delete params.maxPrice;
  } else {
    delete params.maxPrice;
  }
  if (params.minPrice != null && params.maxPrice != null && params.minPrice > params.maxPrice) {
    ElMessage.warning('最低价不能高于最高价');
    return null;
  }
  return params;
};

const loadGoods = async () => {
  loading.value = true;
  try {
    const params = buildSearchParams();
    if (params === null) return;
    const hasFilter = Object.values(params).some((v) => v !== '' && v != null);
    const url = hasFilter ? '/api/goods/search' : '/api/goods/list';
    const res = await axios.get(url, { params: hasFilter ? params : {} });
    goodsList.value = res.data.data || [];
  } catch {
    goodsList.value = [];
  } finally {
    loading.value = false;
  }
};

const addCart = async (goodsId) => {
  if (!localStorage.getItem('authToken')) {
    ElMessage.warning('请先登录');
    return router.push('/login');
  }
  try {
    const res = await axios.post('/api/cart/add', { goodsId, quantity: 1 });
    if (res.data.code === 0) {
      ElMessage.success('已加入购物车');
      window.dispatchEvent(new Event('cart-updated'));
    } else ElMessage.warning(res.data.message || '加购失败');
  } catch {
    ElMessage.error('加购失败');
  }
};

const goDetail = (id) => router.push({ path: '/goods/detail', query: { id } });

onMounted(async () => {
  meta.value = await loadKpopMeta(axios);
  await loadFavoriteIds();
  loadGoods();
});
</script>

<style scoped>
.home-page { display: flex; flex-direction: column; gap: 28px; }

.hero-card {
  padding: 36px 40px;
  border-radius: var(--kc-radius-lg);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.96) 0%, rgba(238, 246, 253, 0.9) 100%);
  box-shadow: var(--kc-shadow);
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24px;
  border: 1px solid var(--kc-border);
  backdrop-filter: blur(12px);
}

.hero-badge {
  display: inline-block;
  padding: 4px 14px;
  border-radius: 999px;
  background: var(--kc-bg-soft);
  color: var(--kc-primary);
  font-size: 13px;
  font-weight: 600;
  margin: 0 0 12px;
}

.hero-content h1 { margin: 0 0 10px; font-size: 34px; font-weight: 800; }
.hero-desc { margin: 0; color: var(--kc-text-muted); max-width: 520px; }
.hero-actions { display: flex; gap: 12px; flex-shrink: 0; }

.filter-panel {
  position: relative;
  padding: 0;
  border-radius: var(--kc-radius-lg);
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.96) 0%, rgba(232, 244, 255, 0.85) 100%);
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
  border: 1px solid rgba(74, 144, 226, 0.28);
  box-shadow: 0 8px 32px rgba(74, 144, 226, 0.08);
  overflow: hidden;
  transition: box-shadow 0.35s ease, border-color 0.35s ease;
}

.filter-panel:hover {
  box-shadow: var(--kc-shadow-hover);
  border-color: rgba(74, 144, 226, 0.38);
}

.filter-grid-bg {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(74, 144, 226, 0.045) 1px, transparent 1px),
    linear-gradient(90deg, rgba(74, 144, 226, 0.045) 1px, transparent 1px);
  background-size: 24px 24px;
  pointer-events: none;
}

.filter-accent {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: linear-gradient(180deg, var(--kc-primary-light) 0%, var(--kc-primary) 50%, var(--kc-primary-dark) 100%);
  pointer-events: none;
}

.filter-inner {
  position: relative;
  z-index: 1;
  padding: 20px 24px 18px 28px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding-bottom: 14px;
  border-bottom: 1px dashed rgba(74, 144, 226, 0.2);
}

.filter-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-icon-wrap {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 10px;
  background: linear-gradient(135deg, rgba(74, 144, 226, 0.12), rgba(255, 255, 255, 0.9));
  border: 1px solid rgba(74, 144, 226, 0.25);
  color: var(--kc-primary);
  box-shadow: 0 2px 8px rgba(74, 144, 226, 0.12);
}

.filter-title-text {
  font-size: 16px;
  font-weight: 700;
  color: var(--kc-primary-dark);
  letter-spacing: 0.02em;
}

.filter-count {
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, var(--kc-primary), var(--kc-primary-light));
  box-shadow: 0 2px 8px rgba(74, 144, 226, 0.25);
}

.filter-hint {
  margin: 0;
  font-size: 12px;
  color: var(--kc-text-muted);
}

.field-label {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: var(--kc-primary-dark);
  margin-bottom: 6px;
  letter-spacing: 0.03em;
}

.filter-search-row {
  padding: 14px 16px;
  border-radius: var(--kc-radius);
  background: rgba(255, 255, 255, 0.75);
  border: 1px solid rgba(74, 144, 226, 0.15);
}

.filter-search-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  min-height: 42px;
  padding-left: 14px;
  background: #fff;
  box-shadow: inset 0 1px 3px rgba(74, 144, 226, 0.06);
}

.filter-select-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
}

.price-cell { grid-column: span 2; }
.price-range { display: flex; align-items: center; gap: 8px; }
.price-input { flex: 1; min-width: 0; }
.price-sep { color: var(--kc-text-muted); flex-shrink: 0; }

.select-cell {
  padding: 12px 12px 10px;
  border-radius: var(--kc-radius);
  background: rgba(255, 255, 255, 0.65);
  border: 1px solid rgba(74, 144, 226, 0.14);
  transition: background 0.25s ease, border-color 0.25s ease, box-shadow 0.25s ease;
}

.select-cell:hover {
  background: rgba(255, 255, 255, 0.92);
  border-color: rgba(74, 144, 226, 0.3);
  box-shadow: 0 4px 14px rgba(74, 144, 226, 0.08);
}

.select-cell.is-disabled-cell {
  opacity: 0.55;
}

.filter-control {
  width: 100%;
}

.filter-panel :deep(.filter-control .el-input__wrapper),
.filter-panel :deep(.filter-control.el-select .el-select__wrapper),
.filter-panel :deep(.filter-control.el-select .el-input__wrapper) {
  border-radius: 10px;
  min-height: 36px;
  background: #fff;
  box-shadow: none;
  border: 1px solid rgba(74, 144, 226, 0.22);
  transition: border-color 0.25s ease, box-shadow 0.25s ease;
}

.filter-panel :deep(.filter-control .el-input__wrapper:hover),
.filter-panel :deep(.filter-control.el-select .el-select__wrapper:hover),
.filter-panel :deep(.filter-control.el-select .el-input__wrapper:hover) {
  border-color: rgba(74, 144, 226, 0.5);
}

.filter-panel :deep(.filter-control .el-input__wrapper.is-focus),
.filter-panel :deep(.filter-control.el-select .el-select__wrapper.is-focused),
.filter-panel :deep(.filter-control.el-select .el-input.is-focus .el-input__wrapper) {
  border-color: var(--kc-primary) !important;
  box-shadow: 0 0 0 3px rgba(74, 144, 226, 0.12) !important;
}

.filter-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding-top: 14px;
  border-top: 1px dashed rgba(74, 144, 226, 0.18);
}

.btn-filter {
  min-width: 96px;
  padding-left: 24px;
  padding-right: 24px;
}

.btn-reset {
  min-width: 88px;
}

.goods-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 22px;
}

.goods-card :deep(.el-card__body) {
  padding: 0;
}

.goods-card {
  border-radius: var(--kc-radius);
  overflow: hidden;
  border: 1px solid var(--kc-border);
}

.card-image-wrap {
  position: relative;
  cursor: pointer;
  overflow: hidden;
}

.card-image { width: 100%; height: 200px; display: block; }

.image-placeholder {
  width: 100%; height: 200px;
  display: flex; align-items: center; justify-content: center;
  background: var(--kc-bg-soft); color: var(--kc-primary);
}

.card-type-badge {
  position: absolute;
  top: 10px; left: 10px;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 11px;
  background: rgba(255,255,255,0.92);
  color: var(--kc-primary-dark);
  font-weight: 600;
}

.favorite-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 34px;
  height: 34px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.92);
  color: #999;
  font-size: 18px;
  line-height: 1;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
  transition: transform 0.2s ease, color 0.2s ease, background 0.2s ease;
}

.favorite-btn:hover {
  transform: scale(1.08);
  color: #f5a623;
}

.favorite-btn.active {
  color: #f5a623;
  background: rgba(255, 248, 230, 0.96);
}

.goods-info { padding: 14px 16px 16px; }
.goods-title { font-weight: 700; margin-bottom: 6px; cursor: pointer; color: var(--kc-text); }
.goods-title:hover { color: var(--kc-primary); }
.goods-meta { color: var(--kc-text-muted); font-size: 13px; margin-bottom: 10px; }
.goods-tags { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 12px; }
.goods-bottom { display: flex; justify-content: space-between; align-items: center; }
.goods-price { font-size: 22px; }
.card-actions { display: flex; gap: 6px; flex-wrap: wrap; justify-content: flex-end; }

.empty-state { padding: 48px 20px; }
.empty-state h3 { margin: 0 0 8px; color: var(--kc-text); }

@media (max-width: 768px) {
  .hero-card { flex-direction: column; align-items: flex-start; padding: 28px 24px; }
  .hero-content h1 { font-size: 26px; }
  .hero-actions { width: 100%; }
  .hero-actions .el-button { flex: 1; }

  .filter-inner { padding: 16px 16px 14px 22px; gap: 12px; }
  .filter-header { flex-direction: column; align-items: flex-start; gap: 4px; }
  .filter-select-grid { grid-template-columns: repeat(2, 1fr); }
  .filter-footer { justify-content: stretch; }
  .filter-footer .el-button { flex: 1; }
}

@media (max-width: 1100px) and (min-width: 769px) {
  .filter-select-grid { grid-template-columns: repeat(3, 1fr); }
}
</style>
