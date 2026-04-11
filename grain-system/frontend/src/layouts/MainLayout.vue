<template>
  <el-container class="main-layout">
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <el-icon :size="24"><House /></el-icon>
        <span>粮食管理系统</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :router="true"
        :collapse="isCollapse"
        class="sidebar-menu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        
        <el-sub-menu index="/system">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/system/user">用户管理</el-menu-item>
          <el-menu-item index="/system/role">角色管理</el-menu-item>
          <el-menu-item index="/system/farmer">农户管理</el-menu-item>
          <el-menu-item index="/system/org">机构管理</el-menu-item>
          <el-menu-item index="/system/grain">粮食配置</el-menu-item>
          <el-menu-item index="/system/position">储位管理</el-menu-item>
          <el-menu-item index="/system/params">系统参数</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/business">
          <template #title>
            <el-icon><Goods /></el-icon>
            <span>业务管理</span>
          </template>
          <el-menu-item index="/business/purchase/reserve">收购预约</el-menu-item>
          <el-menu-item index="/business/purchase/weighing">称重计量</el-menu-item>
          <el-menu-item index="/business/purchase/order">收购单据</el-menu-item>
          <el-menu-item index="/business/purchase/payment">收购付款</el-menu-item>
          <el-menu-item index="/business/warehouse/inbound">粮食入库</el-menu-item>
          <el-menu-item index="/business/warehouse/ledger">库存台账</el-menu-item>
          <el-menu-item index="/business/sales">粮食销售</el-menu-item>
          <el-menu-item index="/business/inventory">库存管理</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/report">
          <template #title>
            <el-icon><DataAnalysis /></el-icon>
            <span>报表统计</span>
          </template>
          <el-menu-item index="/report/daily">日报表</el-menu-item>
          <el-menu-item index="/report/monthly">月报表</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-icon" @click="toggleCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentRoute.meta.title">
              {{ currentRoute.meta.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><User /></el-icon>
              <span>{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="password">修改密码</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  House, HomeFilled, Setting, Goods, DataAnalysis,
  Fold, Expand, User, ArrowDown
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const isCollapse = ref(false)
const activeMenu = computed(() => route.path)
const currentRoute = computed(() => route)

function toggleCollapse() {
  isCollapse.value = !isCollapse.value
}

async function handleCommand(command) {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      userStore.logout()
      router.push('/login')
      ElMessage.success('已退出登录')
    } catch (e) {}
  } else if (command === 'password') {
    router.push('/profile/password')
  } else if (command === 'profile') {
    router.push('/profile/index')
  }
}
</script>

<style scoped>
.main-layout { height: 100vh; }
.sidebar { background-color: #304156; overflow-x: hidden; }
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  border-bottom: 1px solid #404854;
}
.sidebar-menu {
  border-right: none;
  height: calc(100vh - 60px);
  overflow-y: auto;
}
.header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}
.header-left { display: flex; align-items: center; gap: 20px; }
.collapse-icon { cursor: pointer; font-size: 20px; }
.header-right { display: flex; align-items: center; }
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 0 10px;
}
.user-info:hover { background-color: #f5f7fa; border-radius: 4px; }
.main-content {
  background-color: #f0f2f5;
  padding: 20px;
  min-height: calc(100vh - 60px);
}
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
