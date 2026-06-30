<template>
  <div class="kc-auth-shell kc-animate-in">
    <div class="kc-auth-brand">
      <div class="brand-icon">✨</div>
      <h1>加入 K-CARD</h1>
      <p>注册后即可发布小卡、加入购物车<br />与同好安全交易</p>
      <div class="brand-tags">
        <span>免费注册</span>
        <span>校园认证</span>
        <span>安全支付</span>
      </div>
    </div>
    <div class="kc-auth-panel">
      <h2>创建账号</h2>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="submit">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="设置用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input type="password" v-model="form.password" placeholder="至少 6 位" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="passwordConfirm">
          <el-input type="password" v-model="form.passwordConfirm" show-password />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="11 位手机号" />
        </el-form-item>
        <el-form-item label="校园" prop="campus">
          <el-input v-model="form.campus" placeholder="例：XX大学" />
        </el-form-item>
        <el-form-item label="头像（可选）">
          <el-upload
            action="/api/upload/image"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
            :before-upload="beforeUpload"
            name="file"
          >
            <el-button round>上传头像</el-button>
          </el-upload>
          <img v-if="form.avatar" :src="form.avatar" alt="avatar" class="avatar-preview" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" round native-type="button" :loading="loading" style="width:100%" @click="submit">
            注册
          </el-button>
        </el-form-item>
      </el-form>
      <div class="kc-auth-footer">
        已有账号？<span class="kc-link" @click="goLogin">前往登录</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';

const router = useRouter();
const formRef = ref(null);
const loading = ref(false);
const form = reactive({ username: '', password: '', passwordConfirm: '', phone: '', campus: '', avatar: '' });

const validatePasswordConfirm = (rule, value, callback) => {
  if (!value) return callback(new Error('请确认密码'));
  if (value !== form.password) return callback(new Error('两次密码输入不一致'));
  callback();
};

const validatePhone = (rule, value, callback) => {
  if (!value) return callback(new Error('请输入手机号'));
  if (!/^1[3-9]\d{9}$/.test(value)) return callback(new Error('请输入正确手机号'));
  callback();
};

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' },
  ],
  passwordConfirm: [{ validator: validatePasswordConfirm, trigger: ['blur', 'change'] }],
  phone: [{ validator: validatePhone, trigger: ['blur', 'change'] }],
  campus: [{ required: true, message: '请输入校园', trigger: 'blur' }],
};

const handleUploadSuccess = (response) => {
  if (response.code === 0) {
    form.avatar = response.data;
    ElMessage.success('头像上传成功');
  } else {
    ElMessage.error(response.message || '头像上传失败');
  }
};

const beforeUpload = (file) => {
  const isImage = ['image/jpeg', 'image/png', 'image/webp'].includes(file.type);
  const isLt5M = file.size / 1024 / 1024 < 5;
  if (!isImage) { ElMessage.warning('只支持 JPG/PNG/WEBP 图片'); return false; }
  if (!isLt5M) { ElMessage.warning('图片大小不能超过 5MB'); return false; }
  return true;
};

const submit = async () => {
  if (!formRef.value) return;
  try {
    await formRef.value.validate();
  } catch {
    ElMessage.warning('请完善表单信息后再注册');
    return;
  }
  loading.value = true;
  try {
    const res = await axios.post('/api/auth/register', {
      username: form.username.trim(),
      password: form.password,
      phone: form.phone.trim(),
      campus: form.campus.trim(),
      avatar: form.avatar || null,
    });
    if (res.data.code !== 0) {
      ElMessage.error(res.data.message || '注册失败');
      return;
    }
    ElMessage.success('注册成功，即将跳转登录页');
    setTimeout(() => router.push('/login'), 800);
  } catch (error) {
    if (!error.response) {
      ElMessage.error('无法连接服务器，请确认后端已启动（端口 8080）');
    } else {
      ElMessage.error(error.response?.data?.message || '注册失败，请稍后重试');
    }
  } finally {
    loading.value = false;
  }
};

const goLogin = () => router.push('/login');
</script>

<style scoped>
.avatar-preview {
  display: block;
  max-width: 72px;
  margin-top: 10px;
  border-radius: 14px;
  border: 2px solid var(--kc-border);
}
</style>
