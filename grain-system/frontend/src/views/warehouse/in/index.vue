<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="入库单号/收购单号" clearable style="width:180px" />
        </el-form-item>
        <el-form-item label="粮食品种">
          <el-select v-model="searchForm.grainId" placeholder="全部" clearable style="width:150px">
            <el-option v-for="item in grainOptions" :key="item.id" :label="`${item.grainType} - ${item.grainGrade}`" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="储位">
          <el-select v-model="searchForm.positionId" placeholder="全部" clearable style="width:150px">
            <el-option v-for="item in positionOptions" :key="item.id" :label="item.positionName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
        <el-form-item style="float:right">
          <el-button type="primary" @click="handleAdd">新增入库</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top:12px">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column prop="inboundNo" label="入库单号" width="150" />
        <el-table-column prop="purchaseOrderNo" label="收购单号" width="130" />
        <el-table-column label="品种" width="120">
          <template #default="{ row }">{{ row.grainType }} - {{ row.grainGrade }}</template>
        </el-table-column>
        <el-table-column prop="positionName" label="储位" width="120" />
        <el-table-column prop="netWeight" label="净重(kg)" width="100" align="right" />
        <el-table-column prop="statusName" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="warning" size="small">待确认</el-tag>
            <el-tag v-else type="success" size="small">已确认</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">{{ row.createTime ? dayjs(row.createTime).format('YYYY-MM-DD HH:mm') : '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">详情</el-button>
            <el-button v-if="row.status === 0" type="success" link size="small" @click="handleConfirm(row)">确认</el-button>
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

    <el-dialog v-model="dialogVisible" title="新增入库" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="收购单" prop="purchaseOrderId">
          <el-select v-model="form.purchaseOrderId" placeholder="请选择收购单" style="width: 100%" @change="onOrderChange">
            <el-option v-for="item in orderOptions" :key="item.id" :label="item.orderNo" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="粮食品种" prop="grainId">
          <el-select v-model="form.grainId" placeholder="请选择粮食品种" style="width: 100%">
            <el-option v-for="item in grainOptions" :key="item.id" :label="`${item.grainType} - ${item.grainGrade}`" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="储位" prop="positionId">
          <el-select v-model="form.positionId" placeholder="请选择储位" style="width: 100%">
            <el-option v-for="item in positionOptions" :key="item.id" :label="item.positionName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="净重(kg)" prop="weight">
          <el-input-number v-model="form.weight" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="入库详情" width="550px">
      <el-descriptions :column="2" border v-if="detailData">
        <el-descriptions-item label="入库单号">{{ detailData.inboundNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="detailData.status === 0" type="warning">待确认</el-tag>
          <el-tag v-else type="success">已确认</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="收购单号">{{ detailData.purchaseOrderNo }}</el-descriptions-item>
        <el-descriptions-item label="储位">{{ detailData.positionName }}</el-descriptions-item>
        <el-descriptions-item label="粮食品种">{{ detailData.grainType }} - {{ detailData.grainGrade }}</el-descriptions-item>
        <el-descriptions-item label="净重">{{ detailData.netWeight }} kg</el-descriptions-item>
        <el-descriptions-item label="确认人">{{ detailData.confirmUserName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="确认时间">{{ detailData.confirmTime ? dayjs(detailData.confirmTime).format('YYYY-MM-DD HH:mm') : '-' }}</el-descriptions-item>
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
import { getInboundList, getInbound, createInbound, confirmInbound, deleteInbound, getAvailableInboundOrders } from '@/api/warehouse/inbound'
import { getGrainList, getPositionList } from '@/api/config'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const searchForm = reactive({ keyword: '', grainId: null, positionId: null })
const pagination = reactive({ page: 1, size: 20, total: 0 })
const form = reactive({ purchaseOrderId: null, grainId: null, positionId: null, weight: 0 })
const rules = {
  purchaseOrderId: [{ required: true, message: '请选择收购单', trigger: 'change' }],
  grainId: [{ required: true, message: '请选择粮食品种', trigger: 'change' }],
  positionId: [{ required: true, message: '请选择储位', trigger: 'change' }],
  weight: [{ required: true, message: '请输入净重', trigger: 'blur' }]
}

const detailDialogVisible = ref(false)
const detailData = ref(null)
const grainOptions = ref([])
const positionOptions = ref([])
const orderOptions = ref([])

async function fetchData() {
  loading.value = true
  try {
    const res = await getInboundList({
      page: pagination.page,
      size: pagination.size,
      keyword: searchForm.keyword || undefined,
      grainId: searchForm.grainId || undefined,
      positionId: searchForm.positionId || undefined
    })
    const d = res.data
    tableData.value = d?.records ?? (Array.isArray(d) ? d : [])
    pagination.total = d?.total ?? tableData.value.length
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

async function loadOptions() {
  try {
    const [grainRes, positionRes, orderRes] = await Promise.all([
      getGrainList({ status: 1 }),
      getPositionList({ status: 1 }),
      getAvailableInboundOrders()
    ])
    grainOptions.value = grainRes.data || []
    positionOptions.value = positionRes.data || []
    orderOptions.value = orderRes.data || []
  } catch (e) { console.error(e) }
}

function onOrderChange(orderId) {
  const order = orderOptions.value.find(o => o.id === orderId)
  if (order) {
    form.grainId = order.grainId
    form.weight = order.actualWeight
  }
}

function handleSearch() { pagination.page = 1; fetchData() }
function handleReset() { searchForm.keyword = ''; searchForm.grainId = null; searchForm.positionId = null; pagination.page = 1; fetchData() }

function handleAdd() {
  Object.assign(form, { purchaseOrderId: null, grainId: null, positionId: null, weight: 0 })
  dialogTitle.value = '新增入库'
  dialogVisible.value = true
  loadOptions()
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await createInbound({
      purchaseOrderId: form.purchaseOrderId,
      grainId: form.grainId,
      positionId: form.positionId,
      weight: form.weight
    })
    ElMessage.success('创建成功')
    dialogVisible.value = false
    fetchData()
  } catch (e) { ElMessage.error(e?.response?.data?.msg || '创建失败') }
  finally { submitting.value = false }
}

async function handleConfirm(row) {
  try {
    await ElMessageBox.confirm('确定确认该入库吗？', '提示', { type: 'warning' })
    await confirmInbound(row.id)
    ElMessage.success('已确认')
    fetchData()
  } catch (e) { if (e !== 'cancel') ElMessage.error(e?.response?.data?.msg || '确认失败') }
}

async function handleView(row) {
  try {
    const res = await getInbound(row.id)
    detailData.value = res.data
    detailDialogVisible.value = true
  } catch (e) { ElMessage.error('获取详情失败') }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除该入库记录吗？', '提示', { type: 'warning' })
    await deleteInbound(row.id)
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