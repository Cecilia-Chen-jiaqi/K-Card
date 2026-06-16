<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <h2>注册</h2>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input type="password" v-model="form.password" />
        </el-form-item>
        <el-form-item label="确认密码" prop="passwordConfirm">
          <el-input type="password" v-model="form.passwordConfirm" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="校园" prop="campus">
          <el-input v-model="form.campus" />
        </el-form-item>
        <el-form-item label="头像上传">
          <el-upload
            action="/api/upload/image"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
            :before-upload="beforeUpload"
            name="file"
          >
            <el-button type="primary">上传头像</el-button>
          </el-upload>
          <img v-if="form.avatar" :src="form.avatar" alt="avatar" class="avatar-preview" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="submit">注册</el-button>
        </el-form-item>
      </el-form>
      <div class="auth-footer">
        已有账号？<span class="link-text" @click="goLogin">前往登录</span>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';

const router = useRouter();
const formRef = ref(null);
const loading = ref(false);
const form = reactive({ username: '', password: '', passwordConfirm: '', phone: '', campus: '', avatar: '' });

const validatePasswordConfirm = (rule, value, callback) => {
  if (!value) {
    return callback(new Error('请确认密码'));
  }
  if (value !== form.password) {
    return callback(new Error('两次密码输入不一致'));
  }
  callback();
};

const validatePhone = (rule, value, callback) => {
  const phoneReg = /^1[3-9]\d{9}$/;
  if (!value) {
    return callback(new Error('请输入手机号'));
  }
  if (!phoneReg.test(value)) {
    return callback(new Error('请输入正确手机号'));
  }
  callback();
};

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' },
  ],
  passwordConfirm: [{ validator: validatePasswordConfirm, trigger: 'blur' }],
  phone: [{ validator: validatePhone, trigger: 'blur' }],
  campus: [{ required: true, message: '请输入校园', trigger: 'blur' }],
};

const handleUploadSuccess = (response) => {
  if (response.code === 0) {
    form.avatar = response.data;
  } else {
    alert(response.message || '头像上传失败');
  }
};

const beforeUpload = (file) => {
  const isImage = ['image/jpeg', 'image/png', 'image/webp'].includes(file.type);
  const isLt5M = file.size / 1024 / 1024 < 5;
  if (!isImage) {
    alert('只支持 JPG/PNG/WEBP 图片');
    return false;
  }
  if (!isLt5M) {
    alert('图片大小不能超过5MB');
    return false;
  }
  return true;
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
    const res = await axios.post('/api/auth/register', {
      username: form.username,
      password: form.password,
      phone: form.phone,
      campus: form.campus,
      avatar: form.avatar || null,
    });
    if (res.data.code !== 0) {
      return alert('注册失败：' + (res.data.message || '未知错误'));
    }
    alert('注册成功，请前往登录');
    router.push('/login');
  } catch (error) {
    const message = error.response?.data?.message || error.message || '注册失败';
    alert('注册失败：' + message);
  } finally {
    loading.value = false;
  }
};

const goLogin = () => {
  router.push('/login');
};
</script>

<style scoped>
.auth-page {
  max-width: 520px;
  margin: 100px auto 0;
}

.auth-card {
  padding: 24px;
}

.avatar-preview {
  max-width: 100px;
  margin-top: 12px;
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
