<template>
  <div class="checkout-page kc-animate-in page-content">
    <div class="kc-page-header checkout-hero">
      <div>
        <h2 class="kc-gradient-text kc-tech-title">💳 订单结算</h2>
        <p>确认订单信息后完成支付</p>
      </div>
    </div>

    <el-skeleton v-if="loading" :rows="6" animated />

    <template v-else-if="orderDetail">
      <el-card class="checkout-card kc-surface">
        <div v-if="orderLines.length > 1" class="multi-goods-list">
          <div v-for="(line, idx) in orderLines" :key="line.item?.id || idx" class="goods-preview multi-item">
            <el-image :src="line.goods?.coverImage || placeholder" fit="cover" class="preview-img" />
            <div class="multi-item-info">
              <h3>{{ line.goods?.title }}</h3>
              <p>{{ line.goods?.groupName }} · {{ line.goods?.idolName }}</p>
              <p class="item-qty">× {{ line.item?.quantity || 1 }} · <span class="kc-digital-price">¥{{ line.item?.amount }}</span></p>
              <el-tag size="small" :type="itemStatusTag(line.item?.status)">{{ itemStatusLabel(line.item?.status) }}</el-tag>
              <div v-if="orderDetail.order.status >= 1" class="line-item-actions">
                <el-button
                  v-if="canRefundItem(line)"
                  type="warning"
                  size="small"
                  plain
                  @click="requestRefund(line.item?.id, line.goods?.title)"
                >申请退款</el-button>
                <el-button
                  v-if="canConfirmItem(line)"
                  type="success"
                  size="small"
                  @click="confirmReceipt(line.item?.id)"
                >确认收货</el-button>
              </div>
            </div>
          </div>
        </div>
        <div class="goods-preview" v-else-if="orderLines[0]?.goods">
          <el-image :src="orderLines[0].goods.coverImage || placeholder" fit="cover" class="preview-img" />
          <div>
            <h3>{{ orderLines[0].goods.title }}</h3>
            <p>{{ orderLines[0].goods.groupName }} · {{ orderLines[0].goods.idolName }}</p>
            <p v-if="orderLines[0].goods.cardType" class="kc-tag">{{ orderLines[0].goods.cardType }}</p>
          </div>
        </div>

        <el-divider />

        <div class="order-lines">
          <div class="order-line"><span>订单号</span><strong>{{ orderDetail.order.orderNo }}</strong></div>
          <div class="order-line"><span>商品数</span><strong>{{ orderLines.length }} 件 / 共 {{ totalQuantity }} 张</strong></div>
          <div class="order-line"><span>状态</span><el-tag :type="statusTag">{{ statusLabel }}</el-tag></div>
          <div class="order-line total"><span>应付金额</span><strong class="price kc-digital-price">¥{{ orderDetail.order.amount }}</strong></div>
        </div>

        <div v-if="needMailAddress && orderDetail.order.status === 0" class="address-section">
          <div class="address-section-head">
            <h4>收货地址</h4>
            <el-button link type="primary" @click="router.push('/user?tab=address')">管理地址</el-button>
          </div>
          <div v-if="addresses.length" class="checkout-address-list">
            <div
              v-for="item in addresses"
              :key="item.id"
              class="checkout-address-item"
              :class="{ active: selectedAddressId === item.id }"
              @click="selectAddress(item.id)"
            >
              <div class="checkout-address-top">
                <strong>{{ item.receiver }}</strong>
                <span>{{ item.phone }}</span>
                <el-tag v-if="item.isDefault === 1" size="small" type="success">默认</el-tag>
              </div>
              <p>{{ formatAddress(item) }}</p>
            </div>
          </div>
          <el-alert v-else type="warning" show-icon :closable="false" title="请先添加收货地址后再支付" />
        </div>

        <div v-else-if="orderDetail.address && needMailAddress" class="address-section readonly">
          <h4>收货地址</h4>
          <p>{{ orderDetail.address.receiver }} {{ orderDetail.address.phone }}</p>
          <p class="addr-readonly">{{ formatAddress(orderDetail.address) }}</p>
        </div>

        <div v-else-if="isCampusMeetup" class="address-section readonly">
          <h4>交付方式</h4>
          <p class="addr-readonly">校园同城面交，无需填写邮寄地址</p>
        </div>

        <div v-if="orderDetail.order.status >= 2" class="logistics-section">
          <h4>物流信息</h4>
          <div v-if="shippedLines.length" class="logistics-items">
            <div v-for="(line, idx) in shippedLines" :key="line.item?.id || idx" class="logistics-detail kc-tech-card">
              <div class="logistics-row"><span>商品</span><strong>{{ line.goods?.title }}</strong></div>
              <div class="logistics-row"><span>快递公司</span><strong>{{ line.item?.expressCompany || '—' }}</strong></div>
              <div v-if="line.item?.trackingNo" class="logistics-row">
                <span>快递单号</span>
                <strong class="tracking-no">{{ line.item.trackingNo }}</strong>
              </div>
              <div v-if="line.item?.shippedAt" class="logistics-row">
                <span>发货时间</span><strong>{{ formatShipTime(line.item.shippedAt) }}</strong>
              </div>
            </div>
          </div>
          <div v-else class="logistics-timeline">
            <div class="logistics-step done">
              <div class="step-dot" />
              <div class="step-body">
                <strong>卖家已发货</strong>
                <p v-if="orderDetail.order.shippedAt">{{ formatShipTime(orderDetail.order.shippedAt) }}</p>
              </div>
            </div>
            <div class="logistics-detail kc-tech-card">
              <div class="logistics-row">
                <span>快递公司</span>
                <strong>{{ orderDetail.order.expressCompany || '—' }}</strong>
              </div>
              <div v-if="orderDetail.order.trackingNo" class="logistics-row">
                <span>快递单号</span>
                <strong class="tracking-no">{{ orderDetail.order.trackingNo }}</strong>
                <el-button link type="primary" size="small" @click="copyTracking">复制</el-button>
              </div>
            </div>
          </div>
          <div class="logistics-step" :class="{ done: orderDetail.order.status >= 3 }">
            <div class="step-dot" />
            <div class="step-body">
              <strong>{{ orderDetail.order.status >= 3 ? '交易完成' : '等待买家确认收货' }}</strong>
            </div>
          </div>
          <div v-if="orderDetail.order.status === 2" class="buyer-actions">
            <el-button v-if="orderLines.length <= 1" type="success" round @click="confirmReceipt()">确认收货</el-button>
            <el-button v-else-if="hasConfirmableItems" type="success" round @click="confirmReceipt()">全部确认收货</el-button>
          </div>
          <div v-if="orderDetail.order.status === 1 && hasRefundableItems" class="buyer-actions">
            <el-button type="warning" plain round @click="requestRefund()">整单退款</el-button>
          </div>
        </div>

        <el-divider v-if="orderDetail.order.status === 0" />

        <div class="pay-actions" v-if="orderDetail.order.status === 0">
          <el-tabs v-model="payMode" class="pay-tabs">
            <el-tab-pane label="手机扫码（推荐）" name="qr">
              <div class="qr-panel">
                <div v-if="qrLoading" class="qr-loading">正在生成支付二维码…</div>
                <template v-else-if="qrCode">
                  <img :src="qrImageUrl" alt="支付二维码" class="qr-image" />
                  <p class="qr-amount">应付 ¥{{ orderDetail.order.amount }}</p>
                  <p class="qr-tip">请用 <strong>支付宝沙箱版 App</strong> 扫上方二维码</p>
                  <p class="qr-tip sub">扫前请先在沙箱 App 登录买家账号。支付后手机若白屏，直接关掉即可，电脑端会自动同步。</p>
                  <p v-if="polling" class="qr-tip polling">正在检测支付结果…</p>
                </template>
                <el-button v-else link type="primary" @click="loadQrCode">重新生成二维码</el-button>
              </div>
            </el-tab-pane>
            <el-tab-pane label="电脑网页支付" name="web">
              <p class="tab-desc">跳转支付宝收银台，使用沙箱买家账号密码登录付款（无需扫码）。</p>
              <el-button type="primary" size="large" round :loading="paying" @click="pay">
                {{ paying ? '正在跳转…' : '跳转收银台' }}
              </el-button>
            </el-tab-pane>
          </el-tabs>
          <el-button size="large" round :loading="syncing" @click="() => syncPayStatus(false)" class="sync-btn">
            我已支付，同步状态
          </el-button>
        </div>
        <div class="pay-done" v-else-if="orderDetail.order.status === 1">
          <el-result icon="success" title="支付成功" sub-title="等待卖家发货">
            <template #extra>
              <el-button v-if="orderLines.length <= 1 && hasRefundableItems" type="warning" plain @click="requestRefund()">申请退款</el-button>
              <el-button @click="$router.push('/user?tab=buyer')">查看订单</el-button>
            </template>
          </el-result>
        </div>
        <div class="pay-tip" v-else>{{ statusLabel }} — 请在订单中心查看详情</div>
      </el-card>
    </template>

    <el-empty v-else description="订单不存在" />
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onBeforeUnmount } from 'vue';
import axios from 'axios';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { formatShipTime } from '../constants/expressMeta';

