<template>
  <div class="user-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="handleAdd">新增用户</el-button>
        </div>
      </template>

      <div class="search-form">
        <el-form :inline="true" :model="queryParams">
          <el-form-item label="关键字">
            <el-input v-model="queryParams.keyword" placeholder="用户名/姓名/手机号" clearable @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="角色">
            <el-select v-model="queryParams.roleId" placeholder="选择角色" clearable style="width: 150px">
              <el-option v-for="item in roleOptions" :key="item.id" :label="item.roleName" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="queryParams.status" placeholder="选择状态" clearable style="width: 120px">
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleQuery">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="真实姓名" />
        <el-table-column label="手机号">
          <template #default="{ row }">
            {{ row.phoneMask || row.phone }}
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ row.createTime ? dayjs(row.createTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="warning" link size="small" @click="handleAssignRoles(row)">分配角色</el-button>
            <el-button type="info" link size="small" @click="handleResetPwd(row)">重置密码</el-button>
            <el-button :type="row.status === 1 ? 'danger' : 'success'" link size="small" @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 20px; justify-content: flex-end;"
        @change="fetchData"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!form.id">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="角色" prop="roleIds">
          <el-select v-model="form.roleIds" multiple placeholder="请选择角色" style="width: 100%">
            <el-option
              v-for="item in roleOptions"
              :key="item.id"
              :label="item.roleName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="roleDialogVisible" title="分配角色" width="400px">
      <el-form label-width="80px">
        <el-form-item label="用户">
          <span>{{ currentUser?.username }}</span>
        </el-form-item>
        <el-form-item label="角色" prop="roleIds">
          <el-select v-model="roleForm.roleIds" multiple placeholder="请选择角色" style="width: 100%">
            <el-option v-for="item in roleOptions" :key="item.id" :label="item.roleName" :value="item.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRoleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="resetPwdDialogVisible" title="重置密码" width="400px">
      <el-form label-width="80px">
        <el-form-item label="用户">
          <span>{{ currentUser?.username }}</span>
        </el-form-item>
        <el-form-item label="新密码" prop="password">
          <el-input v-model="resetPwdForm.password" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetPwdDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleResetPwdSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { getUserList, getUser, createUser, updateUser, deleteUser, resetPassword, assignRoles, updateStatus } from '@/api/user'
import { getRoleListAll } from '@/api/role'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const roleOptions = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)

const roleDialogVisible = ref(false)
const currentUser = ref(null)
const roleForm = reactive({ roleIds: [] })

const resetPwdDialogVisible = ref(false)
const resetPwdForm = reactive({ password: '' })

const queryParams = reactive({
  keyword: '',
  roleId: null,
  status: null
})

const pagination = reactive({ page: 1, size: 10, total: 0 })
const form = reactive({ id: null, username: '', realName: '', phone: '', email: '', password: '', status: 1, roleIds: [] })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  roleIds: [{ required: true, message: '请至少选择一个角色', trigger: 'change' }]
}

async function fetchRoles() {
  try {
    const res = await getRoleListAll()
    roleOptions.value = res.data || []
  } catch (e) { console.error(e) }
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getUserList({ 
      page: pagination.page, 
      size: pagination.size,
      ...queryParams
    })
    tableData.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (e) { console.error(e) }
  loading.value = false
}

function handleQuery() {
  pagination.page = 1
  fetchData()
}

function resetQuery() {
  Object.assign(queryParams, {
    keyword: '',
    roleId: null,
    status: null
  })
  handleQuery()
}

onMounted(() => {
  fetchData()
  fetchRoles()
})

function handleAdd() {
  Object.assign(form, { id: null, username: '', realName: '', phone: '', email: '', password: '', status: 1, roleIds: [] })
  dialogTitle.value = '新增用户'
  dialogVisible.value = true
}

async function handleEdit(row) {
  try {
    const res = await getUser(row.id)
    const userData = res.data
    Object.assign(form, {
      ...userData,
      password: '',
      roleIds: userData.roles ? userData.roles.map(r => r.id) : []
    })
    dialogTitle.value = '编辑用户'
    dialogVisible.value = true
  } catch (e) {
    console.error(e)
    ElMessage.error('获取用户详情失败')
  }
}

async function handleAssignRoles(row) {
  try {
    const res = await getUser(row.id)
    const userData = res.data
    currentUser.value = userData
    roleForm.roleIds = userData.roles ? userData.roles.map(r => r.id) : []
    roleDialogVisible.value = true
  } catch (e) {
    console.error(e)
    ElMessage.error('获取用户信息失败')
  }
}

async function handleRoleSubmit() {
  if (!roleForm.roleIds || roleForm.roleIds.length === 0) {
    ElMessage.warning('请至少选择一个角色')
    return
  }
  try {
    await assignRoles(currentUser.value.id, roleForm.roleIds)
    ElMessage.success('角色分配成功')
    roleDialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || '角色分配失败')
  }
}

async function handleResetPwd(row) {
  currentUser.value = row
  resetPwdForm.password = ''
  resetPwdDialogVisible.value = true
}

async function handleResetPwdSubmit() {
  if (!resetPwdForm.password || resetPwdForm.password.length < 6) {
    ElMessage.warning('密码长度不能少于6位')
    return
  }
  try {
    await resetPassword(currentUser.value.id, resetPwdForm.password)
    ElMessage.success('密码重置成功')
    resetPwdDialogVisible.value = false
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || '密码重置失败')
  }
}

async function handleToggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确定要${action}用户「${row.username}」吗？`, '提示', { type: 'warning' })
    await updateStatus(row.id, newStatus)
    ElMessage.success(`${action}成功`)
    fetchData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.response?.data?.msg || `${action}失败`)
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？', '提示', { type: 'warning' })
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) { if (e !== 'cancel') ElMessage.error('删除失败') }
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (form.id) {
          await updateUser(form.id, form)
          ElMessage.success('更新成功')
        } else {
          console.log('提交的新增用户信息:', form)
          await createUser(form)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        fetchData()
      } catch (e) { 
        console.error('提交失败详情:', e)
        ElMessage.error(e.response?.data?.msg || '操作失败') 
      }
    }
  })
}
</script>

<style scoped>
.user-manage { padding: 0px; }
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.search-form {
  margin-bottom: 20px;
  background: #fdfdfd;
  padding: 15px 15px 0;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}
</style>
