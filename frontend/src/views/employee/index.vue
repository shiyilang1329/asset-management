<template>
  <div class="employee-management">
    <el-card>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="人员管理" name="employee">
          <div style="display: flex; align-items: center; gap: 10px; flex-wrap: wrap;">
        <el-form :model="queryParams" inline style="flex: 1;">
          <el-form-item label="姓名">
            <el-input v-model="queryParams.name" placeholder="请输入姓名" clearable style="width: 200px" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="queryParams.phone" placeholder="请输入手机号" clearable style="width: 200px" />
          </el-form-item>
          <el-form-item label="部门">
            <el-input v-model="queryParams.department" placeholder="请输入部门" clearable style="width: 200px" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
              <el-option label="在职" :value="1" />
              <el-option label="离职" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
            <el-button type="success" @click="handleAdd" v-permission="'employee:add'">新增人员</el-button>
          </el-form-item>
        </el-form>
        <div style="display: flex; gap: 10px;">
          <el-upload
            action="#"
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleImport"
            accept=".xlsx,.xls"
            v-permission="'employee:import'"
          >
            <el-button type="warning">批量导入</el-button>
          </el-upload>
          <el-button type="info" @click="downloadTemplate" v-permission="'employee:import'">下载模板</el-button>
        </div>
      </div>

      <el-table :data="employeeList" v-loading="loading" border>
        <el-table-column prop="employeeNo" label="工号" width="120" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="department" label="部门" width="120" />
        <el-table-column prop="position" label="职位" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="success">在职</el-tag>
            <el-tag v-else type="info">离职</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <div style="display: flex; gap: 5px; flex-wrap: nowrap;">
              <el-button size="small" @click="handleEdit(row)" v-permission="'employee:edit'">编辑</el-button>
              <el-button size="small" type="danger" @click="handleDelete(row)" v-permission="'employee:delete'">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        @current-change="fetchEmployeeList"
        @size-change="fetchEmployeeList"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 20px; justify-content: flex-end"
      />
        </el-tab-pane>

        <el-tab-pane label="部门管理" name="department">
          <el-button type="primary" @click="handleAddDept" style="margin-bottom: 15px;">新增部门</el-button>

          <el-table :data="allDepartmentList" border>
            <el-table-column prop="deptCode" label="部门编码" width="150" />
            <el-table-column prop="deptName" label="部门名称" width="200" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.status === 1" type="success">启用</el-tag>
                <el-tag v-else type="info">禁用</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <div style="display: flex; gap: 5px;">
                  <el-button size="small" @click="handleEditDept(row)">编辑</el-button>
                  <el-button size="small" type="danger" @click="handleDeleteDept(row)">删除</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogMode === 'add' ? '新增人员' : '编辑人员'" width="500px">
      <el-form :model="employeeForm" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="工号">
          <el-input v-model="employeeForm.employeeNo" placeholder="可选" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="employeeForm.name" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="employeeForm.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="employeeForm.email" />
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="employeeForm.department" placeholder="请选择部门" style="width: 100%">
            <el-option
              v-for="dept in departmentList"
              :key="dept.id"
              :label="dept.deptName"
              :value="dept.deptName"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="职位">
          <el-input v-model="employeeForm.position" />
        </el-form-item>
        <el-form-item label="状态" v-if="dialogMode === 'edit'">
          <el-radio-group v-model="employeeForm.status">
            <el-radio :label="1">在职</el-radio>
            <el-radio :label="0">离职</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="deptDialogVisible" :title="deptDialogMode === 'add' ? '新增部门' : '编辑部门'" width="500px">
      <el-form :model="deptForm" :rules="deptFormRules" ref="deptFormRef" label-width="100px">
        <el-form-item label="部门编码" prop="deptCode">
          <el-input v-model="deptForm.deptCode" :disabled="deptDialogMode === 'edit'" />
        </el-form-item>
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="deptForm.deptName" />
        </el-form-item>
        <el-form-item label="状态" v-if="deptDialogMode === 'edit'">
          <el-radio-group v-model="deptForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="deptDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleDeptSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, UploadFile } from 'element-plus'
import { getEmployeePage, createEmployee, updateEmployee, deleteEmployee, importEmployees, downloadEmployeeTemplate } from '@/api/employee'
import { getDepartmentList, getAllDepartments, createDepartment, updateDepartment, deleteDepartment } from '@/api/department'

const activeTab = ref('employee')

const loading = ref(false)
const employeeList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogMode = ref<'add' | 'edit'>('add')
const formRef = ref<FormInstance>()
const currentEmployeeId = ref<number>()
const departmentList = ref<any[]>([])
const allDepartmentList = ref<any[]>([])

