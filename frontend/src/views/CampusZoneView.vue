<template>
  <div class="zone-page kc-animate-in page-content">
    <section class="zone-hero kc-hero-glass kc-scan-lr">
      <div class="kc-grid-bg" aria-hidden="true" />
      <div class="hero-accent" aria-hidden="true" />

      <div class="hero-main">
        <div class="hero-icon-wrap">
          <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="1.8">
            <path d="M3 21h18M5 21V7l7-4 7 4v14" stroke-linecap="round" stroke-linejoin="round" />
            <path d="M9 21v-6h6v6M12 7v4" stroke-linecap="round" />
          </svg>
        </div>
        <div class="hero-text">
          <span class="zone-badge">🏫 校园专属</span>
          <h2 class="kc-gradient-text kc-tech-title">校园同城面交</h2>
          <p class="hero-desc">同校粉丝线下换卡、面交，省邮费更安心</p>
          <div class="hero-tags">
            <span class="kc-tag">省邮费</span>
            <span class="kc-tag">同校可信</span>
            <span class="kc-tag">线下验卡</span>
          </div>
        </div>
      </div>

      <div class="hero-side">
        <div class="hero-stat">
          <span class="stat-num">{{ goods.length }}</span>
          <span class="stat-label">面交在售</span>
        </div>
        <el-button type="primary" round class="kc-btn-ripple kc-btn-glow" @click="goPublish">
          发布面交小卡
        </el-button>
      </div>
    </section>

    <div v-if="!loading && goods.length > 0" class="section-bar">
      <div class="kc-section-head section-head-inline">
        <h3>面交小卡</h3>
        <span>共 {{ goods.length }} 件</span>
      </div>
      <p class="section-tip">支持同城线下交易，交易前请确认见面地点</p>
    </div>

    <div v-loading="loading" class="goods-grid">
      <el-card
        v-for="(item, idx) in goods"
        :key="item.id"
        v-scroll-reveal="idx"
        class="goods-card kc-tech-card kc-card-hover"
      >
        <div class="card-image-wrap kc-img-zoom" @click="goDetail(item.id)">
          <el-image :src="coverUrl(item)" fit="cover" class="card-image">
            <template #error>
              <div class="image-placeholder">
                <span class="ph-icon">🃏</span>
                <span>暂无图片</span>
              </div>
            </template>
          </el-image>
          <span class="campus-badge">面交</span>
          <span v-if="item.cardType" class="card-type-badge">{{ item.cardType }}</span>
        </div>
        <div class="goods-info">
          <div class="goods-title" @click="goDetail(item.id)">{{ item.title }}</div>
          <div class="goods-meta">{{ item.groupName }} · {{ item.idolName }}</div>
          <div class="goods-tags">
            <span v-if="item.quality" class="kc-tag">{{ item.quality }}</span>
            <span v-if="item.albumEra" class="kc-tag">{{ item.albumEra }}</span>
          </div>
          <div class="goods-bottom">
            <div class="goods-price kc-digital-price">¥{{ item.price }}</div>
            <el-button type="primary" size="small" round @click="goDetail(item.id)">查看</el-button>
          </div>
        </div>
      </el-card>
    </div>

    <div v-if="!loading && goods.length === 0" class="kc-empty zone-empty">
      <div class="empty-icon">🏫</div>
      <h3>暂无校园面交小卡</h3>
      <p>发布时开启「校园面交」，同校买家就能在这里找到你</p>
      <el-button type="primary" round class="kc-btn-ripple" @click="goPublish">去发布</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';

const router = useRouter();
const goods = ref([]);
const loading = ref(false);

const placeholderImage = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='300' height='200'%3E%3Crect width='300' height='200' fill='%23eef6fd'/%3E%3Ctext x='50%25' y='50%25' dominant-baseline='middle' text-anchor='middle' fill='%234A90E2' font-size='16'%3E暂无图片%3C/text%3E%3C/svg%3E";

const coverUrl = (item) => item.coverImage || item.cover_image || placeholderImage;

const loadGoods = async () => {
  loading.value = true;
  try {
    const res = await axios.get('/api/goods/campus');
    goods.value = res.data.data || [];
  } finally {
    loading.value = false;
  }
};

