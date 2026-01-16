<template>
  <div class="asset-management">
    <el-card>
      <div style="display: flex; align-items: center; gap: 10px; flex-wrap: wrap;">
        <el-form :model="queryParams" inline style="flex: 1;">
          <el-form-item label="资产名称">
            <el-input v-model="queryParams.assetName" placeholder="请输入资产名称" clearable style="width: 200px" />
          </el-form-item>
          <el-form-item label="资产编号">
            <el-input v-model="queryParams.assetNo" placeholder="请输入资产编号" clearable style="width: 200px" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
              <el-option label="在库" :value="1" />
              <el-option label="领用" :value="2" />
              <el-option label="预约中" :value="3" />
              <el-option label="维修中" :value="4" />
              <el-option label="报废" :value="5" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
            <el-button type="success" @click="handleAdd" v-permission="'asset:add'">新增资产</el-button>
          </el-form-item>
        </el-form>
        <div style="display: flex; gap: 10px;">
          <el-upload
            action="#"
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleImport"
            accept=".xlsx,.xls"
            v-permission="'asset:import'"
          >
            <el-button type="warning">批量导入</el-button>
          </el-upload>
          <el-button type="info" @click="downloadTemplate" v-permission="'asset:export'">下载模板</el-button>
        </div>
      </div>

      <el-table :data="assetList" v-loading="loading" border>
        <el-table-column prop="assetNo" label="资产编号" width="150" />
        <el-table-column prop="assetName" label="资产名称" />
        <el-table-column prop="brand" label="品牌" />
        <el-table-column prop="model" label="型号" />
        <el-table-column prop="status" label="状态" width="200">
          <template #default="{ row }">
            <div style="display: flex; gap: 5px; flex-wrap: wrap;">
              <el-tag v-if="row.status === 1" type="success">在库</el-tag>
              <el-tag v-else-if="row.status === 5" type="info">报废</el-tag>
              
              <!-- 根据remark字段解析额外状态 -->
              <template v-if="row.remark && row.remark.includes('borrowed:')">
                <el-tag v-if="row.remark.includes('borrowed:true')" type="warning">借出中</el-tag>
                <el-tag v-if="row.remark.includes('maintenance:true')" type="danger">维修中</el-tag>
              </template>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)" v-permission="'asset:edit'">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)" v-permission="'asset:delete'">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        @current-change="fetchAssetList"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'add' ? '新增资产' : '编辑资产'"
      width="600px"
    >
      <el-form :model="assetForm" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="资产编号" prop="assetNo">
          <el-input v-model="assetForm.assetNo" />
        </el-form-item>
        <el-form-item label="资产名称" prop="assetName">
          <el-input v-model="assetForm.assetName" />
        </el-form-item>
        <el-form-item label="品牌">
          <el-input v-model="assetForm.brand" />
        </el-form-item>
        <el-form-item label="型号">
          <el-input v-model="assetForm.model" />
        </el-form-item>
        <el-form-item label="采购价格">
          <el-input-number v-model="assetForm.purchasePrice" :precision="2" />
        </el-form-item>
        <el-form-item label="存放位置">
          <el-input v-model="assetForm.location" />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Upload, Download } from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'
import { getAssetList, createAsset, updateAsset, deleteAsset, importAssets, downloadAssetTemplate } from '@/api/asset'
import type { AssetQuery, AssetForm } from '@/api/asset'

const loading = ref(false)
const assetList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogMode = ref<'add' | 'edit'>('add')
const formRef = ref<FormInstance>()

const queryParams = reactive<AssetQuery>({
  pageNum: 1,
  pageSize: 10,
  assetName: '',
  assetNo: '',
  status: undefined
})

const assetForm = reactive<AssetForm>({
  assetNo: '',
  assetName: '',
  categoryId: 1,
  brand: '',
  model: '',
  purchasePrice: undefined,
  location: ''
})

const formRules = {
  assetNo: [{ required: true, message: '请输入资产编号', trigger: 'blur' }],
  assetName: [{ required: true, message: '请输入资产名称', trigger: 'blur' }]
}

const fetchAssetList = async () => {
  loading.value = true
  try {
    const res = await getAssetList(queryParams)
    assetList.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.pageNum = 1
  fetchAssetList()
}

const handleReset = () => {
  queryParams.assetName = ''
  queryParams.assetNo = ''
  queryParams.status = undefined
  handleSearch()
}

const handleAdd = () => {
  dialogMode.value = 'add'
  Object.assign(assetForm, {
    assetNo: '',
    assetName: '',
    categoryId: 1,
    brand: '',
    model: '',
    purchasePrice: undefined,
    location: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogMode.value = 'edit'
  Object.assign(assetForm, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (dialogMode.value === 'add') {
          await createAsset(assetForm)
        } else {
          await updateAsset((assetForm as any).id, assetForm)
        }
        ElMessage.success('操作成功')
        dialogVisible.value = false
        fetchAssetList()
      } catch (error) {
        console.error(error)
      }
    }
  })
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该资产吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteAsset(row.id)
    ElMessage.success('删除成功')
    fetchAssetList()
  })
}

const handleImport = async (file: any) => {
  if (!file.raw) return
  
  try {
    const res = await importAssets(file.raw)
    const result = res.data
    ElMessage.success(`导入完成！成功${result.success}条，失败${result.fail}条`)
    if (result.message) {
      ElMessageBox.alert(result.message, '导入详情', {
        confirmButtonText: '确定',
        type: result.fail > 0 ? 'warning' : 'success'
      })
    }
    fetchAssetList()
  } catch (error) {
    console.error(error)
  }
}

const downloadTemplate = async () => {
  try {
    const response = await downloadAssetTemplate()
    
    // 创建 Blob 对象
    const blob = new Blob([response.data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })
    
    // 创建下载链接
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '资产导入模板.xlsx'
    document.body.appendChild(link)
    link.click()
    
    // 清理
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('模板下载成功')
  } catch (error) {
    console.error('下载模板失败:', error)
    ElMessage.error('模板下载失败')
  }
}

onMounted(() => {
  fetchAssetList()
})
</script>

<style scoped>
.asset-management {
  padding: 20px;
}
</style>
