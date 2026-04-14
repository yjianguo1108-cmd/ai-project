<template>
  <div class="payment-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>销售收款管理</span>
        </div>
      </template>

      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="下游机构">
          <el-select v-model="queryForm.orgId" placeholder="请选择" clearable filterable>
            <el-option
              v-for="org in orgOptions"
              :key="org.id"
              :label="org.orgName"
              :value="org.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择" clearable>
            <el-option label="未收款" :value="0" />
            <el-option label="部分收款" :value="1" />
            <el-option label="已收款" :value="2" />
            <el-option label="已锁定" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="tableData" stripe>
        <el-table-column prop="paymentNo" label="收款单号" width="160" />
        <el-table-column prop="orderNo" label="关联订单号" width="160" />
        <el-table-column prop="orgName" label="下游机构" width="150" />
        <el-table-column prop="totalReceivable" label="应收总额" width="120" align="right" />
        <el-table-column prop="receivedAmount" label="已收金额" width="120" align="right" />
        <el-table-column prop="unreceivedAmount" label="未收金额" width="120" align="right" />
        <el-table-column prop="statusName" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isLocked" label="锁定" width="80">
          <template #default="{ row }">
            <el-tag :type="row.isLocked === 1 ? 'danger' : 'success'">{{ row.isLocked === 1 ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">详情</el-button>
            <el-button link type="warning" size="small" @click="handleReceive(row)" v-if="row.status !== 2 && row.status !== 3">收款</el-button>
            <el-button link type="primary" size="small" @click="handleRemind(row)" v-if="row.status !== 2">催款</el-button>
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

    <el-dialog v-model="detailVisible" title="收款单详情" width="700px" destroy-on-close>
      <el-descriptions :column="2" border v-if="detailData.id">
        <el-descriptions-item label="收款单号">{{ detailData.paymentNo }}</el-descriptions-item>
        <el-descriptions-item label="关联订单号">{{ detailData.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="下游机构">{{ detailData.orgName }}</el-descriptions-item>
        <el-descriptions-item label="应收总额">{{ detailData.totalReceivable }}</el-descriptions-item>
        <el-descriptions-item label="已收金额">{{ detailData.receivedAmount }}</el-descriptions-item>
        <el-descriptions-item label="未收金额">{{ detailData.unreceivedAmount }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(detailData.status)">{{ detailData.statusName }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="是否锁定">
          <el-tag :type="detailData.isLocked === 1 ? 'danger' : 'success'">{{ detailData.isLocked === 1 ? '是' : '否' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detailData.updateTime }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="receiveVisible" title="登记收款" width="500px" destroy-on-close>
      <el-form ref="receiveFormRef" :model="receiveForm" :rules="receiveRules" label-width="120px">
        <el-form-item label="收款金额" prop="receiveAmount">
          <el-input-number v-model="receiveForm.receiveAmount" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="收款方式" prop="receiveMethod">
          <el-select v-model="receiveForm.receiveMethod" placeholder="请选择">
            <el-option label="现金" :value="1" />
            <el-option label="银行转账" :value="2" />
            <el-option label="微信支付" :value="3" />
            <el-option label="支付宝" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="收款时间">
          <el-date-picker v-model="receiveForm.receiveTime" type="datetime" placeholder="选择时间" />
        </el-form-item>
        <el-form-item label="流水号">
          <el-input v-model="receiveForm.flowNo" placeholder="请输入流水号" />
        </el-form-item>
        <el-form-item label="银行信息">
          <el-input v-model="receiveForm.bankInfo" placeholder="请输入银行信息" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="receiveForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="receiveVisible = false">取消</el-button>
        <el-button type="primary" @click="handleReceiveSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSalesPaymentList, getSalesPayment, receivePayment, addRemind } from '@/api/sales/order'
import { getOrgList } from '@/api/org'

const loading = ref(false)
const tableData = ref([])
const queryForm = reactive({
  orgId: null,
  status: null
})
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const orgOptions = ref([])

const detailVisible = ref(false)
const detailData = ref({})

const receiveVisible = ref(false)
const receiveFormRef = ref()
const receiveForm = reactive({
  receiveAmount: 0,
  receiveMethod: 1,
  receiveTime: null,
  flowNo: '',
  bankInfo: '',
  remark: ''
})
const currentPaymentId = ref(null)

const receiveRules = {
  receiveAmount: [{ required: true, message: '请输入收款金额', trigger: 'blur' }],
  receiveMethod: [{ required: true, message: '请选择收款方式', trigger: 'change' }]
}

onMounted(() => {
  loadOrgs()
  loadData()
})

async function loadOrgs() {
  try {
    const res = await getOrgList()
    orgOptions.value = res.data?.records || res.data || []
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
      orgId: queryForm.orgId,
      status: queryForm.status
    }
    const res = await getSalesPaymentList(params)
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
  queryForm.orgId = null
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
    const res = await getSalesPayment(row.id)
    detailData.value = res.data || {}
    detailVisible.value = true
  } catch (e) {
    console.error(e)
  }
}

function handleReceive(row) {
  currentPaymentId.value = row.id
  receiveForm.receiveAmount = row.unreceivedAmount
  receiveForm.receiveMethod = 1
  receiveForm.receiveTime = new Date()
  receiveForm.flowNo = ''
  receiveForm.bankInfo = ''
  receiveForm.remark = ''
  receiveVisible.value = true
}

async function handleReceiveSubmit() {
  try {
    await receiveFormRef.value.validate()
    await receivePayment(currentPaymentId.value, receiveForm)
    ElMessage.success('收款成功')
    receiveVisible.value = false
    loadData()
  } catch (e) {
    console.error(e)
  }
}

async function handleRemind(row) {
  try {
    const { value } = await ElMessageBox.prompt('请输入催款内容', '登记催款记录', {
      type: 'warning',
      inputPlaceholder: '请输入催款内容'
    })
    if (!value) {
      ElMessage.warning('请输入催款内容')
      return
    }
    await addRemind(row.salesOrderId, value)
    ElMessage.success('催款记录已登记')
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

function getStatusType(status) {
  const types = { 0: 'danger', 1: 'warning', 2: 'success', 3: 'info' }
  return types[status] || 'info'
}
</script>

<style scoped>
.payment-container {
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
