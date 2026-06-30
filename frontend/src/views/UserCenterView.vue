<template>
  <div class="user-center-page kc-animate-in page-content">
    <div class="profile-banner kc-scan-lr">
      <div class="profile-grid" aria-hidden="true" />
      <div class="profile-top">
        <div class="avatar-box">
          <img v-if="user?.avatar" :src="user.avatar" alt="头像" class="user-avatar" />
          <div v-else class="avatar-fallback">{{ user?.username?.charAt(0)?.toUpperCase() || 'U' }}</div>
        </div>
        <div class="profile-info">
          <h2>{{ user?.username || '用户' }}</h2>
          <p>🏫 {{ user?.campus || '未填写校园' }} · 📱 {{ user?.phone || '未填写手机' }}</p>
        </div>
      </div>
      <div class="profile-actions">
        <el-button round class="btn-edit kc-btn-ripple" @click="openProfileDialog">编辑资料</el-button>
        <el-button type="primary" round class="btn-publish kc-btn-ripple kc-btn-glow" @click="goPublish">发布小卡</el-button>
        <el-button round class="btn-cart kc-btn-ripple" @click="goCart">购物车</el-button>
      </div>
    </div>

    <el-card v-loading="tabLoading" class="order-card kc-surface kc-tech-card kc-scan-lr">
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <el-tab-pane label="买家订单" name="buyer" />
        <el-tab-pane label="卖家订单" name="seller" />
        <el-tab-pane label="我的发布" name="goods" />
        <el-tab-pane label="我的收藏" name="favorites" />
        <el-tab-pane label="关注卖家" name="follows" />
        <el-tab-pane label="收货地址" name="address" />
      </el-tabs>

      <div v-if="activeTab === 'buyer'">
        <el-table :data="buyerOrders" stripe class="order-table">
          <el-table-column type="expand" width="48">
            <template #default="{ row }">
              <div v-if="displayLines(row).length > 1" class="order-expand">
                <div v-for="line in displayLines(row)" :key="line.item?.id || line.goods?.id" class="expand-line">
                  <div class="expand-line-info">
                    <strong>{{ line.goods?.title }}</strong>
                    <span class="small-text">× {{ line.item?.quantity || 1 }} · ¥{{ line.item?.amount }}</span>
                    <el-tag size="small" :type="itemStatusTag(line.item?.status)">{{ itemStatusLabel(line.item?.status) }}</el-tag>
                  </div>
                  <div class="expand-line-actions">
                    <el-button
                      v-if="canRefundItem(line)"
                      type="warning"
                      size="small"
                      plain
                      @click="requestRefund(row.order.orderNo, line.item?.id, line.goods?.title)"
                    >退款</el-button>
                    <el-button
                      v-if="canConfirmItem(line)"
                      type="success"
                      size="small"
                      @click="confirmReceipt(row.order.orderNo, line.item?.id)"
                    >确认收货</el-button>
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="order.orderNo" label="订单号" width="170" />
          <el-table-column label="商品">
            <template #default="{ row }">
              <div v-if="displayLines(row).length > 1">
                <div>{{ displayLines(row)[0].goods?.title }}</div>
                <div class="small-text">等 {{ displayLines(row).length }} 件商品</div>
              </div>
              <template v-else>
                <div>{{ row.goods?.title || displayLines(row)[0]?.goods?.title }}</div>
                <div class="small-text">{{ (row.goods || displayLines(row)[0]?.goods)?.groupName }} / {{ (row.goods || displayLines(row)[0]?.goods)?.idolName }}</div>
              </template>
            </template>
          </el-table-column>
          <el-table-column label="金额" width="90">
            <template #default="{ row }"><span class="kc-digital-price">¥{{ row.order.amount }}</span></template>
          </el-table-column>
          <el-table-column label="状态" width="120">
            <template #default="{ row }">
              <el-tag size="small" :type="statusTag(row.order.status)">{{ statusLabel(row.order.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="物流" min-width="160">
            <template #default="{ row }">
              <div v-if="row.order.status >= 2" class="logistics-cell">
                <template v-if="shippedLineCount(row) > 0 && displayLines(row).length > 1">
                  <div>已发货 {{ shippedLineCount(row) }}/{{ displayLines(row).length }} 件</div>
                </template>
                <template v-else>
                  <div>{{ row.order.expressCompany || displayLines(row)[0]?.item?.expressCompany || '—' }}</div>
                  <div v-if="row.order.trackingNo || displayLines(row)[0]?.item?.trackingNo" class="small-text">
                    单号 {{ row.order.trackingNo || displayLines(row)[0]?.item?.trackingNo }}
                  </div>
                </template>
              </div>
              <span v-else class="small-text">—</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="240">
            <template #default="{ row }">
              <el-button v-if="row.order.status === 0" type="primary" size="small" round @click="goPay(row.order.orderNo)">去支付</el-button>
              <el-button v-if="row.order.status === 0" type="danger" size="small" plain @click="cancelOrder(row.order.orderNo)">取消</el-button>
              <template v-if="displayLines(row).length <= 1">
                <el-button v-if="row.order.status === 1" type="warning" size="small" plain @click="requestRefund(row.order.orderNo)">申请退款</el-button>
                <el-button v-if="row.order.status === 2" type="success" size="small" @click="confirmReceipt(row.order.orderNo)">确认收货</el-button>
              </template>
              <template v-else>
                <el-button v-if="row.order.status === 1 && hasRefundableItems(row)" type="warning" size="small" plain @click="requestRefund(row.order.orderNo)">整单退款</el-button>
                <el-button v-if="row.order.status === 2 && hasConfirmableItems(row)" type="success" size="small" @click="confirmReceipt(row.order.orderNo)">全部确认</el-button>
              </template>
              <el-button type="text" size="small" @click="viewOrder(row.order.orderNo)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div v-if="buyerOrders.length === 0" class="kc-empty"><p>暂无买家订单</p></div>
      </div>

      <div v-else-if="activeTab === 'seller'">
        <el-table :data="sellerOrders" stripe class="order-table">
          <el-table-column prop="order.orderNo" label="订单号" width="170" />
          <el-table-column label="商品">
            <template #default="{ row }">
              <div v-if="displayLines(row).length > 1">{{ displayLines(row)[0].goods?.title }} 等{{ displayLines(row).length }}件</div>
              <div v-else>{{ row.goods?.title || displayLines(row)[0]?.goods?.title }}</div>
            </template>
          </el-table-column>
          <el-table-column label="金额" width="90">
            <template #default="{ row }"><span class="kc-digital-price">¥{{ row.order.amount }}</span></template>
          </el-table-column>
          <el-table-column label="状态" width="120">
            <template #default="{ row }">{{ statusLabel(row.order.status) }}</template>
          </el-table-column>
          <el-table-column label="物流" min-width="140">
            <template #default="{ row }">
              <div v-if="row.order.status >= 2" class="logistics-cell">
                <template v-if="shippedLineCount(row) > 1">
                  <div>已发货 {{ shippedLineCount(row) }}/{{ displayLines(row).length }} 件</div>
                </template>
                <template v-else>
                  <div>{{ row.order.expressCompany || displayLines(row)[0]?.item?.expressCompany }}</div>
                  <div v-if="row.order.trackingNo || displayLines(row)[0]?.item?.trackingNo" class="small-text">
                    {{ row.order.trackingNo || displayLines(row)[0]?.item?.trackingNo }}
                  </div>
                </template>
              </div>
              <span v-else class="small-text">待发货</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160">
            <template #default="{ row }">
              <el-button v-if="row.order.status === 1 && canSellerShip(row)" type="primary" size="small" @click="openShipDialog(row)">发货</el-button>
              <el-button type="text" size="small" @click="viewOrder(row.order.orderNo)">详情</el-button>
            </template>
          </el-table-column>
          <el-table-column label="收货信息" min-width="180">
            <template #default="{ row }">
              <div v-if="row.address" class="addr-cell">
                <div>{{ row.address.receiver }} {{ row.address.phone }}</div>
                <div class="small-text">{{ formatAddress(row.address) }}</div>
              </div>
              <span v-else-if="row.goods?.deliveryMode === 2" class="small-text">校园面交</span>
              <span v-else class="small-text">未填写</span>
            </template>
          </el-table-column>
        </el-table>
        <div v-if="sellerOrders.length === 0" class="kc-empty"><p>暂无卖家订单</p></div>
      </div>

      <div v-else-if="activeTab === 'goods'">
        <div class="my-goods-grid">
          <el-card v-for="item in myGoods" :key="item.id" class="my-goods-card kc-tech-card kc-card-hover">
            <el-image :src="item.coverImage || placeholder" fit="cover" style="width:100%;height:140px;border-radius:12px" />
            <h4>{{ item.title }}</h4>
            <p class="small-text">{{ item.groupName }} · {{ item.idolName }}</p>
            <div class="goods-row">
              <span class="price kc-digital-price">¥{{ item.price }}</span>
              <el-tag size="small" :type="myGoodsStatusTag(item.status)">{{ myGoodsStatusLabel(item.status) }}</el-tag>
            </div>
            <p v-if="item.status === 3 && item.rejectReason" class="small-text reject-reason">拒绝原因：{{ item.rejectReason }}</p>
            <div class="goods-actions">
              <el-button v-if="item.status === 1" size="small" plain @click="delistGoods(item.id)">下架</el-button>
              <el-button v-else-if="item.status !== 2 && item.stock > 0" size="small" type="primary" plain @click="relistGoods(item.id)">重新提交</el-button>
              <el-button size="small" link type="primary" @click="viewGoods(item.id)">查看</el-button>
            </div>
          </el-card>
        </div>
        <div v-if="myGoods.length === 0" class="kc-empty">
          <p>还没有发布过小卡</p>
          <el-button type="primary" round @click="goPublish">去发布</el-button>
        </div>
      </div>

      <div v-else-if="activeTab === 'favorites'">
        <div v-if="favorites.length" class="my-goods-grid">
          <el-card v-for="item in favorites" :key="item.favorite?.id || item.goods?.id" class="my-goods-card kc-tech-card kc-card-hover">
            <el-image :src="item.goods?.coverImage || placeholder" fit="cover" style="width:100%;height:140px;border-radius:12px" />
            <h4>{{ item.goods?.title }}</h4>
            <p class="small-text">{{ item.goods?.groupName }} · {{ item.goods?.idolName }}</p>
            <div class="goods-row">
              <span class="price kc-digital-price">¥{{ item.goods?.price }}</span>
              <el-tag size="small" :type="item.goods?.status === 1 ? 'success' : 'info'">{{ item.goods?.status === 1 ? '上架' : '已下架' }}</el-tag>
            </div>
            <div class="goods-actions">
              <el-button v-if="item.goods?.status === 1" size="small" type="primary" plain @click="viewGoods(item.goods.id)">查看</el-button>
              <el-button size="small" plain @click="removeFavorite(item.goods?.id)">取消收藏</el-button>
            </div>
          </el-card>
        </div>
        <div v-else class="kc-empty">
          <p>还没有收藏的小卡</p>
          <el-button type="primary" round @click="router.push('/')">去逛逛</el-button>
        </div>
      </div>

      <div v-else-if="activeTab === 'follows'">
        <div v-if="follows.length" class="follow-list">
          <div v-for="item in follows" :key="item.follow?.id || item.seller?.id" class="follow-card kc-tech-card">
            <el-avatar :size="48" :src="item.seller?.avatar || ''">{{ item.seller?.username?.charAt(0)?.toUpperCase() }}</el-avatar>
            <div class="follow-info">
              <strong>{{ item.seller?.nickname || item.seller?.username }}</strong>
              <p class="small-text">{{ item.seller?.campus || '未填写校园' }}</p>
              <p v-if="item.seller?.intro" class="small-text follow-intro">{{ item.seller.intro }}</p>
            </div>
            <el-button size="small" plain type="danger" @click="removeFollow(item.seller?.id)">取消关注</el-button>
          </div>
        </div>
        <div v-else class="kc-empty">
          <p>还没有关注的卖家</p>
          <el-button type="primary" round @click="router.push('/')">去逛逛</el-button>
        </div>
      </div>

      <div v-else-if="activeTab === 'address'">
        <div class="address-toolbar">
          <p class="address-tip">管理你的邮寄收货地址，结算时可快速选用</p>
          <el-button type="primary" round @click="openAddressDialog()">新增地址</el-button>
        </div>
        <div v-if="addresses.length" class="address-list">
          <div
            v-for="item in addresses"
            :key="item.id"
            class="address-card kc-tech-card"
            :class="{ 'is-default': item.isDefault === 1 }"
          >
            <div class="address-card-head">
              <div>
                <strong>{{ item.receiver }}</strong>
                <span class="addr-phone">{{ item.phone }}</span>
                <el-tag v-if="item.isDefault === 1" size="small" type="success" class="default-tag">默认</el-tag>
              </div>
              <div class="address-actions">
                <el-button link type="primary" @click="openAddressDialog(item)">编辑</el-button>
                <el-button v-if="item.isDefault !== 1" link type="primary" @click="setDefaultAddress(item.id)">设为默认</el-button>
                <el-button link type="danger" @click="removeAddress(item.id)">删除</el-button>
              </div>
            </div>
            <p class="address-full">{{ formatAddress(item) }}</p>
          </div>
        </div>
        <div v-else class="kc-empty">
          <p>还没有收货地址</p>
          <el-button type="primary" round @click="openAddressDialog()">添加第一个地址</el-button>
        </div>
      </div>
    </el-card>

    <el-dialog v-model="addressDialogVisible" :title="addressForm.id ? '编辑地址' : '新增地址'" width="520px" destroy-on-close>
      <el-form :model="addressForm" label-width="88px" class="address-form">
        <el-form-item label="收货人" required>
          <el-input v-model="addressForm.receiver" placeholder="姓名" maxlength="64" />
        </el-form-item>
        <el-form-item label="手机号" required>
          <el-input v-model="addressForm.phone" placeholder="11 位手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="所在地区">
          <div class="region-row">
            <el-input v-model="addressForm.province" placeholder="省" />
            <el-input v-model="addressForm.city" placeholder="市" />
            <el-input v-model="addressForm.district" placeholder="区/县" />
          </div>
        </el-form-item>
        <el-form-item label="详细地址" required>
          <el-input v-model="addressForm.detail" type="textarea" :rows="2" placeholder="街道、门牌号等" maxlength="255" />
        </el-form-item>
        <el-form-item label="默认地址">
          <el-switch v-model="addressForm.isDefault" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addressDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="addressSaving" @click="saveAddress">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="shipDialogVisible" title="填写物流信息" width="480px" destroy-on-close @closed="resetShipForm">
      <el-form :model="shipForm" label-width="88px" class="ship-form">
        <el-alert
          v-if="sellerNeedsCampusOnly(shipTarget)"
          type="info"
          :closable="false"
          show-icon
          title="本单含校园面交商品，填写约定地点或备注即可"
          class="ship-alert"
        />
        <el-form-item v-if="!sellerNeedsCampusOnly(shipTarget)" label="快递公司" required>
          <el-select v-model="shipForm.expressCompany" placeholder="选择快递公司" filterable allow-create style="width:100%">
            <el-option v-for="c in expressCompanies" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!sellerNeedsCampusOnly(shipTarget)" label="快递单号" required>
          <el-input v-model="shipForm.trackingNo" placeholder="请输入快递单号" maxlength="64" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="shipForm.logisticsNote"
            type="textarea"
            :rows="2"
            :placeholder="sellerNeedsCampusOnly(shipTarget) ? '如：图书馆门口、周三晚7点' : '选填，如：已包装好、请勿折叠'"
            maxlength="255"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="shipSaving" @click="submitShip">确认发货</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="profileDialogVisible" title="编辑资料" width="480px" destroy-on-close>
      <el-form :model="profileForm" label-width="72px">
        <el-form-item label="头像">
          <div class="profile-avatar-row">
            <el-avatar :size="56" :src="profileForm.avatar || ''">{{ user?.username?.charAt(0)?.toUpperCase() }}</el-avatar>
            <el-upload
              action="/api/upload/image"
              :show-file-list="false"
              :headers="uploadHeaders"
              name="file"
              :on-success="handleAvatarUpload"
            >
              <el-button size="small">上传头像</el-button>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="profileForm.nickname" placeholder="展示昵称" maxlength="32" />
        </el-form-item>
        <el-form-item label="校园">
          <el-input v-model="profileForm.campus" placeholder="所在校园" maxlength="64" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="profileForm.intro" type="textarea" :rows="3" placeholder="介绍一下自己" maxlength="255" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="profileDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="profileSaving" @click="saveProfile">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue';
import axios from 'axios';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { loadExpressCompanies, formatShipTime } from '../constants/expressMeta';

const router = useRouter();
const route = useRoute();
const user = ref(null);
const buyerOrders = ref([]);
const sellerOrders = ref([]);
const myGoods = ref([]);
const favorites = ref([]);
const follows = ref([]);
const addresses = ref([]);
const activeTab = ref(route.query.tab || 'buyer');
const addressDialogVisible = ref(false);
const addressSaving = ref(false);
const emptyAddressForm = () => ({
  id: null,
  receiver: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: 0,
});
const addressForm = ref(emptyAddressForm());
const expressCompanies = ref([]);
const shipDialogVisible = ref(false);
const shipSaving = ref(false);
const shipTarget = ref(null);
const shipForm = ref({ expressCompany: '', trackingNo: '', logisticsNote: '' });
const tabLoading = ref(false);
const profileDialogVisible = ref(false);
const profileSaving = ref(false);
const profileForm = ref({ nickname: '', campus: '', intro: '', avatar: '' });
const uploadHeaders = { Authorization: `Bearer ${localStorage.getItem('authToken') || ''}` };
const placeholder = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='200' height='140'%3E%3Crect fill='%23e8f2ff' width='200' height='140'/%3E%3C/svg%3E";

const loadUser = async () => {
  try {
    const res = await axios.get('/api/auth/me');
    if (res.data.code === 0 && res.data.data) {
      user.value = res.data.data;
      localStorage.setItem('currentUser', JSON.stringify(res.data.data));
      return;
    }
  } catch { /* fallback */ }
  try { user.value = JSON.parse(localStorage.getItem('currentUser') || 'null'); } catch { user.value = null; }
};

const loadOrders = async () => {
  tabLoading.value = true;
  try {
    if (activeTab.value === 'seller') {
      const res = await axios.get('/api/orders/seller/list');
      sellerOrders.value = res.data.data || [];
    } else if (activeTab.value === 'buyer') {
      const res = await axios.get('/api/orders/buyer/list');
      buyerOrders.value = res.data.data || [];
    }
  } catch {
    ElMessage.error('加载订单失败');
  } finally {
    tabLoading.value = false;
  }
};

const loadMyGoods = async () => {
  tabLoading.value = true;
  try {
    const res = await axios.get('/api/goods/my');
    myGoods.value = res.data.data || [];
  } catch {
    ElMessage.error('加载商品失败');
  } finally {
    tabLoading.value = false;
  }
};

const loadFavorites = async () => {
  tabLoading.value = true;
  try {
    const res = await axios.get('/api/favorite/list');
    favorites.value = res.data.data || [];
  } catch {
    ElMessage.error('加载收藏失败');
  } finally {
    tabLoading.value = false;
  }
};

const loadFollows = async () => {
  tabLoading.value = true;
  try {
    const res = await axios.get('/api/follow/list');
    follows.value = res.data.data || [];
  } catch {
    ElMessage.error('加载关注失败');
  } finally {
    tabLoading.value = false;
  }
};

const loadAddresses = async () => {
  tabLoading.value = true;
  try {
    const res = await axios.get('/api/address/list');
    addresses.value = res.data.data || [];
  } catch {
    ElMessage.error('加载地址失败');
  } finally {
    tabLoading.value = false;
  }
};

const formatAddress = (addr) => {
  if (!addr) return '';
  return [addr.province, addr.city, addr.district, addr.detail].filter(Boolean).join('');
};

const displayLines = (row) => {
  if (row?.sellerItems?.length) return row.sellerItems;
  if (row?.items?.length) return row.items;
  return row?.goods ? [{ goods: row.goods, item: row.order }] : [];
};

const shippedLineCount = (row) => displayLines(row).filter((l) => l.item?.status >= 2).length;

const itemStatusLabel = (s) => ({ 0: '待支付', 1: '待发货', 2: '已发货', 3: '已完成', 5: '已退款' }[s] || '—');
const itemStatusTag = (s) => ({ 0: 'info', 1: 'warning', 2: 'primary', 3: 'success', 5: 'danger' }[s] || 'info');
const canRefundItem = (line) => line.item?.status === 1 || line.item?.status === 2;
const canConfirmItem = (line) => line.item?.status === 2;
const hasRefundableItems = (row) => displayLines(row).some((l) => canRefundItem(l));
const hasConfirmableItems = (row) => displayLines(row).some((l) => canConfirmItem(l));

const sellerNeedsCampusOnly = (row) => {
  const lines = displayLines(row);
  return lines.length > 0 && lines.every((l) => l.goods?.deliveryMode === 2);
};

const canSellerShip = (row) => {
  const lines = displayLines(row);
  if (!lines.length) return true;
  return lines.some((l) => !l.item?.status || l.item.status < 2);
};

const openAddressDialog = (item = null) => {
  addressForm.value = item
    ? { ...item, isDefault: item.isDefault || 0 }
    : emptyAddressForm();
  addressDialogVisible.value = true;
};

const saveAddress = async () => {
  addressSaving.value = true;
  try {
    const payload = { ...addressForm.value };
    const res = payload.id
      ? await axios.post('/api/address/update', payload)
      : await axios.post('/api/address/create', payload);
    if (res.data.code !== 0) {
      ElMessage.error(res.data.message || '保存失败');
      return;
    }
    ElMessage.success('地址已保存');
    addressDialogVisible.value = false;
    loadAddresses();
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '保存失败');
  } finally {
    addressSaving.value = false;
  }
};

const setDefaultAddress = async (id) => {
  const res = await axios.post(`/api/address/set-default/${id}`);
  if (res.data.code === 0) {
    ElMessage.success('已设为默认地址');
    loadAddresses();
  } else {
    ElMessage.error(res.data.message || '操作失败');
  }
};

const removeAddress = async (id) => {
  await ElMessageBox.confirm('确定删除该收货地址？', '提示', { type: 'warning' });
  const res = await axios.delete(`/api/address/${id}`);
  if (res.data.code === 0) {
    ElMessage.success('已删除');
    loadAddresses();
  } else {
    ElMessage.error(res.data.message || '删除失败');
  }
};

const onTabChange = (tab) => {
  router.replace({ query: { tab } });
  if (tab === 'goods') loadMyGoods();
  else if (tab === 'favorites') loadFavorites();
  else if (tab === 'follows') loadFollows();
  else if (tab === 'address') loadAddresses();
  else loadOrders();
};

const statusLabel = (s) => ({ 0: '待支付', 1: '已支付', 2: '已发货', 3: '完成', 4: '关闭', 5: '已退款' }[s] || '未知');
const statusTag = (s) => ({ 0: 'warning', 1: 'success', 2: 'primary', 3: 'success', 4: 'info', 5: 'danger' }[s] || 'info');

const goPay = (orderNo) => router.push(`/checkout/${orderNo}`);
const viewOrder = (orderNo) => router.push(`/checkout/${orderNo}`);
const viewGoods = (id) => router.push({ path: '/goods/detail', query: { id } });
const goPublish = () => router.push('/publish');
const goCart = () => router.push('/cart');

const myGoodsStatusLabel = (s) => ({ 0: '已下架', 1: '上架中', 2: '待审核', 3: '已拒绝' }[s] || '未知');
const myGoodsStatusTag = (s) => ({ 0: 'info', 1: 'success', 2: 'warning', 3: 'danger' }[s] || 'info');

const openProfileDialog = () => {
  profileForm.value = {
    nickname: user.value?.nickname || '',
    campus: user.value?.campus || '',
    intro: user.value?.intro || '',
    avatar: user.value?.avatar || '',
  };
  profileDialogVisible.value = true;
};

const handleAvatarUpload = (response) => {
  if (response.code === 0) {
    profileForm.value.avatar = response.data;
    ElMessage.success('头像上传成功');
  } else {
    ElMessage.error(response.message || '上传失败');
  }
};

const saveProfile = async () => {
  profileSaving.value = true;
  try {
    const res = await axios.post('/api/auth/profile', profileForm.value);
    if (res.data.code !== 0) {
      ElMessage.error(res.data.message || '保存失败');
      return;
    }
    user.value = res.data.data;
    localStorage.setItem('currentUser', JSON.stringify(res.data.data));
    window.dispatchEvent(new Event('user-updated'));
    ElMessage.success('资料已更新');
    profileDialogVisible.value = false;
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '保存失败');
  } finally {
    profileSaving.value = false;
  }
};

const delistGoods = async (id) => {
  await ElMessageBox.confirm('确定下架该小卡？', '提示', { type: 'warning' });
  try {
    const res = await axios.post(`/api/goods/delist/${id}`);
    if (res.data.code !== 0) {
      ElMessage.error(res.data.message || '下架失败');
      return;
    }
    ElMessage.success('已下架');
    loadMyGoods();
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '下架失败');
  }
};

