<template>
  <canvas ref="canvasRef" class="tech-canvas" aria-hidden="true" />
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue';

const canvasRef = ref(null);
let rafId = 0;

const init = () => {
  const canvas = canvasRef.value;
  if (!canvas) return;
  const ctx = canvas.getContext('2d');
  const dpr = window.devicePixelRatio || 1;
  const w = canvas.offsetWidth;
  const h = canvas.offsetHeight;
  canvas.width = w * dpr;
  canvas.height = h * dpr;
  ctx.scale(dpr, dpr);

  const dots = Array.from({ length: Math.min(24, Math.floor(w / 50)) }, () => ({
    x: Math.random() * w,
    y: Math.random() * h,
    r: Math.random() * 1.2 + 0.3,
    vx: (Math.random() - 0.5) * 0.12,
    vy: (Math.random() - 0.5) * 0.1,
  }));

  const draw = () => {
    ctx.clearRect(0, 0, w, h);
    dots.forEach((d) => {
      d.x += d.vx;
      d.y += d.vy;
      if (d.x < 0) d.x = w;
      if (d.x > w) d.x = 0;
      if (d.y < 0) d.y = h;
      if (d.y > h) d.y = 0;
      ctx.beginPath();
      ctx.arc(d.x, d.y, d.r, 0, Math.PI * 2);
      ctx.fillStyle = 'rgba(74, 144, 226, 0.15)';
      ctx.fill();
    });
    rafId = requestAnimationFrame(draw);
  };
  draw();
};

const onResize = () => {
  cancelAnimationFrame(rafId);
  init();
};

onMounted(() => {
  init();
  window.addEventListener('resize', onResize);
});

onUnmounted(() => {
  cancelAnimationFrame(rafId);
  window.removeEventListener('resize', onResize);
});
</script>

<style scoped>
.tech-canvas {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  opacity: 0.7;
}
</style>
