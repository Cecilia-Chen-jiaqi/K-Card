<template>

  <div v-loading="loading" class="admin-dashboard">

    <section class="overview-bar kc-tech-card">

      <div class="overview-left">

        <span class="section-label">OVERVIEW</span>

        <h2 class="kc-tech-title">运营数据面板</h2>

        <p>实时同步平台核心指标 · {{ username }} · 每 60s 自动刷新</p>

      </div>

      <div class="overview-kpi">

        <div class="kpi-item">

          <span class="kpi-key">TODAY USERS</span>

          <span class="kpi-val kc-digital-price">+{{ stats?.todayNewUsers ?? 0 }}</span>

        </div>

        <div class="kpi-divider" />

        <div class="kpi-item">

          <span class="kpi-key">TODAY ORDERS</span>

          <span class="kpi-val kc-digital-price">+{{ stats?.todayNewOrders ?? 0 }}</span>

        </div>

      </div>

    </section>



    <div class="stat-grid">

      <div v-for="item in statCards" :key="item.key" class="stat-card kc-tech-card">

        <div class="stat-top">

          <span class="stat-key">{{ item.key }}</span>

          <span class="stat-icon" v-html="item.svg" />

        </div>

        <div class="stat-value kc-digital-price">{{ item.value }}</div>

        <div class="stat-label">{{ item.label }}</div>

        <div v-if="item.sub" class="stat-sub">{{ item.sub }}</div>

      </div>

    </div>



    <div class="panel-card kc-tech-card trend-panel">

      <div class="panel-head">

        <div>

          <span class="section-label">TREND</span>

          <h3>近 7 日增长趋势</h3>

        </div>

        <span class="panel-badge">LIVE</span>

      </div>

      <AdminEChart v-if="stats" :option="trendOption" height="300px" />

    </div>



    <div class="panel-grid">

      <div class="panel-card kc-tech-card">

        <div class="panel-head">

          <div>

            <span class="section-label">REVENUE</span>

            <h3>近 7 日成交额</h3>

          </div>

          <span class="panel-badge">¥{{ stats?.totalGmv ?? '0.00' }} TOTAL</span>

        </div>

        <AdminEChart v-if="stats" :option="gmvOption" height="260px" />

      </div>



      <div class="panel-card kc-tech-card">

        <div class="panel-head">

          <div>

            <span class="section-label">ORDERS</span>

            <h3>订单状态分布</h3>

          </div>

          <span class="panel-badge">{{ stats?.orderCount ?? 0 }} TOTAL</span>

        </div>

        <AdminEChart v-if="stats" :option="orderPieOption" height="280px" />

      </div>



      <div class="panel-card kc-tech-card">

        <div class="panel-head">

          <div>

            <span class="section-label">GOODS</span>

            <h3>商品状态分布</h3>

          </div>

          <span class="panel-badge">{{ stats?.goodsOnSale ?? 0 }} ON SALE</span>

        </div>

        <AdminEChart v-if="stats" :option="goodsPieOption" height="280px" />

      </div>

    </div>



    <div class="bottom-row">

      <div class="metric-block kc-tech-card">

        <span class="section-label">REVENUE</span>

        <div class="metric-main">

          <span class="metric-value kc-digital-price">¥{{ stats?.totalGmv ?? '0.00' }}</span>

          <span class="metric-desc">累计成交额 · 已支付 {{ stats?.paidOrderCount ?? 0 }} 笔</span>

        </div>

      </div>

      <div class="metric-block kc-tech-card action-block">

        <span class="section-label">REVIEW</span>

        <div class="metric-main">

          <span class="metric-value kc-digital-price">{{ stats?.goodsPending ?? 0 }}</span>

          <span class="metric-desc">待审核商品 · 今日新增 {{ stats?.todayNewGoods ?? 0 }}</span>

        </div>

        <el-button type="primary" size="small" @click="$router.push('/admin/goods')">进入审核</el-button>

      </div>

    </div>

  </div>

</template>



<script setup>

import { computed, onMounted, onUnmounted, ref } from 'vue';

import axios from 'axios';

