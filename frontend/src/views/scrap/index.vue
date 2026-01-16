<template>
  <div class="scrap-container">
    <el-card>
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="资产名称">
          <el-input v-model="queryForm.assetName" placeholder="请输入资产名称" clearable />
        </el-form-item>
        <el-form-item label="资产编号">
          <el-input v-model="queryForm.assetNo" placeholder="请输入资产编号" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleAdd">新增报废</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border>
        <el-table-column prop="assetNo" label="资产编号" width="150" />
        <el-table-column prop="assetName" label="资产名称" width="200" />
        <el-table-column prop="scrapDate" label="报废日期" width="120" />
        <el-table-column prop="scrapReason" label="报废原因" show-overflow-tooltip />
        <el-table-column prop="operatorName" label="操作人" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryForm.pageNum"
        v-model:page-size="queryForm.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleQuery"
        @current-change="handleQuery"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增报废" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="资产">
          <el-select v-model="form.assetId" placeholder="请选择资产" filterable>
            <el-option
              v-for="asset in availableAssets"
              :key="asset.id"
              :label="`${asset.assetNo} - ${asset.assetName}`"
              :value="asset.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="报废日期">
          <el-date-picker
            v-model="form.scrapDate"
            type="date"
            placeholder="请选择报废日期"
            value-format="YYYY-MM-DD"
            clearable
          />
        </el-form-item>
        <el-form-item label="报废原因">
          <el-input v-model="form.scrapReason" type="textarea" :rows="4" placeholder="请输入报废原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getScrapList, createScrap, deleteScrap } from '@/api/scrap'
import { getAssetList } from '@/api/asset'

const queryForm = ref({
  pageNum: 1,
  pageSize: 10,
  assetName: '',
  assetNo: ''
})

const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const availableAssets = ref([])

const form = ref({
  assetId: null,
  scrapDate: null,
  scrapReason: ''
})

const fetchList = async () => {
  try {
    const res = await getScrapList(queryForm.value)
    tableData.value = res.data.list || []
    total.value = res.data.total || 0
  } catch (error) {
    ElMessage.error('获取报废记录失败')
  }
}

const fetchAvailableAssets = async () => {
  try {
    const res = await getAssetList({ pageNum: 1, pageSize: 1000 })
    // 过滤出在库状态的资产
    availableAssets.value = res.data.list.filter((asset: any) => asset.status === 1)
  } catch (error) {
    ElMessage.error('获取资产列表失败')
  }
}

const handleQuery = () => {
  queryForm.value.pageNum = 1
  fetchList()
}

const handleReset = () => {
  queryForm.value = {
    pageNum: 1,
    pageSize: 10,
    assetName: '',
    assetNo: ''
  }
  fetchList()
}

const handleAdd = () => {
  form.value = {
    assetId: null,
    scrapDate: null,
    scrapReason: ''
  }
  fetchAvailableAssets()
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await createScrap(form.value)
    ElMessage.success('报废成功')
    dialogVisible.value = false
    fetchList()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '报废失败')
  }
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('删除报废记录将恢复资产状态，确认删除？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteScrap(row.id)
      ElMessage.success('删除成功')
      fetchList()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.scrap-container {
  padding: 20px;
}

.el-pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>
