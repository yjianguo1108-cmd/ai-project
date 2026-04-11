<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="称重单号/预约单号" clearable style="width:180px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width:120px">
            <el-option label="待审核" :value="0" />
            <el-option label="已确认" :value="1" />
            <el-option label="已作废" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
        <el-form-item style="float:right">
          <el-button type="primary" @click="handleAdd">新增称重</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top:12px">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column prop="weighNo" label="称重单号" width="150" />
        <el-table-column prop="reserveNo" label="关联预约" width="130" />
        <el-table-column label="毛重(kg)" width="100" align="right">
          <template #default="{ row }">{{ row.grossWeight }}</template>
        </el-table-column>
        <el-table-column label="皮重(kg)" width="100" align="right">
          <template #default="{ row }">{{ row.tareWeight }}</template>
        </el-table-column>
        <el-table-column label="净重(kg)" width="100" align="right">
          <template #default="{ row }"><strong>{{ row.netWeight }}</strong></template>
        </el-table-column>
        <el-table-column label="称重时间" width="160">
          <template #default="{ row }">{{ row.weighTime ? dayjs(row.weighTime).format('YYYY-MM-DD HH:mm') : '-' }}</template>
        </el-table-column>
        <el-table-column prop="dataSourceName" label="数据来源" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="warning" size="small">待审核</el-tag>
            <el-tag v-else-if="row.status === 1" type="success" size="small">已确认</el-tag>
            <el-tag v-else type="info" size="small">已作废</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">详情</el-button>
            <el-button v-if="row.status === 0" type="success" link size="small" @click="handleAudit(row)">审核</el-button>
            <el-button v-if="row.status === 0" type="warning" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 0" type="danger" link size="small" @click="handleVoid(row)">作废</el-button>
            <el-button v-if="row.status === 0" type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        style="margin-top:16px; justify-content:flex-end"
        @change="fetchData"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="关联预约">
          <el-select v-model="form.reserveId" placeholder="可选择关联预约单" clearable style="width: 100%" @change="onReserveChange">
            <el-option v-for="item in reserveOptions" :key="item.id" :label="item.reserveNo" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="毛重(kg)" prop="grossWeight">
          <el-input-number v-model="form.grossWeight" :min="0" :precision="2" :step="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="皮重(kg)" prop="tareWeight">
          <el-input-number v-model="form.tareWeight" :min="0" :precision="2" :step="10" style="width: 100%" />
        </el-form-item>
        <el-form-item label="净重(kg)">
          <el-input-number v-model="calculatedNetWeight" :min="0" :precision="2" disabled style="width: 100%" />
        </el-form-item>
        <el-form-item label="称重时间" prop="weighTime">
          <el-date-picker v-model="form.weighTime" type="datetime" placeholder="选择称重时间" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="数据来源" prop="dataSource">
          <el-select v-model="form.dataSource" placeholder="请选择数据来源" style="width: 100%">
            <el-option label="自动采集" :value="0" />
            <el-option label="手动录入" :value="1" />
            <el-option label="断网补录" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="可选" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="voidDialogVisible" title="作废称重记录" width="400px">
      <el-form label-width="80px">
        <el-form-item label="作废原因">
          <el-input v-model="voidForm.reason" type="textarea" :rows="3" placeholder="请输入作废原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="voidDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleVoidSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="称重详情" width="600px">
      <el-descriptions :column="2" border v-if="detailData">
        <el-descriptions-item label="称重单号">{{ detailData.weighNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="detailData.status === 0" type="warning">待审核</el-tag>
          <el-tag v-else-if="detailData.status === 1" type="success">已确认</el-tag>
          <el-tag v-else type="info">已作废</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="关联预约">{{ detailData.reserveNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="数据来源">{{ detailData.dataSourceName }}</el-descriptions-item>
        <el-descriptions-item label="毛重">{{ detailData.grossWeight }} kg</el-descriptions-item>
        <el-descriptions-item label="皮重">{{ detailData.tareWeight }} kg</el-descriptions-item>
        <el-descriptions-item label="净重"><strong>{{ detailData.netWeight }} kg</strong></el-descriptions-item>
        <el-descriptions-item label="称重时间">{{ detailData.weighTime ? dayjs(detailData.weighTime).format('YYYY-MM-DD HH:mm') : '-' }}</el-descriptions-item>
        <el-descriptions-item label="审核人">{{ detailData.auditUserName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审核时间">{{ detailData.auditTime ? dayjs(detailData.auditTime).format('YYYY-MM-DD HH:mm') : '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailData.remark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建人">{{ detailData.createUserName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime ? dayjs(detailData.createTime).format('YYYY-MM-DD HH:mm') : '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { getWeighingList, getWeighing, createWeighing, updateWeighing, deleteWeighing, auditWeighing, voidWeighing } from '@/api/purchase/weighing'
import { getReserveList } from '@/api/purchase/reserve'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const searchForm = reactive({ keyword: '', status: null })
const pagination = reactive({ page: 1, size: 20, total: 0 })
const form = reactive({ id: null, reserveId: null, grossWeight: 0, tareWeight: 0, weighTime: null, dataSource: 1, remark: '' })
const rules = {
  grossWeight: [{ required: true, message: '请输入毛重', trigger: 'blur' }],
  tareWeight: [{ required: true, message: '请输入皮重', trigger: 'blur' }],
  weighTime: [{ required: true, message: '请选择称重时间', trigger: 'change' }],
  dataSource: [{ required: true, message: '请选择数据来源', trigger: 'change' }]
}

const calculatedNetWeight = computed(() => {
  return Math.max(0, (form.grossWeight || 0) - (form.tareWeight || 0)).toFixed(2)
})

const voidDialogVisible = ref(false)
const voidForm = reactive({ id: null, reason: '' })
const detailDialogVisible = ref(false)
const detailData = ref(null)
const reserveOptions = ref([])

async function fetchData() {
  loading.value = true
  try {
    const res = await getWeighingList({
      page: pagination.page,
      size: pagination.size,
      keyword: searchForm.keyword || undefined,
      status: searchForm.status || undefined
    })
    const d = res.data
    tableData.value = d?.records ?? (Array.isArray(d) ? d : [])
    pagination.total = d?.total ?? tableData.value.length
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

async function loadReserveOptions() {
  try {
    const res = await getReserveList({ status: 2, size: 100 })
    reserveOptions.value = res.data?.records ?? []
  } catch (e) { console.error(e) }
}

function handleSearch() { pagination.page = 1; fetchData() }
function handleReset() { searchForm.keyword = ''; searchForm.status = null; pagination.page = 1; fetchData() }

function handleAdd() {
  Object.assign(form, { id: null, reserveId: null, grossWeight: 0, tareWeight: 0, weighTime: null, dataSource: 1, remark: '' })
  dialogTitle.value = '新增称重'
  dialogVisible.value = true
  loadReserveOptions()
}

function onReserveChange(reserveId) {
  form.reserveId = reserveId
}

async function handleEdit(row) {
  try {
    const res = await getWeighing(row.id)
    const d = res.data
    Object.assign(form, {
      id: d.id,
      reserveId: d.reserveId,
      grossWeight: d.grossWeight,
      tareWeight: d.tareWeight,
      weighTime: d.weighTime,
      dataSource: d.dataSource,
      remark: d.remark
    })
    dialogTitle.value = '编辑称重'
    dialogVisible.value = true
    loadReserveOptions()
  } catch (e) { ElMessage.error('获取详情失败') }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (form.grossWeight <= form.tareWeight) {
    ElMessage.warning('毛重必须大于皮重')
    return
  }
  submitting.value = true
  try {
    if (form.id) {
      await updateWeighing(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createWeighing(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) { ElMessage.error(e?.response?.data?.msg || '操作失败') }
  finally { submitting.value = false }
}

async function handleAudit(row) {
  try {
    await ElMessageBox.confirm('确定审核通过该称重记录吗？', '提示', { type: 'warning' })
    await auditWeighing(row.id)
    ElMessage.success('审核成功')
    fetchData()
  } catch (e) { if (e !== 'cancel') ElMessage.error(e?.response?.data?.msg || '审核失败') }
}

function handleVoid(row) {
  voidForm.id = row.id
  voidForm.reason = ''
  voidDialogVisible.value = true
}

async function handleVoidSubmit() {
  try {
    await voidWeighing(voidForm.id, voidForm.reason)
    ElMessage.success('已作废')
    voidDialogVisible.value = false
    fetchData()
  } catch (e) { ElMessage.error(e?.response?.data?.msg || '作废失败') }
}

async function handleView(row) {
  try {
    const res = await getWeighing(row.id)
    detailData.value = res.data
    detailDialogVisible.value = true
  } catch (e) { ElMessage.error('获取详情失败') }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除该称重记录吗？', '提示', { type: 'warning' })
    await deleteWeighing(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) { if (e !== 'cancel') ElMessage.error(e?.response?.data?.msg || '删除失败') }
}

onMounted(fetchData)
</script>

<style scoped>
.page-container { padding: 20px; }
.search-card :deep(.el-card__body) { padding-bottom: 8px; }
</style>