import { ElMessage } from 'element-plus';

import AdminEChart from '../../components/admin/AdminEChart.vue';

import { getCurrentUser } from '../../utils/auth';



const loading = ref(false);

const stats = ref(null);

const username = computed(() => getCurrentUser()?.username || 'admin');

let refreshTimer = null;



const CHART_BLUE = ['#3A7BC8', '#4A90E2', '#6BB3F0', '#94C5F0', '#B8D9F5', '#D4E8FA'];
const CHART_TEXT = '#666666';
const CHART_GRID = 'rgba(74, 144, 226, 0.12)';

/** 饼图统一蓝色渐变（深→浅） */
const PIE_BLUE_GRADIENTS = [
  ['#2E6AAC', '#3A7BC8'],
  ['#3A7BC8', '#4A90E2'],
  ['#4A90E2', '#6BB3F0'],
  ['#6BB3F0', '#94C5F0'],
  ['#94C5F0', '#B8D9F5'],
  ['#B8D9F5', '#D4E8FA'],
];

const pieGradient = (pair) => ({
  type: 'linear',
  x: 0,
  y: 0,
  x2: 1,
  y2: 1,
  colorStops: [
    { offset: 0, color: pair[0] },
    { offset: 1, color: pair[1] },
  ],
});

const enrichPieData = (data) =>
  data.map((item, i) => {
    const pair = PIE_BLUE_GRADIENTS[i % PIE_BLUE_GRADIENTS.length];
    return {
      ...item,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 3,
        shadowBlur: 6,
        shadowColor: 'rgba(58, 123, 200, 0.12)',
        color: pieGradient(pair),
      },
    };
  });

const buildPieOption = ({ data, centerLabel = 'TOTAL' }) => {
  const total = data.reduce((sum, d) => sum + d.value, 0);
  const enriched = enrichPieData(data);
  return {
    animationDuration: 1000,
    animationEasing: 'cubicOut',
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(255,255,255,0.98)',
      borderColor: 'rgba(74, 144, 226, 0.3)',
      borderWidth: 1,
      padding: [10, 14],
      textStyle: { color: '#333', fontSize: 12 },
      extraCssText: 'box-shadow: 0 8px 24px rgba(74,144,226,0.12); border-radius: 8px;',
      formatter: (p) => `${p.marker} ${p.name}<br/><b>${p.value}</b> · ${p.percent}%`,
    },
    legend: {
      orient: 'vertical',
      right: 4,
      top: 'middle',
      itemGap: 14,
      itemWidth: 12,
      itemHeight: 12,
      icon: 'circle',
      textStyle: {
        color: CHART_TEXT,
        fontSize: 12,
        rich: {
          name: { width: 52, color: '#444' },
          val: { color: '#3A7BC8', fontWeight: 600, fontVariantNumeric: 'tabular-nums' },
        },
      },
      formatter: (name) => {
        const item = data.find((d) => d.name === name);
        const val = item?.value ?? 0;
        const pct = total > 0 ? Math.round((val / total) * 100) : 0;
        return `{name|${name}}  {val|${val}} · ${pct}%`;
      },
    },
    title: {
      text: String(total),
      subtext: centerLabel,
      left: '34%',
      top: '42%',
      textAlign: 'center',
      textVerticalAlign: 'middle',
      textStyle: {
        fontSize: 26,
        fontWeight: 700,
        color: '#3A7BC8',
        fontFamily: 'Segoe UI, PingFang SC, sans-serif',
      },
      subtextStyle: {
        fontSize: 10,
        letterSpacing: 2,
        color: '#999',
        fontWeight: 600,
      },
    },
    series: [{
      type: 'pie',
      radius: ['46%', '72%'],
      center: ['35%', '50%'],
      padAngle: 2.5,
      minAngle: 4,
      avoidLabelOverlap: true,
      label: {
        show: total > 0,
        position: 'outside',
        formatter: '{d}%',
        fontSize: 11,
        fontWeight: 600,
        color: '#555',
      },
      labelLine: {
        show: total > 0,
        length: 12,
        length2: 10,
        smooth: 0.3,
        lineStyle: { color: 'rgba(74,144,226,0.35)', width: 1 },
      },
      emphasis: {
        scale: true,
        scaleSize: 8,
        itemStyle: {
          shadowBlur: 16,
          shadowColor: 'rgba(58, 123, 200, 0.35)',
        },
        label: { show: true, fontSize: 13, fontWeight: 700 },
      },
      data: enriched.length ? enriched : [{
        name: '暂无',
        value: 1,
        itemStyle: { color: '#EEF6FD', borderColor: '#fff', borderWidth: 3 },
        label: { show: false },
        emphasis: { disabled: true },
      }],
    }],
  };
};



