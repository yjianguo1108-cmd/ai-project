<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="粮食品种">
          <el-select v-model="searchForm.grainId" placeholder="全部" clearable style="width:150px">
            <el-option v-for="item in grainOptions" :key="item.id" :label="`${item.grainType} - ${item.grainGrade}`" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="储位">
          <el-select v-model="searchForm.storagePositionId" placeholder="全部" clearable style="width:150px">
            <el-option v-for="item in positionOptions" :key="item.id" :label="item.positionName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top:12px">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column label="粮食品种" width="140">
          <template #default="{ row }">{{ row.grainType }} - {{ row.grainGrade }}</template>
        </el-table-column>
        <el-table-column prop="storagePositionName" label="储位" width="120" />
        <el-table-column prop="currentWeight" label="当前库存(kg)" width="120" align="right">
          <template #default="{ row }"><strong>{{ row.currentWeight }}</strong></template>
        </el-table-column>
        <el-table-column prop="refPrice" label="参考价格" width="100" align="right">
          <template #default="{ row }">¥{{ row.refPrice }}/kg</template>
        </el-table-column>
        <el-table-column label="库存价值" width="110" align="right">
          <template #default="{ row }">¥{{ row.totalAmount }}</template>
        </el-table-column>
        <el-table-column prop="inTotalWeight" label="累计入库(kg)" width="120" align="right" />
        <el-table-column prop="outTotalWeight" label="累计出库(kg)" width="120" align="right" />
        <el-table-column label="最近入库" width="160">
          <template #default="{ row }">{{ row.lastInTime ? dayjs(row.lastInTime).format('YYYY-MM-DD') : '-' }}</template>
        </el-table-column>
        <el-table-column label="最近出库" width="160">
          <template #default="{ row }">{{ row.lastOutTime ? dayjs(row.lastOutTime).format('YYYY-MM-DD') : '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">详情</el-button>
            <el-button type="warning" link size="small" @click="handleAdjust(row)">调整</el-button>
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

    <el-dialog v-model="adjustDialogVisible" title="库存调整" width="400px">
      <el-form label-width="100px">
        <el-form-item label="当前库存">
          <span>{{ currentRow?.currentWeight }} kg</span>
        </el-form-item>
        <el-form-item label="调整重量(kg)">
          <el-input-number v-model="adjustForm.adjustWeight" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="调整原因">
          <el-input v-model="adjustForm.reason" type="textarea" :rows="3" placeholder="请输入调整原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAdjustSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="库存详情" width="600px">
      <el-descriptions :column="2" border v-if="detailData">
        <el-descriptions-item label="粮食品种">{{ detailData.grainType }} - {{ detailData.grainGrade }}</el-descriptions-item>
        <el-descriptions-item label="储位">{{ detailData.storagePositionName }}</el-descriptions-item>
        <el-descriptions-item label="当前库存"><strong>{{ detailData.currentWeight }} kg</strong></el-descriptions-item>
        <el-descriptions-item label="参考价格">¥{{ detailData.refPrice }}/kg</el-descriptions-item>
        <el-descriptions-item label="库存价值">¥{{ detailData.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="累计入库">{{ detailData.inTotalWeight }} kg</el-descriptions-item>
        <el-descriptions-item label="累计出库">{{ detailData.outTotalWeight }} kg</el-descriptions-item>
        <el-descriptions-item label="最近入库">{{ detailData.lastInTime ? dayjs(detailData.lastInTime).format('YYYY-MM-DD HH:mm') : '-' }}</el-descriptions-item>
        <el-descriptions-item label="最近出库">{{ detailData.lastOutTime ? dayjs(detailData.lastOutTime).format('YYYY-MM-DD HH:mm') : '-' }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detailData.updateTime ? dayjs(detailData.updateTime).format('YYYY-MM-DD HH:mm') : '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { getLedgerList, getLedger, adjustLedger } from '@/api/warehouse/ledger'
import { getGrainList, getPositionList } from '@/api/config'

const loading = ref(false)
const tableData = ref([])
const searchForm = reactive({ grainId: null, storagePositionId: null })
const pagination = reactive({ page: 1, size: 20, total: 0 })

const adjustDialogVisible = ref(false)
const adjustForm = reactive({ adjustWeight: 0, reason: '' })
const currentRow = ref(null)

const detailDialogVisible = ref(false)
const detailData = ref(null)

const grainOptions = ref([])
const positionOptions = ref([])

async function fetchData() {
  loading.value = true
  try {
    const res = await getLedgerList({
      page: pagination.page,
      size: pagination.size,
      grainId: searchForm.grainId || undefined,
      storagePositionId: searchForm.storagePositionId || undefined
    })
    const d = res.data
    tableData.value = d?.records ?? (Array.isArray(d) ? d : [])
    pagination.total = d?.total ?? tableData.value.length
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

async function loadOptions() {
  try {
    const [grainRes, positionRes] = await Promise.all([
      getGrainList({ status: 1 }),
      getPositionList({ status: 1 })
    ])
    grainOptions.value = grainRes.data || []
    positionOptions.value = positionRes.data || []
  } catch (e) { console.error(e) }
}

function handleSearch() { pagination.page = 1; fetchData() }
function handleReset() { searchForm.grainId = null; searchForm.storagePositionId = null; pagination.page = 1; fetchData() }

function handleAdjust(row) {
  currentRow.value = row
  adjustForm.adjustWeight = 0
  adjustForm.reason = ''
  adjustDialogVisible.value = true
}

async function handleAdjustSubmit() {
  if (adjustForm.adjustWeight === 0) {
    ElMessage.warning('调整重量不能为0')
    return
  }
  try {
    await adjustLedger(currentRow.value.id, adjustForm.adjustWeight, adjustForm.reason)
    ElMessage.success('调整成功')
    adjustDialogVisible.value = false
    fetchData()
  } catch (e) { ElMessage.error(e?.response?.data?.msg || '调整失败') }
}

async function handleView(row) {
  try {
    const res = await getLedger(row.id)
    detailData.value = res.data
    detailDialogVisible.value = true
  } catch (e) { ElMessage.error('获取详情失败') }
}

onMounted(() => { fetchData(); loadOptions() })
</script>

<style scoped>
.page-container { padding: 20px; }
.search-card :deep(.el-card__body) { padding-bottom: 8px; }
</style>