// 部门管理相关
const deptDialogVisible = ref(false)
const deptDialogMode = ref<'add' | 'edit'>('add')
const deptFormRef = ref<FormInstance>()
const currentDeptId = ref<number>()

const deptForm = reactive({
  deptCode: '',
  deptName: '',
  status: 1
})

const deptFormRules = {
  deptCode: [{ required: true, message: '请输入部门编码', trigger: 'blur' }],
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }]
}

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  name: '',
  phone: '',
  department: '',
  status: undefined
})

const employeeForm = reactive({
  employeeNo: '',
  name: '',
  phone: '',
  email: '',
  department: '',
  position: '',
  status: 1
})

const formRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

const fetchEmployeeList = async () => {
  loading.value = true
  try {
    const res = await getEmployeePage(queryParams)
    employeeList.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const fetchDepartmentList = async () => {
  try {
    const res = await getDepartmentList()
    departmentList.value = res.data || []
  } catch (error) {
    console.error('获取部门列表失败:', error)
  }
}

const fetchAllDepartments = async () => {
  try {
    const res = await getAllDepartments()
    allDepartmentList.value = res.data || []
  } catch (error) {
    console.error('获取所有部门失败:', error)
  }
}

const handleSearch = () => {
  queryParams.pageNum = 1
  fetchEmployeeList()
}

const handleReset = () => {
  queryParams.name = ''
  queryParams.phone = ''
  queryParams.department = ''
  queryParams.status = undefined
  queryParams.pageNum = 1
  fetchEmployeeList()
}

const handleAdd = () => {
  dialogMode.value = 'add'
  Object.assign(employeeForm, {
    employeeNo: '',
    name: '',
    phone: '',
    email: '',
    department: '',
    position: '',
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogMode.value = 'edit'
  currentEmployeeId.value = row.id
  Object.assign(employeeForm, {
    employeeNo: row.employeeNo,
    name: row.name,
    phone: row.phone,
    email: row.email,
    department: row.department,
    position: row.position,
    status: row.status
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (dialogMode.value === 'add') {
          await createEmployee(employeeForm)
        } else {
          await updateEmployee(currentEmployeeId.value!, employeeForm)
        }
        ElMessage.success('操作成功')
        dialogVisible.value = false
        fetchEmployeeList()
      } catch (error) {
        console.error(error)
      }
    }
  })
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该人员吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteEmployee(row.id)
    ElMessage.success('删除成功')
    fetchEmployeeList()
  })
}

const handleImport = async (file: UploadFile) => {
  if (!file.raw) return
  
  try {
    const res = await importEmployees(file.raw)
    const result = res.data
    ElMessage.success(`导入完成！成功${result.success}条，失败${result.fail}条`)
    if (result.message) {
      ElMessageBox.alert(result.message, '导入详情', {
        confirmButtonText: '确定',
        type: result.fail > 0 ? 'warning' : 'success'
      })
    }
    fetchEmployeeList()
  } catch (error) {
    console.error(error)
  }
}

const downloadTemplate = async () => {
  try {
    const response = await downloadEmployeeTemplate()
    
    const blob = new Blob([response.data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })
    
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '人员导入模板.xlsx'
    document.body.appendChild(link)
    link.click()
    
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('模板下载成功')
  } catch (error) {
    console.error('下载模板失败:', error)
    ElMessage.error('模板下载失败')
  }
}

onMounted(() => {
  fetchEmployeeList()
  fetchDepartmentList()
  fetchAllDepartments()
})

// 部门管理方法
const handleAddDept = () => {
  deptDialogMode.value = 'add'
  Object.assign(deptForm, {
    deptCode: '',
    deptName: '',
    status: 1
  })
  deptDialogVisible.value = true
}

const handleEditDept = (row: any) => {
  deptDialogMode.value = 'edit'
  currentDeptId.value = row.id
  Object.assign(deptForm, {
    deptCode: row.deptCode,
    deptName: row.deptName,
    status: row.status
  })
  deptDialogVisible.value = true
}

const handleDeptSubmit = async () => {
  if (!deptFormRef.value) return
  await deptFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (deptDialogMode.value === 'add') {
          await createDepartment(deptForm)
        } else {
          await updateDepartment(currentDeptId.value!, deptForm)
        }
        ElMessage.success('操作成功')
        deptDialogVisible.value = false
        fetchDepartmentList()
        fetchAllDepartments()
      } catch (error) {
        console.error(error)
      }
    }
  })
}

const handleDeleteDept = (row: any) => {
  ElMessageBox.confirm('确定要删除该部门吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteDepartment(row.id)
    ElMessage.success('删除成功')
    fetchDepartmentList()
    fetchAllDepartments()
  })
}
</script>

<style scoped>
.employee-management {
  padding: 20px;
}
</style>
