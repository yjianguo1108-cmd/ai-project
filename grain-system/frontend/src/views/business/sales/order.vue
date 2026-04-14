<template>
  <div class="sales-order-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>销售订单管理</span>
          <el-button type="primary" @click="handleCreate">新建订单</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="queryForm.keyword" placeholder="订单号/机构名称" clearable />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="queryForm.status" placeholder="请选择" clearable>
            <el-option label="草稿" :value="0" />
            <el-option label="待审核" :value="1" />
            <el-option label="待出库" :value="2" />
            <el-option label="部分出库" :value="3" />
            <el-option label="已出库" :value="4" />
            <el-option label="已完成" :value="5" />
            <el-option label="已驳回" :value="6" />
            <el-option label="已作废" :value="7" />
          </el-select>
        </el-form-item>
        <el-form-item label="收款状态">
          <el-select v-model="queryForm.paymentStatus" placeholder="请选择" clearable>
            <el-option label="未收款" :value="0" />
            <el-option label="部分收款" :value="1" />
            <el-option label="已收款" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="tableData" stripe>
        <el-table-column prop="orderNo" label="订单号" width="160" />
        <el-table-column prop="orgName" label="下游机构" width="150" />
        <el-table-column prop="grainType" label="粮食品种" width="100" />
        <el-table-column prop="grainGrade" label="等级" width="80" />
        <el-table-column prop="planWeight" label="计划重量" width="100" align="right" />
        <el-table-column prop="actualOutWeight" label="实际出库" width="100" align="right" />
        <el-table-column prop="actualPrice" label="单价" width="80" align="right" />
        <el-table-column prop="totalAmount" label="订单金额" width="120" align="right" />
        <el-table-column prop="receivedAmount" label="已收款" width="100" align="right" />
        <el-table-column prop="statusName" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentStatusName" label="收款状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getPaymentStatusType(row.paymentStatus)">{{ row.paymentStatusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">详情</el-button>
            <el-button link type="primary" size="small" @click="handleSubmit(row)" v-if="row.status === 0">提交</el-button>
            <el-button link type="primary" size="small" @click="handleAudit(row)" v-if="row.status === 1">审核</el-button>
            <el-button link type="success" size="small" @click="handleStartOutbound(row)" v-if="row.status === 2">开始出库</el-button>
            <el-button link type="success" size="small" @click="handleOutbound(row)" v-if="row.status === 3">继续出库</el-button>
            <el-button link type="warning" size="small" @click="handleReceive(row)" v-if="row.status === 4">收款</el-button>
            <el-button link type="danger" size="small" @click="handleVoid(row)" v-if="row.status !== 5 && row.status !== 7">作废</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="800px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="下游机构" prop="orgId">
          <el-select v-model="form.orgId" placeholder="请选择" filterable>
            <el-option
              v-for="org in orgOptions"
              :key="org.id"
              :label="org.orgName"
              :value="org.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="粮食品种" prop="grainId">
          <el-select v-model="form.grainId" placeholder="请选择">
            <el-option
              v-for="grain in grainOptions"
              :key="grain.id"
              :label="`${grain.grainType} - ${grain.grainGrade}`"
              :value="grain.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="储位" prop="positionId">
          <el-select v-model="form.positionId" placeholder="请选择">
            <el-option
              v-for="pos in positionOptions"
              :key="pos.id"
              :label="pos.positionName"
              :value="pos.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="计划重量" prop="planWeight">
          <el-input-number v-model="form.planWeight" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="实际单价" prop="actualPrice">
          <el-input-number v-model="form.actualPrice" :min="0" :precision="4" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="订单详情" width="900px" destroy-on-close>
      <el-descriptions :column="2" border v-if="detailData.id">
        <el-descriptions-item label="订单号">{{ detailData.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="下游机构">{{ detailData.orgName }}</el-descriptions-item>
        <el-descriptions-item label="粮食品种">{{ detailData.grainType }} - {{ detailData.grainGrade }}</el-descriptions-item>
        <el-descriptions-item label="储位">{{ detailData.positionName }}</el-descriptions-item>
        <el-descriptions-item label="计划重量">{{ detailData.planWeight }}</el-descriptions-item>
        <el-descriptions-item label="实际出库">{{ detailData.actualOutWeight }}</el-descriptions-item>
        <el-descriptions-item label="参考单价">{{ detailData.refPrice }}</el-descriptions-item>
        <el-descriptions-item label="实际单价">{{ detailData.actualPrice }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">{{ detailData.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="实际结算金额">{{ detailData.actualAmount }}</el-descriptions-item>
        <el-descriptions-item label="已收款">{{ detailData.receivedAmount }}</el-descriptions-item>
        <el-descriptions-item label="未收款">{{ detailData.unreceivedAmount }}</el-descriptions-item>
        <el-descriptions-item label="预占库存">{{ detailData.reservedStock }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="getStatusType(detailData.status)">{{ detailData.statusName }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="出库状态">
          <el-tag>{{ detailData.outStatusName }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="收款状态">
          <el-tag :type="getPaymentStatusType(detailData.paymentStatus)">{{ detailData.paymentStatusName }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建人">{{ detailData.createUserName }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="审核人">{{ detailData.auditUserName }}</el-descriptions-item>
        <el-descriptions-item label="审核时间">{{ detailData.auditTime }}</el-descriptions-item>
        <el-descriptions-item label="作废原因" v-if="detailData.voidReason">{{ detailData.voidReason }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSalesOrderList, getSalesOrder, createSalesOrder, auditSalesOrder, voidSalesOrder, submitSalesOrder, startOutbound, addOutbound, receivePayment } from '@/api/sales/order'
import { getGrainList, getPositionList } from '@/api/config'
import { getOrgList } from '@/api/org'

const loading = ref(false)
const tableData = ref([])
const queryForm = reactive({
  keyword: '',
  status: null,
  paymentStatus: null
})
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogTitle = ref('新建订单')
const formRef = ref()
const detailData = ref({})

const form = reactive({
  orgId: null,
  grainId: null,
  positionId: null,
  planWeight: 0,
  actualPrice: 0,
  remark: ''
})

const rules = {
  orgId: [{ required: true, message: '请选择下游机构', trigger: 'change' }],
  grainId: [{ required: true, message: '请选择粮食品种', trigger: 'change' }],
  positionId: [{ required: true, message: '请选择储位', trigger: 'change' }],
  planWeight: [{ required: true, message: '请输入计划重量', trigger: 'blur' }],
  actualPrice: [{ required: true, message: '请输入实际单价', trigger: 'blur' }]
}

const orgOptions = ref([])
const grainOptions = ref([])
const positionOptions = ref([])

onMounted(() => {
  loadData()
  loadOptions()
})

async function loadData() {
  loading.value = true
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
      ...queryForm
    }
    const res = await getSalesOrderList(params)
    tableData.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function loadOptions() {
  try {
    const [grainRes, orgRes, posRes] = await Promise.all([
      getGrainList({ status: 1 }),
      getOrgList(),
      getPositionList({ status: 1 })
    ])
    console.log('grainRes:', grainRes)
    console.log('orgRes:', orgRes)
    console.log('posRes:', posRes)
    grainOptions.value = grainRes.data?.records || grainRes.data || []
    orgOptions.value = orgRes.data?.records || orgRes.data || []
    positionOptions.value = posRes.data?.records || posRes.data || []
  } catch (e) {
    console.error('loadOptions error:', e)
  }
}

function handleQuery() {
  pagination.current = 1
  loadData()
}

function handleReset() {
  queryForm.keyword = ''
  queryForm.status = null
  queryForm.paymentStatus = null
  handleQuery()
}

function handleSizeChange() {
  loadData()
}

function handleCurrentChange() {
  loadData()
}

function handleCreate() {
  dialogTitle.value = '新建订单'
  form.orgId = null
  form.grainId = null
  form.positionId = null
  form.planWeight = 0
  form.actualPrice = 0
  form.remark = ''
  dialogVisible.value = true
  if (orgOptions.value.length === 0) {
    loadOptions()
  }
}

async function handleView(row) {
  try {
    const res = await getSalesOrder(row.id)
    detailData.value = res.data || {}
    detailVisible.value = true
  } catch (e) {
    console.error(e)
  }
}

async function handleAudit(row) {
  try {
    await ElMessageBox.confirm('确认审核通过该订单?', '审核确认', {
      type: 'warning'
    })
    await auditSalesOrder(row.id, 1, null)
    ElMessage.success('审核成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

async function handleSubmit(row) {
  try {
    await ElMessageBox.confirm('确认提交该订单?', '提交订单', {
      type: 'warning'
    })
    await submitSalesOrder(row.id)
    ElMessage.success('提交成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

async function handleStartOutbound(row) {
  try {
    await ElMessageBox.confirm('确认开始出库?', '开始出库', {
      type: 'warning'
    })
    await startOutbound(row.id)
    ElMessage.success('已开始出库')
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

async function handleOutbound(row) {
  try {
    const { value } = await ElMessageBox.prompt('请输入出库重量', '继续出库', {
      type: 'warning',
      inputPlaceholder: '请输入出库重量'
    })
    if (!value || parseFloat(value) <= 0) {
      ElMessage.warning('请输入有效的出库重量')
      return
    }
    await addOutbound(row.id, parseFloat(value))
    ElMessage.success('已添加出库单')
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

async function handleReceive(row) {
  try {
    const { value: amount } = await ElMessageBox.prompt('请输入收款金额', '登记收款', {
      type: 'warning',
      inputPlaceholder: '请输入收款金额'
    })
    if (!amount || parseFloat(amount) <= 0) {
      ElMessage.warning('请输入有效的收款金额')
      return
    }
    await receivePayment(row.id, {
      receiveAmount: parseFloat(amount),
      receiveMethod: 1
    })
    ElMessage.success('收款成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

async function handleVoid(row) {
  try {
    const { value } = await ElMessageBox.prompt('请输入作废原因', '作废订单', {
      type: 'warning'
    })
    await voidSalesOrder(row.id, value)
    ElMessage.success('作废成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

async function handleCreateSubmit() {
  try {
    await formRef.value.validate()
    await createSalesOrder({
      orgId: form.orgId,
      items: [{
        grainId: form.grainId,
        positionId: form.positionId,
        planWeight: form.planWeight,
        actualPrice: form.actualPrice
      }],
      remark: form.remark
    })
    ElMessage.success('创建成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    console.error(e)
  }
}

function getStatusType(status) {
  const types = { 0: 'info', 1: 'warning', 2: 'primary', 3: 'warning', 4: 'success', 5: 'success', 6: 'danger', 7: 'danger' }
  return types[status] || 'info'
}

function getPaymentStatusType(status) {
  const types = { 0: 'danger', 1: 'warning', 2: 'success' }
  return types[status] || 'info'
}
</script>

<style scoped>
.sales-order-container {
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
