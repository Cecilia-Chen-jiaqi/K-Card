<template>
  <el-container>
    <el-main style="max-width: 400px; margin: 40px auto;">
      <el-card>
        <h2>登录</h2>
        <el-form :model="form" label-width="80px">
          <el-form-item label="用户名">
            <el-input v-model="form.username" autocomplete="username" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input type="password" v-model="form.password" autocomplete="current-password" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="submit">登录</el-button>
            <el-button type="text" @click="goRegister">注册</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </el-main>
  </el-container>
</template>

<script setup>
import { reactive } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';

const router = useRouter();
const form = reactive({ username: '', password: '' });

const submit = async () => {
  const res = await axios.post('/api/auth/login', form);
  if (res.data.code !== 0) {
    return alert(res.data.message || '登录失败');
  }
  localStorage.setItem('authToken', res.data.data.token);
  localStorage.setItem('currentUser', JSON.stringify(res.data.data.user));
  router.push('/');
};

const goRegister = () => {
  router.push('/register');
};
</script>