const route = useRoute();
const router = useRouter();
const orderDetail = ref(null);
const addresses = ref([]);
const selectedAddressId = ref(null);
const loading = ref(true);
const paying = ref(false);
const syncing = ref(false);
const payMode = ref('qr');
const qrCode = ref('');
const qrLoading = ref(false);
const polling = ref(false);
let pollTimer = null;

const qrImageUrl = computed(() => {
  if (!qrCode.value) return '';
  return `https://api.qrserver.com/v1/create-qr-code/?size=260x260&data=${encodeURIComponent(qrCode.value)}`;
});
const placeholder = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='120' height='120'%3E%3Crect fill='%23e8f2ff' width='120' height='120'/%3E%3C/svg%3E";

const statusLabel = computed(() => {
  const s = orderDetail.value?.order?.status;
  return ({ 0: '待支付', 1: '已支付待发货', 2: '已发货', 3: '交易完成', 4: '已关闭', 5: '已退款' }[s] || '未知');
});

const statusTag = computed(() => {
  const s = orderDetail.value?.order?.status;
  return ({ 0: 'warning', 1: 'success', 2: 'primary', 3: 'success', 4: 'info', 5: 'danger' }[s] || 'info');
});

const orderLines = computed(() => {
  if (orderDetail.value?.items?.length) {
    return orderDetail.value.items;
  }
  if (orderDetail.value?.goods) {
    return [{
      item: {
        quantity: orderDetail.value.order?.quantity,
        amount: orderDetail.value.order?.amount,
        expressCompany: orderDetail.value.order?.expressCompany,
        trackingNo: orderDetail.value.order?.trackingNo,
        shippedAt: orderDetail.value.order?.shippedAt,
        status: orderDetail.value.order?.status,
      },
      goods: orderDetail.value.goods,
    }];
  }
  return [];
});