const relistGoods = async (id) => {
  try {
    const res = await axios.post(`/api/goods/relist/${id}`);
    if (res.data.code !== 0) {
      ElMessage.error(res.data.message || '上架失败');
      return;
    }
    ElMessage.success('已重新提交审核');
    loadMyGoods();
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '上架失败');
  }
};

const resetShipForm = () => {
  shipTarget.value = null;
  shipForm.value = { expressCompany: '', trackingNo: '', logisticsNote: '' };
};

const openShipDialog = (row) => {
  shipTarget.value = row;
  const campus = sellerNeedsCampusOnly(row);
  shipForm.value = {
    expressCompany: campus ? '校园面交' : '',
    trackingNo: '',
    logisticsNote: '',
  };
  shipDialogVisible.value = true;
};

const submitShip = async () => {
  if (!shipTarget.value?.order?.orderNo) return;
  const isCampus = sellerNeedsCampusOnly(shipTarget.value);
  const payload = {
    orderNo: shipTarget.value.order.orderNo,
    expressCompany: isCampus ? '校园面交' : shipForm.value.expressCompany?.trim(),
    trackingNo: isCampus ? '' : shipForm.value.trackingNo?.trim(),
    logisticsNote: shipForm.value.logisticsNote?.trim() || '',
  };
  if (!isCampus) {
    if (!payload.expressCompany) {
      ElMessage.warning('请选择快递公司');
      return;
    }
    if (!payload.trackingNo || payload.trackingNo.length < 6) {
      ElMessage.warning('请填写正确的快递单号');
      return;
    }
  }
  shipSaving.value = true;
  try {
    const res = await axios.post('/api/orders/ship', payload);
    if (res.data.code !== 0) {
      ElMessage.error(res.data.message || '发货失败');
      return;
    }
    ElMessage.success('已确认发货');
    shipDialogVisible.value = false;
    loadOrders();
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '发货失败');
  } finally {
    shipSaving.value = false;
  }
};

