<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="单据号/农户姓名" clearable style="width:180px" />
        </el-form-item>
        <el-form-item label="单据状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width:120px">
            <el-option label="草稿" :value="0" />
            <el-option label="待审核" :value="1" />
            <el-option label="审核通过" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已作废" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="付款状态">
          <el-select v-model="searchForm.paymentStatus" placeholder="全部" clearable style="width:120px">
            <el-option label="未付款" :value="0" />
            <el-option label="部分付款" :value="1" />
            <el-option label="已付款" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
        <el-form-item style="float:right">
          <el-button type="primary" @click="handleAdd">新增单据</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top:12px">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column prop="orderNo" label="单据号" width="150" />
        <el-table-column prop="farmerName" label="农户" width="100" />
        <el-table-column label="品种" width="120">
          <template #default="{ row }">{{ row.grainType }} - {{ row.grainGrade }}</template>
        </el-table-column>
        <el-table-column prop="weighNo" label="称重单号" width="130" />
        <el-table-column prop="actualWeight" label="重量(kg)" width="100" align="right" />
        <el-table-column prop="actualPrice" label="单价" width="80" align="right" />
        <el-table-column prop="totalAmount" label="应付金额" width="100" align="right">
          <template #default="{ row }">¥{{ row.totalAmount }}</template>
        </el-table-column>
        <el-table-column prop="paymentStatusName" label="付款状态" width="100" align="center" />
        <el-table-column prop="statusName" label="单据状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" size="small">草稿</el-tag>
            <el-tag v-else-if="row.status === 1" type="warning" size="small">待审核</el-tag>
            <el-tag v-else-if="row.status === 2" type="primary" size="small">审核通过</el-tag>
            <el-tag v-else-if="row.status === 3" type="success" size="small">已完成</el-tag>
            <el-tag v-else type="info" size="small">已作废</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">{{ row.createTime ? dayjs(row.createTime).format('YYYY-MM-DD HH:mm') : '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">详情</el-button>
            <el-button v-if="row.status === 0" type="warning" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 0" type="success" link size="small" @click="handleAudit(row)">审核</el-button>
            <el-button v-if="row.status === 2" type="success" link size="small" @click="handleComplete(row)">完成</el-button>
            <el-button v-if="row.status !== 3 && row.status !== 4" type="danger" link size="small" @click="handleVoid(row)">作废</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="农户" prop="farmerId">
          <el-select v-model="form.farmerId" placeholder="请选择农户" style="width: 100%" @change="onFarmerChange">
            <el-option v-for="item in farmerOptions" :key="item.id" :label="item.label" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="粮食品种" prop="grainId">
          <el-select v-model="form.grainId" placeholder="请选择粮食品种" style="width: 100%" @change="onGrainChange">
            <el-option v-for="item in grainOptions" :key="item.id" :label="`${item.grainType} - ${item.grainGrade}`" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="称重记录" prop="weighRecordId">
          <el-select v-model="form.weighRecordId" placeholder="请选择称重记录" style="width: 100%" @change="onWeighingChange">
            <el-option v-for="item in weighingOptions" :key="item.id" :label="`${item.weighNo} | 净重:${item.netWeight}kg`" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="实际重量(kg)" prop="actualWeight">
          <el-input-number v-model="form.actualWeight" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="参考单价">
          <span>¥{{ form.refPrice || 0 }}/kg</span>
        </el-form-item>
        <el-form-item label="实际单价(元/kg)" prop="actualPrice">
          <el-input-number v-model="form.actualPrice" :min="0" :precision="4" :step="0.01" style="width: 100%" />
        </el-form-item>
        <el-form-item label="应付金额">
          <span style="font-size:18px;color:#E6A23C">¥{{ calculatedAmount }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="voidDialogVisible" title="作废单据" width="400px">
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

    <el-dialog v-model="detailDialogVisible" title="单据详情" width="700px">
      <el-descriptions :column="2" border v-if="detailData">
        <el-descriptions-item label="单据号">{{ detailData.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="单据状态">
          <el-tag v-if="detailData.status === 0" size="small">草稿</el-tag>
          <el-tag v-else-if="detailData.status === 1" type="warning" size="small">待审核</el-tag>
          <el-tag v-else-if="detailData.status === 2" type="primary" size="small">审核通过</el-tag>
          <el-tag v-else-if="detailData.status === 3" type="success" size="small">已完成</el-tag>
          <el-tag v-else type="info" size="small">已作废</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="农户姓名">{{ detailData.farmerName }}</el-descriptions-item>
        <el-descriptions-item label="联系方式">{{ detailData.farmerPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="粮食品种">{{ detailData.grainType }} - {{ detailData.grainGrade }}</el-descriptions-item>
        <el-descriptions-item label="称重单号">{{ detailData.weighNo }}</el-descriptions-item>
        <el-descriptions-item label="实际重量">{{ detailData.actualWeight }} kg</el-descriptions-item>
        <el-descriptions-item label="参考单价">¥{{ detailData.refPrice }}/kg</el-descriptions-item>
        <el-descriptions-item label="实际单价">¥{{ detailData.actualPrice }}/kg</el-descriptions-item>
        <el-descriptions-item label="应付金额"><strong style="color:#E6A23C">¥{{ detailData.totalAmount }}</strong></el-descriptions-item>
        <el-descriptions-item label="已付金额">¥{{ detailData.paidAmount }}</el-descriptions-item>
        <el-descriptions-item label="付款状态">{{ detailData.paymentStatusName }}</el-descriptions-item>
        <el-descriptions-item label="审核人">{{ detailData.auditUserName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审核时间">{{ detailData.auditTime ? dayjs(detailData.auditTime).format('YYYY-MM-DD HH:mm') : '-' }}</el-descriptions-item>
        <el-descriptions-item label="作废原因" :span="2" v-if="detailData.voidReason">{{ detailData.voidReason }}</el-descriptions-item>
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
import { getOrderList, getOrder, createOrder, updateOrder, deleteOrder, auditOrder, completeOrder, voidOrder } from '@/api/purchase/order'
import { getFarmerList } from '@/api/farmer'
import { getGrainList } from '@/api/config'
import { getWeighingList } from '@/api/purchase/weighing'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const searchForm = reactive({ keyword: '', status: null, paymentStatus: null })
const pagination = reactive({ page: 1, size: 20, total: 0 })
const form = reactive({ id: null, farmerId: null, grainId: null, weighRecordId: null, actualWeight: 0, actualPrice: 0, refPrice: 0 })
const rules = {
  farmerId: [{ required: true, message: '请选择农户', trigger: 'change' }],
  grainId: [{ required: true, message: '请选择粮食品种', trigger: 'change' }],
  weighRecordId: [{ required: true, message: '请选择称重记录', trigger: 'change' }],
  actualWeight: [{ required: true, message: '请输入实际重量', trigger: 'blur' }],
  actualPrice: [{ required: true, message: '请输入实际单价', trigger: 'blur' }]
}

const calculatedAmount = computed(() => {
  return ((form.actualWeight || 0) * (form.actualPrice || 0)).toFixed(2)
})

const voidDialogVisible = ref(false)
const voidForm = reactive({ id: null, reason: '' })
const detailDialogVisible = ref(false)
const detailData = ref(null)
const farmerOptions = ref([])
const grainOptions = ref([])
const weighingOptions = ref([])

async function fetchData() {
  loading.value = true
  try {
    const res = await getOrderList({
      page: pagination.page,
      size: pagination.size,
      keyword: searchForm.keyword || undefined,
      status: searchForm.status || undefined,
      paymentStatus: searchForm.paymentStatus || undefined
    })
    const d = res.data
    tableData.value = d?.records ?? (Array.isArray(d) ? d : [])
    pagination.total = d?.total ?? tableData.value.length
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

async function loadFarmerOptions() {
  try {
    const res = await getFarmerList({ page: 1, size: 100, auditStatus: 1 })
    farmerOptions.value = (res.data?.records ?? []).map(f => ({ id: f.id, label: `${f.realName}（${f.phoneMask || f.phone}）` }))
  } catch (e) { console.error(e) }
}

async function loadGrainOptions() {
  try {
    const res = await getGrainList({ status: 1 })
    grainOptions.value = res.data || []
  } catch (e) { console.error(e) }
}

async function loadWeighingOptions() {
  try {
    const res = await getWeighingList({ page: 1, size: 100, status: 1 })
    weighingOptions.value = res.data?.records ?? []
  } catch (e) { console.error(e) }
}

function onFarmerChange() {}
function onGrainChange(grainId) {
  const grain = grainOptions.value.find(g => g.id === grainId)
  if (grain) {
    form.refPrice = grain.refPurchasePrice
    form.actualPrice = grain.refPurchasePrice
  }
}

function onWeighingChange(weighingId) {
  const weighing = weighingOptions.value.find(w => w.id === weighingId)
  if (weighing) {
    form.actualWeight = weighing.netWeight
  }
}

function handleSearch() { pagination.page = 1; fetchData() }
function handleReset() { searchForm.keyword = ''; searchForm.status = null; searchForm.paymentStatus = null; pagination.page = 1; fetchData() }

function handleAdd() {
  Object.assign(form, { id: null, farmerId: null, grainId: null, weighRecordId: null, actualWeight: 0, actualPrice: 0, refPrice: 0 })
  dialogTitle.value = '新增收购单据'
  dialogVisible.value = true
  loadFarmerOptions()
  loadGrainOptions()
  loadWeighingOptions()
}

async function handleEdit(row) {
  try {
    const res = await getOrder(row.id)
    const d = res.data
    Object.assign(form, {
      id: d.id,
      farmerId: d.farmerId,
      grainId: d.grainId,
      weighRecordId: d.weighRecordId,
      actualWeight: d.actualWeight,
      actualPrice: d.actualPrice,
      refPrice: d.refPrice
    })
    await loadFarmerOptions()
    await loadGrainOptions()
    await loadWeighingOptions()
    dialogTitle.value = '编辑收购单据'
    dialogVisible.value = true
  } catch (e) { ElMessage.error('获取详情失败') }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (form.id) {
      await updateOrder(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createOrder(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) { ElMessage.error(e?.response?.data?.msg || '操作失败') }
  finally { submitting.value = false }
}

async function handleAudit(row) {
  try {
    await ElMessageBox.confirm('确定审核通过该单据吗？', '提示', { type: 'warning' })
    await auditOrder(row.id)
    ElMessage.success('审核成功')
    fetchData()
  } catch (e) { if (e !== 'cancel') ElMessage.error(e?.response?.data?.msg || '审核失败') }
}

async function handleComplete(row) {
  try {
    await ElMessageBox.confirm('确定完成该收购吗？完成后将触发入库流程。', '提示', { type: 'warning' })
    await completeOrder(row.id)
    ElMessage.success('已完成')
    fetchData()
  } catch (e) { if (e !== 'cancel') ElMessage.error(e?.response?.data?.msg || '操作失败') }
}

function handleVoid(row) {
  voidForm.id = row.id
  voidForm.reason = ''
  voidDialogVisible.value = true
}

async function handleVoidSubmit() {
  try {
    await voidOrder(voidForm.id, voidForm.reason)
    ElMessage.success('已作废')
    voidDialogVisible.value = false
    fetchData()
  } catch (e) { ElMessage.error(e?.response?.data?.msg || '作废失败') }
}

async function handleView(row) {
  try {
    const res = await getOrder(row.id)
    detailData.value = res.data
    detailDialogVisible.value = true
  } catch (e) { ElMessage.error('获取详情失败') }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除该单据吗？', '提示', { type: 'warning' })
    await deleteOrder(row.id)
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