const totalQuantity = computed(() => orderLines.value.reduce((sum, line) => sum + (line.item?.quantity || 0), 0));

const shippedLines = computed(() => orderLines.value.filter((line) => line.item?.status >= 2));

const itemStatusLabel = (s) => ({ 0: '待支付', 1: '待发货', 2: '已发货', 3: '已完成', 5: '已退款' }[s] || '—');
const itemStatusTag = (s) => ({ 0: 'info', 1: 'warning', 2: 'primary', 3: 'success', 5: 'danger' }[s] || 'info');
const canRefundItem = (line) => line.item?.status === 1 || line.item?.status === 2;
const canConfirmItem = (line) => line.item?.status === 2;
const hasRefundableItems = computed(() => orderLines.value.some((l) => canRefundItem(l)));
const hasConfirmableItems = computed(() => orderLines.value.some((l) => canConfirmItem(l)));

const isCampusMeetup = computed(() => {
  if (!orderLines.value.length) return false;
  return orderLines.value.every((line) => line.goods?.deliveryMode === 2);
});
const needMailAddress = computed(() => !isCampusMeetup.value);

const formatAddress = (addr) => {
  if (!addr) return '';
  return [addr.province, addr.city, addr.district, addr.detail].filter(Boolean).join('');
};

const loadAddresses = async () => {
  try {
    const res = await axios.get('/api/address/list');
    addresses.value = res.data.data || [];
    const boundId = orderDetail.value?.order?.addressId;
    if (boundId) {
      selectedAddressId.value = boundId;
      return;
    }
    const def = addresses.value.find((a) => a.isDefault === 1) || addresses.value[0];
    if (def) {
      await selectAddress(def.id);
    }
  } catch {
    addresses.value = [];
  }
};

const selectAddress = async (addressId) => {
  if (!orderDetail.value?.order?.orderNo || selectedAddressId.value === addressId) {
    selectedAddressId.value = addressId;
    return;
  }
  selectedAddressId.value = addressId;
  try {
    const res = await axios.post('/api/orders/bind-address', {
      orderNo: orderDetail.value.order.orderNo,
      addressId,
    });
    if (res.data.code !== 0) {
      ElMessage.error(res.data.message || '地址绑定失败');
    }
  } catch {
    ElMessage.error('地址绑定失败');
  }
};

const ensureAddressReady = () => {
  if (!needMailAddress.value) return true;
  if (selectedAddressId.value) return true;
  ElMessage.warning('请先选择收货地址');
  return false;
};

