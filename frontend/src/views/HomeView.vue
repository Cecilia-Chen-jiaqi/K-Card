<template>
  <el-container>
    <el-header>
      <el-input v-model="search" placeholder="搜索团体/爱豆/品相" clearable />
    </el-header>
    <el-main>
      <el-row :gutter="20">
        <el-col :span="6"><el-card><router-link to="/campus">校园同城专区</router-link></el-card></el-col>
        <el-col :span="6"><el-card><router-link to="/exchange">换卡专区</router-link></el-card></el-col>
        <el-col :span="6"><el-card><router-link to="/publish">发布商品</router-link></el-card></el-col>
      </el-row>
      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col v-for="item in goodsList" :key="item.id" :span="6">
          <el-card>
            <img :src="item.coverImage || 'https://via.placeholder.com/200'" style="width: 100%; height: 150px; object-fit: cover;" />
            <h3>{{ item.title }}</h3>
            <div>{{ item.groupName }} / {{ item.idolName }}</div>
            <div>{{ item.quality }} / {{ item.tradeType }}</div>
            <div>¥{{ item.price }}</div>
            <el-button type="primary" @click="goDetail(item.id)">查看</el-button>
          </el-card>
        </el-col>
      </el-row>
    </el-main>
  </el-container>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';

const search = ref('');
const goodsList = ref([]);

const loadGoods = async () => {
  const res = await axios.get('/api/goods/list');
  goodsList.value = res.data.data || [];
};

const goDetail = (id) => {
  window.location.href = `/goods/${id}`;
};

onMounted(loadGoods);
</script>
