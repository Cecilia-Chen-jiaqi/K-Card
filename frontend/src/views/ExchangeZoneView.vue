<template>
  <div class="zone-page kc-animate-in page-content">
    <section class="kc-zone-hero kc-hero-glass kc-scan-lr">
      <div class="kc-grid-bg" aria-hidden="true" />
      <h2 class="kc-gradient-text">🔄 换卡专区 WTT</h2>
      <p>Want To Trade — 用 duplicates 换本命，友善换卡文化</p>
    </section>

    <div v-if="!loading && goods.length > 0" class="kc-section-head">
      <h3>换卡 / 预留</h3>
      <span>共 {{ goods.length }} 件</span>
    </div>

    <div v-loading="loading" class="goods-grid">
      <el-card
        v-for="(item, idx) in goods"
        :key="item.id"
        v-scroll-reveal="idx"
        class="goods-card kc-tech-card kc-card-hover"
        @click="goDetail(item.id)"
      >
        <div class="card-cover kc-img-zoom">
          <el-image :src="item.coverImage || placeholder" fit="cover" class="cover-img" />
        </div>
        <div class="card-body">
          <h3>{{ item.title }}</h3>
          <p class="meta">{{ item.groupName }} · {{ item.idolName }}</p>
          <span class="kc-tag">{{ item.tradeType }}</span>
          <div class="bottom">
            <span class="price kc-digital-price" v-if="item.tradeType !== '可交换'">¥{{ item.price }}</span>
            <span class="price exchange" v-else>可交换</span>
          </div>
        </div>
      </el-card>
    </div>

    <div v-if="!loading && goods.length === 0" class="kc-empty">
      <div class="empty-icon">🔄</div>
      <h3>暂无换卡/预留商品</h3>
      <p>发布时选择「可交换」或「支持预留」即可展示</p>
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
const placeholder = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='200' height='160'%3E%3Crect fill='%23eef6fd' width='200' height='160'/%3E%3Ctext x='50%25' y='50%25' dominant-baseline='middle' text-anchor='middle' fill='%234A90E2' font-size='14'%3E暂无图片%3C/text%3E%3C/svg%3E";

const loadGoods = async () => {
  loading.value = true;
  try {
    const res = await axios.get('/api/goods/exchange');
    goods.value = res.data.data || [];
  } finally { loading.value = false; }
};

const goDetail = (id) => router.push({ path: '/goods/detail', query: { id } });
onMounted(loadGoods);
</script>

<style scoped>
.zone-page { display: flex; flex-direction: column; gap: 24px; }

.goods-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}

.goods-card {
  cursor: pointer;
  border-radius: var(--kc-radius);
  border: 1px solid var(--kc-border);
  overflow: hidden;
}

.goods-card :deep(.el-card__body) { padding: 0; }

.card-cover { overflow: hidden; }
.cover-img { width: 100%; height: 160px; display: block; }

.card-body { padding: 14px 16px 16px; }
.goods-card h3 { margin: 0 0 4px; font-size: 15px; color: var(--kc-text); }
.meta { color: var(--kc-text-muted); font-size: 13px; margin: 0 0 8px; }
.bottom { margin-top: 10px; display: flex; align-items: center; }
.price { font-size: 18px; }
.exchange { color: var(--kc-primary-dark); font-weight: 600; font-size: 16px; }
</style>