const baseGrid = {

  left: 48,

  right: 24,

  top: 48,

  bottom: 32,

  containLabel: true,

};



const baseTooltip = {

  trigger: 'axis',

  backgroundColor: 'rgba(255,255,255,0.96)',

  borderColor: 'rgba(74, 144, 226, 0.25)',

  textStyle: { color: '#333', fontSize: 12 },

};



const svgUser = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6"><circle cx="12" cy="8" r="3.5"/><path d="M5 20c0-3.9 3.1-7 7-7s7 3.1 7 7"/></svg>';

const svgClock = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6"><circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/></svg>';

const svgBox = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6"><rect x="4" y="4" width="16" height="16" rx="2"/><path d="M4 9h16"/></svg>';

const svgList = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6"><path d="M8 6h13M8 12h13M8 18h13M3 6h.01M3 12h.01M3 18h.01"/></svg>';

const svgCheck = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6"><path d="M5 12l4 4L19 6"/></svg>';

const svgCoin = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6"><circle cx="12" cy="12" r="9"/><path d="M12 7v10M9 10h4a2 2 0 100 4h-2a2 2 0 110 4h4"/></svg>';



const orderStatusLabel = (s) => ({

  0: '待支付', 1: '已支付', 2: '已发货', 3: '完成', 4: '关闭', 5: '已退款',

}[s] || `状态${s}`);



const goodsStatusLabel = (s) => ({

  0: '下架', 1: '上架', 2: '待审核', 3: '已拒绝',

}[s] || `状态${s}`);



const mapToPieData = (mapObj, labelFn) => {

  if (!mapObj) return [];

  return Object.entries(mapObj)

    .map(([k, v]) => ({ name: labelFn(k), value: Number(v) || 0 }))

    .filter((d) => d.value > 0)

    .sort((a, b) => b.value - a.value);

};



const buildFallbackTrend = () => {

  const list = [];

  const today = new Date();

  for (let i = 6; i >= 0; i -= 1) {

    const d = new Date(today);

    d.setDate(d.getDate() - i);

    const mm = String(d.getMonth() + 1).padStart(2, '0');

    const dd = String(d.getDate()).padStart(2, '0');

    list.push({ date: `${mm}-${dd}`, newUsers: 0, newOrders: 0, newGoods: 0, gmv: 0 });

  }

  return list;

};



const trendData = computed(() => {

  const api = stats.value?.trend7d;

  if (Array.isArray(api) && api.length > 0) return api;

  return buildFallbackTrend();

});



