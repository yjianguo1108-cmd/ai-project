<template>
  <div class="page-container">
    <el-card>
      <el-form :inline="true"><el-form-item label="参数分组">
        <el-select v-model="searchForm.paramGroup" clearable style="width:150px" @change="fetchData">
          <el-option label="收购参数" value="purchase" /><el-option label="销售参数" value="sales" />
          <el-option label="仓储参数" value="inventory" /><el-option label="预警参数" value="alert" />
        </el-select></el-form-item>
      </el-form>
    </el-card>
    <el-card style="margin-top:12px">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column prop="paramKey" label="参数键" min-width="180" />
        <el-table-column prop="paramValue" label="参数值" min-width="150">
          <template #default="{ row }">
            <el-input
              v-if="row.isEditable === 1 && editingId === row.id"
              v-model="row.paramValue"
              size="small"
              @keyup.enter="saveParam(row)"
            />
            <span v-else>{{ row.paramValue }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="paramDesc" label="说明" min-width="200" />
        <el-table-column prop="paramGroup" label="分组" width="100">
          <template #default="{ row }">{{ paramGroupLabel(row.paramGroup) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{row}">
            <template v-if="row.isEditable === 1">
              <el-button v-if="editingId === row.id" type="success" link size="small" @click="saveParam(row)">保存</el-button>
              <el-button v-else type="primary" link size="small" @click="startEdit(row)">编辑</el-button>
            </template>
            <span v-else style="color:#999;font-size:12px">系统参数</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getSystemParams, updateSystemParam } from '@/api/config'

const PARAM_GROUP_LABELS = {
  purchase: '收购',
  sales: '销售',
  inventory: '仓储',
  alert: '预警'
}

const loading = ref(false)
const tableData = ref([])
const editingId = ref(null)
const searchForm = reactive({ paramGroup: '' })

function paramGroupLabel(g) {
  if (g == null || g === '') return '-'
  return PARAM_GROUP_LABELS[g] || g
}

function startEdit(row) {
  editingId.value = row.id
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getSystemParams(searchForm.paramGroup || undefined)
    tableData.value = res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function saveParam(row) {
  try {
    await updateSystemParam(row.id, row.paramValue)
    editingId.value = null
    ElMessage.success('保存成功')
    await fetchData()
  } catch (e) {
    ElMessage.error(e?.response?.data?.msg || '保存失败')
  }
}

onMounted(fetchData)
</script>
<style scoped>.page-container{padding:20px}</style>