const confirmReceipt = async (orderNo, orderItemId = null) => {
  const payload = orderItemId ? { orderNo, orderItemIds: [orderItemId] } : { orderNo };
  try {
    const res = await axios.post('/api/orders/complete', payload);
    if (res.data.code !== 0) {
      ElMessage.error(res.data.message || '确认收货失败');
      return;
    }
    ElMessage.success('已确认收货');
    loadOrders();
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '确认收货失败');
  }
};

const cancelOrder = async (orderNo) => {
  await axios.post('/api/orders/cancel', { orderNo });
  ElMessage.success('订单已取消');
  loadOrders();
};

const requestRefund = async (orderNo, orderItemId = null, itemTitle = '') => {
  const tip = orderItemId
    ? `确认对「${itemTitle || '该商品'}」申请退款？退款将原路返回支付账户`
    : '确认申请退款？退款将原路返回支付账户';
  await ElMessageBox.confirm(tip, '退款确认');
  const payload = orderItemId
    ? { orderNo, orderItemIds: [orderItemId], reason: '买家申请退款' }
    : { orderNo, reason: '买家申请退款' };
  try {
    const res = await axios.post('/api/pay/refund', payload);
    if (res.data.code === 0) ElMessage.success('退款成功');
    else ElMessage.error(res.data.message || '退款失败');
    loadOrders();
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '退款失败');
  }
};

