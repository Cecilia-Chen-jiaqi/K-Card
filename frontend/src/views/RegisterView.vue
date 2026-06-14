<template>
  <el-container>
    <el-main style="max-width: 500px; margin: 40px auto;">
      <el-card>
        <h2>注册</h2>
        <el-form :model="form" label-width="100px">
          <el-form-item label="用户名"><el-input v-model="form.username" /></el-form-item>
          <el-form-item label="密码"><el-input type="password" v-model="form.password" /></el-form-item>
          <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
          <el-form-item label="校园"><el-input v-model="form.campus" /></el-form-item>
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
            <img v-if="form.avatar" :src="form.avatar" alt="avatar" style="max-width: 100px; margin-top: 12px;" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="submit">注册</el-button>
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
const form = reactive({ username: '', password: '', phone: '', campus: '', avatar: '' });

const handleUploadSuccess = (response) => {
  if (response.code === 0) {
    form.avatar = response.data;
  } else {
    alert(response.message || '头像上传失败');
  }
};

const beforeUpload = (file) => {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/webp';
  const isLt5M = file.size / 1024 / 1024 < 5;
  if (!isImage) {
    alert('只支持 JPG/PNG/WEBP 图片');
  }
  if (!isLt5M) {
    alert('图片大小不能超过5MB');
  }
  return isImage && isLt5M;
};

const submit = async () => {
  const res = await axios.post('/api/auth/register', form);
  if (res.data.code !== 0) {
    return alert(res.data.message || '注册失败');
  }
  alert('注册成功，请登录');
  router.push('/login');
};
</script>
