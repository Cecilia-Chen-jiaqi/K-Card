import { createRouter, createWebHistory } from 'vue-router';
import LayoutMain from '../components/LayoutMain.vue';
import LayoutAdmin from '../components/LayoutAdmin.vue';
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
import AdminDashboardView from '../views/admin/AdminDashboardView.vue';
import AdminGoodsAuditView from '../views/admin/AdminGoodsAuditView.vue';
import AdminUsersView from '../views/admin/AdminUsersView.vue';
import { isAdmin } from '../utils/auth';

const mainChildren = [
  { path: '', component: HomeView, meta: { showBack: false } },
  { path: 'goods/detail', component: GoodsDetailView, meta: { title: '商品详情' } },
  { path: 'goods/:id', component: GoodsDetailView, meta: { title: '商品详情' } },
  { path: 'cart', component: CartView, meta: { requiresAuth: true, title: '我的购物车' } },
  { path: 'checkout/:orderNo', component: OrderCheckoutView, meta: { requiresAuth: true, title: '订单结算' } },
  { path: 'pay-success', component: PaySuccessView, meta: { title: '支付结果' } },
  { path: 'user', component: UserCenterView, meta: { requiresAuth: true, title: '个人中心' } },
  { path: 'publish', component: GoodsPublishView, meta: { requiresAuth: true, title: '发布小卡' } },
  { path: 'campus', component: CampusZoneView, meta: { showBack: false } },
  { path: 'exchange', component: ExchangeZoneView, meta: { showBack: false } },
  { path: 'login', component: LoginView, meta: { title: '登录', fallback: '/' } },
  { path: 'register', component: RegisterView, meta: { title: '注册', fallback: '/' } },
];

const routes = [
  { path: '/', component: LayoutMain, children: mainChildren },
  {
    path: '/admin',
    component: LayoutAdmin,
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', component: AdminDashboardView, meta: { title: '数据统计', requiresAuth: true, requiresAdmin: true } },
      { path: 'goods', component: AdminGoodsAuditView, meta: { title: '商品审核', requiresAuth: true, requiresAdmin: true } },
      { path: 'users', component: AdminUsersView, meta: { title: '用户管理', requiresAuth: true, requiresAdmin: true } },
    ],
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('authToken');
  const needsAuth = to.matched.some((record) => record.meta.requiresAuth);
  const needsAdmin = to.matched.some((record) => record.meta.requiresAdmin);

  if (needsAuth && !token) {
    return next({ path: '/login', query: { redirect: to.fullPath } });
  }
  if (needsAdmin && !isAdmin()) {
    return next('/');
  }
  if ((to.path === '/login' || to.path === '/register') && token) {
    return next(isAdmin() && to.query.redirect?.startsWith('/admin') ? to.query.redirect : '/');
  }
  next();
});

export default router;
