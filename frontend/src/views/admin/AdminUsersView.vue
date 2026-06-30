<template>
  <div class="admin-users">
    <div class="panel-toolbar kc-tech-card">
      <el-input v-model="keyword" placeholder="用户名/手机/昵称/校园" clearable style="width:280px" @keyup.enter="reload" />
      <el-button type="primary" round @click="reload">查询</el-button>
    </div>

    <div class="table-panel kc-tech-card">
    <el-table v-loading="loading" :data="rows" stripe class="admin-table">
      <template #empty><span /></template>
      <el-table-column label="用户" min-width="140">
        <template #default="{ row }">
          <div>{{ row.user?.username }}</div>
          <div class="sub">{{ row.user?.nickname || '未设置昵称' }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="user.phone" label="手机" width="130" />
      <el-table-column prop="user.campus" label="校园" min-width="120" />
      <el-table-column label="角色" width="120">
        <template #default="{ row }">
          <el-tag size="small" :type="row.user?.role === 1 ? 'danger' : 'info'">
            {{ row.user?.role === 1 ? '管理员' : '普通用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="row.user?.accountStatus === 0 ? 'danger' : 'success'">
            {{ row.user?.accountStatus === 0 ? '已禁用' : '正常' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发布/订单" width="110">
        <template #default="{ row }">{{ row.goodsCount }} / {{ row.orderCount }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.user?.role !== 1"
            size="small"
            @click="setRole(row.user.id, 1)"
          >设为管理员</el-button>
          <el-button
            v-else
            size="small"
            plain
            @click="setRole(row.user.id, 0)"
          >取消管理员</el-button>
          <el-button
            v-if="row.user?.accountStatus !== 0"
            size="small"
            type="danger"
            plain
            @click="setStatus(row.user.id, 0)"
          >禁用</el-button>
          <el-button
            v-else
            size="small"
            type="success"
            plain
            @click="setStatus(row.user.id, 1)"
          >启用</el-button>
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
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import axios from 'axios';
import { ElMessage, ElMessageBox } from 'element-plus';

const loading = ref(false);
const rows = ref([]);
const page = ref(1);
const size = ref(10);
const total = ref(0);
const keyword = ref('');

const loadData = async () => {
  loading.value = true;
  try {
    const params = { page: page.value, size: size.value };
    if (keyword.value.trim()) params.keyword = keyword.value.trim();
    const res = await axios.get('/api/admin/users', { params });
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

const setRole = async (id, role) => {
  const tip = role === 1 ? '确认设为管理员？' : '确认取消管理员权限？';
  try {
    await ElMessageBox.confirm(tip, '提示');
    const res = await axios.put(`/api/admin/users/${id}/role`, { role });
    if (res.data.code !== 0) {
      ElMessage.error(res.data.message || '操作失败');
      return;
    }
    ElMessage.success('已更新');
    loadData();
  } catch (err) {
    if (err !== 'cancel') ElMessage.error(err.response?.data?.message || '操作失败');
  }
};

const setStatus = async (id, accountStatus) => {
  const tip = accountStatus === 0 ? '确认禁用该用户？' : '确认启用该用户？';
  try {
    await ElMessageBox.confirm(tip, '提示');
    const res = await axios.put(`/api/admin/users/${id}/status`, { accountStatus });
    if (res.data.code !== 0) {
      ElMessage.error(res.data.message || '操作失败');
      return;
    }
    ElMessage.success('已更新');
    loadData();
  } catch (err) {
    if (err !== 'cancel') ElMessage.error(err.response?.data?.message || '操作失败');
  }
};

onMounted(loadData);
</script>

<style scoped>
.panel-toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 14px;
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
.admin-table :deep(.el-table__header th) {
  background: var(--kc-bg-soft) !important;
  color: var(--kc-primary-dark);
  font-weight: 600;
}
.sub { color: #888; font-size: 12px; }
.pager { display: flex; justify-content: flex-end; margin-top: 16px; padding: 0 16px; }
</style>