const trendOption = computed(() => {

  const trend = trendData.value;

  return {

    color: CHART_BLUE,

    animationDuration: 800,

    animationEasing: 'cubicOut',

    tooltip: baseTooltip,

    legend: {

      top: 0,

      right: 0,

      textStyle: { color: CHART_TEXT, fontSize: 11 },

      itemWidth: 12,

      itemHeight: 8,

    },

    grid: baseGrid,

    xAxis: {

      type: 'category',

      data: trend.map((p) => p.date),

      boundaryGap: false,

      axisLine: { lineStyle: { color: CHART_GRID } },

      axisLabel: { color: CHART_TEXT, fontSize: 11 },

      axisTick: { show: false },

    },

    yAxis: {

      type: 'value',

      minInterval: 1,

      splitLine: { lineStyle: { color: CHART_GRID, type: 'dashed' } },

      axisLabel: { color: CHART_TEXT, fontSize: 11 },

    },

    series: [

      {

        name: '新增用户',

        type: 'line',

        smooth: true,

        symbol: 'circle',

        symbolSize: 6,

        lineStyle: { width: 2.5 },

        areaStyle: {

          color: {

            type: 'linear', x: 0, y: 0, x2: 0, y2: 1,

            colorStops: [

              { offset: 0, color: 'rgba(74, 144, 226, 0.28)' },

              { offset: 1, color: 'rgba(74, 144, 226, 0.02)' },

            ],

          },

        },

        data: trend.map((p) => Number(p.newUsers) || 0),

      },

      {

        name: '新增订单',

        type: 'line',

        smooth: true,

        symbol: 'circle',

        symbolSize: 6,

        lineStyle: { width: 2.5 },

        data: trend.map((p) => Number(p.newOrders) || 0),

      },

      {

        name: '新增商品',

        type: 'line',

        smooth: true,

        symbol: 'circle',

        symbolSize: 6,

        lineStyle: { width: 2.5 },

        data: trend.map((p) => Number(p.newGoods) || 0),

      },

    ],

  };

});



const gmvOption = computed(() => {

  const trend = trendData.value;

  return {

    color: ['#3A7BC8'],

    animationDuration: 800,

    tooltip: {

      ...baseTooltip,

      valueFormatter: (v) => `¥${Number(v).toFixed(2)}`,

    },

    grid: { ...baseGrid, top: 24 },

    xAxis: {

      type: 'category',

      data: trend.map((p) => p.date),

      axisLine: { lineStyle: { color: CHART_GRID } },

      axisLabel: { color: CHART_TEXT, fontSize: 11 },

      axisTick: { show: false },

    },

    yAxis: {

      type: 'value',

      splitLine: { lineStyle: { color: CHART_GRID, type: 'dashed' } },

      axisLabel: {

        color: CHART_TEXT,

        fontSize: 11,

        formatter: (v) => (v >= 1000 ? `${(v / 1000).toFixed(1)}k` : v),

      },

    },

    series: [{

      type: 'bar',

      barWidth: '42%',

      itemStyle: {

        borderRadius: [4, 4, 0, 0],

        color: {

          type: 'linear', x: 0, y: 0, x2: 0, y2: 1,

          colorStops: [

            { offset: 0, color: '#6BB3F0' },

            { offset: 1, color: '#3A7BC8' },

          ],

        },

      },

      data: trend.map((p) => Number(p.gmv ?? 0)),

    }],

  };

});



const orderPieOption = computed(() =>
  buildPieOption({
    data: mapToPieData(stats.value?.ordersByStatus, orderStatusLabel),
    centerLabel: 'ORDERS',
  }),
);

const goodsPieOption = computed(() =>
  buildPieOption({
    data: mapToPieData(stats.value?.goodsByStatus, goodsStatusLabel),
    centerLabel: 'GOODS',
  }),
);



const statCards = computed(() => {

  const s = stats.value || {};

  return [

    { key: 'USERS', label: '注册用户', value: s.userCount ?? 0, sub: `今日 +${s.todayNewUsers ?? 0}`, svg: svgUser },

    { key: 'PENDING', label: '待审核商品', value: s.goodsPending ?? 0, sub: `今日 +${s.todayNewGoods ?? 0}`, svg: svgClock },

    { key: 'ON SALE', label: '在售商品', value: s.goodsOnSale ?? 0, svg: svgBox },

    { key: 'ORDERS', label: '订单总数', value: s.orderCount ?? 0, sub: `今日 +${s.todayNewOrders ?? 0}`, svg: svgList },

    { key: 'PAID', label: '已支付订单', value: s.paidOrderCount ?? 0, svg: svgCheck },

    { key: 'GMV', label: '成交总额', value: `¥${s.totalGmv ?? 0}`, svg: svgCoin },

  ];

});



const loadStats = async (silent = false) => {

  if (!silent) loading.value = true;

  try {

    const res = await axios.get('/api/admin/stats/overview');

    if (res.data.code !== 0) {

      if (!silent) ElMessage.error(res.data.message || '加载统计失败');

      return;

    }

    stats.value = res.data.data;

  } catch (err) {

    if (!silent) ElMessage.error(err.response?.data?.message || '加载统计失败');

  } finally {

    if (!silent) loading.value = false;

  }

};



