<template>
  <el-container>
    <el-main>
      <h2>发布商品</h2>
      <el-form :model="form" label-width="120px">
        <el-form-item label="商品标题">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="团体">
          <el-input v-model="form.groupName" />
        </el-form-item>
        <el-form-item label="爱豆">
          <el-input v-model="form.idolName" />
        </el-form-item>
        <el-form-item label="品相">
          <el-select v-model="form.quality">
            <el-option label="无暇" value="无暇" />
            <el-option label="微瑕" value="微瑕" />
            <el-option label="重瑕" value="重瑕" />
          </el-select>
        </el-form-item>
        <el-form-item label="交易模式">
          <el-select v-model="form.tradeType">
            <el-option label="仅出售" value="仅出售" />
            <el-option label="可交换" value="可交换" />
            <el-option label="支持预留" value="支持预留" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格">
          <el-input type="number" v-model="form.price" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input type="number" v-model="form.stock" />
        </el-form-item>
        <el-form-item label="是否校园面交">
          <el-switch v-model="form.deliveryMode" active-value="2" inactive-value="1" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input type="textarea" v-model="form.description" />
        </el-form-item>
        <el-form-item label="封面图片">
          <el-upload
            action="/api/upload/image"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
            :before-upload="beforeUpload"
            :headers="uploadHeaders"
            name="file"
          >
            <el-button type="primary">上传封面图</el-button>
          </el-upload>
          <img
            v-if="form.coverImage"
            :src="form.coverImage"
            style="max-width: 200px; margin-top: 12px;"
          />
        </el-form-item>
        <!-- 删除多余 </el-input> 闭合标签 -->
        <el-form-item label="瑕疵图地址">
          <el-input v-model="form.defectImages" placeholder="多张图片用逗号分隔" />
        </el-form-item>
        <el-form-item label="捆卡说明">
          <el-input v-model="form.cardBundle" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submit">发布</el-button>
        </el-form-item>
      </el-form>
    </el-main>
  </el-container>
</template>

<script setup>
import { reactive } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';

const router = useRouter();
const form = reactive({
  title: '',
  description: '',
  price: 0,
  stock: 1,
  groupName: '',
  idolName: '',
  quality: '无暇',
  tradeType: '仅出售',
  reserveSupport: 0,
  deliveryMode: 1,
  coverImage: '',
  defectImages: '',
  cardBundle: '',
});

// 只保留一次 uploadHeaders，删除下方重复定义
const uploadHeaders = {
  Authorization: `Bearer ${localStorage.getItem('authToken') || ''}`,
};

const handleUploadSuccess = (response) => {
  if (response.code === 0) {
    form.coverImage = response.data;
  } else {
    alert(response.message || '图片上传失败');
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
    alert('图片大小不能超过 5MB');
    return false;
  }
  return true;
};

const submit = async () => {
  try {
    await axios.post('/api/goods/create', form);
    alert('商品已发布');
    router.push('/');
  } catch (err) {
    alert('发布失败：' + (err.response?.data?.message || '接口异常'));
  }
};
</script>