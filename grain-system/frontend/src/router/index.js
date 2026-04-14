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
      }
    ]
  },
  {
    path: '/business',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/business/purchase/reserve',
    meta: { title: '收购管理', icon: 'Document', requiresAuth: true },
    children: [
      {
        path: 'purchase/reserve',
        name: 'PurchaseReserve',
        component: () => import('@/views/purchase/reserve/index.vue'),
        meta: { title: '收购预约', icon: 'Calendar' }
      },
      {
        path: 'purchase/weighing',
        name: 'PurchaseWeighing',
        component: () => import('@/views/purchase/weighing/index.vue'),
        meta: { title: '称重记录', icon: 'Scale' }
      },
      {
        path: 'purchase/order',
        name: 'PurchaseOrder',
        component: () => import('@/views/purchase/order/index.vue'),
        meta: { title: '收购单据', icon: 'Document' }
      },
      {
        path: 'purchase/payment',
        name: 'PurchasePayment',
        component: () => import('@/views/purchase/payment/index.vue'),
        meta: { title: '付款管理', icon: 'Money' }
      },
      {
        path: 'warehouse/inbound',
        name: 'WarehouseInbound',
        component: () => import('@/views/warehouse/in/index.vue'),
        meta: { title: '入库管理', icon: 'Download' }
      },
      {
        path: 'warehouse/inventory',
        name: 'WarehouseInventory',
        component: () => import('@/views/warehouse/ledger/index.vue'),
        meta: { title: '库存管理', icon: 'Box' }
      },
      {
        path: 'warehouse/check',
        name: 'WarehouseCheck',
        component: () => import('@/views/warehouse/check/index.vue'),
        meta: { title: '盘点管理', icon: 'Check' }
      },
      {
        path: 'warehouse/out',
        name: 'WarehouseOutbound',
        component: () => import('@/views/warehouse/out/index.vue'),
        meta: { title: '出库管理', icon: 'Upload' }
      },
      {
        path: 'sales/order',
        name: 'SalesOrder',
        component: () => import('@/views/business/sales/order.vue'),
        meta: { title: '销售订单', icon: 'Document' }
      },
      {
        path: 'sales/outbound',
        name: 'SalesOutbound',
        component: () => import('@/views/business/sales/outbound.vue'),
        meta: { title: '出库核销', icon: 'Van' }
      },
      {
        path: 'sales/payment',
        name: 'SalesPayment',
        component: () => import('@/views/business/sales/payment.vue'),
        meta: { title: '收款管理', icon: 'Money' }
      },
      {
        path: 'sales/reconciliation',
        name: 'SalesReconciliation',
        component: () => import('@/views/business/sales/reconciliation.vue'),
        meta: { title: '对账管理', icon: 'Tickets' }
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
        component: () => import('@/views/report/daily/index.vue'),
        meta: { title: '日报表', icon: 'Document' }
      },
      {
        path: 'monthly',
        name: 'MonthlyReport',
        component: () => import('@/views/report/monthly/index.vue'),
        meta: { title: '月报表', icon: 'Calendar' }
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
