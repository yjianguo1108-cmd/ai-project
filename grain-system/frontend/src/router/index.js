import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { usePermissionStore } from '@/stores/permission'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      }
    ]
  },
  {
    path: '/system',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/system/user',
    meta: { title: '系统管理', icon: 'Setting', requiresAuth: true },
    children: [
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/system/user/index.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'role',
        name: 'Role',
        component: () => import('@/views/system/role/index.vue'),
        meta: { title: '角色管理', icon: 'UserFilled' }
      },
      {
        path: 'farmer',
        name: 'Farmer',
        component: () => import('@/views/system/farmer/index.vue'),
        meta: { title: '农户管理', icon: 'Avatar' }
      },
      {
        path: 'org',
        name: 'Org',
        component: () => import('@/views/system/org/index.vue'),
        meta: { title: '机构管理', icon: 'OfficeBuilding' }
      },
      { 
        path: 'grain', 
        name: 'Grain', 
        component: () => import('@/views/system/grain/index.vue'), 
        meta: { title: '粮食配置', icon: 'Coin' } 
      },
      { 
        path: 'position', 
        name: 'Position', 
        component: () => import('@/views/system/position/index.vue'), 
        meta: { title: '储位管理', icon: 'Box' } 
      },
      { 
        path: 'params', 
        name: 'Params', 
        component: () => import('@/views/system/params/index.vue'), 
        meta: { title: '系统参数', icon: 'Tools' } 
      },
    ]
  },
  {
    path: '/business',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/business/purchase/reserve',
    meta: { title: '业务管理', icon: 'Goods', requiresAuth: true },
    children: [
      {
        path: '/business/purchase/reserve',
        name: 'PurchaseReserve',
        component: () => import('@/views/purchase/reserve/index.vue'),
        meta: { title: '收购预约', icon: 'Calendar' }
      },
      {
        path: '/business/purchase/weighing',
        name: 'PurchaseWeighing',
        component: () => import('@/views/purchase/weighing/index.vue'),
        meta: { title: '称重计量', icon: 'Scale' }
      },
      {
        path: '/business/purchase/order',
        name: 'PurchaseOrder',
        component: () => import('@/views/purchase/order/index.vue'),
        meta: { title: '收购单据', icon: 'Document' }
      },
      {
        path: '/business/purchase/payment',
        name: 'PurchasePayment',
        component: () => import('@/views/purchase/payment/index.vue'),
        meta: { title: '收购付款', icon: 'Money' }
      },
      {
        path: '/business/warehouse/inbound',
        name: 'WarehouseInbound',
        component: () => import('@/views/warehouse/in/index.vue'),
        meta: { title: '粮食入库', icon: 'Download' }
      },
      {
        path: '/business/warehouse/ledger',
        name: 'WarehouseLedger',
        component: () => import('@/views/warehouse/ledger/index.vue'),
        meta: { title: '库存台账', icon: 'Box' }
      },
      {
        path: '/business/sales',
        name: 'Sales',
          // component: () => import('@/views/business/sales/index.vue'),
          // meta: { title: '粮食销售', icon: 'Sell' }
      },
      {
        path: '/business/inventory',
        name: 'Inventory',
          // component: () => import('@/views/business/inventory/index.vue'),
          // meta: { title: '库存管理', icon: 'Box' }
      }
    ]
  },
  {
    path: '/report',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/report/daily',
    meta: { title: '报表统计', icon: 'DataAnalysis', requiresAuth: true },
    children: [
      {
        path: 'daily',
        name: 'DailyReport',
          // component: () => import('@/views/report/daily/index.vue'),
          // meta: { title: '日报表', icon: 'Document' }
      },
      {
        path: 'monthly',
        name: 'MonthlyReport',
          // component: () => import('@/views/report/monthly/index.vue'),
          // meta: { title: '月报表', icon: 'Calendar' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404',
    meta: { requiresAuth: false }
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404', requiresAuth: false }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  const permissionStore = usePermissionStore()
  
  if (to.meta.requiresAuth !== false) {
    if (!userStore.token) {
      next({ name: 'Login', query: { redirect: to.fullPath } })
      return
    }
    
    if (!userStore.userInfo) {
      try {
        await userStore.getUserInfoAction()
      } catch (error) {
        next({ name: 'Login' })
        return
      }
    }
  }
  
  if (to.name === 'Login' && userStore.token) {
    next({ name: 'Dashboard' })
    return
  }
  
  next()
})

export default router
