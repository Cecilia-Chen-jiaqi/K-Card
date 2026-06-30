<template>
  <el-container class="admin-layout">
    <div class="admin-bg" aria-hidden="true" />
    <el-aside width="232px" class="admin-aside glass-nav">
      <div class="aside-grid" aria-hidden="true" />
      <div class="admin-brand" @click="router.push('/admin/dashboard')">
        <span class="logo-icon">◆</span>
        <div>
          <strong class="logo-text">K-CARD</strong>
          <small>Admin Console</small>
        </div>
      </div>
      <nav class="admin-nav">
        <router-link
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ active: activeMenu === item.path }"
        >
          <span class="nav-icon" v-html="item.svg" />
          <span class="nav-text">{{ item.label }}</span>
        </router-link>
      </nav>
      <div class="admin-aside-foot">
        <button type="button" class="back-link" @click="router.push('/')">
          <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
          返回前台
        </button>
      </div>
    </el-aside>
    <el-container class="admin-body">
      <el-header class="admin-header glass-nav">
        <div class="header-left">
          <span class="header-tag">ADMIN</span>
          <div>
            <h1 class="kc-tech-title">{{ pageTitle }}</h1>
            <p class="header-sub">{{ headerSub }}</p>
          </div>
        </div>
        <div class="header-right">
          <span class="status-dot" /><span class="status-text">在线</span>
          <div class="admin-user-chip">
            <el-avatar :size="34">{{ avatarLetter }}</el-avatar>
            <div>
              <strong>{{ currentUser?.username || '管理员' }}</strong>
              <span>System Admin</span>
            </div>
          </div>
        </div>
      </el-header>
      <el-main class="admin-main">
        <router-view v-slot="{ Component }">
          <transition name="admin-fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getCurrentUser } from '../utils/auth';

const route = useRoute();
const router = useRouter();
const currentUser = computed(() => getCurrentUser());
const activeMenu = computed(() => route.path);
const pageTitle = computed(() => route.meta.title || '管理后台');
const avatarLetter = computed(() => currentUser.value?.username?.charAt(0)?.toUpperCase() || 'A');

const headerSub = computed(() => ({
  '/admin/dashboard': 'Platform metrics & analytics',
  '/admin/goods': 'Review seller listings',
  '/admin/users': 'Manage roles & accounts',
}[route.path] || ''));

const iconChart = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M4 19V5M10 19V9M16 19v-6M22 19V3"/></svg>';
const iconGoods = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><rect x="3" y="5" width="18" height="14" rx="2"/><path d="M3 10h18"/></svg>';
const iconUsers = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><circle cx="9" cy="8" r="3"/><path d="M3 19c0-3.3 2.7-6 6-6s6 2.7 6 6M17 11v6M20 14h-6"/></svg>';

const navItems = [
  { path: '/admin/dashboard', label: '数据统计', svg: iconChart },
  { path: '/admin/goods', label: '商品审核', svg: iconGoods },
  { path: '/admin/users', label: '用户管理', svg: iconUsers },
];
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
  position: relative;
  background: var(--kc-bg-subtle);
}

.admin-bg {
  position: fixed;
  inset: 0;
  background:
    radial-gradient(ellipse 70% 45% at 50% -5%, rgba(74, 144, 226, 0.08), transparent),
    linear-gradient(rgba(74, 144, 226, 0.025) 1px, transparent 1px),
    linear-gradient(90deg, rgba(74, 144, 226, 0.025) 1px, transparent 1px);
  background-size: auto, 28px 28px, 28px 28px;
  pointer-events: none;
  z-index: 0;
}

.glass-nav {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.92) 0%, rgba(248, 251, 255, 0.88) 100%);
  backdrop-filter: blur(20px) saturate(1.12);
  -webkit-backdrop-filter: blur(20px) saturate(1.12);
  border-color: var(--kc-border);
  box-shadow: 0 4px 24px rgba(74, 144, 226, 0.05);
}

.admin-aside {
  position: relative;
  z-index: 2;
  display: flex;
  flex-direction: column;
  border-right: 1px solid var(--kc-border);
}

.aside-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(74, 144, 226, 0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(74, 144, 226, 0.04) 1px, transparent 1px);
  background-size: 20px 20px;
  mask-image: linear-gradient(180deg, rgba(0,0,0,0.4) 0%, transparent 80%);
  pointer-events: none;
}

.admin-brand {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 24px 20px 20px;
  cursor: pointer;
  border-bottom: 1px solid var(--kc-border);
}

.logo-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: var(--kc-primary);
  background: var(--kc-bg-soft);
  border: 1px solid var(--kc-border);
}

.logo-text {
  display: block;
  font-size: 16px;
  font-weight: 800;
  background: linear-gradient(135deg, var(--kc-primary-dark), var(--kc-primary));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.admin-brand small {
  color: var(--kc-text-muted);
  font-size: 11px;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.admin-nav {
  position: relative;
  flex: 1;
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: 10px;
  color: var(--kc-text-muted);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: background 0.2s, color 0.2s;
  border: 1px solid transparent;
}

.nav-item:hover {
  background: var(--kc-bg-soft);
  color: var(--kc-primary-dark);
}

.nav-item.active {
  background: var(--kc-bg-soft);
  color: var(--kc-primary-dark);
  border-color: var(--kc-border);
  box-shadow: inset 3px 0 0 var(--kc-primary);
}

.nav-icon {
  width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0.75;
}

.nav-item.active .nav-icon { opacity: 1; color: var(--kc-primary); }

.admin-aside-foot {
  position: relative;
  padding: 16px;
  border-top: 1px solid var(--kc-border);
}

.back-link {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  padding: 10px;
  border: 1px solid var(--kc-border);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.6);
  color: var(--kc-primary);
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s, border-color 0.2s;
}

.back-link:hover {
  background: var(--kc-bg-soft);
  border-color: rgba(74, 144, 226, 0.45);
}

.admin-body { position: relative; z-index: 1; }

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 68px;
  padding: 0 28px;
  border-bottom: 1px solid var(--kc-border);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.header-tag {
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.12em;
  color: var(--kc-primary);
  padding: 4px 8px;
  border-radius: 4px;
  border: 1px solid var(--kc-border);
  background: var(--kc-bg-soft);
}

.header-left h1 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
}

.header-sub {
  margin: 2px 0 0;
  font-size: 12px;
  color: var(--kc-text-muted);
  letter-spacing: 0.02em;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 14px;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #52c41a;
  box-shadow: 0 0 6px rgba(82, 196, 26, 0.6);
}

.status-text {
  font-size: 12px;
  color: var(--kc-text-muted);
  margin-right: 4px;
}

.admin-user-chip {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 12px 4px 4px;
  border-radius: 999px;
  background: var(--kc-bg-soft);
  border: 1px solid var(--kc-border);
}

.admin-user-chip strong {
  display: block;
  font-size: 13px;
  color: var(--kc-text);
}

.admin-user-chip span {
  font-size: 10px;
  color: var(--kc-text-muted);
  letter-spacing: 0.04em;
}

.admin-main { padding: 24px 28px 32px; }

.admin-fade-enter-active,
.admin-fade-leave-active { transition: opacity 0.18s ease; }
.admin-fade-enter-from,
.admin-fade-leave-to { opacity: 0; }

@media (max-width: 768px) {
  .admin-aside { width: 64px !important; }
  .admin-brand div, .nav-text, .admin-aside-foot { display: none; }
  .admin-brand { justify-content: center; padding: 16px 8px; }
  .nav-item { justify-content: center; padding: 10px; }
}
</style>
