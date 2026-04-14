<template>
  <div class="outbound-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>出库核销管理</span>
        </div>
      </template>

      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="销售订单">
          <el-select v-model="queryForm.salesOrderId" placeholder="请选择" clearable filterable>
            <el-option
              v-for="order in orderOptions"
              :key="order.id"
              :label="order.orderNo"
              :value="order.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择" clearable>
            <el-option label="待出库" :value="0" />
            <el-option label="执行中" :value="1" />
            <el-option label="已完成" :value="2" />
            <el-option label="已作废" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="tableData" stripe>
        <el-table-column prop="outboundNo" label="出库单号" width="160" />
        <el-table-column prop="orderNo" label="关联订单号" width="160" />
        <el-table-column prop="grainType" label="粮食品种" width="100" />
        <el-table-column prop="grainGrade" label="等级" width="80" />
        <el-table-column prop="positionName" label="储位" width="120" />
        <el-table-column prop="planWeight" label="计划重量" width="100" align="right" />
        <el-table-column prop="actualOutWeight" label="实际出库" width="100" align="right" />
        <el-table-column prop="actualPrice" label="销售单价" width="100" align="right" />
        <el-table-column prop="totalAmount" label="出库金额" width="120" align="right" />
        <el-table-column prop="statusName" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.outStatus)">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">详情</el-button>
            <el-button link type="success" size="small" @click="handleStart(row)" v-if="row.outStatus === 0">开始出库</el-button>
            <el-button link type="success" size="small" @click="handleConfirm(row)" v-if="row.outStatus === 1">确认出库</el-button>
            <el-button link type="danger" size="small" @click="handleVoid(row)" v-if="row.outStatus !== 2 && row.outStatus !== 3">作废</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>

    <el-dialog v-model="detailVisible" title="出库单详情" width="700px" destroy-on-close>
      <el-descriptions :column="2" border v-if="detailData.id">
        <el-descriptions-item label="出库单号">{{ detailData.outboundNo }}</el-descriptions-item>
        <el-descriptions-item label="关联订单号">{{ detailData.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="粮食品种">{{ detailData.grainType }} - {{ detailData.grainGrade }}</el-descriptions-item>
        <el-descriptions-item label="储位">{{ detailData.positionName }}</el-descriptions-item>
        <el-descriptions-item label="计划重量">{{ detailData.planWeight }}</el-descriptions-item>
        <el-descriptions-item label="实际出库">{{ detailData.actualOutWeight }}</el-descriptions-item>
        <el-descriptions-item label="销售单价">{{ detailData.actualPrice }}</el-descriptions-item>
        <el-descriptions-item label="出库金额">{{ detailData.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(detailData.outStatus)">{{ detailData.statusName }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="confirmVisible" title="确认出库" width="500px" destroy-on-close>
      <el-form ref="confirmFormRef" :model="confirmForm" :rules="confirmRules" label-width="120px">
        <el-form-item label="实际出库重量" prop="actualWeight">
          <el-input-number v-model="confirmForm.actualWeight" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="附件上传">
          <el-input v-model="confirmForm.attachmentUrl" placeholder="请输入附件URL" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="confirmVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOutboundList, startOutbound, confirmOutbound, voidOutbound, getOutboundById } from '@/api/sales/order'
import { getAvailableOrdersForOutbound } from '@/api/sales/order'

const loading = ref(false)
const tableData = ref([])
const queryForm = reactive({
  salesOrderId: null,
  status: null
})
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const orderOptions = ref([])

const detailVisible = ref(false)
const detailData = ref({})

const confirmVisible = ref(false)
const confirmFormRef = ref()
const confirmForm = reactive({
  actualWeight: 0,
  attachmentUrl: ''
})
const currentOutboundId = ref(null)

const confirmRules = {
  actualWeight: [{ required: true, message: '请输入实际出库重量', trigger: 'blur' }]
}

onMounted(() => {
  loadOrders()
  loadData()
})

async function loadOrders() {
  try {
    const res = await getAvailableOrdersForOutbound()
    orderOptions.value = res.data || []
  } catch (e) {
    console.error(e)
  }
}

async function loadData() {
  loading.value = true
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
      salesOrderId: queryForm.salesOrderId,
      status: queryForm.status
    }
    const res = await getOutboundList(params)
    tableData.value = res.data?.records || res.data || []
    pagination.total = res.data?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  pagination.current = 1
  loadData()
}

function handleReset() {
  queryForm.salesOrderId = null
  queryForm.status = null
  handleQuery()
}

function handleSizeChange() {
  loadData()
}

function handleCurrentChange() {
  loadData()
}

async function handleView(row) {
  try {
    const res = await getOutboundById(row.id)
    detailData.value = res.data || {}
    detailVisible.value = true
  } catch (e) {
    console.error(e)
  }
}

async function handleStart(row) {
  try {
    await ElMessageBox.confirm('确认开始出库?', '开始出库', { type: 'warning' })
    await startOutbound(row.id)
    ElMessage.success('已开始出库')
    loadData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

function handleConfirm(row) {
  currentOutboundId.value = row.id
  confirmForm.actualWeight = row.planWeight
  confirmForm.attachmentUrl = ''
  confirmVisible.value = true
}

async function handleConfirmSubmit() {
  try {
    await confirmFormRef.value.validate()
    await confirmOutbound(currentOutboundId.value, confirmForm.actualWeight, confirmForm.attachmentUrl)
    ElMessage.success('确认出库成功')
    confirmVisible.value = false
    loadData()
  } catch (e) {
    console.error(e)
  }
}

async function handleVoid(row) {
  try {
    const { value } = await ElMessageBox.prompt('请输入作废原因', '作废出库单', { type: 'warning' })
    await voidOutbound(row.id, value)
    ElMessage.success('作废成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

function getStatusType(status) {
  const types = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }
  return types[status] || 'info'
}
</script>

<style scoped>
.outbound-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.search-form {
  margin-bottom: 20px;
}
</style>