onMounted(() => {

  loadStats();

  refreshTimer = setInterval(() => loadStats(true), 60000);

});



onUnmounted(() => {

  if (refreshTimer) clearInterval(refreshTimer);

});

</script>



<style scoped>

.admin-dashboard {

  display: flex;

  flex-direction: column;

  gap: 18px;

}



.section-label {

  display: block;

  font-size: 10px;

  font-weight: 700;

  letter-spacing: 0.14em;

  color: var(--kc-primary);

  margin-bottom: 4px;

}



.overview-bar {

  display: flex;

  justify-content: space-between;

  align-items: center;

  gap: 24px;

  flex-wrap: wrap;

  padding: 22px 26px;

  border-radius: var(--kc-radius);

}



.overview-left h2 {

  margin: 0 0 4px;

  font-size: 20px;

  font-weight: 700;

}



.overview-left p {

  margin: 0;

  font-size: 13px;

  color: var(--kc-text-muted);

}



.overview-kpi {

  display: flex;

  align-items: center;

  gap: 20px;

}



.kpi-item { text-align: right; }

.kpi-key {

  display: block;

  font-size: 10px;

  letter-spacing: 0.1em;

  color: var(--kc-text-muted);

  margin-bottom: 4px;

}

.kpi-val { font-size: 22px; }

.kpi-divider {

  width: 1px;

  height: 36px;

  background: var(--kc-border);

}



.stat-grid {

  display: grid;

  grid-template-columns: repeat(auto-fill, minmax(168px, 1fr));

  gap: 14px;

}



.stat-card {

  padding: 16px 18px;

  border-radius: var(--kc-radius);

  transition: box-shadow 0.25s ease;

}



.stat-card:hover {

  box-shadow: var(--kc-shadow-hover);

}



.stat-top {

  display: flex;

  justify-content: space-between;

  align-items: center;

  margin-bottom: 10px;

}



.stat-key {

  font-size: 10px;

  font-weight: 700;

  letter-spacing: 0.12em;

  color: var(--kc-text-muted);

}



.stat-icon {

  width: 18px;

  height: 18px;

  color: var(--kc-primary);

  opacity: 0.65;

}



.stat-value {

  font-size: 26px;

  line-height: 1.1;

  margin-bottom: 4px;

}



.stat-label {

  font-size: 13px;

  color: var(--kc-text);

  font-weight: 500;

}



.stat-sub {

  margin-top: 6px;

  font-size: 11px;

  color: var(--kc-text-muted);

  font-variant-numeric: tabular-nums;

}



.trend-panel {

  padding: 20px 22px;

  border-radius: var(--kc-radius);

}



.panel-grid {

  display: grid;

  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));

  gap: 14px;

}



.panel-card {

  padding: 20px 22px;

  border-radius: var(--kc-radius);

}



.panel-head {

  display: flex;

  justify-content: space-between;

  align-items: flex-start;

  margin-bottom: 8px;

}



.panel-head h3 {

  margin: 0;

  font-size: 15px;

  font-weight: 600;

  color: var(--kc-text);

}



.panel-badge {

  font-size: 10px;

  font-weight: 700;

  letter-spacing: 0.08em;

  color: var(--kc-primary-dark);

  padding: 4px 10px;

  border-radius: 4px;

  border: 1px solid var(--kc-border);

  background: var(--kc-bg-soft);

}



.bottom-row {

  display: grid;

  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));

  gap: 14px;

}



.metric-block {

  padding: 20px 22px;

  border-radius: var(--kc-radius);

}



.action-block {

  display: flex;

  flex-direction: column;

  gap: 12px;

}



.metric-main { display: flex; flex-direction: column; gap: 6px; }



.metric-value {

  font-size: 30px;

  line-height: 1.1;

}



.metric-desc {

  font-size: 12px;

  color: var(--kc-text-muted);

}



.action-block .el-button { align-self: flex-start; }

</style>


