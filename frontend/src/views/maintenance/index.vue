<template>
  <div class="maintenance-container">
    <el-card>
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="资产名称">
          <el-input v-model="queryParams.assetName" placeholder="请输入资产名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="待维修" :value="1" />
            <el-option label="维修中" :value="2" />
            <el-option label="已完成" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleAdd">新增维修</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="maintenanceList" v-loading="loading" border>
        <el-table-column prop="assetNo" label="资产编号" width="150" />
        <el-table-column prop="assetName" label="资产名称" width="150" />
        <el-table-column label="资产规格" width="200">
          <template #default="{ row }">
            <span v-if="row.assetBrand || row.assetModel">
              {{ row.assetBrand }} {{ row.assetModel }}
            </span>
            <span v-else style="color: #999">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="reportUserName" label="报修人" width="120" />
        <el-table-column prop="reportDate" label="报修日期" width="120" />
        <el-table-column prop="problemDesc" label="问题描述" width="200" show-overflow-tooltip />
        <el-table-column prop="maintenanceDate" label="维修日期" width="120">
          <template #default="{ row }">
            <span v-if="row.maintenanceDate">{{ row.maintenanceDate }}</span>
            <span v-else style="color: #999">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="maintenanceCost" label="维修费用" width="120">
          <template #default="{ row }">
            <span v-if="row.maintenanceCost">¥{{ row.maintenanceCost }}</span>
            <span v-else style="color: #999">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="maintenanceResult" label="维修结果" width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.maintenanceResult">{{ row.maintenanceResult }}</span>
            <span v-else style="color: #999">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" fixed="right">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="warning">待维修</el-tag>
            <el-tag v-else-if="row.status === 2" type="danger">维修中</el-tag>
            <el-tag v-else-if="row.status === 3" type="success">已完成</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        @current-change="fetchMaintenanceList"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="maintenanceForm" :rules="formRules" ref="formRef" label-width="120px">
        <el-form-item label="选择资产" prop="assetId">
          <el-select 
            v-model="maintenanceForm.assetId" 
            placeholder="请选择资产" 
            style="width: 100%"
            filterable
            clearable
          >
            <el-option
              v-for="asset in availableAssets"
              :key="asset.id"
              :label="`${asset.assetNo} - ${asset.assetName}`"
              :value="asset.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="报修人" prop="reportUserId">
          <el-select 
            v-model="maintenanceForm.reportUserId" 
            placeholder="请选择报修人" 
            style="width: 100%"
            filterable
            clearable
          >
            <el-option
              v-for="employee in employeeList"
              :key="employee.id"
              :label="`${employee.name} - ${employee.department || '未设置部门'}`"
              :value="employee.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="报修日期" prop="reportDate">
          <el-date-picker 
            v-model="maintenanceForm.reportDate" 
            type="date" 
            style="width: 100%" 
            clearable
            placeholder="请选择报修日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="问题描述" prop="problemDesc">
          <el-input v-model="maintenanceForm.problemDesc" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="维修日期">
          <el-date-picker 
            v-model="maintenanceForm.maintenanceDate" 
            type="date" 
            style="width: 100%" 
            clearable
            placeholder="请选择维修日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="维修费用">
          <el-input-number v-model="maintenanceForm.maintenanceCost" :precision="2" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="维修结果">
          <el-input v-model="maintenanceForm.maintenanceResult" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="maintenanceForm.status" style="width: 100%">
            <el-option label="待维修" :value="1" />
            <el-option label="维修中" :value="2" />
            <el-option label="已完成" :value="3" />
          </el-select>
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
import type { FormInstance } from 'element-plus'
import { getMaintenanceList, createMaintenance, updateMaintenance, deleteMaintenance } from '@/api/maintenance'
import { getAvailableForMaintenance } from '@/api/asset'
import { getEmployeeList } from '@/api/employee'

const loading = ref(false)
const maintenanceList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const editId = ref<number>()
const formRef = ref<FormInstance>()
const availableAssets = ref([])
const employeeList = ref([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  assetName: '',
  status: undefined
})

const maintenanceForm = reactive({
  assetId: undefined,
  reportUserId: undefined,
  reportDate: null,
  problemDesc: '',
  maintenanceDate: null,
  maintenanceCost: undefined,
  maintenanceResult: '',
  status: 1
})

const formRules = {
  assetId: [{ required: true, message: '请选择资产', trigger: 'change' }],
  reportUserId: [{ required: true, message: '请选择报修人', trigger: 'change' }],
  reportDate: [{ required: true, message: '请选择报修日期', trigger: 'change' }],
  problemDesc: [{ required: true, message: '请输入问题描述', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const fetchMaintenanceList = async () => {
  loading.value = true
  try {
    const res = await getMaintenanceList(queryParams)
    maintenanceList.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.pageNum = 1
  fetchMaintenanceList()
}

const handleReset = () => {
  queryParams.pageNum = 1
  queryParams.assetName = ''
  queryParams.status = undefined
  fetchMaintenanceList()
}

const fetchAssetList = async () => {
  try {
    const res = await getAvailableForMaintenance({ pageNum: 1, pageSize: 1000 })
    availableAssets.value = res.data.list || []
  } catch (error) {
    console.error('获取资产列表失败:', error)
  }
}

const fetchEmployeeList = async () => {
  try {
    const res = await getEmployeeList()
    employeeList.value = res.data || []
  } catch (error) {
    console.error('获取人员列表失败:', error)
  }
}

const resetForm = () => {
  Object.assign(maintenanceForm, {
    assetId: undefined,
    reportUserId: undefined,
    reportDate: null,
    problemDesc: '',
    maintenanceDate: null,
    maintenanceCost: undefined,
    maintenanceResult: '',
    status: 1
  })
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增维修记录'
  isEdit.value = false
  dialogVisible.value = true
  fetchAssetList()
  fetchEmployeeList()
}

const handleEdit = (row: any) => {
  resetForm()
  Object.assign(maintenanceForm, {
    assetId: row.assetId,
    reportUserId: row.reportUserId,
    reportDate: row.reportDate || null,
    problemDesc: row.problemDesc,
    maintenanceDate: row.maintenanceDate || null,
    maintenanceCost: row.maintenanceCost,
    maintenanceResult: row.maintenanceResult,
    status: row.status
  })
  dialogTitle.value = '编辑维修记录'
  isEdit.value = true
  editId.value = row.id
  dialogVisible.value = true
  fetchAssetList()
  fetchEmployeeList()
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 使用value-format后，日期已经是字符串格式，直接提交即可
        const submitData = {
          ...maintenanceForm
        }
        
        if (isEdit.value) {
          await updateMaintenance(editId.value!, submitData)
        } else {
          await createMaintenance(submitData)
        }
        
        ElMessage.success('操作成功')
        dialogVisible.value = false
        fetchMaintenanceList()
      } catch (error) {
        console.error(error)
      }
    }
  })
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该维修记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteMaintenance(row.id)
      ElMessage.success('删除成功')
      fetchMaintenanceList()
    } catch (error) {
      console.error(error)
    }
  })
}

onMounted(() => {
  fetchMaintenanceList()
})
</script>

<style scoped>
.maintenance-container {
  padding: 20px;
}

.el-pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>