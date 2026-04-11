<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="关键词"><el-input v-model="searchForm.keyword" placeholder="机构名称/联系人" clearable style="width:180px" /></el-form-item>
        <el-form-item label="审核状态"><el-select v-model="searchForm.auditStatus" placeholder="全部" clearable style="width:120px">
          <el-option label="待审核" :value="0" /><el-option label="已通过" :value="1" /><el-option label="已驳回" :value="2" />
        </el-select></el-form-item>
        <el-form-item><el-button type="primary" @click="handleSearch">查询</el-button><el-button @click="handleReset">重置</el-button></el-form-item>
        <el-form-item style="float:right"><el-button type="primary" @click="handleAdd">新增机构</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card style="margin-top:12px">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column prop="orgName" label="机构名称" min-width="150" />
        <el-table-column prop="orgType" label="类型" width="80" align="center">
          <template #default="{ row }">{{ ['','粮库','加工厂','贸易商','其他'][row.orgType] || '-' }}</template>
        </el-table-column>
        <el-table-column prop="contactName" label="联系人" width="100" />
        <el-table-column prop="phone" label="电话" width="120" />
        <el-table-column prop="address" label="地址" min-width="150" show-overflow-tooltip />
        <el-table-column prop="auditStatus" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.auditStatus===0" type="warning" size="small">待审核</el-tag>
            <el-tag v-else-if="row.auditStatus===1" type="success" size="small">已通过</el-tag>
            <el-tag v-else type="danger" size="small">已驳回</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" link size="small" @click="handleAudit(row)">审核</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size" :total="pagination.total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next" style="margin-top:16px;justify-content:flex-end" @change="fetchData" />
    </el-card>
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px">
      <el-form ref="formRef" :model="form" label-width="90px">
        <el-form-item label="关联用户"><el-input-number v-model="form.userId" :min="1" :disabled="!!form.id" style="width:100%" /></el-form-item>
        <el-form-item label="机构名称"><el-input v-model="form.orgName" /></el-form-item>
        <el-form-item label="机构类型"><el-select v-model="form.orgType" style="width:100%">
          <el-option label="粮库" :value="0" /><el-option label="加工厂" :value="1" /><el-option label="贸易商" :value="2" /><el-option label="其他" :value="3" />
        </el-select></el-form-item>
        <el-form-item label="联系人"><el-input v-model="form.contactName" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="form.address" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button></template>
    </el-dialog>
    <el-dialog v-model="auditDialogVisible" title="审核机构" width="400px">
      <el-form label-width="80px"><el-form-item label="审核结果"><el-radio-group v-model="auditForm.auditStatus">
        <el-radio :value="1">通过</el-radio><el-radio :value="2">驳回</el-radio>
      </el-radio-group></el-form-item></el-form>
      <template #footer><el-button @click="auditDialogVisible=false">取消</el-button><el-button type="primary" @click="handleAuditSubmit">确定</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrgList, createOrg, updateOrg, deleteOrg, auditOrg } from '@/api/org'
const loading = ref(false), submitting = ref(false), tableData = ref([]), dialogVisible = ref(false), dialogTitle = ref(''), formRef = ref(null)
const searchForm = reactive({ keyword: '', auditStatus: null })
const pagination = reactive({ page: 1, size: 20, total: 0 })
const form = reactive({ id: null, userId: null, orgName: '', orgType: 0, contactName: '', phone: '', address: '' })
const auditDialogVisible = ref(false), auditForm = reactive({ id: null, auditStatus: 1, rejectReason: '' })
async function fetchData(){ loading.value=true; try{ const res=await getOrgList({page:pagination.page,size:pagination.size,keyword:searchForm.keyword||undefined,auditStatus:searchForm.auditStatus||undefined});const d=res.data;tableData.value=d?.records??(Array.isArray(d)?d:[]);pagination.total=d?.total??tableData.value.length }catch(e){console.error(e)}finally{loading.value=false} }
function handleSearch(){pagination.page=1;fetchData()}
function handleReset(){searchForm.keyword='';searchForm.auditStatus=null;pagination.page=1;fetchData()}
function handleAdd(){Object.assign(form,{id:null,userId:null,orgName:'',orgType:0,contactName:'',phone:'',address:''});dialogTitle.value='新增机构';dialogVisible.value=true}
function handleEdit(row){Object.assign(form,{...row});dialogTitle.value='编辑机构';dialogVisible.value=true}
async function handleSubmit(){submitting.value=true;try{if(form.id){await updateOrg(form.id,form);ElMessage.success('更新成功')}else{await createOrg(form);ElMessage.success('创建成功')};dialogVisible.value=false;fetchData()}catch(e){ElMessage.error(e?.response?.data?.msg||'操作失败')}finally{submitting.value=false}}
async function handleDelete(row){try{await ElMessageBox.confirm(`确定删除机构「${row.orgName}」？`,'提示',{type:'warning'});await deleteOrg(row.id);ElMessage.success('删除成功');fetchData()}catch(e){if(e!=='cancel')ElMessage.error('删除失败')}}
function handleAudit(row){auditForm.id=row.id;auditForm.auditStatus=1;auditDialogVisible.value=true}
async function handleAuditSubmit(){try{await auditOrg(auditForm.id,auditForm.auditStatus,'');ElMessage.success('审核成功');auditDialogVisible.value=false;fetchData()}catch(e){ElMessage.error('审核失败')}}
onMounted(fetchData)
</script>
<style scoped>.page-container{padding:20px}</style>