const removeFavorite = async (goodsId) => {
  if (!goodsId) return;
  try {
    const res = await axios.delete(`/api/favorite/remove/${goodsId}`);
    if (res.data.code !== 0) {
      ElMessage.error(res.data.message || '取消收藏失败');
      return;
    }
    ElMessage.success('已取消收藏');
    loadFavorites();
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '取消收藏失败');
  }
};

const removeFollow = async (sellerId) => {
  if (!sellerId) return;
  try {
    const res = await axios.delete(`/api/follow/remove/${sellerId}`);
    if (res.data.code !== 0) {
      ElMessage.error(res.data.message || '取消关注失败');
      return;
    }
    ElMessage.success('已取消关注');
    loadFollows();
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '取消关注失败');
  }
};

onMounted(async () => {
  expressCompanies.value = await loadExpressCompanies(axios);
  await loadUser();
  if (activeTab.value === 'goods') loadMyGoods();
  else if (activeTab.value === 'favorites') loadFavorites();
  else if (activeTab.value === 'follows') loadFollows();
  else if (activeTab.value === 'address') loadAddresses();
  else loadOrders();
});

watch(() => route.query.tab, (t) => {
  if (t && t !== activeTab.value) {
    activeTab.value = t;
    if (t === 'goods') loadMyGoods();
    else if (t === 'favorites') loadFavorites();
    else if (t === 'follows') loadFollows();
    else if (t === 'address') loadAddresses();
    else loadOrders();
  }
});
</script>

