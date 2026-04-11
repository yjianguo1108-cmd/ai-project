<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="付款单号/收购单号" clearable style="width:180px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
        <el-form-item style="float:right">
          <el-button type="primary" @click="handleAdd">新增付款</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top:12px">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column prop="paymentNo" label="付款单号" width="150" />
        <el-table-column prop="orderNo" label="收购单号" width="130" />
        <el-table-column label="订单金额" width="100" align="right">
          <template #default="{ row }">¥{{ row.orderTotalAmount }}</template>
        </el-table-column>
        <el-table-column label="已付金额" width="100" align="right">
          <template #default="{ row }">¥{{ row.orderPaidAmount }}</template>
        </el-table-column>
        <el-table-column label="未付金额" width="100" align="right">
          <template #default="{ row }">
            <span :style="{ color: row.orderUnpaidAmount > 0 ? '#E6A23C' : '#67C23A' }">¥{{ row.orderUnpaidAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="本次付款" width="100" align="right">
          <template #default="{ row }"><strong>¥{{ row.paymentAmount }}</strong></template>
        </el-table-column>
        <el-table-column prop="paymentMethodName" label="付款方式" width="90" align="center" />
        <el-table-column prop="recipientName" label="收款人" width="100" />
        <el-table-column label="付款时间" width="160">
          <template #default="{ row }">{{ row.paymentTime ? dayjs(row.paymentTime).format('YYYY-MM-DD HH:mm') : '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">详情</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" title="新增付款" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="收购单" prop="orderId">
          <el-select v-model="form.orderId" placeholder="请选择收购单" style="width: 100%" @change="onOrderChange">
            <el-option v-for="item in orderOptions" :key="item.id" :label="item.orderNo" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="付款金额" prop="paymentAmount">
          <el-input-number v-model="form.paymentAmount" :min="0" :precision="2" :step="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="付款方式" prop="paymentMethod">
          <el-select v-model="form.paymentMethod" placeholder="请选择付款方式" style="width: 100%">
            <el-option label="现金" :value="1" />
            <el-option label="银行转账" :value="2" />
            <el-option label="微信支付" :value="3" />
            <el-option label="支付宝" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="收款人" prop="recipientName">
          <el-input v-model="form.recipientName" placeholder="请输入收款人姓名" />
        </el-form-item>
        <el-form-item label="付款账号">
          <el-input v-model="form.paymentAccount" placeholder="可选" />
        </el-form-item>
        <el-form-item label="付款时间">
          <el-date-picker v-model="form.paymentTime" type="datetime" placeholder="选择付款时间" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
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

    <el-dialog v-model="detailDialogVisible" title="付款详情" width="550px">
      <el-descriptions :column="2" border v-if="detailData">
        <el-descriptions-item label="付款单号">{{ detailData.paymentNo }}</el-descriptions-item>
        <el-descriptions-item label="付款方式">{{ detailData.paymentMethodName }}</el-descriptions-item>
        <el-descriptions-item label="收购单号">{{ detailData.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="收款人">{{ detailData.recipientName }}</el-descriptions-item>
        <el-descriptions-item label="本次付款"><strong style="color:#E6A23C">¥{{ detailData.paymentAmount }}</strong></el-descriptions-item>
        <el-descriptions-item label="付款账号">{{ detailData.paymentAccount || '-' }}</el-descriptions-item>
        <el-descriptions-item label="付款时间">{{ detailData.paymentTime ? dayjs(detailData.paymentTime).format('YYYY-MM-DD HH:mm') : '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建人">{{ detailData.createUserName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailData.remark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime ? dayjs(detailData.createTime).format('YYYY-MM-DD HH:mm') : '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { getPaymentList, getPayment, createPayment, deletePayment } from '@/api/purchase/payment'
import { getOrderList } from '@/api/purchase/order'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const formRef = ref(null)
const searchForm = reactive({ keyword: '' })
const pagination = reactive({ page: 1, size: 20, total: 0 })
const form = reactive({ orderId: null, paymentAmount: 0, paymentMethod: 1, paymentAccount: '', recipientName: '', paymentTime: null, remark: '' })
const rules = {
  orderId: [{ required: true, message: '请选择收购单', trigger: 'change' }],
  paymentAmount: [{ required: true, message: '请输入付款金额', trigger: 'blur' }],
  paymentMethod: [{ required: true, message: '请选择付款方式', trigger: 'change' }],
  recipientName: [{ required: true, message: '请输入收款人', trigger: 'blur' }]
}

const detailDialogVisible = ref(false)
const detailData = ref(null)
const orderOptions = ref([])

async function fetchData() {
  loading.value = true
  try {
    const res = await getPaymentList({
      page: pagination.page,
      size: pagination.size,
      keyword: searchForm.keyword || undefined
    })
    const d = res.data
    tableData.value = d?.records ?? (Array.isArray(d) ? d : [])
    pagination.total = d?.total ?? tableData.value.length
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

async function loadOrderOptions() {
  try {
    const res = await getOrderList({ status: 2, size: 100 })
    orderOptions.value = res.data?.records ?? []
  } catch (e) { console.error(e) }
}

function onOrderChange(orderId) {
  const order = orderOptions.value.find(o => o.id === orderId)
  if (order) {
    form.recipientName = order.farmerName || ''
  }
}

function handleSearch() { pagination.page = 1; fetchData() }
function handleReset() { searchForm.keyword = ''; pagination.page = 1; fetchData() }

function handleAdd() {
  Object.assign(form, { orderId: null, paymentAmount: 0, paymentMethod: 1, paymentAccount: '', recipientName: '', paymentTime: null, remark: '' })
  dialogVisible.value = true
  loadOrderOptions()
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await createPayment(form)
    ElMessage.success('创建成功')
    dialogVisible.value = false
    fetchData()
  } catch (e) { ElMessage.error(e?.response?.data?.msg || '创建失败') }
  finally { submitting.value = false }
}

async function handleView(row) {
  try {
    const res = await getPayment(row.id)
    detailData.value = res.data
    detailDialogVisible.value = true
  } catch (e) { ElMessage.error('获取详情失败') }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除该付款记录吗？', '提示', { type: 'warning' })
    await deletePayment(row.id)
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