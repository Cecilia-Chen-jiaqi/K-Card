<template>
  <el-container class="layout-main">
    <div
      class="kc-route-progress"
      :class="{ 'is-loading': routeLoading, 'is-done': routeDone }"
    />
    <el-header class="layout-header glass-nav">
      <div class="nav-scroll-line" :style="{ '--scroll-progress': scrollProgress }" />
      <div class="header-left">
        <div class="logo" @click="goHome">
          <span class="logo-icon">💎</span>
          <span class="logo-text">K-CARD</span>
          <span class="logo-sub">小卡交易站</span>
        </div>
        <nav class="main-nav">
          <router-link to="/" class="nav-link" :class="{ active: route.path === '/' }">首页</router-link>
          <router-link to="/campus" class="nav-link" :class="{ active: route.path === '/campus' }">校园面交</router-link>
          <router-link to="/exchange" class="nav-link" :class="{ active: route.path === '/exchange' }">换卡专区</router-link>
          <router-link v-if="isLoggedIn" to="/publish" class="nav-link" :class="{ active: route.path === '/publish' }">发布小卡</router-link>
        </nav>
        <button type="button" class="mobile-menu-btn" aria-label="菜单" @click="mobileMenuOpen = !mobileMenuOpen">
          <span /><span /><span />
        </button>
      </div>
      <transition name="fade">
        <div v-if="mobileMenuOpen" class="mobile-nav-overlay" @click="mobileMenuOpen = false">
          <nav class="mobile-nav" @click.stop>
            <router-link to="/" class="mobile-nav-link" @click="mobileMenuOpen = false">首页</router-link>
            <router-link to="/campus" class="mobile-nav-link" @click="mobileMenuOpen = false">校园面交</router-link>
            <router-link to="/exchange" class="mobile-nav-link" @click="mobileMenuOpen = false">换卡专区</router-link>
            <router-link v-if="isLoggedIn" to="/publish" class="mobile-nav-link" @click="mobileMenuOpen = false">发布小卡</router-link>
            <router-link v-if="isLoggedIn" to="/cart" class="mobile-nav-link" @click="mobileMenuOpen = false">购物车</router-link>
            <router-link v-if="isLoggedIn" to="/user" class="mobile-nav-link" @click="mobileMenuOpen = false">个人中心</router-link>
          </nav>
        </div>
      </transition>
      <div class="nav-actions">
        <template v-if="isLoggedIn">
          <el-badge
            :value="cartHint"
            :hidden="!cartHint"
            class="cart-badge kc-cart-badge"
            :class="{ 'is-bounce': badgeBounce }"
          >
            <el-button circle class="cart-btn" @click="router.push('/cart')">
              <span class="cart-icon">🛒</span>
            </el-button>
          </el-badge>
          <el-dropdown @command="handleCommand" trigger="hover" :show-arrow="false">
            <span class="user-dropdown" tabindex="-1">
              <el-avatar :size="38" :src="currentUser?.avatar || ''" class="user-avatar">
                <template #default>{{ avatarText }}</template>
              </el-avatar>
              <span class="user-name">{{ currentUser?.username || '用户' }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="user">个人中心</el-dropdown-item>
                <el-dropdown-item command="my-goods">我的发布</el-dropdown-item>
                <el-dropdown-item command="favorites">我的收藏</el-dropdown-item>
                <el-dropdown-item command="follows">关注卖家</el-dropdown-item>
                <el-dropdown-item command="cart">我的购物车</el-dropdown-item>
                <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                <el-dropdown-item v-if="isAdminUser" divided command="admin">管理后台</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button type="text" @click="goLogin" class="text-button">登录</el-button>
          <el-button type="primary" round @click="goRegister">注册</el-button>
        </template>
      </div>
    </el-header>

    <el-main class="layout-body">
      <div class="layout-bg" aria-hidden="true">
        <TechBackground />
      </div>
      <div class="layout-inner">
      <PageBackBar
        v-if="showBackBar"
        :title="pageTitle"
        :fallback="pageFallback"
      />
      <router-view v-slot="{ Component }">
        <transition name="page-fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
      </div>
    </el-main>

    <footer class="layout-footer glass-nav">
      <span class="footer-brand">K-CARD · KPOP 小卡友善交易平台</span>
    </footer>
  </el-container>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import axios from 'axios';
import PageBackBar from './PageBackBar.vue';
import TechBackground from './TechBackground.vue';
import { isAdmin } from '../utils/auth';

const router = useRouter();
const route = useRoute();
const refreshCount = ref(0);
const cartHint = ref(0);
const badgeBounce = ref(false);
const scrollY = ref(0);
const routeLoading = ref(false);
const routeDone = ref(false);
const mobileMenuOpen = ref(false);
let routeTimer = null;
let scrollHandler = null;

const scrollProgress = computed(() => Math.min(scrollY.value / 320, 1));

const isLoggedIn = computed(() => {
  refreshCount.value;
  return !!localStorage.getItem('authToken');
});

const currentUser = computed(() => {
  refreshCount.value;
  const raw = localStorage.getItem('currentUser');
  if (!raw) return null;
  try { return JSON.parse(raw); } catch { return null; }
});

const avatarText = computed(() => currentUser.value?.username?.charAt(0)?.toUpperCase() || 'U');
const isAdminUser = computed(() => {
  refreshCount.value;
  return isAdmin();
});

const showBackBar = computed(() => route.meta.showBack !== false);
const pageTitle = computed(() => route.meta.title || '');
const pageFallback = computed(() => route.meta.fallback || '/');

const goHome = () => router.push('/');
const goLogin = () => router.push('/login');
const goRegister = () => router.push('/register');

const handleCommand = (command) => {
  const map = {
    user: '/user',
    'my-goods': '/user?tab=goods',
    favorites: '/user?tab=favorites',
    follows: '/user?tab=follows',
    cart: '/cart',
    orders: '/user?tab=buyer',
    admin: '/admin/dashboard',
  };
  if (command === 'logout') {
    localStorage.removeItem('authToken');
    localStorage.removeItem('currentUser');
    window.location.href = '/';
    return;
  }
  if (map[command]) router.push(map[command]);
};

const fetchCartCount = async () => {
  if (!localStorage.getItem('authToken')) {
    cartHint.value = 0;
    return;
  }
  try {
    const res = await axios.get('/api/cart/list');
    const list = res.data.data || [];
    cartHint.value = list.reduce((sum, item) => sum + (item.cart?.quantity || 0), 0);
  } catch {
    cartHint.value = 0;
  }
};

const onCartUpdated = () => fetchCartCount();

watch(cartHint, (next, prev) => {
  if (next > prev) {
    badgeBounce.value = true;
    setTimeout(() => { badgeBounce.value = false; }, 500);
  }
});

onMounted(() => {
  scrollHandler = () => { scrollY.value = window.scrollY; };
  window.addEventListener('scroll', scrollHandler, { passive: true });
  window.addEventListener('cart-updated', onCartUpdated);
  window.addEventListener('user-updated', () => { refreshCount.value += 1; });
  scrollHandler();
  fetchCartCount();

  router.beforeEach((_to, _from, next) => {
    routeDone.value = false;
    routeLoading.value = true;
    next();
  });
  router.afterEach(() => {
    refreshCount.value += 1;
    routeLoading.value = false;
    routeDone.value = true;
    mobileMenuOpen.value = false;
    fetchCartCount();
    clearTimeout(routeTimer);
    routeTimer = setTimeout(() => { routeDone.value = false; }, 400);
  });
});

onUnmounted(() => {
  if (scrollHandler) window.removeEventListener('scroll', scrollHandler);
  window.removeEventListener('cart-updated', onCartUpdated);
  clearTimeout(routeTimer);
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
  z-index: 100;
  overflow: visible;
}

.nav-scroll-line {
  position: absolute;
  bottom: 0;
  left: 0;
  height: 2px;
  width: calc(var(--scroll-progress, 0) * 100%);
  background: linear-gradient(
    90deg,
    transparent 0%,
    var(--kc-primary) 30%,
    var(--kc-primary-light) 70%,
    transparent 100%
  );
  opacity: calc(0.35 + var(--scroll-progress, 0) * 0.65);
  transition: width 0.12s ease, opacity 0.12s ease;
  pointer-events: none;
}

.glass-nav {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.88) 0%, rgba(248, 251, 255, 0.82) 100%);
  backdrop-filter: blur(20px) saturate(1.15);
  -webkit-backdrop-filter: blur(20px) saturate(1.15);
  border-bottom: 1px solid var(--kc-border);
  box-shadow: 0 4px 24px rgba(74, 144, 226, 0.06);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 32px;
}