<style scoped>
.user-center-page {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.profile-banner {
  position: relative;
  overflow: hidden;
  padding: 32px 36px;
  border-radius: var(--kc-radius-lg);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9) 0%, rgba(232, 244, 255, 0.78) 100%);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(74, 144, 226, 0.22);
  box-shadow: 0 8px 32px rgba(74, 144, 226, 0.08);
  transition: transform 0.35s ease, box-shadow 0.35s ease;
}

.profile-banner:hover {
  transform: translateY(-4px);
  box-shadow: 0 16px 40px rgba(74, 144, 226, 0.12);
}

.profile-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(74, 144, 226, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(74, 144, 226, 0.05) 1px, transparent 1px);
  background-size: 28px 28px;
  pointer-events: none;
  opacity: 0.55;
  mask-image: linear-gradient(180deg, rgba(0, 0, 0, 0.5) 0%, transparent 100%);
}

.profile-top {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 24px;
}

.avatar-box {
  width: 88px;
  height: 88px;
  border-radius: 22px;
  overflow: hidden;
  background: var(--kc-bg-soft);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid rgba(74, 144, 226, 0.35);
  box-shadow: 0 0 0 4px rgba(74, 144, 226, 0.1), 0 0 24px rgba(74, 144, 226, 0.2);
  flex-shrink: 0;
}

