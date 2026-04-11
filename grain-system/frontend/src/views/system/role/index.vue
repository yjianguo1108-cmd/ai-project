<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="角色名称/编码" clearable style="width:200px" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
        <el-form-item style="float:right">
          <el-button type="primary" @click="handleAdd">新增角色</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top:12px">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="roleName" label="角色名称" min-width="120" />
        <el-table-column prop="roleCode" label="角色编码" min-width="130" />
        <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip />
        <el-table-column prop="isSystem" label="内置" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isSystem === 1 ? 'warning' : 'info'" size="small">{{ row.isSystem === 1 ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" link size="small" @click="handlePerm(row)">分配权限</el-button>
            <el-button type="danger" link size="small" :disabled="row.isSystem === 1" @click="handleDelete(row)">删除</el-button>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" :disabled="!!(form.id)" placeholder="如 OPERATOR（新增后不可改）" />
          <div class="form-tip">角色编码创建后不可修改，请谨慎填写</div>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="角色描述（可选）" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配权限弹窗 -->
    <el-dialog v-model="permDialogVisible" :title="`分配权限 — ${currentRole?.roleName || ''}`" width="440px" :close-on-click-modal="false">
      <div v-loading="permLoading" style="max-height:500px;overflow-y:auto;padding:0 8px">
        <el-tree
          ref="permTreeRef"
          :data="permissionTree"
          :props="{ label: 'permissionName', children: 'children' }"
          show-checkbox
          node-key="id"
          :default-checked-keys="checkedPermIds"
          default-expand-all
        />
      </div>
      <template #footer>
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="permSubmitting" @click="handlePermSubmit">保存权限</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoleList, createRole, updateRole, deleteRole, getRolePermissions, assignPermissions, getPermissionTree } from '@/api/role'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)

const searchForm = reactive({ keyword: '' })
const pagination = reactive({ page: 1, size: 20, total: 0 })
const form = reactive({ id: null, roleName: '', roleCode: '', description: '', status: 1, isSystem: 0 })

const permDialogVisible = ref(false)
const permLoading = ref(false)
const permSubmitting = ref(false)
const permTreeRef = ref(null)
const permissionTree = ref([])
const checkedPermIds = ref([])
const currentRole = ref(null)

const rules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getRoleList({ page: pagination.page, size: pagination.size, keyword: searchForm.keyword || undefined })
    const d = res.data
    tableData.value = d?.records ?? (Array.isArray(d) ? d : [])
    pagination.total = d?.total ?? tableData.value.length
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handleSearch() { pagination.page = 1; fetchData() }
function handleReset() { searchForm.keyword = ''; pagination.page = 1; fetchData() }

function handleAdd() {
  Object.assign(form, { id: null, roleName: '', roleCode: '', description: '', status: 1, isSystem: 0 })
  dialogTitle.value = '新增角色'
  dialogVisible.value = true
  formRef.value?.resetFields()
}

function handleEdit(row) {
  Object.assign(form, { ...row })
  dialogTitle.value = '编辑角色'
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (form.id) {
      await updateRole(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createRole(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error(e?.response?.data?.msg || '操作失败')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除角色「${row.roleName}」？内置角色不可删除。`, '警告', { type: 'warning' })
    await deleteRole(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e?.response?.data?.msg || '删除失败')
  }
}

async function handlePerm(row) {
  currentRole.value = row
  permLoading.value = true
  permDialogVisible.value = true
  checkedPermIds.value = []
  permissionTree.value = []
  try {
    const [treeRes, checkedRes] = await Promise.all([getPermissionTree(), getRolePermissions(row.id)])
    permissionTree.value = treeRes.data || []
    checkedPermIds.value = checkedRes.data || []
  } catch (e) {
    ElMessage.error('加载权限数据失败')
  } finally {
    permLoading.value = false
  }
}

async function handlePermSubmit() {
  if (!permTreeRef.value) return
  const checked = permTreeRef.value.getCheckedKeys()
  const halfChecked = permTreeRef.value.getHalfCheckedKeys()
  permSubmitting.value = true
  try {
    await assignPermissions(currentRole.value.id, [...checked, ...halfChecked])
    ElMessage.success('权限分配成功')
    permDialogVisible.value = false
  } catch (e) {
    ElMessage.error('权限分配失败')
  } finally {
    permSubmitting.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.page-container { padding: 20px; }
.search-card :deep(.el-card__body) { padding-bottom: 8px; }
.form-tip { font-size: 12px; color: #909399; margin-top: 4px; }
</style>
