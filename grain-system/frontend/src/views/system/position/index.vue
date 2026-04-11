<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :inline="true">
        <el-form-item label="关键词"><el-input v-model="searchForm.keyword" placeholder="储位编码/名称" clearable style="width:180px" /></el-form-item>
        <el-form-item><el-button type="primary" @click="handleSearch">查询</el-button><el-button @click="handleReset">重置</el-button></el-form-item>
        <el-form-item style="float:right"><el-button type="primary" @click="handleAdd">新增储位</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card style="margin-top:12px">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column prop="positionCode" label="储位编码" width="120" />
        <el-table-column prop="positionName" label="储位名称" min-width="150" />
        <el-table-column prop="capacity" label="容量(kg)" width="120" />
        <el-table-column prop="currentStock" label="当前库存(kg)" width="120" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{row}"><el-tag :type="row.status===1?'success':(row.status===2?'warning':'danger')" size="small">{{['','可用','维修中','不可用'][row.status]}}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{row}">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size" :total="pagination.total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next" style="margin-top:16px;justify-content:flex-end" @change="fetchData" />
    </el-card>
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="form" label-width="90px">
        <el-form-item label="储位编码"><el-input v-model="form.positionCode" :disabled="!!form.id" /></el-form-item>
        <el-form-item label="储位名称"><el-input v-model="form.positionName" /></el-form-item>
        <el-form-item label="设计容量"><el-input-number v-model="form.capacity" :min="0" :step="1000" style="width:100%" /> kg</el-form-item>
        <el-form-item label="状态"><el-radio-group v-model="form.status">
          <el-radio :value="1">可用</el-radio><el-radio :value="2">维修中</el-radio><el-radio :value="0">不可用</el-radio>
        </el-radio-group></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup>
import {ref,reactive,onMounted} from 'vue'
import {ElMessage,ElMessageBox} from 'element-plus'
import {getPositionPage,createPosition,updatePosition,deletePosition} from '@/api/config'
const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const searchForm=reactive({keyword:''})
const pagination=reactive({page:1,size:20,total:0})
const form=reactive({id:null,positionCode:'',positionName:'',capacity:0,status:1,remark:''})
async function fetchData(){loading.value=true;try{const res=await getPositionPage({page:pagination.page,size:pagination.size,keyword:searchForm.keyword||undefined});const d=res.data;tableData.value=d?.records??[];pagination.total=d?.total??0}catch(e){console.error(e)}finally{loading.value=false}}
function handleSearch(){pagination.page=1;fetchData()}
function handleReset(){searchForm.keyword='';pagination.page=1;fetchData()}
function handleAdd(){Object.assign(form,{id:null,positionCode:'',positionName:'',capacity:0,status:1,remark:''});dialogTitle.value='新增储位';dialogVisible.value=true}
function handleEdit(row){Object.assign(form,{...row});dialogTitle.value='编辑储位';dialogVisible.value=true}
async function handleSubmit(){submitting.value=true;try{if(form.id){await updatePosition(form.id,form);ElMessage.success('更新成功')}else{await createPosition(form);ElMessage.success('创建成功')};dialogVisible.value=false;fetchData()}catch(e){ElMessage.error(e?.response?.data?.msg||'操作失败')}finally{submitting.value=false}}
async function handleDelete(row){try{await ElMessageBox.confirm(`删除储位「${row.positionCode}」?`,'提示',{type:'warning'});await deletePosition(row.id);ElMessage.success('删除成功');fetchData()}catch(e){if(e!=='cancel')ElMessage.error('删除失败')}}
onMounted(fetchData)
</script>
<style scoped>.page-container{padding:20px}</style>