.user-avatar { width: 100%; height: 100%; object-fit: cover; }

.avatar-fallback {
  font-size: 32px;
  font-weight: 800;
  color: var(--kc-primary-dark);
}

.profile-info h2 {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 700;
  color: #333;
}

.profile-info p {
  margin: 0;
  font-size: 14px;
  color: #666;
}

.profile-actions {
  position: relative;
  z-index: 1;
  margin-top: 22px;
  display: flex;
  gap: 12px;
}

.btn-publish {
  background: linear-gradient(135deg, #4A90E2, #6BB3F0) !important;
  border: none !important;
  box-shadow: 0 4px 16px rgba(74, 144, 226, 0.32);
}

.btn-cart {
  background: #fff !important;
  border: 1.5px solid rgba(74, 144, 226, 0.45) !important;
  color: var(--kc-primary) !important;
}

.btn-cart:hover {
  border-color: var(--kc-primary-dark) !important;
  background: rgba(74, 144, 226, 0.06) !important;
  color: var(--kc-primary-dark) !important;
}

.order-card {
  border: 1px solid var(--kc-border);
  box-shadow: var(--kc-shadow);
  border-radius: var(--kc-radius-lg);
  padding: 12px 16px 20px;
}

.order-table { margin-top: 16px; }
.order-table :deep(.el-table__expanded-cell) {
  padding: 12px 24px;
  background: rgba(74, 144, 226, 0.04);
}
.order-expand { display: flex; flex-direction: column; gap: 10px; }
.expand-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
  border-bottom: 1px dashed rgba(74, 144, 226, 0.15);
}
.expand-line:last-child { border-bottom: none; }
.expand-line-info { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.expand-line-actions { display: flex; gap: 8px; flex-shrink: 0; }
.small-text { color: #666; font-size: 12px; }

.my-goods-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
  margin-top: 16px;
}

.my-goods-card {
  border-radius: var(--kc-radius);
  border: 1px solid var(--kc-border);
}

.my-goods-card h4 { margin: 10px 0 4px; font-size: 14px; color: #333; }
.goods-row { display: flex; justify-content: space-between; align-items: center; }
.price { font-size: 16px; }

.address-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-top: 8px;
  flex-wrap: wrap;
}

.address-tip { margin: 0; color: var(--kc-text-muted); font-size: 14px; }

.address-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-top: 18px;
}