const loadOrder = async () => {
  loading.value = true;
  try {
    const res = await axios.get(`/api/orders/${route.params.orderNo}`);
    orderDetail.value = res.data.data;
    if (orderDetail.value?.order?.status !== 0) {
      stopPolling();
    }
  } catch {
    orderDetail.value = null;
    ElMessage.error('加载订单失败');
  } finally {
    loading.value = false;
  }
};

const loadQrCode = async () => {
  if (!orderDetail.value?.order?.orderNo || qrLoading.value) return;
  if (!ensureAddressReady()) return;
  qrLoading.value = true;
  try {
    const res = await axios.get('/api/pay/qrcode', {
      params: { orderNo: orderDetail.value.order.orderNo },
    });
    if (res.data.code !== 0) {
      ElMessage.error(res.data.message || '二维码生成失败');
      qrCode.value = '';
      return;
    }
    qrCode.value = res.data.data?.qrCode || '';
  } catch {
    ElMessage.error('二维码生成失败');
    qrCode.value = '';
  } finally {
    qrLoading.value = false;
  }
};

const pay = async () => {
  if (paying.value) return;
  if (!ensureAddressReady()) return;
  paying.value = true;
  try {
    const res = await axios.post('/api/pay/submit', { orderNo: orderDetail.value.order.orderNo });
    if (res.data.code !== 0) {
      ElMessage.error(res.data.message || '支付发起失败');
      paying.value = false;
      return;
    }
    const payUrl = (res.data.data || '').trim();
    if (payUrl.startsWith('http://') || payUrl.startsWith('https://')) {
      window.location.href = payUrl;
      return;
    }
    ElMessage.error('支付链接生成异常，请稍后重试');
    paying.value = false;
  } catch {
    ElMessage.error('支付请求失败');
    paying.value = false;
  }
};

const syncPayStatus = async (silent = false) => {
  if (syncing.value || !orderDetail.value?.order?.orderNo) return false;
  syncing.value = true;
  try {
    const res = await axios.get('/api/pay/sync-status', {
      params: { orderNo: orderDetail.value.order.orderNo },
    });
    if (res.data.code === 0 && res.data.data?.paid) {
      if (!silent) ElMessage.success('支付已确认');
      stopPolling();
      await loadOrder();
      return true;
    }
    if (!silent) {
      ElMessage.warning('尚未检测到支付成功，请确认已在沙箱完成付款后再试');
    }
    return false;
  } catch {
    if (!silent) ElMessage.error('同步失败，请稍后重试');
    return false;
  } finally {
    syncing.value = false;
  }
};

const startPolling = () => {
  stopPolling();
  if (orderDetail.value?.order?.status !== 0) return;
  polling.value = true;
  pollTimer = setInterval(() => {
    syncPayStatus(true);
  }, 3000);
};

const stopPolling = () => {
  polling.value = false;
  if (pollTimer) {
    clearInterval(pollTimer);
    pollTimer = null;
  }
};

const copyTracking = async () => {
  const no = orderDetail.value?.order?.trackingNo;
  if (!no) return;
  try {
    await navigator.clipboard.writeText(no);
    ElMessage.success('单号已复制');
  } catch {
    ElMessage.warning('复制失败，请手动复制');
  }
};

const confirmReceipt = async (orderItemId = null) => {
  const orderNo = orderDetail.value?.order?.orderNo;
  if (!orderNo) return;
  const payload = orderItemId ? { orderNo, orderItemIds: [orderItemId] } : { orderNo };
  try {
    const res = await axios.post('/api/orders/complete', payload);
    if (res.data.code !== 0) {
      ElMessage.error(res.data.message || '确认收货失败');
      return;
    }
    ElMessage.success('已确认收货');
    await loadOrder();
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '确认收货失败');
  }
};

const requestRefund = async (orderItemId = null, itemTitle = '') => {
  const orderNo = orderDetail.value?.order?.orderNo;
  if (!orderNo) return;
  const tip = orderItemId
    ? `确认对「${itemTitle || '该商品'}」申请退款？`
    : '确认申请整单退款？';
  try {
    await ElMessageBox.confirm(tip, '退款确认');
    const payload = orderItemId
      ? { orderNo, orderItemIds: [orderItemId], reason: '买家申请退款' }
      : { orderNo, reason: '买家申请退款' };
    const res = await axios.post('/api/pay/refund', payload);
    if (res.data.code !== 0) {
      ElMessage.error(res.data.message || '退款失败');
      return;
    }
    ElMessage.success('退款成功');
    await loadOrder();
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error(err.response?.data?.message || '退款失败');
    }
  }
};

