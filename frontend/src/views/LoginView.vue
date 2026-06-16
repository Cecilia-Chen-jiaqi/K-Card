<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <h2>登录</h2>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input type="password" v-model="form.password" autocomplete="current-password" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="submit">登录</el-button>
        </el-form-item>
      </el-form>
      <div class="auth-footer">
        还没有账号？<span class="link-text" @click="goRegister">前往注册</span>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import axios from 'axios';
import { useRouter, useRoute } from 'vue-router';

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
  if (!formRef.value) {
    return;
  }
  try {
    await formRef.value.validate();
  } catch (_) {
    return;
  }
  loading.value = true;
  try {
    const res = await axios.post('/api/auth/login', {
      username: form.username,
      password: form.password,
    });
    if (res.data.code !== 0) {
      return alert(res.data.message || '登录失败');
    }
    localStorage.setItem('authToken', res.data.data.token);
    localStorage.setItem('currentUser', JSON.stringify(res.data.data.user));
    const redirect = route.query.redirect || '/';
    router.push(typeof redirect === 'string' ? redirect : '/');
  } catch (error) {
    alert(error.response?.data?.message || '登录失败');
  } finally {
    loading.value = false;
  }
};

const goRegister = () => {
  router.push('/register');
};
</script>

<style scoped>
.auth-page {
  max-width: 420px;
  margin: 100px auto 0;
}

.auth-card {
  padding: 24px;
}

.auth-footer {
  margin-top: 16px;
  color: #606266;
}

.link-text {
  color: #409eff;
  cursor: pointer;
}
</style>
