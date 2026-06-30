<template>
  <div class="publish-page kc-animate-in page-content">
    <div class="kc-page-header publish-hero">
      <div>
        <h2 class="kc-gradient-text">✨ 发布小卡</h2>
        <p class="page-desc">填写团体、成员、卡种等信息，让买家精准找到你的小卡</p>
      </div>
    </div>

    <el-card class="form-card kc-surface kc-tech-card kc-scan-lr">
      <el-form :model="form" label-width="120px" label-position="left">
        <el-divider content-position="left">基本信息</el-divider>
        <el-form-item label="商品标题" required>
          <el-input v-model="form.title" placeholder="例：SEVENTEEN 正规四 珉奎 特典卡" />
        </el-form-item>
        <el-form-item label="团体" required>
          <el-select v-model="form.groupName" filterable allow-create placeholder="选择或输入团体" style="width:100%" @change="onGroupChange">
            <el-option v-for="g in groupNames" :key="g" :label="g" :value="g" />
          </el-select>
        </el-form-item>
        <el-form-item label="成员" required>
          <el-select v-model="form.idolName" filterable allow-create placeholder="选择或输入成员" style="width:100%">
            <el-option v-for="m in memberOptions" :key="m" :label="m" :value="m" />
          </el-select>
        </el-form-item>
        <el-form-item label="卡种类型">
          <el-select v-model="form.cardType" filterable allow-create placeholder="专辑卡/特典卡/签售卡..." style="width:100%">
            <el-option v-for="t in cardTypes" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="回归/专辑">
          <el-input v-model="form.albumEra" placeholder="例：FML 正规四 / Super Real Me" />
        </el-form-item>

        <el-divider content-position="left">交易信息</el-divider>
        <el-form-item label="品相">
          <el-radio-group v-model="form.quality">
            <el-radio-button v-for="q in qualities" :key="q" :label="q">{{ q }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="交易模式">
          <el-radio-group v-model="form.tradeType" @change="handleTradeTypeChange">
            <el-radio-button v-for="t in tradeTypes" :key="t" :label="t">{{ t }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="价格 (¥)" required>
          <el-input-number v-model="form.price" :min="0.01" :precision="2" :step="1" />
        </el-form-item>
        <el-form-item label="库存" required>
          <el-input-number v-model="form.stock" :min="1" :max="99" />
        </el-form-item>
        <el-form-item label="校园面交">
          <el-switch v-model="campusMode" active-text="支持同城面交" />
        </el-form-item>
        <el-form-item label="预留截止" v-if="form.tradeType === '支持预留'">
          <el-date-picker v-model="form.reserveDeadline" type="datetime" format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DD HH:mm" style="width:100%" />
        </el-form-item>

        <el-divider content-position="left">图片与描述</el-divider>
        <el-form-item label="封面图">
          <el-upload action="/api/upload/image" :show-file-list="false" :on-success="handleUploadSuccess" :before-upload="beforeUpload" :headers="uploadHeaders" name="file">
            <el-button type="primary" round>上传封面</el-button>
          </el-upload>
          <img v-if="form.coverImage" :src="form.coverImage" class="preview-image" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input type="textarea" v-model="form.description" :rows="3" placeholder="描述卡面、角瑕位置、是否捆出等" />
        </el-form-item>
        <el-form-item label="瑕疵图 URL">
          <el-input v-model="form.defectImages" placeholder="多张用逗号分隔" />
        </el-form-item>
        <el-form-item label="捆卡说明">
          <el-input v-model="form.cardBundle" placeholder="是否必须捆出、捆哪些卡" />
        </el-form-item>
        <el-form-item label="换卡说明" v-if="form.tradeType === '可交换'">
          <el-input v-model="form.exchangeInfo" placeholder="想换哪位成员/哪张卡" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" size="large" round class="kc-btn-ripple kc-btn-glow" :loading="submitting" @click="submit">提交审核</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { loadKpopMeta, GROUP_MEMBERS, CARD_TYPES, QUALITIES, TRADE_TYPES } from '../constants/kpopMeta';

const router = useRouter();
const submitting = ref(false);
const meta = ref({ groups: GROUP_MEMBERS, cardTypes: CARD_TYPES, qualities: QUALITIES, tradeTypes: TRADE_TYPES });
const campusMode = ref(false);

const form = reactive({
  title: '', description: '', price: 0.01, stock: 1,
  groupName: '', idolName: '', cardType: '', albumEra: '',
  quality: '无暇', tradeType: '仅出售', reserveSupport: 0, reserveDeadline: '',
  deliveryMode: 1, coverImage: '', defectImages: '',
  cardBundle: '', exchangeInfo: '', extraInfo: '',
});

const groupNames = computed(() => Object.keys(meta.value.groups || {}));
const memberOptions = computed(() => meta.value.groups[form.groupName] || []);
const cardTypes = computed(() => meta.value.cardTypes || CARD_TYPES);
const qualities = computed(() => meta.value.qualities || QUALITIES);
const tradeTypes = computed(() => meta.value.tradeTypes || TRADE_TYPES);

const uploadHeaders = { Authorization: `Bearer ${localStorage.getItem('authToken') || ''}` };

const onGroupChange = () => { form.idolName = ''; };

const handleUploadSuccess = (response) => {
  if (response.code === 0) form.coverImage = response.data;
  else ElMessage.error(response.message || '上传失败');
};

const handleTradeTypeChange = (value) => {
  form.reserveSupport = value === '支持预留' ? 1 : 0;
  if (value !== '支持预留') form.reserveDeadline = '';
};

const beforeUpload = (file) => {
  if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type)) {
    ElMessage.warning('仅支持 JPG/PNG/WEBP');
    return false;
  }
  if (file.size / 1024 / 1024 > 5) {
    ElMessage.warning('图片不能超过 5MB');
    return false;
  }
  return true;
};

const submit = async () => {
  if (!form.title.trim() || !form.groupName.trim() || !form.idolName.trim()) {
    return ElMessage.warning('请填写标题、团体和成员');
  }
  form.deliveryMode = campusMode.value ? 2 : 1;
  submitting.value = true;
  try {
    const res = await axios.post('/api/goods/create', form);
    if (res.data.code === 0) {
      ElMessage.success('已提交审核，管理员通过后将在首页展示');
      router.push('/');
    } else {
      ElMessage.error(res.data.message || '发布失败');
    }
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '发布失败');
  } finally {
    submitting.value = false;
  }
};

onMounted(async () => { meta.value = await loadKpopMeta(axios); });
</script>

<style scoped>
.publish-page h2 { margin: 0 0 8px; }
.publish-hero .page-desc { margin: 0; }
.form-card {
  border: 1px solid var(--kc-border);
  padding: 20px 24px 28px;
  border-radius: var(--kc-radius-lg);
}

.preview-image {
  display: block;
  margin-top: 12px;
  max-width: 200px;
  border-radius: 12px;
  border: 1px solid var(--kc-border);
  box-shadow: var(--kc-shadow);
}
</style>
