<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="种类"><el-input v-model="searchForm.keyword" placeholder="粮食种类" clearable style="width:150px" /></el-form-item>
        <el-form-item label="状态"><el-select v-model="searchForm.status" clearable style="width:100px">
          <el-option label="启用" :value="1" /><el-option label="禁用" :value="0" />
        </el-select></el-form-item>
        <el-form-item><el-button type="primary" @click="handleSearch">查询</el-button><el-button @click="handleReset">重置</el-button></el-form-item>
        <el-form-item style="float:right"><el-button type="primary" @click="handleAdd">新增</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card style="margin-top:12px">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column prop="grainType" label="种类" width="100" />
        <el-table-column prop="grainGrade" label="等级" width="80" />
        <el-table-column prop="refPurchasePrice" label="收购价" width="100" />
        <el-table-column prop="refSalePrice" label="销售价" width="100" />
        <el-table-column prop="moistureMax" label="含水率" width="80" />
        <el-table-column prop="storageTempMin" label="温度范围" width="100">
          <template #default="{row}">{{row.storageTempMin}}~{{row.storageTempMax}}°C</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="70" align="center">
          <template #default="{row}"><el-tag :type="row.status===1?'success':'danger'" size="small">{{row.status===1?'启用':'禁用'}}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{row}">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size" :total="pagination.total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next" style="margin-top:16px;justify-content:flex-end" @change="fetchData" />
    </el-card>
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" label-width="100px">
        <el-form-item label="粮食种类"><el-input v-model="form.grainType" /></el-form-item>
        <el-form-item label="等级"><el-input v-model="form.grainGrade" /></el-form-item>
        <el-form-item label="收购单价"><el-input-number v-model="form.refPurchasePrice" :precision="4" :step="0.01" style="width:100%" /></el-form-item>
        <el-form-item label="销售单价"><el-input-number v-model="form.refSalePrice" :precision="4" :step="0.01" style="width:100%" /></el-form-item>
        <el-form-item label="允许含水率"><el-input-number v-model="form.moistureMax" :precision="2" :step="0.1" :max="100" style="width:100%" />%</el-form-item>
        <el-form-item label="允许杂质率"><el-input-number v-model="form.impurityMax" :precision="2" :step="0.1" style="width:100%" />%</el-form-item>
        <el-form-item label="温度范围"><el-input-number v-model="form.storageTempMin" :precision="1" style="width:45%" />~<el-input-number v-model="form.storageTempMax" :precision="1" style="width:45%" />°C</el-form-item>
        <el-form-item label="状态"><el-radio-group v-model="form.status"><el-radio :value="1">启用</el-radio><el-radio :value="0">禁用</el-radio></el-radio-group></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getGrainPage, createGrain, updateGrain, deleteGrain } from '@/api/config'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const searchForm = reactive({ keyword: '', status: null })
const pagination = reactive({ page: 1, size: 20, total: 0 })
const form = reactive({
  id: null,
  grainType: '',
  grainGrade: '',
  refPurchasePrice: 0,
  refSalePrice: 0,
  moistureMax: 0,
  impurityMax: 0,
  storageTempMin: 0,
  storageTempMax: 30,
  status: 1
})

async function fetchData() {
  loading.value = true
  try {
    const res = await getGrainPage({
      page: pagination.page,
      size: pagination.size,
      keyword: searchForm.keyword || undefined,
      status: searchForm.status ?? undefined
    })
    const d = res.data
    tableData.value = d?.records ?? []
    pagination.total = d?.total ?? 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchData()
}

function handleReset() {
  searchForm.keyword = ''
  searchForm.status = null
  pagination.page = 1
  fetchData()
}

function handleAdd() {
  Object.assign(form, {
    id: null,
    grainType: '',
    grainGrade: '',
    refPurchasePrice: 0,
    refSalePrice: 0,
    moistureMax: 0,
    impurityMax: 0,
    storageTempMin: 0,
    storageTempMax: 30,
    status: 1
  })
  dialogTitle.value = '新增粮食'
  dialogVisible.value = true
}

function handleEdit(row) {
  Object.assign(form, { ...row })
  dialogTitle.value = '编辑粮食'
  dialogVisible.value = true
}
async function handleSubmit() {
  submitting.value = true
  try {
    if (form.id) {
      await updateGrain(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createGrain(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error(e?.response?.data?.msg || '操作失败')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`删除「${row.grainType}」?`, '提示', { type: 'warning' })
    await deleteGrain(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(fetchData)
</script>
<style scoped>.page-container{padding:20px}</style>