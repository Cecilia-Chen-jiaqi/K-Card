<template>
  <div class="admin-goods">
    <div class="panel-toolbar kc-tech-card">
      <el-radio-group v-model="statusFilter" @change="reload" class="filter-group">
        <el-radio-button :label="2">待审核</el-radio-button>
        <el-radio-button :label="null">全部</el-radio-button>
        <el-radio-button :label="1">已上架</el-radio-button>
        <el-radio-button :label="3">已拒绝</el-radio-button>
      </el-radio-group>
      <el-input v-model="keyword" placeholder="搜索标题" clearable style="width:220px" @keyup.enter="reload" />
      <el-button type="primary" round @click="reload">查询</el-button>
    </div>

    <div class="table-panel kc-tech-card">
    <el-table v-loading="loading" :data="rows" stripe class="admin-table">
      <template #empty><span /></template>
      <el-table-column label="商品" min-width="220">
        <template #default="{ row }">
          <div class="goods-cell">
            <el-image :src="row.goods?.coverImage" fit="cover" class="thumb" />
            <div>
              <div>{{ row.goods?.title }}</div>
              <div class="sub">{{ row.goods?.groupName }} · {{ row.goods?.idolName }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="卖家" width="120">
        <template #default="{ row }">{{ row.sellerUsername || '—' }}</template>
      </el-table-column>
      <el-table-column label="价格" width="90">
        <template #default="{ row }">¥{{ row.goods?.price }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="statusTag(row.goods?.status)">{{ statusLabel(row.goods?.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="拒绝原因" min-width="140">
        <template #default="{ row }">{{ row.goods?.rejectReason || '—' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <template v-if="row.goods?.status === 2">
            <el-button type="success" size="small" @click="audit(row.goods.id, true)">通过</el-button>
            <el-button type="danger" size="small" plain @click="openReject(row)">拒绝</el-button>
          </template>
          <span v-else class="sub">—</span>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        layout="total, prev, pager, next"
        :total="total"
        @current-change="loadData"
      />
    </div>
    </div>

    <el-dialog v-model="rejectVisible" title="拒绝原因" width="420px" class="admin-dialog">
      <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请填写拒绝原因" />
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';

const loading = ref(false);
const rows = ref([]);
const page = ref(1);
const size = ref(10);
const total = ref(0);
const statusFilter = ref(2);
const keyword = ref('');
const rejectVisible = ref(false);
const rejectReason = ref('');
const rejectTargetId = ref(null);

const statusLabel = (s) => ({ 0: '下架', 1: '上架', 2: '待审核', 3: '已拒绝' }[s] || '未知');
const statusTag = (s) => ({ 0: 'info', 1: 'success', 2: 'warning', 3: 'danger' }[s] || 'info');

const loadData = async () => {
  loading.value = true;
  try {
    const params = { page: page.value, size: size.value };
    if (statusFilter.value != null) params.status = statusFilter.value;
    if (keyword.value.trim()) params.keyword = keyword.value.trim();
    const url = statusFilter.value === 2 && !keyword.value.trim()
      ? '/api/admin/goods/pending'
      : '/api/admin/goods';
    const res = await axios.get(url, { params });
    if (res.data.code !== 0) {
      ElMessage.error(res.data.message || '加载失败');
      return;
    }
    rows.value = res.data.data?.list || [];
    total.value = res.data.data?.total || 0;
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '加载失败');
  } finally {
    loading.value = false;
  }
};

const reload = () => {
  page.value = 1;
  loadData();
};

const audit = async (goodsId, approved, reason = '') => {
  try {
    const res = await axios.post('/api/admin/goods/audit', { goodsId, approved, reason });
    if (res.data.code !== 0) {
      ElMessage.error(res.data.message || '操作失败');
      return;
    }
    ElMessage.success(res.data.message || '操作成功');
    loadData();
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '操作失败');
  }
};

const openReject = (row) => {
  rejectTargetId.value = row.goods?.id;
  rejectReason.value = '';
  rejectVisible.value = true;
};

const confirmReject = async () => {
  if (!rejectTargetId.value) return;
  await audit(rejectTargetId.value, false, rejectReason.value || '不符合平台规范');
  rejectVisible.value = false;
};

onMounted(loadData);
</script>

<style scoped>
.panel-toolbar {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 14px;
  align-items: center;
  padding: 14px 18px;
  border-radius: var(--kc-radius);
}
.table-panel {
  padding: 2px 2px 12px;
  border-radius: var(--kc-radius);
  overflow: hidden;
}
.admin-table :deep(.el-table__empty-block) {
  min-height: 40px;
}
.admin-table :deep(.el-table__empty-text) {
  display: none;
}
.goods-cell { display: flex; gap: 12px; align-items: center; }
.thumb {
  width: 56px;
  height: 56px;
  border-radius: 10px;
  flex-shrink: 0;
  border: 1px solid var(--kc-border);
}
.sub { color: #888; font-size: 12px; }
.pager { display: flex; justify-content: flex-end; margin-top: 16px; padding: 0 16px; }
</style>