.logo {
  cursor: pointer;
  display: inline-flex;
  align-items: baseline;
  gap: 6px;
}

.logo-icon { font-size: 22px; animation: kc-float 3s ease-in-out infinite; }
.logo-text {
  font-size: 20px;
  font-weight: 800;
  background: linear-gradient(135deg, var(--kc-primary-dark), var(--kc-primary));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.logo-sub { font-size: 13px; color: var(--kc-text-muted); }

.main-nav {
  display: flex;
  gap: 8px;
}

.nav-link {
  padding: 8px 16px;
  border-radius: 999px;
  text-decoration: none;
  color: var(--kc-text-muted);
  font-weight: 500;
  transition: all 0.25s ease;
}

.nav-link:hover, .nav-link.active {
  color: var(--kc-primary-dark);
  background: rgba(74, 144, 226, 0.1);
  box-shadow: inset 0 0 0 1px rgba(74, 144, 226, 0.22);
}

.layout-body {
  position: relative;
  padding: 96px 32px 80px;
  min-height: calc(100vh - 72px);
  overflow: hidden;
}

.layout-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background: linear-gradient(180deg, #FFFFFF 0%, #F8FBFF 50%, #EEF6FD 100%);
}

.layout-inner {
  position: relative;
  z-index: 1;
  max-width: 1200px;
  margin: 0 auto;
}

.layout-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: var(--kc-text-muted);
  z-index: 99;
  border-top: 1px solid var(--kc-border);
}