onMounted(async () => {
  await loadOrder();
  if (orderDetail.value?.order?.status === 0) {
    if (needMailAddress.value) {
      await loadAddresses();
    }
    loadQrCode();
    startPolling();
  }
});

onBeforeUnmount(stopPolling);
</script>

<style scoped>
.checkout-page { max-width: 720px; }
.checkout-hero { align-items: center; }
.checkout-card { padding: 12px 16px 20px; border: none; }

.goods-preview {
  display: flex;
  gap: 16px;
  align-items: center;
}

.preview-img { width: 100px; height: 100px; border-radius: 16px; flex-shrink: 0; }
.goods-preview h3 { margin: 0 0 6px; }
.goods-preview p { margin: 0; color: var(--kc-text-muted); font-size: 14px; }

.order-lines { display: flex; flex-direction: column; gap: 12px; }
.order-line { display: flex; justify-content: space-between; align-items: center; }
.order-line span { color: var(--kc-text-muted); }
.order-line.total .price { font-size: 28px; }

.address-section { margin-top: 20px; text-align: left; }
.address-section-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.address-section h4 { margin: 0; font-size: 16px; color: var(--kc-text); }
.checkout-address-list { display: flex; flex-direction: column; gap: 10px; }
.checkout-address-item {
  padding: 12px 14px;
  border: 1.5px solid var(--kc-border);
  border-radius: 12px;
  cursor: pointer;
  transition: border-color 0.2s, background 0.2s;
}
.checkout-address-item.active {
  border-color: var(--kc-primary);
  background: rgba(74, 144, 226, 0.06);
}
.checkout-address-top { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; margin-bottom: 6px; }
.checkout-address-item p { margin: 0; color: var(--kc-text-muted); font-size: 13px; line-height: 1.5; }
.addr-readonly { color: var(--kc-text-muted); font-size: 14px; line-height: 1.6; }

.pay-actions { margin-top: 24px; text-align: center; display: flex; flex-direction: column; gap: 16px; align-items: center; }
.pay-tabs { width: 100%; max-width: 420px; }
.tab-desc { font-size: 13px; color: var(--kc-text-muted); margin: 0 0 16px; line-height: 1.6; }
.qr-panel { display: flex; flex-direction: column; align-items: center; gap: 10px; padding: 8px 0 4px; }
.qr-image { width: 260px; height: 260px; border-radius: 12px; border: 1px solid var(--kc-border); background: #fff; }
.qr-amount { font-size: 22px; font-weight: 700; color: var(--kc-primary-dark); margin: 4px 0 0; }
.qr-tip { margin: 0; font-size: 14px; color: var(--kc-text); }
.qr-tip.sub { font-size: 13px; color: var(--kc-text-muted); }
.qr-loading { padding: 40px 0; color: var(--kc-text-muted); }
.polling { color: var(--kc-primary); font-weight: 600; }
.sync-btn { margin-top: 4px; }

.logistics-section { margin-top: 20px; text-align: left; }
.logistics-section h4 { margin: 0 0 14px; font-size: 16px; }
.logistics-timeline { display: flex; flex-direction: column; gap: 12px; }
.logistics-step { display: flex; gap: 12px; align-items: flex-start; opacity: 0.45; }
.logistics-step.done { opacity: 1; }
.step-dot {
  width: 10px; height: 10px; border-radius: 50%;
  background: var(--kc-primary); margin-top: 5px; flex-shrink: 0;
}
.logistics-step:not(.done) .step-dot { background: var(--kc-border); }
.step-body strong { display: block; font-size: 14px; }
.step-body p { margin: 4px 0 0; font-size: 13px; color: var(--kc-text-muted); }
.logistics-detail { padding: 14px 16px; display: flex; flex-direction: column; gap: 10px; margin-left: 22px; }
.logistics-row { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; font-size: 14px; }
.logistics-row span { color: var(--kc-text-muted); min-width: 72px; }
.tracking-no { font-family: ui-monospace, monospace; letter-spacing: 0.02em; }
.buyer-actions { margin-top: 16px; text-align: center; }
.multi-goods-list { display: flex; flex-direction: column; gap: 12px; }
.multi-item { padding-bottom: 8px; border-bottom: 1px dashed var(--kc-border); }
.multi-item:last-child { border-bottom: none; }
.multi-item-info h3 { margin: 0 0 6px; font-size: 16px; }
.line-item-actions { display: flex; gap: 8px; margin-top: 10px; flex-wrap: wrap; }
.item-qty { margin: 4px 0 0; color: var(--kc-text-muted); font-size: 13px; }
.logistics-items { display: flex; flex-direction: column; gap: 10px; margin-bottom: 12px; }
</style>
