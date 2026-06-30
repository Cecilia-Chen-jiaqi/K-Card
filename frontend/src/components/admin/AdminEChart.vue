<template>
  <div ref="rootRef" class="admin-echart" :style="{ height }" />
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref, shallowRef, watch } from 'vue';
import * as echarts from 'echarts/core';
import { BarChart, LineChart, PieChart } from 'echarts/charts';
import {
  GridComponent,
  LegendComponent,
  TooltipComponent,
} from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';

echarts.use([
  LineChart,
  BarChart,
  PieChart,
  GridComponent,
  TooltipComponent,
  LegendComponent,
  CanvasRenderer,
]);

const props = defineProps({
  option: { type: Object, required: true },
  height: { type: String, default: '280px' },
});

const rootRef = ref(null);
const chart = shallowRef(null);

const render = async () => {
  if (!chart.value || !props.option) return;
  chart.value.setOption(props.option, { notMerge: true, lazyUpdate: false });
  await nextTick();
  chart.value.resize();
};

const onResize = () => chart.value?.resize();

onMounted(async () => {
  await nextTick();
  if (!rootRef.value) return;
  chart.value = echarts.init(rootRef.value);
  await render();
  window.addEventListener('resize', onResize);
});

watch(() => props.option, () => { render(); }, { deep: true });

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize);
  chart.value?.dispose();
  chart.value = null;
});
</script>

<style scoped>
.admin-echart {
  width: 100%;
  min-height: 200px;
}
</style>
