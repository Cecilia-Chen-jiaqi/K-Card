<template>
  <el-container>
    <el-main>
      <h2>校园同城专区</h2>
      <el-row :gutter="20">
        <el-col v-for="item in goods" :key="item.id" :span="6">
          <el-card>
            <h3>{{ item.title }}</h3>
            <div>{{ item.groupName }} / {{ item.idolName }}</div>
            <div>价格：¥{{ item.price }}</div>
            <el-button type="primary" @click="goDetail(item.id)">查看</el-button>
          </el-card>
        </el-col>
      </el-row>
    </el-main>
  </el-container>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

const goods = ref([]);
const loadGoods = async () => {
  const res = await axios.get('/api/goods/campus');
  goods.value = res.data.data || [];
};
const goDetail = (id) => window.location.href = `/goods/${id}`;

onMounted(loadGoods);
</script>
