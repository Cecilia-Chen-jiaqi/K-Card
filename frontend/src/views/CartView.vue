<template>
  <div class="page-content cart-page kc-animate-in">
    <div class="kc-page-header">
      <div>
        <h2 class="kc-gradient-text">🛒 我的购物车</h2>
        <p>勾选商品后点击「去结算」，支持多件合并下单</p>
      </div>
      <div class="header-stat">
        <div>总计 {{ totalQuantity }} 件</div>
        <div class="amount kc-digital-price">¥{{ totalAmount }}</div>
      </div>
    </div>

    <div v-if="cartList.length > 0" class="kc-surface cart-table-wrap kc-scan-lr">
      <el-table
        ref="tableRef"
        :data="cartList"
        border
        stripe
        class="cart-table"
        empty-text=""
        @selection-change="onSelectionChange"
      >
        <el-table-column type="selection" width="48" />
        <el-table-column label="商品" min-width="240">
          <template #default="{ row }">
            <div class="cart-goods">
              <el-image
                :src="row.goods.coverImage || placeholderImage"
                fit="cover"
                class="goods-thumb"
              >
                <template #error>
                  <div class="empty-img">暂无图片</div>
                </template>
              </el-image>
              <div>
                <div class="goods-title">{{ row.goods.title }}</div>
                <div class="goods-meta">{{ row.goods.groupName }} / {{ row.goods.idolName }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="数量" width="130" align="center">
          <template #default="{ row }">
            <el-input-number
              :model-value="row.cart.quantity"
              :min="1"
              :max="row.goods.stock || 99"
              size="small"
              controls-position="right"
              @change="(val) => updateQuantity(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="单价" width="100" align="center">
          <template #default="{ row }"><span class="kc-digital-price">¥{{ row.goods.price }}</span></template>
        </el-table-column>
        <el-table-column label="小计" width="110" align="center">
          <template #default="{ row }"><span class="kc-digital-price">¥{{ lineTotal(row) }}</span></template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="danger" size="small" link @click="remove(row.cart.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div v-if="cartList.length === 0" class="kc-empty">
      <div class="empty-icon">🛍️</div>
      <h3>购物车空空如也</h3>
      <p>快去首页挑选你的心仪小卡吧</p>
      <el-button type="primary" round @click="router.push('/')">去逛逛</el-button>
    </div>

    <div v-else class="kc-sticky-footer">
      <div class="footer-left">
        <el-checkbox v-model="selectAll" @change="toggleSelectAll">全选</el-checkbox>
        <span class="footer-meta">已选 {{ selectedCount }} 件</span>
        <span class="footer-meta">合计 <strong class="footer-price kc-digital-price">¥{{ selectedAmount }}</strong></span>
      </div>
      <el-button type="primary" size="large" round class="kc-btn-ripple kc-btn-glow" :disabled="selectedCount === 0" @click="checkoutSelected">
        去结算 →
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, nextTick } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';

const router = useRouter();
const tableRef = ref(null);
const cartList = ref([]);
const selectedRows = ref([]);
const selectAll = ref(false);
const placeholderImage = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='120' height='120'%3E%3Crect width='120' height='120' fill='%23eef6fd'/%3E%3Ctext x='50%25' y='50%25' dominant-baseline='middle' text-anchor='middle' fill='%234A90E2' font-size='14'%3E暂无图片%3C/text%3E%3C/svg%3E";

const lineTotal = (row) => ((row.goods.price || 0) * (row.cart.quantity || 0)).toFixed(2);

const totalQuantity = computed(() => cartList.value.reduce((sum, item) => sum + (item.cart.quantity || 0), 0));
const totalAmount = computed(() => cartList.value.reduce((sum, item) => sum + Number(lineTotal(item)), 0).toFixed(2));
const selectedCount = computed(() => selectedRows.value.reduce((sum, item) => sum + (item.cart.quantity || 0), 0));
const selectedAmount = computed(() => selectedRows.value.reduce((sum, item) => sum + Number(lineTotal(item)), 0).toFixed(2));

const syncSelectAll = () => {
  selectAll.value = cartList.value.length > 0 && selectedRows.value.length === cartList.value.length;
};

const onSelectionChange = (rows) => {
  selectedRows.value = rows;
  syncSelectAll();
};

const toggleSelectAll = (checked) => {
  if (!tableRef.value) return;
  if (checked) {
    cartList.value.forEach((row) => tableRef.value.toggleRowSelection(row, true));
  } else {
    tableRef.value.clearSelection();
  }
};

const selectAllRows = async () => {
  await nextTick();
  toggleSelectAll(true);
};

const loadCart = async () => {
  try {
    const res = await axios.get('/api/cart/list');
    cartList.value = res.data.data || [];
    await selectAllRows();
  } catch (error) {
    cartList.value = [];
    console.error('加载购物车失败', error);
  }
};

const checkout = async (cartId) => {
  const res = await axios.post(`/api/cart/checkout/${cartId}`);
  if (res.data.code !== 0) {
    throw new Error(res.data.message || '结算失败');
  }
  const orderNo = res.data.data?.orderNo;
  if (!orderNo) {
    throw new Error('订单创建失败');
  }
  router.push(`/checkout/${orderNo}`);
};

const checkoutSelected = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先勾选要结算的商品');
    return;
  }
  try {
    const cartIds = selectedRows.value.map((row) => row.cart.id);
    const res = await axios.post('/api/cart/checkout-batch', { cartIds });
    if (res.data.code !== 0) {
      throw new Error(res.data.message || '结算失败');
    }
    const orderNo = res.data.data?.orderNo;
    if (!orderNo) {
      throw new Error('订单创建失败');
    }
    router.push(`/checkout/${orderNo}`);
  } catch (err) {
    ElMessage.error(err.response?.data?.message || err.message || '结算失败');
  }
};

const remove = async (cartId) => {
  try {
    await axios.delete(`/api/cart/remove/${cartId}`);
    ElMessage.success('已删除');
    loadCart();
    window.dispatchEvent(new Event('cart-updated'));
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '删除失败');
  }
};

const updateQuantity = async (row, quantity) => {
  if (!quantity || quantity === row.cart.quantity) return;
  try {
    const res = await axios.post('/api/cart/update', { id: row.cart.id, quantity });
    if (res.data.code !== 0) {
      ElMessage.error(res.data.message || '更新失败');
      loadCart();
      return;
    }
    row.cart.quantity = quantity;
    window.dispatchEvent(new Event('cart-updated'));
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '更新失败');
    loadCart();
  }
};

onMounted(loadCart);
</script>

<style scoped>
.cart-page {
  padding-bottom: 88px;
}

.cart-table-wrap {
  padding: 4px;
}

.cart-table {
  width: 100%;
}

.cart-goods {
  display: flex;
  align-items: center;
  gap: 14px;
}

.goods-thumb {
  width: 72px;
  height: 72px;
  border-radius: 14px;
  background: var(--kc-bg-soft);
  flex-shrink: 0;
  border: 1px solid var(--kc-border);
}

.goods-title {
  font-weight: 600;
  color: var(--kc-text);
}

.goods-meta {
  color: var(--kc-text-muted);
  font-size: 12px;
  margin-top: 4px;
}

.footer-left {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.footer-meta {
  color: var(--kc-text-muted);
  font-size: 14px;
}

.footer-price {
  font-size: 22px;
  background: linear-gradient(135deg, var(--kc-primary), var(--kc-primary-light));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
</style>
