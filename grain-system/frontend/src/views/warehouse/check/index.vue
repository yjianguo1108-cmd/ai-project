<template>
  <div class="check-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>盘点管理</span>
          <el-button type="primary" @click="handleCreate">新建盘点计划</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="盘点类型">
          <el-select v-model="queryForm.checkType" placeholder="请选择" clearable>
            <el-option label="全量盘点" :value="0" />
            <el-option label="指定品种" :value="1" />
            <el-option label="指定储位" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择" clearable>
            <el-option label="草稿" :value="0" />
            <el-option label="进行中" :value="1" />
            <el-option label="待审核" :value="2" />
            <el-option label="差异处理中" :value="3" />
            <el-option label="已完成" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="tableData" stripe>
        <el-table-column prop="checkNo" label="盘点编号" width="150" />
        <el-table-column prop="checkTypeName" label="盘点类型" width="100" />
        <el-table-column prop="totalItems" label="总条数" width="80" align="center" />
        <el-table-column prop="checkedItems" label="已实盘" width="80" align="center" />
        <el-table-column prop="matchRate" label="相符率" width="80" align="center">
          <template #default="{ row }">
            {{ row.matchRate ? row.matchRate + '%' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="statusName" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="deadline" label="截止日期" width="160" />
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">详情</el-button>
            <el-button link type="success" size="small" @click="handlePublish(row)" v-if="row.status === 0">发布</el-button>
            <el-button link type="warning" size="small" @click="handleDetail(row)" v-if="row.status === 1">录入实盘</el-button>
            <el-button link type="primary" size="small" @click="handleSubmit(row)" v-if="row.status === 1">提交</el-button>
            <el-button link type="danger" size="small" @click="handleAudit(row)" v-if="row.status === 2">审核</el-button>
            <el-button link type="info" size="small" @click="handleReport(row)" v-if="row.status === 4">查看报告</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)" v-if="row.status === 0">删除</el-button>
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

    <el-dialog v-model="createVisible" title="新建盘点计划" width="600px" destroy-on-close>
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="120px">
        <el-form-item label="盘点类型" prop="checkType">
          <el-select v-model="createForm.checkType" placeholder="请选择">
            <el-option label="全量盘点" :value="0" />
            <el-option label="指定品种" :value="1" />
            <el-option label="指定储位" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="粮食品种" prop="grainIds">
          <el-select v-model="createForm.grainIds" multiple placeholder="请选择" filterable style="width: 100%">
            <el-option
              v-for="grain in grainOptions"
              :key="grain.id"
              :label="grain.grainType + ' - ' + grain.grainGrade"
              :value="grain.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="储位" prop="positionIds">
          <el-select v-model="createForm.positionIds" multiple placeholder="请选择" filterable style="width: 100%">
            <el-option
              v-for="pos in positionOptions"
              :key="pos.id"
              :label="pos.positionName"
              :value="pos.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="截止日期" prop="deadline">
          <el-date-picker v-model="createForm.deadline" type="datetime" placeholder="选择日期时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="操作员" prop="operatorIds">
          <el-select v-model="createForm.operatorIds" multiple placeholder="请选择" style="width: 100%">
            <el-option
              v-for="user in userOptions"
              :key="user.id"
              :label="user.realName || user.username"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="盘点明细" width="900px" destroy-on-close>
      <el-table v-loading="detailLoading" :data="detailData" stripe @row-click="handleRowClick">
        <el-table-column prop="grainType" label="粮食品种" width="120" />
        <el-table-column prop="grainGrade" label="等级" width="80" />
        <el-table-column prop="positionName" label="储位" width="120" />
        <el-table-column prop="bookQty" label="账面数量" width="100" align="right" />
        <el-table-column prop="actualQty" label="实盘数量" width="100" align="right">
          <template #default="{ row }">
            <span v-if="row.actualQty != null">{{ row.actualQty }}</span>
            <el-tag v-else type="warning">待录入</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="diffQty" label="差异量" width="100" align="right">
          <template #default="{ row }">
            <span :class="getDiffClass(row.diffQty)">{{ row.diffQty || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="diffRate" label="差异率" width="80" align="center">
          <template #default="{ row }">
            {{ row.diffRate ? row.diffRate + '%' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="statusName" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.actualQty != null ? 'success' : 'info'">{{ row.actualQty != null ? '已录入' : '待录入' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleRecord(row)" :disabled="row.actualQty != null">录入</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="auditVisible" title="审核盘点结果" width="900px" destroy-on-close>
      <el-table v-loading="auditLoading" :data="auditData" stripe>
        <el-table-column prop="grainType" label="粮食品种" width="120" />
        <el-table-column prop="grainGrade" label="等级" width="80" />
        <el-table-column prop="positionName" label="储位" width="120" />
        <el-table-column prop="bookQty" label="账面数量" width="100" align="right" />
        <el-table-column prop="actualQty" label="实盘数量" width="100" align="right" />
        <el-table-column prop="diffQty" label="差异量" width="100" align="right">
          <template #default="{ row }">
            <span :class="getDiffClass(row.diffQty)">{{ row.diffQty || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="diffRate" label="差异率" width="80" align="center">
          <template #default="{ row }">
            {{ row.diffRate ? row.diffRate + '%' : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="处理方式" width="150">
          <template #default="{ row }">
            <el-select v-model="row.handleType" placeholder="选择处理方式" style="width: 100%">
              <el-option label="调账" :value="0" />
              <el-option label="重新盘点" :value="1" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="差异原因" width="150">
          <template #default="{ row }">
            <el-input v-model="row.adjustReason" placeholder="填写原因" />
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 20px; text-align: center;">
        <el-button type="primary" @click="handleAuditPass">审核通过</el-button>
        <el-button @click="handleAuditReject">驳回重盘</el-button>
      </div>
    </el-dialog>

    <el-dialog v-model="recordVisible" title="录入实盘数量" width="500px" destroy-on-close>
      <el-form ref="recordFormRef" :model="recordForm" :rules="recordRules" label-width="120px">
        <el-form-item label="粮食品种">
          <span>{{ recordForm.grainType }} - {{ recordForm.grainGrade }}</span>
        </el-form-item>
        <el-form-item label="储位">
          <span>{{ recordForm.positionName }}</span>
        </el-form-item>
        <el-form-item label="账面数量">
          <span>{{ recordForm.bookQty }}</span>
        </el-form-item>
        <el-form-item label="实盘数量" prop="actualQty">
          <el-input-number v-model="recordForm.actualQty" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="recordVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRecordSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCheckPlanPage, getCheckPlanById, createCheckPlan, publishCheckPlan, getCheckDetailList, updateCheckDetail, submitCheckPlan, auditCheckPlan, getCheckReport, deleteCheckPlan } from '@/api/warehouse/check'
import { getGrainList, getPositionList } from '@/api/config'
import { getUserList } from '@/api/user'

const loading = ref(false)
const tableData = ref([])
const queryForm = reactive({
  checkType: null,
  status: null
})
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const grainOptions = ref([])
const positionOptions = ref([])
const userOptions = ref([])

const createVisible = ref(false)
const createFormRef = ref()
const createForm = reactive({
  checkType: 0,
  grainIds: [],
  positionIds: [],
  deadline: null,
  operatorIds: []
})
const createRules = {
  checkType: [{ required: true, message: '请选择盘点类型', trigger: 'change' }],
  grainIds: [{ required: true, message: '请选择粮食品种', trigger: 'change' }],
  positionIds: [{ required: true, message: '请选择储位', trigger: 'change' }],
  deadline: [{ required: true, message: '请选择截止日期', trigger: 'change' }],
  operatorIds: [{ required: true, message: '请选择操作员', trigger: 'change' }]
}

const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref([])
const currentPlanId = ref(null)

const auditVisible = ref(false)
const auditLoading = ref(false)
const auditData = ref([])

const recordVisible = ref(false)
const recordFormRef = ref()
const recordForm = reactive({
  id: null,
  grainType: '',
  grainGrade: '',
  positionName: '',
  bookQty: 0,
  actualQty: 0
})
const recordRules = {
  actualQty: [{ required: true, message: '请输入实盘数量', trigger: 'blur' }]
}

const statusMap = { 0: '草稿', 1: '进行中', 2: '待审核', 3: '差异处理中', 4: '已完成' }
const checkTypeMap = { 0: '全量盘点', 1: '指定品种', 2: '指定储位' }

onMounted(() => {
  loadData()
  loadOptions()
})

async function loadOptions() {
  try {
    const [grainRes, posRes, userRes] = await Promise.all([
      getGrainList({ status: 1 }),
      getPositionList({ status: 1 }),
      getUserList()
    ])
    grainOptions.value = grainRes.data || []
    positionOptions.value = posRes.data || []
    userOptions.value = userRes.data?.records || userRes.data || []
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
      checkType: queryForm.checkType,
      status: queryForm.status
    }
    const res = await getCheckPlanPage(params)
    tableData.value = (res.data?.records || res.data || []).map(item => ({
      ...item,
      statusName: statusMap[item.status] || '未知',
      checkTypeName: checkTypeMap[item.checkType] || '未知'
    }))
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
  queryForm.checkType = null
  queryForm.status = null
  handleQuery()
}

function handleSizeChange() {
  loadData()
}

function handleCurrentChange() {
  loadData()
}

function getStatusType(status) {
  const types = { 0: 'info', 1: 'warning', 2: 'warning', 3: 'danger', 4: 'success' }
  return types[status] || 'info'
}

function getDiffClass(diffQty) {
  if (diffQty === null || diffQty === undefined) return ''
  if (diffQty > 0) return 'text-success'
  if (diffQty < 0) return 'text-danger'
  return ''
}

function handleCreate() {
  createForm.checkType = 0
  createForm.grainIds = []
  createForm.positionIds = []
  createForm.deadline = null
  createForm.operatorIds = []
  createVisible.value = true
}

async function handleCreateSubmit() {
  try {
    await createFormRef.value.validate()
    await createCheckPlan({
      checkType: createForm.checkType,
      grainIds: createForm.grainIds,
      positionIds: createForm.positionIds,
      deadline: createForm.deadline,
      operatorIds: createForm.operatorIds
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
    const res = await getCheckPlanById(row.id)
    currentPlanId.value = row.id
    detailData.value = res.data?.details || []
    detailVisible.value = true
  } catch (e) {
    console.error(e)
  }
}

async function handlePublish(row) {
  try {
    await ElMessageBox.confirm('确认发布该盘点计划？', '发布确认', { type: 'warning' })
    await publishCheckPlan(row.id)
    ElMessage.success('发布成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

async function handleDetail(row) {
  try {
    detailLoading.value = true
    currentPlanId.value = row.id
    const res = await getCheckDetailList(row.id)
    detailData.value = res.data || []
    detailVisible.value = true
  } catch (e) {
    console.error(e)
  } finally {
    detailLoading.value = false
  }
}

function handleRecord(row) {
  recordForm.id = row.id
  recordForm.grainType = row.grainType || ''
  recordForm.grainGrade = row.grainGrade || ''
  recordForm.positionName = row.positionName || ''
  recordForm.bookQty = row.bookQty || 0
  recordForm.actualQty = 0
  recordVisible.value = true
}

function handleRowClick(row) {
  if (row.actualQty != null) {
    ElMessage.warning('该明细已录入实盘数量')
    return
  }
  handleRecord(row)
}

async function handleSubmit(row) {
  try {
    await ElMessageBox.confirm('确认提交盘点结果？', '提交确认', { type: 'warning' })
    await submitCheckPlan(row.id)
    ElMessage.success('提交成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除该盘点计划？', '删除确认', { type: 'warning' })
    await deleteCheckPlan(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

async function handleAudit(row) {
  try {
    auditLoading.value = true
    currentPlanId.value = row.id
    const res = await getCheckDetailList(row.id)
    auditData.value = res.data || []
    auditVisible.value = true
  } catch (e) {
    console.error(e)
  } finally {
    auditLoading.value = false
  }
}

async function handleAuditPass() {
  try {
    await ElMessageBox.confirm('审核通过后，系统将自动修正库存数据，确认继续？', '审核确认', { type: 'warning' })
    const diffDetails = auditData.value.map(item => ({
      detailId: item.id,
      handleType: item.handleType === 0 ? 'adjust' : 'recheck',
      adjustReason: item.adjustReason || ''
    }))
    await auditCheckPlan(currentPlanId.value, { result: 'pass', diffDetails })
    ElMessage.success('审核成功')
    auditVisible.value = false
    loadData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

async function handleAuditReject() {
  try {
    await ElMessageBox.confirm('确认驳回该盘点计划，重新盘点？', '驳回确认', { type: 'warning' })
    await auditCheckPlan(currentPlanId.value, { result: 'diff', diffDetails: [] })
    ElMessage.success('已驳回')
    auditVisible.value = false
    loadData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

async function handleReport(row) {
  try {
    const res = await getCheckReport(row.id)
    if (res.data) {
      window.open(res.data, '_blank')
    } else {
      ElMessage.warning('暂无盘点报告')
    }
  } catch (e) {
    console.error(e)
  }
}
</script>

<style scoped>
.check-container {
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
.text-success {
  color: #67c23a;
}
.text-danger {
  color: #f56c6c;
}
</style>
