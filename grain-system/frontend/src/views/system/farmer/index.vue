<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="姓名/手机号" clearable style="width:180px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.auditStatus" placeholder="全部" clearable style="width:120px">
            <el-option label="待审核" :value="0" />
            <el-option label="正常" :value="1" />
            <el-option label="冻结" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
        <el-form-item style="float:right">
          <el-button type="primary" @click="handleAdd">新增农户</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top:12px">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column prop="realName" label="姓名" min-width="100" />
        <el-table-column label="手机号" min-width="120">
          <template #default="{ row }">
            {{ row.phoneMask || row.phone || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="idCardMask" label="身份证号" min-width="160" />
        <el-table-column prop="bankCardMask" label="银行卡号" min-width="160" />
        <el-table-column prop="bankName" label="开户行" min-width="120" />
        <el-table-column prop="auditStatus" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.auditStatus === 0" type="warning" size="small">待审核</el-tag>
            <el-tag v-else-if="row.auditStatus === 1" type="success" size="small">正常</el-tag>
            <el-tag v-else type="danger" size="small">冻结</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">
            {{ row.createTime ? dayjs(row.createTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" link size="small" @click="handleAudit(row)">审核</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" @opened="onFormDialogOpened">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="关联用户" prop="userId">
          <el-select
            v-model="form.userId"
            filterable
            remote
            clearable
            reserve-keyword
            :placeholder="farmerRoleId ? '搜索姓名/用户名/手机号' : '未找到农户角色，请检查角色配置'"
            :remote-method="onUserRemoteSearch"
            :loading="userSelectLoading"
            :disabled="!farmerRoleId"
            style="width: 100%"
          >
            <el-option v-for="u in farmerUserOptions" :key="u.id" :label="formatUserOptionLabel(u)" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="身份证号">
          <el-input v-model="form.idCard" placeholder="身份证号（将加密存储）" />
        </el-form-item>
        <el-form-item label="银行卡号">
          <el-input v-model="form.bankCard" placeholder="银行卡号（将加密存储）" />
        </el-form-item>
        <el-form-item label="开户行">
          <el-input v-model="form.bankName" placeholder="如 中国农业银行" />
        </el-form-item>
        <el-form-item label="常售粮食品种">
          <el-input v-model="form.preferredGrainTypes" placeholder="JSON数组，如 [1,3]" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="auditDialogVisible" title="审核农户" width="400px">
      <el-form label-width="80px">
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.auditStatus">
            <el-radio :value="1">通过</el-radio>
            <el-radio :value="2">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAuditSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { getFarmerList, getFarmer, createFarmer, updateFarmer, deleteFarmer, updateFarmerAuditStatus } from '@/api/farmer'
import { getUserList, getUser } from '@/api/user'
import { getRoleListAll } from '@/api/role'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const searchForm = reactive({ keyword: '', auditStatus: null })
const pagination = reactive({ page: 1, size: 20, total: 0 })
const form = reactive({ id: null, userId: null, idCard: '', bankCard: '', bankName: '', preferredGrainTypes: '', remark: '' })
const auditDialogVisible = ref(false)
const auditForm = reactive({ id: null, auditStatus: 1 })
const rules = { userId: [{ required: true, message: '请选择关联用户', trigger: 'change' }] }

const farmerRoleId = ref(null)
const farmerUserOptions = ref([])
const userSelectLoading = ref(false)

function formatUserOptionLabel(u) {
  const name = u.realName || u.username || `用户#${u.id}`
  const phone = u.phoneMask || u.phone || ''
  return phone ? `${name}（${phone}）` : name
}

async function resolveFarmerRoleId() {
  if (farmerRoleId.value != null) return
  try {
    const res = await getRoleListAll()
    const roles = res.data || []
    farmerRoleId.value = roles.find((r) => r.roleCode === 'FARMER')?.id ?? null
  } catch (e) {
    console.error(e)
  }
}

/** 合并远程列表与当前已选项，保证编辑时回显 */
async function mergeFarmerUserOptions(keyword) {
  if (!farmerRoleId.value) return
  userSelectLoading.value = true
  try {
    const res = await getUserList({
      page: 1,
      size: 100,
      roleId: farmerRoleId.value,
      keyword: keyword || undefined,
      status: 1
    })
    const records = res.data?.records ?? []
    const map = new Map()
    farmerUserOptions.value.forEach((u) => map.set(u.id, u))
    records.forEach((u) => map.set(u.id, u))
    if (form.userId != null && !map.has(form.userId)) {
      try {
        const ur = await getUser(form.userId)
        const ud = ur.data
        if (ud) map.set(ud.id, ud)
      } catch (_) {
        /* 详情失败时保留下拉中可能已有的占位项 */
      }
    }
    farmerUserOptions.value = Array.from(map.values())
  } finally {
    userSelectLoading.value = false
  }
}

function onUserRemoteSearch(query) {
  mergeFarmerUserOptions(query)
}

async function onFormDialogOpened() {
  await resolveFarmerRoleId()
  await mergeFarmerUserOptions('')
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getFarmerList({ page: pagination.page, size: pagination.size, keyword: searchForm.keyword || undefined, auditStatus: searchForm.auditStatus || undefined })
    const d = res.data
    tableData.value = d?.records ?? (Array.isArray(d) ? d : [])
    pagination.total = d?.total ?? tableData.value.length
  } catch (e) { console.error(e) } finally { loading.value = false }
}
function handleSearch() { pagination.page = 1; fetchData() }
function handleReset() { searchForm.keyword = ''; searchForm.auditStatus = null; pagination.page = 1; fetchData() }
function handleAdd() {
  farmerUserOptions.value = []
  Object.assign(form, { id: null, userId: null, idCard: '', bankCard: '', bankName: '', preferredGrainTypes: '', remark: '' })
  dialogTitle.value = '新增农户'
  dialogVisible.value = true
}
async function handleEdit(row) {
  try {
    const res = await getFarmer(row.id)
    const d = res.data
    Object.assign(form, {
      id: d.id,
      userId: d.userId,
      idCard: d.idCard,
      bankCard: d.bankCard,
      bankName: d.bankName,
      preferredGrainTypes: d.preferredGrainTypes,
      remark: d.remark
    })
    farmerUserOptions.value =
      d.userId != null
        ? [
            {
              id: d.userId,
              username: '',
              realName: d.realName || '',
              phone: d.phone,
              phoneMask: d.phoneMask || ''
            }
          ]
        : []
    dialogTitle.value = '编辑农户'
    dialogVisible.value = true
  } catch (e) {
    console.error(e)
    ElMessage.error('获取农户详情失败')
  }
}
async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (form.id) { await updateFarmer(form.id, form); ElMessage.success('更新成功') }
    else { await createFarmer(form); ElMessage.success('创建成功') }
    dialogVisible.value = false
    fetchData()
  } catch (e) { ElMessage.error(e?.response?.data?.msg || '操作失败') } finally { submitting.value = false }
}
async function handleDelete(row) {
  try { await ElMessageBox.confirm(`确定删除农户「${row.realName}」？`, '提示', { type: 'warning' }); await deleteFarmer(row.id); ElMessage.success('删除成功'); fetchData() }
  catch (e) { if (e !== 'cancel') ElMessage.error('删除失败') }
}
function handleAudit(row) { auditForm.id = row.id; auditForm.auditStatus = 1; auditDialogVisible.value = true }
async function handleAuditSubmit() {
  try {
    await updateFarmerAuditStatus(auditForm.id, auditForm.auditStatus)
    ElMessage.success(auditForm.auditStatus === 1 ? '审核通过' : '已驳回')
  } catch (e) {
    ElMessage.error(e?.response?.data?.msg || '审核失败')
  }
  auditDialogVisible.value = false
  fetchData()
}
onMounted(() => {
  resolveFarmerRoleId()
  fetchData()
})
</script>

<style scoped>.page-container { padding: 20px; }</style>