const goDetail = (id) => router.push({ path: '/goods/detail', query: { id } });
const goPublish = () => router.push('/publish');

onMounted(loadGoods);
</script>

<style scoped>
.zone-page {
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.zone-hero {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 32px 36px;
  border-radius: var(--kc-radius-lg);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.96) 0%, rgba(232, 244, 255, 0.88) 100%);
  border: 1px solid rgba(74, 144, 226, 0.25);
  box-shadow: 0 8px 32px rgba(74, 144, 226, 0.08);
  overflow: hidden;
}

.hero-accent {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: linear-gradient(180deg, var(--kc-primary-light), var(--kc-primary-dark));
  pointer-events: none;
}

.hero-main {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: flex-start;
  gap: 20px;
  flex: 1;
}

.hero-icon-wrap {
  flex-shrink: 0;
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(74, 144, 226, 0.15), rgba(255, 255, 255, 0.95));
  border: 1px solid rgba(74, 144, 226, 0.25);
  color: var(--kc-primary);
  box-shadow: 0 4px 16px rgba(74, 144, 226, 0.12);
}

.zone-badge {
  display: inline-block;
  padding: 3px 12px;
  margin-bottom: 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  color: var(--kc-primary-dark);
  background: rgba(74, 144, 226, 0.1);
  border: 1px solid var(--kc-border);
}

.hero-text h2 {
  margin: 0 0 8px;
  font-size: 30px;
  font-weight: 800;
}

.hero-desc {
  margin: 0 0 14px;
  color: var(--kc-text-muted);
  font-size: 14px;
  line-height: 1.6;
}

.hero-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.hero-side {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 14px;
  flex-shrink: 0;
}

.hero-stat {
  text-align: right;
  padding: 10px 18px;
  border-radius: var(--kc-radius);
  background: rgba(255, 255, 255, 0.75);
  border: 1px solid var(--kc-border);
}

.stat-num {
  display: block;
  font-size: 28px;
  font-weight: 800;
  color: var(--kc-primary-dark);
  line-height: 1.1;
}

.stat-label {
  font-size: 12px;
  color: var(--kc-text-muted);
}

.section-bar {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.section-head-inline {
  margin-bottom: 0;
}

.section-tip {
  margin: 0;
  font-size: 12px;
  color: var(--kc-text-muted);
}

.goods-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 22px;
}

.goods-card {
  border-radius: var(--kc-radius);
  overflow: hidden;
  border: 1px solid var(--kc-border);
}

.goods-card :deep(.el-card__body) {
  padding: 0;
}

.card-image-wrap {
  position: relative;
  cursor: pointer;
  overflow: hidden;
}

.card-image {
  width: 100%;
  height: 200px;
  display: block;
}

.image-placeholder {
  width: 100%;
  height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background: linear-gradient(160deg, #eef6fd 0%, #dceefb 100%);
  color: var(--kc-primary);
  font-size: 13px;
}

.ph-icon {
  font-size: 32px;
  opacity: 0.7;
}

.campus-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, var(--kc-primary), var(--kc-primary-light));
  box-shadow: 0 2px 10px rgba(74, 144, 226, 0.35);
}

.card-type-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 11px;
  background: rgba(255, 255, 255, 0.92);
  color: var(--kc-primary-dark);
  font-weight: 600;
  border: 1px solid var(--kc-border);
}

.goods-info {
  padding: 14px 16px 16px;
}

.goods-title {
  font-weight: 700;
  margin-bottom: 6px;
  cursor: pointer;
  color: var(--kc-text);
  line-height: 1.4;
}

.goods-title:hover {
  color: var(--kc-primary);
}

.goods-meta {
  color: var(--kc-text-muted);
  font-size: 13px;
  margin-bottom: 10px;
}

.goods-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
}

.goods-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.goods-price {
  font-size: 22px;
}

.zone-empty p {
  margin: 0 0 16px;
}

@media (max-width: 768px) {
  .zone-hero {
    flex-direction: column;
    align-items: stretch;
    padding: 24px;
  }

  .hero-side {
    align-items: stretch;
  }

  .hero-stat {
    text-align: center;
  }

  .hero-text h2 {
    font-size: 24px;
  }

  .section-bar {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
}
</style>
