<template>
  <div class="kc-auth-shell kc-animate-in">
    <div class="kc-auth-brand">
      <div class="brand-icon">💎</div>
      <h1>K-CARD</h1>
      <p>KPOP 小卡友善交易平台<br />收藏、换卡、面交，一站搞定</p>
      <div class="brand-tags">
        <span>安全交易</span>
        <span>校园面交</span>
        <span>换卡专区</span>
      </div>
    </div>
    <div class="kc-auth-panel">
      <h2>欢迎回来</h2>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="submit">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" size="large" autocomplete="username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input type="password" v-model="form.password" size="large" autocomplete="current-password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" round native-type="button" :loading="loading" style="width:100%" @click="submit">
            登录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="kc-auth-footer">
        还没有账号？<span class="kc-link" @click="goRegister">立即注册</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import axios from 'axios';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';

const router = useRouter();
const route = useRoute();
const formRef = ref(null);
const loading = ref(false);
const form = reactive({ username: '', password: '' });

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
};

const submit = async () => {
  if (!formRef.value) return;
  try {
    await formRef.value.validate();
  } catch {
    ElMessage.warning('请输入用户名和密码');
    return;
  }
  loading.value = true;
  try {
    const res = await axios.post('/api/auth/login', {
      username: form.username.trim(),
      password: form.password,
    });
    if (res.data.code !== 0) {
      ElMessage.error(res.data.message || '登录失败');
      return;
    }
    localStorage.setItem('authToken', res.data.data.token);
    localStorage.setItem('currentUser', JSON.stringify(res.data.data.user));
    ElMessage.success('登录成功');
    const redirect = route.query.redirect || '/';
    router.push(typeof redirect === 'string' ? redirect : '/');
  } catch (error) {
    if (!error.response) {
      ElMessage.error('无法连接服务器，请确认后端已启动（端口 8080）');
    } else {
      ElMessage.error(error.response?.data?.message || '登录失败');
    }
  } finally {
    loading.value = false;
  }
};

const goRegister = () => router.push('/register');
</script>
