<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #409eff;">
            <el-icon :size="30"><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-title">用户总数</div>
            <div class="stat-value">{{ stats.userCount }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #67c23a;">
            <el-icon :size="30"><Avatar /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-title">农户总数</div>
            <div class="stat-value">{{ stats.farmerCount }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #e6a23c;">
            <el-icon :size="30"><ShoppingCart /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-title">今日收购</div>
            <div class="stat-value">{{ stats.todayPurchase }}吨</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #f56c6c;">
            <el-icon :size="30"><Sell /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-title">今日销售</div>
            <div class="stat-value">{{ stats.todaySales }}吨</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>近7天收购趋势</span>
          </template>
          <div ref="purchaseChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>粮食库存分布</span>
          </template>
          <div ref="stockChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { User, Avatar, ShoppingCart, Sell } from '@element-plus/icons-vue'

const stats = ref({
  userCount: 12,
  farmerCount: 156,
  todayPurchase: 28.5,
  todaySales: 15.2
})

const purchaseChartRef = ref(null)
const stockChartRef = ref(null)

onMounted(() => {
})
</script>

<style scoped>
.dashboard { padding: 20px; }
.stat-card { display: flex; align-items: center; padding: 20px; }
.stat-icon {
  width: 60px; height: 60px;
  border-radius: 8px;
  display: flex; align-items: center; justify-content: center;
  color: #fff; margin-right: 16px;
}
.stat-content { flex: 1; }
.stat-title { color: #909399; font-size: 14px; }
.stat-value { font-size: 24px; font-weight: bold; margin-top: 8px; }
</style>
