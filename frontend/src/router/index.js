import { createRouter, createWebHistory } from 'vue-router';
import HomeView from '../views/HomeView.vue';
import GoodsDetailView from '../views/GoodsDetailView.vue';
import CartView from '../views/CartView.vue';
import OrderCheckoutView from '../views/OrderCheckoutView.vue';
import PaySuccessView from '../views/PaySuccessView.vue';
import UserCenterView from '../views/UserCenterView.vue';
import GoodsPublishView from '../views/GoodsPublishView.vue';
import CampusZoneView from '../views/CampusZoneView.vue';
import ExchangeZoneView from '../views/ExchangeZoneView.vue';
import LoginView from '../views/LoginView.vue';
import RegisterView from '../views/RegisterView.vue';

const routes = [
  { path: '/', component: HomeView },
  { path: '/goods/:id', component: GoodsDetailView },
  { path: '/cart', component: CartView, meta: { requiresAuth: true } },
  { path: '/checkout/:orderNo', component: OrderCheckoutView, meta: { requiresAuth: true } },
  { path: '/pay-success', component: PaySuccessView },
  { path: '/user', component: UserCenterView, meta: { requiresAuth: true } },
  { path: '/publish', component: GoodsPublishView, meta: { requiresAuth: true } },
  { path: '/campus', component: CampusZoneView },
  { path: '/exchange', component: ExchangeZoneView },
  { path: '/login', component: LoginView },
  { path: '/register', component: RegisterView },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('authToken');
  if (to.meta.requiresAuth && !token) {
    return next('/login');
  }
  if ((to.path === '/login' || to.path === '/register') && token) {
    return next('/');
  }
  next();
});

export default router;
