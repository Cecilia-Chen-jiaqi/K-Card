<template>
  <div class="page-back-bar kc-scan-lr">
    <div class="bar-grid" aria-hidden="true" />
    <div class="bar-accent" aria-hidden="true" />

    <div class="back-actions">
      <button type="button" class="nav-pill" @click="goBack">
        <span class="nav-icon">←</span>
        <span>返回</span>
      </button>
      <span class="nav-sep" aria-hidden="true" />
      <button type="button" class="nav-pill nav-pill-soft" @click="goHome">
        <span class="nav-icon home-icon">⌂</span>
        <span>回首页</span>
      </button>
    </div>

    <div v-if="title" class="back-title">
      <span class="title-chip">{{ title }}</span>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';

const props = defineProps({
  title: { type: String, default: '' },
  fallback: { type: String, default: '/' },
});

const router = useRouter();

const goBack = () => {
  if (window.history.state?.back) {
    router.back();
    return;
  }
  router.push(props.fallback);
};

const goHome = () => router.push('/');
</script>

<style scoped>
.page-back-bar {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 22px;
  padding: 10px 18px 10px 14px;
  border-radius: var(--kc-radius);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.94) 0%, rgba(238, 246, 253, 0.88) 100%);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid var(--kc-border);
  box-shadow: var(--kc-shadow);
  overflow: hidden;
  transition: box-shadow 0.3s ease, transform 0.3s ease;
}

.page-back-bar:hover {
  box-shadow: var(--kc-shadow-hover);
}

.bar-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(74, 144, 226, 0.035) 1px, transparent 1px),
    linear-gradient(90deg, rgba(74, 144, 226, 0.035) 1px, transparent 1px);
  background-size: 22px 22px;
  pointer-events: none;
  mask-image: linear-gradient(90deg, rgba(0, 0, 0, 0.35) 0%, transparent 70%);
}

.bar-accent {
  position: absolute;
  left: 0;
  top: 12px;
  bottom: 12px;
  width: 3px;
  border-radius: 0 3px 3px 0;
  background: linear-gradient(180deg, var(--kc-primary-light), var(--kc-primary));
  pointer-events: none;
}

.back-actions,
.back-title {
  position: relative;
  z-index: 1;
}

.back-actions {
  display: flex;
  align-items: center;
  gap: 6px;
}

.nav-pill {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 7px 14px;
  border: 1px solid rgba(74, 144, 226, 0.28);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.85);
  color: var(--kc-primary-dark);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.25s ease, border-color 0.25s ease, box-shadow 0.25s ease, transform 0.2s ease;
  font-family: inherit;
}

.nav-pill:hover {
  background: rgba(74, 144, 226, 0.08);
  border-color: var(--kc-primary);
  box-shadow: 0 4px 14px rgba(74, 144, 226, 0.14);
  transform: translateY(-1px);
}

.nav-pill:active {
  transform: translateY(0);
}

.nav-pill-soft {
  border-color: rgba(74, 144, 226, 0.18);
  color: var(--kc-text-muted);
  background: rgba(255, 255, 255, 0.6);
}

.nav-pill-soft:hover {
  color: var(--kc-primary-dark);
}

.nav-icon {
  font-size: 14px;
  line-height: 1;
  color: var(--kc-primary);
}

.home-icon {
  font-size: 15px;
}

.nav-sep {
  width: 1px;
  height: 16px;
  background: var(--kc-border);
  margin: 0 2px;
}

.back-title {
  flex-shrink: 0;
}

.title-chip {
  display: inline-flex;
  align-items: center;
  padding: 5px 14px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 600;
  color: var(--kc-primary-dark);
  background: var(--kc-bg-soft);
  border: 1px solid var(--kc-border);
  letter-spacing: 0.02em;
}

@media (max-width: 540px) {
  .page-back-bar {
    flex-wrap: wrap;
    padding: 10px 14px 10px 12px;
  }

  .back-title {
    width: 100%;
    padding-left: 4px;
  }
}
</style>
