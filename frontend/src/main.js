import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import './styles/theme.css';
import axios from 'axios';
import { scrollReveal } from './directives/scrollReveal';

axios.interceptors.request.use((config) => {
  const token = localStorage.getItem('authToken');
  if (token) {
    config.headers = config.headers || {};
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

axios.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status;
    if (status === 401) {
      localStorage.removeItem('authToken');
      localStorage.removeItem('currentUser');
      router.push('/login');
    } else if (status === 403 || error.response?.data?.code === 403) {
      // 无权限时不强制登出
    }
    return Promise.reject(error);
  }
);

const app = createApp(App);
app.directive('scroll-reveal', scrollReveal);
app.use(router);
app.use(ElementPlus);
app.mount('#app');
