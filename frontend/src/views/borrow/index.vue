<template>
  <div class="borrow-container">
    <el-card>
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="资产名称">
          <el-input v-model="queryParams.assetName" placeholder="请输入资产名称" clearable />
        </el-form-item>
        <el-form-item label="资产规格">
          <el-input v-model="queryParams.assetModel" placeholder="请输入品牌或型号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="借出中" :value="1" />
            <el-option label="已归还" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleAdd">新增领用</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="borrowList" v-loading="loading" border>
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
        <el-table-column label="借用人" width="150">
          <template #default="{ row }">
            {{ row.borrowerRealName || row.borrowerUsername }}
          </template>
        </el-table-column>
        <el-table-column prop="borrowerPhone" label="联系电话" width="130" />
        <el-table-column prop="borrowDate" label="借用日期" width="120" />
        <el-table-column prop="expectReturnDate" label="预计归还" width="120" />
        <el-table-column prop="borrowReason" label="借用原因" width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.borrowReason">{{ row.borrowReason }}</span>
            <span v-else style="color: #999">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="actualReturnDate" label="实际归还" width="120">
          <template #default="{ row }">
            <span v-if="row.actualReturnDate">{{ row.actualReturnDate }}</span>
            <span v-else style="color: #999">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" fixed="right">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="warning">借出中</el-tag>
            <el-tag v-else-if="row.status === 2" type="success">已归还</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 1"
              size="small"
              type="primary"
              @click="handleReturn(row)"
              v-permission="'borrow:return'"
            >
              归还
            </el-button>
            <el-button
              v-else-if="row.status === 2"
              size="small"
              type="danger"
              @click="handleDelete(row)"
              v-permission="'borrow:delete'"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        @current-change="fetchBorrowList"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增领用" width="500px">
      <el-form :model="borrowForm" :rules="formRules" ref="formRef" label-width="120px">
        <el-form-item label="选择资产" prop="assetId">
          <el-select 
            v-model="borrowForm.assetId" 
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
        <el-form-item label="选择借用人" prop="borrowerId">
          <el-select 
            v-model="borrowForm.borrowerId" 
            placeholder="请选择借用人" 
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
        <el-form-item label="借用日期" prop="borrowDate">
          <el-date-picker 
            v-model="borrowForm.borrowDate" 
            type="date" 
            style="width: 100%" 
            clearable
            placeholder="请选择借用日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="预计归还日期">
          <el-date-picker 
            v-model="borrowForm.expectReturnDate" 
            type="date" 
            style="width: 100%" 
            clearable
            placeholder="请选择预计归还日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="借用原因">
          <el-input v-model="borrowForm.borrowReason" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="returnDialogVisible" title="确认归还" width="400px">
      <el-form label-width="120px">
        <el-form-item label="实际归还日期">
          <el-date-picker 
            v-model="returnDate" 
            type="date" 
            style="width: 100%" 
            placeholder="请选择归还日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-alert 
          v-if="isReturnDateFuture" 
          type="warning" 
          :closable="false"
          style="margin-top: 10px;"
        >
          归还日期为未来日期，资产将在该日期自动归还
        </el-alert>
      </el-form>
      <template #footer>
        <el-button @click="returnDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleReturnSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { getBorrowList, createBorrow, returnAsset, deleteBorrow, getAvailableAssets } from '@/api/borrow'
import { getEmployeeList } from '@/api/employee'

const loading = ref(false)
const borrowList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const availableAssets = ref([])
const employeeList = ref([])
const returnDialogVisible = ref(false)
const currentBorrowId = ref<number>()
const returnDate = ref('')

// 计算属性：判断归还日期是否是未来日期
const isReturnDateFuture = computed(() => {
  if (!returnDate.value) return false
  const selectedDate = new Date(returnDate.value)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return selectedDate > today
})

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  assetName: '',
  assetModel: '',
  status: undefined
})

const borrowForm = reactive({
  assetId: undefined,
  borrowerId: undefined,
  borrowDate: null,
  expectReturnDate: null,
  borrowReason: ''
})

const formRules = {
  assetId: [{ required: true, message: '请选择资产', trigger: 'change' }],
  borrowerId: [{ required: true, message: '请选择借用人', trigger: 'change' }],
  borrowDate: [{ required: true, message: '请选择借用日期', trigger: 'change' }]
}

const fetchBorrowList = async () => {
  loading.value = true
  try {
    const res = await getBorrowList(queryParams)
    borrowList.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.pageNum = 1
  fetchBorrowList()
}

const handleReset = () => {
  queryParams.pageNum = 1
  queryParams.assetName = ''
  queryParams.assetModel = ''
  queryParams.status = undefined
  fetchBorrowList()
}

const fetchAvailableAssets = async () => {
  try {
    const res = await getAvailableAssets()
    availableAssets.value = res.data || []
  } catch (error) {
    console.error('获取可用资产失败:', error)
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

const handleAdd = () => {
  Object.assign(borrowForm, {
    assetId: undefined,
    borrowerId: undefined,
    borrowDate: null,
    expectReturnDate: null,
    borrowReason: ''
  })
  dialogVisible.value = true
  fetchAvailableAssets()
  fetchEmployeeList()
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 使用value-format后，日期已经是字符串格式，直接提交即可
        const submitData = {
          ...borrowForm
        }
        
        await createBorrow(submitData)
        ElMessage.success('操作成功')
        dialogVisible.value = false
        fetchBorrowList()
      } catch (error) {
        console.error(error)
      }
    }
  })
}

const handleReturn = (row: any) => {
  currentBorrowId.value = row.id
  returnDate.value = new Date().toISOString().split('T')[0] // 默认今天
  returnDialogVisible.value = true
}

const handleReturnSubmit = async () => {
  if (!returnDate.value) {
    ElMessage.error('请选择归还日期')
    return
  }
  
  try {
    await returnAsset(currentBorrowId.value!, returnDate.value)
    ElMessage.success('归还成功')
    returnDialogVisible.value = false
    fetchBorrowList()
  } catch (error) {
    console.error(error)
  }
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该领用记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteBorrow(row.id)
      ElMessage.success('删除成功')
      fetchBorrowList()
    } catch (error) {
      console.error(error)
    }
  })
}

onMounted(() => {
  fetchBorrowList()
})
</script>

<style scoped>
.borrow-container {
  padding: 20px;
}

.el-pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>
