<template>
  <el-container>
    <el-main style="max-width: 720px; margin: 40px auto;">
      <h2>个人中心</h2>
      <el-card>
        <div style="display: flex; align-items: center; gap: 16px;">
          <img v-if="user?.avatar" :src="user.avatar" alt="头像" style="width: 80px; height: 80px; border-radius: 50%; object-fit: cover;" />
          <div>
            <div><strong>用户名：</strong>{{ user?.username }}</div>
            <div><strong>校园：</strong>{{ user?.campus || '未填写' }}</div>
            <div><strong>手机号：</strong>{{ user?.phone || '未填写' }}</div>
          </div>
        </div>
      </el-card>
      <el-card style="margin-top: 20px;">
        <el-button type="primary" @click="goPublish">发布商品</el-button>
        <el-button type="success" @click="goCart">我的购物车</el-button>
        <el-button type="warning" @click="logout">退出登录</el-button>
      </el-card>
    </el-main>
  </el-container>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const user = ref(null);

onMounted(() => {
  try {
    user.value = JSON.parse(localStorage.getItem('currentUser') || 'null');
  } catch (e) {
    user.value = null;
  }
});

const goPublish = () => router.push('/publish');
const goCart = () => router.push('/cart');
const logout = () => {
  localStorage.removeItem('authToken');
  localStorage.removeItem('currentUser');
  router.push('/login');
};
</script>