.address-card {
  padding: 16px 18px;
  border: 1px solid var(--kc-border);
  border-radius: var(--kc-radius);
  transition: border-color 0.2s, box-shadow 0.2s;
}

.address-card.is-default {
  border-color: rgba(74, 144, 226, 0.45);
  box-shadow: 0 4px 16px rgba(74, 144, 226, 0.1);
}

.address-card-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  flex-wrap: wrap;
}

.addr-phone { margin-left: 12px; color: var(--kc-text-muted); font-weight: 400; }
.default-tag { margin-left: 8px; }
.address-actions { display: flex; gap: 4px; flex-wrap: wrap; }
.address-full { margin: 10px 0 0; color: #444; line-height: 1.6; font-size: 14px; }
.addr-cell .small-text { margin-top: 4px; }
.logistics-cell { line-height: 1.45; }
.ship-alert { margin-bottom: 16px; }
.ship-form { padding-top: 4px; }
.goods-actions { display: flex; gap: 8px; margin-top: 10px; flex-wrap: wrap; }
.reject-reason { color: #e6a23c; margin: 6px 0 0; }

.follow-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-top: 16px;
}

.follow-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 18px;
  border: 1px solid var(--kc-border);
  border-radius: var(--kc-radius);
}

.follow-info { flex: 1; min-width: 0; }
.follow-info strong { display: block; margin-bottom: 4px; }
.follow-intro { margin-top: 4px; color: var(--kc-text-muted); }

.profile-avatar-row { display: flex; align-items: center; gap: 16px; }
.order-card { overflow-x: auto; }

.region-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  width: 100%;
}

@media (max-width: 640px) {
  .region-row { grid-template-columns: 1fr; }
}
</style>
