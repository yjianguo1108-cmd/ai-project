<template>
  <div class="out-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>出库管理</span>
          <el-button type="primary" @click="handleCreate">新建出库单</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="粮食品种">
          <el-select v-model="queryForm.grainId" placeholder="请选择" clearable filterable>
            <el-option
              v-for="grain in grainOptions"
              :key="grain.id"
              :label="grain.grainType + ' - ' + grain.grainGrade"
              :value="grain.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="储位">
          <el-select v-model="queryForm.positionId" placeholder="请选择" clearable filterable>
            <el-option
              v-for="pos in positionOptions"
              :key="pos.id"
              :label="pos.positionName"
              :value="pos.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择" clearable>
            <el-option label="待确认" :value="0" />
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
        <el-table-column prop="outboundNo" label="出库单号" width="150" />
        <el-table-column prop="grainType" label="粮食品种" width="120" />
        <el-table-column prop="grainGrade" label="等级" width="80" />
        <el-table-column prop="positionName" label="储位" width="120" />
        <el-table-column prop="planWeight" label="计划重量" width="100" align="right" />
        <el-table-column prop="actualWeight" label="实际重量" width="100" align="right">
          <template #default="{ row }">
            {{ row.actualWeight || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="deviationRate" label="偏差率" width="80" align="center">
          <template #default="{ row }">
            {{ row.deviationRate ? row.deviationRate + '%' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="outboundTypeName" label="出库类型" width="100" />
        <el-table-column prop="outboundReason" label="出库原因" width="150" show-overflow-tooltip />
        <el-table-column prop="statusName" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">详情</el-button>
            <el-button link type="success" size="small" @click="handleConfirm(row)" v-if="row.status === 0">确认</el-button>
            <el-button link type="danger" size="small" @click="handleVoid(row)" v-if="row.status === 0 || row.status === 1">作废</el-button>
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

    <el-dialog v-model="createVisible" title="新建出库单" width="600px" destroy-on-close>
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="120px">
        <el-form-item label="粮食品种" prop="grainId">
          <el-select v-model="createForm.grainId" placeholder="请选择" filterable style="width: 100%">
            <el-option
              v-for="grain in grainOptions"
              :key="grain.id"
              :label="grain.grainType + ' - ' + grain.grainGrade"
              :value="grain.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="储位" prop="positionId">
          <el-select v-model="createForm.positionId" placeholder="请选择" filterable style="width: 100%">
            <el-option
              v-for="pos in positionOptions"
              :key="pos.id"
              :label="pos.positionName"
              :value="pos.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="出库重量" prop="outWeight">
          <el-input-number v-model="createForm.outWeight" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="出库原因">
          <el-input v-model="createForm.outboundReason" type="textarea" :rows="3" placeholder="请输入出库原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="出库单详情" width="700px" destroy-on-close>
      <el-descriptions :column="2" border v-if="detailData.id">
        <el-descriptions-item label="出库单号">{{ detailData.outboundNo }}</el-descriptions-item>
        <el-descriptions-item label="出库类型">{{ detailData.outboundTypeName }}</el-descriptions-item>
        <el-descriptions-item label="粮食品种">{{ detailData.grainType }} - {{ detailData.grainGrade }}</el-descriptions-item>
        <el-descriptions-item label="储位">{{ detailData.positionName }}</el-descriptions-item>
        <el-descriptions-item label="计划重量">{{ detailData.planWeight }}</el-descriptions-item>
        <el-descriptions-item label="实际重量">{{ detailData.actualWeight || '-' }}</el-descriptions-item>
        <el-descriptions-item label="偏差率">{{ detailData.deviationRate ? detailData.deviationRate + '%' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(detailData.status)">{{ detailData.statusName }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="出库原因">{{ detailData.outboundReason || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="确认时间">{{ detailData.confirmTime || '-' }}</el-descriptions-item>
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
import { getOutboundPage, getOutboundById, createOutbound, confirmOutbound, voidOutbound } from '@/api/warehouse/out'
import { getGrainList, getPositionList } from '@/api/config'

const loading = ref(false)
const tableData = ref([])
const queryForm = reactive({
  grainId: null,
  positionId: null,
  dateFrom: null,
  dateTo: null,
  status: null
})
const dateRange = ref(null)
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const grainOptions = ref([])
const positionOptions = ref([])

const createVisible = ref(false)
const createFormRef = ref()
const createForm = reactive({
  grainId: null,
  positionId: null,
  outWeight: 0,
  outboundReason: ''
})
const createRules = {
  grainId: [{ required: true, message: '请选择粮食品种', trigger: 'change' }],
  positionId: [{ required: true, message: '请选择储位', trigger: 'change' }],
  outWeight: [{ required: true, message: '请输入出库重量', trigger: 'blur' }]
}

const detailVisible = ref(false)
const detailData = ref({})

const statusMap = { 0: '待确认', 1: '执行中', 2: '已完成', 3: '已作废' }
const outboundTypeMap = { 0: '销售出库', 1: '调拨出库', 2: '应急出库' }

onMounted(() => {
  loadOptions()
  loadData()
})

async function loadOptions() {
  try {
    const [grainRes, posRes] = await Promise.all([
      getGrainList({ status: 1 }),
      getPositionList({ status: 1 })
    ])
    grainOptions.value = grainRes.data || []
    positionOptions.value = posRes.data || []
  } catch (e) {
    console.error(e)
  }
}

async function loadData() {
  loading.value = true
  try {
    if (dateRange.value && dateRange.value.length === 2) {
      queryForm.dateFrom = dateRange.value[0]
      queryForm.dateTo = dateRange.value[1]
    } else {
      queryForm.dateFrom = null
      queryForm.dateTo = null
    }
    const params = {
      page: pagination.current,
      size: pagination.size,
      grainId: queryForm.grainId,
      positionId: queryForm.positionId,
      dateFrom: queryForm.dateFrom,
      dateTo: queryForm.dateTo,
      status: queryForm.status
    }
    const res = await getOutboundPage(params)
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
  queryForm.grainId = null
  queryForm.positionId = null
  queryForm.status = null
  dateRange.value = null
  handleQuery()
}

function handleSizeChange() {
  loadData()
}

function handleCurrentChange() {
  loadData()
}

function getStatusType(status) {
  const types = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }
  return types[status] || 'info'
}

function handleCreate() {
  createForm.grainId = null
  createForm.positionId = null
  createForm.outWeight = 0
  createForm.outboundReason = ''
  createVisible.value = true
}

async function handleCreateSubmit() {
  try {
    await createFormRef.value.validate()
    await createOutbound({
      grainId: createForm.grainId,
      positionId: createForm.positionId,
      outWeight: createForm.outWeight,
      outboundReason: createForm.outboundReason
    })
    ElMessage.success('创建成功')
    createVisible.value = false
    loadData()
  } catch (e) {
    console.error(e)
  }
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

async function handleConfirm(row) {
  try {
    await ElMessageBox.confirm('确认出库？', '确认出库', { type: 'warning' })
    await confirmOutbound(row.id)
    ElMessage.success('确认成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

async function handleVoid(row) {
  try {
    await ElMessageBox.prompt('请输入作废原因', '作废出库单', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '请输入作废原因'
    }).then(async ({ value }) => {
      await voidOutbound(row.id, value)
      ElMessage.success('作废成功')
      loadData()
    })
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}
</script>

<style scoped>
.out-container {
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
