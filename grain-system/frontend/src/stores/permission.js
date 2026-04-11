import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getMenuList } from '@/api/menu'
import { useUserStore } from './user'

export const usePermissionStore = defineStore('permission', () => {
  const routes = ref([])
  const dynamicRoutes = ref([])
  const menuList = ref([])

  async function setRoutes() {
    const userStore = useUserStore()
    const roles = userStore.roles
    
    const accessedRoutes = filterAsyncRoutes(asyncRoutes, roles)
    dynamicRoutes.value = accessedRoutes
    return accessedRoutes
  }

  function filterAsyncRoutes(routes, roles) {
    const res = []
    routes.forEach(route => {
      const tmp = { ...route }
      if (hasPermission(roles, tmp.meta?.roles)) {
        if (tmp.children) {
          tmp.children = filterAsyncRoutes(tmp.children, roles)
        }
        res.push(tmp)
      }
    })
    return res
  }

  function hasPermission(roles, routeRoles) {
    if (!routeRoles || routeRoles.length === 0) {
      return true
    }
    return roles.some(role => routeRoles.includes(role))
  }

  async function getMenuListAction() {
    try {
      const res = await getMenuList()
      menuList.value = res.data || []
      return menuList.value
    } catch (error) {
      console.error('获取菜单失败:', error)
      return []
    }
  }

  function reset() {
    routes.value = []
    dynamicRoutes.value = []
    menuList.value = []
  }

  return {
    routes,
    dynamicRoutes,
    menuList,
    setRoutes,
    getMenuListAction,
    reset
  }
})

const asyncRoutes = [
  {
    path: '/system',
    name: 'System',
    meta: { title: '系统管理', icon: 'Setting', roles: ['ADMIN', 'OPERATOR'] },
    children: [
      {
        path: '/system/user',
        name: 'User',
        meta: { title: '用户管理', icon: 'User', roles: ['ADMIN'] },
        component: 'system/user/index'
      },
      {
        path: '/system/role',
        name: 'Role',
        meta: { title: '角色管理', icon: 'UserFilled', roles: ['ADMIN'] },
        component: 'system/role/index'
      },
      {
        path: '/system/farmer',
        name: 'Farmer',
        meta: { title: '农户管理', icon: 'Avatar', roles: ['ADMIN', 'OPERATOR'] },
        component: 'system/farmer/index'
      },
      {
        path: '/system/org',
        name: 'Org',
        meta: { title: '机构管理', icon: 'OfficeBuilding', roles: ['ADMIN'] },
        component: 'system/org/index'
      },
      {
        path: '/system/grain',
        name: 'Grain',
        meta: { title: '粮食配置', icon: 'Coin', roles: ['ADMIN', 'OPERATOR'] },
        component: 'system/grain/index'
      },
      {
        path: '/system/position',
        name: 'Position',
        meta: { title: '储位管理', icon: 'Box', roles: ['ADMIN', 'OPERATOR'] },
        component: 'system/position/index'
      },
      {
        path: '/system/params',
        name: 'Params',
        meta: { title: '系统参数', icon: 'Tools', roles: ['ADMIN'] },
        component: 'system/params/index'
      }
    ]
  },
  {
    path: '/business',
    name: 'Business',
    meta: { title: '业务管理', icon: 'Goods', roles: ['admin', 'manager', 'operator'] },
    children: [
      {
        path: '/business/purchase/reserve',
        name: 'PurchaseReserve',
        meta: { title: '收购预约', icon: 'Calendar', roles: ['admin', 'manager', 'operator'] },
        component: 'purchase/reserve/index'
      },
      {
        path: '/business/purchase/weighing',
        name: 'PurchaseWeighing',
        meta: { title: '称重计量', icon: 'Scale', roles: ['admin', 'manager', 'operator'] },
        component: 'purchase/weighing/index'
      },
      {
        path: '/business/purchase/order',
        name: 'PurchaseOrder',
        meta: { title: '收购单据', icon: 'Document', roles: ['admin', 'manager', 'operator'] },
        component: 'purchase/order/index'
      },
      {
        path: '/business/purchase/payment',
        name: 'PurchasePayment',
        meta: { title: '收购付款', icon: 'Money', roles: ['admin', 'manager', 'operator'] },
        component: 'purchase/payment/index'
      },
      {
        path: '/business/warehouse/inbound',
        name: 'WarehouseInbound',
        meta: { title: '粮食入库', icon: 'Download', roles: ['admin', 'manager', 'operator'] },
        component: 'warehouse/in/index'
      },
      {
        path: '/business/warehouse/ledger',
        name: 'WarehouseLedger',
        meta: { title: '库存台账', icon: 'Box', roles: ['admin', 'manager'] },
        component: 'warehouse/ledger/index'
      },
      {
        path: '/business/sales',
        name: 'Sales',
        meta: { title: '粮食销售', icon: 'Sell', roles: ['admin', 'manager', 'operator'] },
        component: 'business/sales/index'
      },
      {
        path: '/business/inventory',
        name: 'Inventory',
        meta: { title: '库存管理', icon: 'Box', roles: ['admin', 'manager'] },
        component: 'business/inventory/index'
      }
    ]
  },
  {
    path: '/report',
    name: 'Report',
    meta: { title: '报表统计', icon: 'DataAnalysis', roles: ['admin', 'manager'] },
    children: [
      {
        path: '/report/daily',
        name: 'DailyReport',
        meta: { title: '日报表', icon: 'Document', roles: ['admin', 'manager'] },
        component: 'report/daily/index'
      },
      {
        path: '/report/monthly',
        name: 'MonthlyReport',
        meta: { title: '月报表', icon: 'Calendar', roles: ['admin', 'manager'] },
        component: 'report/monthly/index'
      }
    ]
  }
]
