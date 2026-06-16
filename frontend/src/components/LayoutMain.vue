<template>
  <el-container class="layout-main">
    <el-header class="layout-header">
      <div class="logo" @click="goHome">
        <span class="logo-icon">✨</span>
        K-CARD 小卡交易站
      </div>
      <div class="nav-actions">
        <template v-if="isLoggedIn">
          <el-dropdown @command="handleCommand" trigger="hover">
            <span class="user-dropdown">
              <el-avatar :size="38" :src="currentUser?.avatar || ''">
                <template #default>{{ avatarText }}</template>
              </el-avatar>
              <span class="user-name">{{ currentUser?.username || '用户' }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="user">个人中心</el-dropdown-item>
                <el-dropdown-item command="publish">我的发布</el-dropdown-item>
                <el-dropdown-item command="cart">我的购物车</el-dropdown-item>
                <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button type="text" @click="goLogin" class="text-button">登录</el-button>
          <el-button type="primary" @click="goRegister">注册</el-button>
        </template>
      </div>
    </el-header>

    <el-main class="layout-body">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const refreshCount = ref(0);

const isLoggedIn = computed(() => {
  refreshCount.value;
  return !!localStorage.getItem('authToken');
});

const currentUser = computed(() => {
  refreshCount.value;
  const raw = localStorage.getItem('currentUser');
  if (!raw) {
    return null;
  }
  try {
    return JSON.parse(raw);
  } catch (err) {
    return null;
  }
});

const avatarText = computed(() => {
  if (!currentUser.value?.username) {
    return 'U';
  }
  return currentUser.value.username.charAt(0).toUpperCase();
});

const goHome = () => {
  router.push('/');
};

const goLogin = () => {
  router.push('/login');
};

const goRegister = () => {
  router.push('/register');
};

const handleCommand = (command) => {
  switch (command) {
    case 'user':
      router.push('/user');
      break;
    case 'publish':
      router.push('/publish');
      break;
    case 'cart':
      router.push('/cart');
      break;
    case 'orders':
      router.push({ path: '/user', query: { tab: 'buyer' } });
      break;
    case 'logout':
      localStorage.removeItem('authToken');
      localStorage.removeItem('currentUser');
      window.location.href = '/';
      break;
    default:
      break;
  }
};

onMounted(() => {
  router.afterEach(() => {
    refreshCount.value += 1;
  });
});
</script>

<style scoped>
.layout-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 72px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 28px;
  background: linear-gradient(90deg, #fff4f8 0%, #fffafa 100%);
  border-bottom: 1px solid rgba(242, 152, 188, 0.24);
  box-shadow: 0 12px 24px rgba(245, 162, 186, 0.12);
  z-index: 100;
}

.layout-body {
  padding: 110px 32px 32px;
  min-height: calc(100vh - 120px);
  background: #fff7fb;
}

.logo {
  font-size: 20px;
  font-weight: 700;
  color: #c2185b;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.logo-icon {
  font-size: 24px;
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.text-button {
  color: #d81b60;
  padding: 0;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.user-name {
  color: #303133;
  font-weight: 600;
}
</style>