.footer-brand { letter-spacing: 0.02em; opacity: 0.85; }

.nav-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.text-button { color: var(--kc-primary); padding: 0; }

.user-dropdown {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 4px 14px 4px 4px;
  border-radius: 999px;
  border: 1px solid transparent;
  outline: none;
  box-shadow: none;
  background: transparent;
  transition: background 0.2s ease, border-color 0.2s ease;
}

.user-dropdown:hover {
  background: rgba(74, 144, 226, 0.08);
  border-color: rgba(74, 144, 226, 0.16);
}

.user-dropdown:focus,
.user-dropdown:focus-visible {
  outline: none;
  box-shadow: none;
}

.user-name { color: var(--kc-text); font-weight: 600; font-size: 14px; }

:deep(.el-dropdown) {
  outline: none;
}

:deep(.el-dropdown .el-tooltip__trigger) {
  outline: none !important;
  border: none !important;
  box-shadow: none !important;
}

:deep(.el-dropdown .el-tooltip__trigger:focus),
:deep(.el-dropdown .el-tooltip__trigger:focus-visible) {
  outline: none !important;
  box-shadow: none !important;
}

.user-avatar {
  border: 2px solid rgba(74, 144, 226, 0.22);
  background: linear-gradient(135deg, #e8f2ff, #f8fbff);
  flex-shrink: 0;
}

.user-avatar :deep(img) {
  object-fit: cover;
}

.cart-btn {
  border: 1px solid var(--kc-border);
  background: var(--kc-bg-soft);
}

.page-fade-enter-active, .page-fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}
.page-fade-enter-from { opacity: 0; transform: translateY(12px); }
.page-fade-leave-to { opacity: 0; transform: translateY(-8px); }

.cart-icon { font-size: 16px; }

:deep(.kc-cart-badge.is-bounce .el-badge__content) {
  animation: kc-badge-pop 0.45s ease, kc-badge-glow 3s ease-in-out infinite 0.45s;
}

@keyframes kc-badge-pop {
  0% { transform: scale(1); }
  40% { transform: scale(1.25); }
  100% { transform: scale(1); }
}

@media (max-width: 900px) {
  .main-nav { display: none; }
  .mobile-menu-btn { display: flex; }
  .logo-sub { display: none; }
  .user-name { display: none; }
  .layout-header { padding: 0 16px; }
  .layout-body { padding: 88px 16px 72px; }
}

.mobile-menu-btn {
  display: none;
  flex-direction: column;
  justify-content: center;
  gap: 5px;
  width: 40px;
  height: 40px;
  border: 1px solid var(--kc-border);
  border-radius: 10px;
  background: var(--kc-bg-soft);
  cursor: pointer;
  padding: 0;
}
.mobile-menu-btn span {
  display: block;
  width: 18px;
  height: 2px;
  background: var(--kc-primary-dark);
  margin: 0 auto;
  border-radius: 1px;
}
.mobile-nav-overlay {
  position: fixed;
  inset: 72px 0 0;
  background: rgba(15, 23, 42, 0.35);
  z-index: 200;
  backdrop-filter: blur(2px);
}
.mobile-nav {
  background: #fff;
  padding: 12px 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  border-bottom: 1px solid var(--kc-border);
  box-shadow: 0 8px 24px rgba(74, 144, 226, 0.1);
}
.mobile-nav-link {
  padding: 12px 14px;
  border-radius: 10px;
  text-decoration: none;
  color: var(--kc-text);
  font-weight: 500;
}
.mobile-nav-link.router-link-active {
  background: rgba(74, 144, 226, 0.1);
  color: var(--kc-primary-dark);
}
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
