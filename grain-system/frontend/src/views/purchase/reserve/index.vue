<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="预约单号/农户姓名" clearable style="width:180px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width:120px">
            <el-option label="待审核" :value="0" />
            <el-option label="已审核" :value="1" />
            <el-option label="已排期" :value="2" />
            <el-option label="已取消" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
        <el-form-item style="float:right">
          <el-button type="primary" @click="handleAdd">新增预约</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top:12px">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column prop="reserveNo" label="预约单号" width="150" />
        <el-table-column prop="farmerName" label="农户姓名" width="100" />
        <el-table-column label="联系方式" width="120">
          <template #default="{ row }">{{ row.farmerPhone || '-' }}</template>
        </el-table-column>
        <el-table-column label="粮食品种" width="120">
          <template #default="{ row }">{{ row.grainType }} - {{ row.grainGrade }}</template>
        </el-table-column>
        <el-table-column prop="estimatedWeight" label="预估重量(kg)" width="120" align="right" />
        <el-table-column label="预约时间" width="160">
          <template #default="{ row }">{{ row.reserveTime ? dayjs(row.reserveTime).format('YYYY-MM-DD HH:mm') : '-' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="warning" size="small">待审核</el-tag>
            <el-tag v-else-if="row.status === 1" type="primary" size="small">已审核</el-tag>
            <el-tag v-else-if="row.status === 2" type="success" size="small">已排期</el-tag>
            <el-tag v-else type="info" size="small">已取消</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">{{ row.createTime ? dayjs(row.createTime).format('YYYY-MM-DD HH:mm') : '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 0" type="success" link size="small" @click="handleAudit(row)">审核</el-button>
            <el-button v-if="row.status === 1" type="warning" link size="small" @click="handleSchedule(row)">排期</el-button>
            <el-button v-if="row.status !== 2 && row.status !== 3" type="danger" link size="small" @click="handleCancel(row)">取消</el-button>
            <el-button type="info" link size="small" @click="handleView(row)">详情</el-button>
            <el-button v-if="row.status === 0 || row.status === 3" type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
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
        <el-form-item label="农户" prop="farmerId">
          <el-select
            v-model="form.farmerId"
            filterable
            placeholder="搜索或选择农户"
            :loading="farmerLoading"
            style="width: 100%"
            @focus="loadFarmerOptions"
            @change="onFarmerChange"
          >
            <el-option v-for="item in farmerOptions" :key="item.id" :label="item.label" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="粮食品种" prop="grainId">
          <el-select v-model="form.grainId" placeholder="请选择粮食品种" style="width: 100%">
            <el-option v-for="item in grainOptions" :key="item.id" :label="`${item.grainType} - ${item.grainGrade}`" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="预估重量(kg)" prop="estimatedWeight">
          <el-input-number v-model="form.estimatedWeight" :min="0" :precision="2" :step="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="预约时间" prop="reserveTime">
          <el-date-picker v-model="form.reserveTime" type="datetime" placeholder="选择预约收购时间" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="auditDialogVisible" title="审核预约" width="400px">
      <el-form label-width="80px">
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.auditStatus">
            <el-radio :value="1">通过</el-radio>
            <el-radio :value="3">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="auditForm.auditStatus === 3" label="驳回原因">
          <el-input v-model="auditForm.rejectReason" type="textarea" :rows="3" placeholder="请输入驳回原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAuditSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="cancelDialogVisible" title="取消预约" width="400px">
      <el-form label-width="80px">
        <el-form-item label="取消原因">
          <el-input v-model="cancelForm.reason" type="textarea" :rows="3" placeholder="请输入取消原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCancelSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="预约详情" width="600px">
      <el-descriptions :column="2" border v-if="detailData">
        <el-descriptions-item label="预约单号">{{ detailData.reserveNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="detailData.status === 0" type="warning">待审核</el-tag>
          <el-tag v-else-if="detailData.status === 1" type="primary">已审核</el-tag>
          <el-tag v-else-if="detailData.status === 2" type="success">已排期</el-tag>
          <el-tag v-else type="info">已取消</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="农户姓名">{{ detailData.farmerName }}</el-descriptions-item>
        <el-descriptions-item label="联系方式">{{ detailData.farmerPhone }}</el-descriptions-item>
        <el-descriptions-item label="粮食品种">{{ detailData.grainType }} - {{ detailData.grainGrade }}</el-descriptions-item>
        <el-descriptions-item label="预估重量">{{ detailData.estimatedWeight }} kg</el-descriptions-item>
        <el-descriptions-item label="预约时间">{{ detailData.reserveTime ? dayjs(detailData.reserveTime).format('YYYY-MM-DD HH:mm') : '-' }}</el-descriptions-item>
        <el-descriptions-item label="驳回原因" v-if="detailData.rejectReason">{{ detailData.rejectReason }}</el-descriptions-item>
        <el-descriptions-item label="审核人">{{ detailData.auditUserName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审核时间">{{ detailData.auditTime ? dayjs(detailData.auditTime).format('YYYY-MM-DD HH:mm') : '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建人">{{ detailData.createUserName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime ? dayjs(detailData.createTime).format('YYYY-MM-DD HH:mm') : '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { getReserveList, getReserve, createReserve, updateReserve, deleteReserve, auditReserve, scheduleReserve, cancelReserve } from '@/api/purchase/reserve'
import { getFarmerList } from '@/api/farmer'
import { getGrainList } from '@/api/config'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const searchForm = reactive({ keyword: '', status: null })
const pagination = reactive({ page: 1, size: 20, total: 0 })
const form = reactive({ id: null, farmerId: null, grainId: null, estimatedWeight: 0, reserveTime: null })
const rules = {
  farmerId: [{ required: true, message: '请选择农户', trigger: 'change' }],
  grainId: [{ required: true, message: '请选择粮食品种', trigger: 'change' }],
  estimatedWeight: [{ required: true, message: '请输入预估重量', trigger: 'blur' }],
  reserveTime: [{ required: true, message: '请选择预约时间', trigger: 'change' }]
}

const auditDialogVisible = ref(false)
const auditForm = reactive({ id: null, auditStatus: 1, rejectReason: '' })
const cancelDialogVisible = ref(false)
const cancelForm = reactive({ id: null, reason: '' })
const detailDialogVisible = ref(false)
const detailData = ref(null)

const farmerOptions = ref([])
const farmerLoading = ref(false)
const grainOptions = ref([])

async function fetchData() {
  loading.value = true
  try {
    const res = await getReserveList({
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

async function searchFarmer(query) {
  farmerLoading.value = true
  try {
    const res = await getFarmerList({ page: 1, size: 50, keyword: query, auditStatus: 1 })
    const list = res.data?.records ?? []
    farmerOptions.value = list.map(f => ({ id: f.id, label: `${f.realName}（${f.phoneMask || f.phone}）` }))
  } catch (e) { console.error(e) }
  finally { farmerLoading.value = false }
}

function onFarmerChange(farmerId) {
  form.farmerId = farmerId
}

async function loadFarmerOptions() {
  if (farmerOptions.value.length > 0) return
  await searchFarmer('')
}

async function loadGrainOptions() {
  try {
    const res = await getGrainList({ status: 1 })
    grainOptions.value = res.data || []
  } catch (e) { console.error(e) }
}

function handleSearch() { pagination.page = 1; fetchData() }
function handleReset() { searchForm.keyword = ''; searchForm.status = null; pagination.page = 1; fetchData() }

function handleAdd() {
  Object.assign(form, { id: null, farmerId: null, grainId: null, estimatedWeight: 0, reserveTime: null })
  dialogTitle.value = '新增预约'
  dialogVisible.value = true
  loadGrainOptions()
  searchFarmer('')
}

async function handleEdit(row) {
  try {
    const res = await getReserve(row.id)
    const d = res.data
    Object.assign(form, {
      id: d.id,
      farmerId: d.farmerId,
      grainId: d.grainId,
      estimatedWeight: d.estimatedWeight,
      reserveTime: d.reserveTime
    })
    await searchFarmer('')
    farmerOptions.value = [{ id: d.farmerId, label: `${d.farmerName}（${d.farmerPhone || ''}）` }]
    dialogTitle.value = '编辑预约'
    dialogVisible.value = true
    loadGrainOptions()
  } catch (e) { ElMessage.error('获取详情失败') }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (form.id) {
      await updateReserve(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createReserve(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) { ElMessage.error(e?.response?.data?.msg || '操作失败') }
  finally { submitting.value = false }
}

async function handleAudit(row) {
  auditForm.id = row.id
  auditForm.auditStatus = 1
  auditForm.rejectReason = ''
  auditDialogVisible.value = true
}

async function handleAuditSubmit() {
  try {
    await auditReserve(auditForm.id, auditForm.auditStatus, auditForm.rejectReason || '')
    ElMessage.success(auditForm.auditStatus === 1 ? '审核通过' : '已驳回')
    auditDialogVisible.value = false
    fetchData()
  } catch (e) { ElMessage.error(e?.response?.data?.msg || '审核失败') }
}

async function handleSchedule(row) {
  try {
    await ElMessageBox.confirm('确定要排期该预约吗？', '提示', { type: 'warning' })
    await scheduleReserve(row.id)
    ElMessage.success('排期成功')
    fetchData()
  } catch (e) { if (e !== 'cancel') ElMessage.error(e?.response?.data?.msg || '排期失败') }
}

function handleCancel(row) {
  cancelForm.id = row.id
  cancelForm.reason = ''
  cancelDialogVisible.value = true
}

async function handleCancelSubmit() {
  try {
    await cancelReserve(cancelForm.id, cancelForm.reason)
    ElMessage.success('已取消')
    cancelDialogVisible.value = false
    fetchData()
  } catch (e) { ElMessage.error(e?.response?.data?.msg || '取消失败') }
}

async function handleView(row) {
  try {
    const res = await getReserve(row.id)
    detailData.value = res.data
    detailDialogVisible.value = true
  } catch (e) { ElMessage.error('获取详情失败') }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除该预约吗？', '提示', { type: 'warning' })
    await deleteReserve(row.id)
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