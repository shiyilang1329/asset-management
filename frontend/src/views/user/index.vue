<template>
  <div class="user-management">
    <el-card>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="用户管理" name="user">
          <el-alert type="info" :closable="false" style="margin-bottom: 15px;">
            <template #title>
              <span style="font-weight: bold;">用户管理</span> - 创建用户账号并分配角色，用户将拥有所分配角色的所有权限
            </template>
          </el-alert>
      
      <div style="display: flex; align-items: center; gap: 10px; flex-wrap: wrap;">
        <el-form :model="queryParams" inline style="flex: 1;">
          <el-form-item label="用户名">
            <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable style="width: 200px" />
          </el-form-item>
          <el-form-item label="真实姓名">
            <el-input v-model="queryParams.realName" placeholder="请输入真实姓名" clearable style="width: 200px" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
            <el-button type="success" @click="handleAdd">新增用户</el-button>
          </el-form-item>
        </el-form>
        <div style="display: flex; gap: 10px;">
          <el-upload
            action="#"
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleImport"
            accept=".xlsx,.xls"
          >
            <el-button type="warning">批量导入</el-button>
          </el-upload>
          <el-button type="info" @click="downloadTemplate">下载模板</el-button>
        </div>
      </div>

      <el-table :data="userList" v-loading="loading" border>
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="realName" label="真实姓名" width="150" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="phone" label="电话" width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="success">启用</el-tag>
            <el-tag v-else type="danger">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="350" fixed="right">
          <template #default="{ row }">
            <div style="display: flex; gap: 5px; flex-wrap: wrap;">
              <el-button size="small" @click="handleEdit(row)" v-permission="'user:edit'">编辑</el-button>
              <el-button size="small" type="primary" @click="handleResetPassword(row)" v-permission="'user:reset'">重置密码</el-button>
              <el-button size="small" type="warning" @click="handleAssignRole(row)" v-permission="'user:assign'">分配角色</el-button>
              <el-button size="small" type="success" @click="handleUnlock(row)">解锁</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        @current-change="fetchUserList"
        @size-change="fetchUserList"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 20px; justify-content: flex-end"
      />
        </el-tab-pane>

        <el-tab-pane label="角色管理" name="role">
          <el-alert type="info" :closable="false" style="margin-bottom: 15px;">
            <template #title>
              <span style="font-weight: bold;">角色管理</span> - 角色是权限的集合，点击"分配权限"可使用快速配置模板
            </template>
          </el-alert>
          
          <el-button type="primary" @click="handleAddRole" v-permission="'role:add'">新增角色</el-button>

          <el-table :data="roleList" border style="margin-top: 20px;">
            <el-table-column prop="roleCode" label="角色编码" width="150" />
            <el-table-column prop="roleName" label="角色名称" width="150" />
            <el-table-column prop="description" label="描述" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" width="280" fixed="right">
              <template #default="{ row }">
                <div style="display: flex; gap: 5px;">
                  <el-button type="primary" size="small" @click="handleEditRole(row)" v-permission="'role:edit'">编辑</el-button>
                  <el-button type="warning" size="small" @click="handleAssignPermission(row)" v-permission="'role:assign'">配置权限</el-button>
                  <el-button type="danger" size="small" @click="handleDeleteRole(row)" v-permission="'role:delete'">删除</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-model:current-page="roleQueryParams.pageNum"
            v-model:page-size="roleQueryParams.pageSize"
            :total="roleTotal"
            @current-change="fetchRoleList"
            @size-change="fetchRoleList"
            layout="total, sizes, prev, pager, next, jumper"
            style="margin-top: 20px; justify-content: flex-end"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogMode === 'add' ? '新增用户' : '编辑用户'" width="500px">
      <el-form :model="userForm" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" :disabled="dialogMode === 'edit'" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="dialogMode === 'add'">
          <el-input v-model="userForm.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="userForm.realName" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="userForm.email" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="userForm.phone" />
        </el-form-item>
        <el-form-item label="状态" v-if="dialogMode === 'edit'">
          <el-radio-group v-model="userForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="roleDialogVisible" title="分配角色" width="500px">
      <el-alert type="info" :closable="false" style="margin-bottom: 15px;">
        用户可以拥有多个角色，将拥有所有角色权限的总和
      </el-alert>
      
      <el-checkbox-group v-model="selectedRoles">
        <el-checkbox v-for="role in allRoles" :key="role.id" :label="role.id" style="display: block; margin-bottom: 10px;">
          <span style="font-weight: bold;">{{ role.roleName }}</span>
          <span style="color: #999; margin-left: 10px; font-size: 12px;">{{ role.description }}</span>
        </el-checkbox>
      </el-checkbox-group>
      
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRoleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="passwordDialogVisible" title="重置密码" width="400px">
      <el-form label-width="100px">
        <el-form-item label="新密码">
          <el-input v-model="newPassword" type="password" show-password placeholder="请输入新密码（至少6位）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePasswordSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="roleFormDialogVisible" :title="roleDialogMode === 'add' ? '新增角色' : '编辑角色'" width="600px">
      <el-form :model="roleForm" label-width="100px">
        <el-form-item label="角色编码">
          <el-input v-model="roleForm.roleCode" placeholder="请输入角色编码" :disabled="roleDialogMode === 'edit'" />
        </el-form-item>
        <el-form-item label="角色名称">
          <el-input v-model="roleForm.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="roleForm.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="roleForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleFormDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRoleFormSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="permissionDialogVisible" title="分配权限" width="800px">
      <div style="margin-bottom: 20px;">
        <el-alert type="info" :closable="false" style="margin-bottom: 15px;">
          <template #title>
            <span style="font-weight: bold;">为角色"{{ currentRoleName }}"分配权限</span>
          </template>
        </el-alert>
        
        <div style="margin-bottom: 15px;">
          <span style="font-weight: bold; margin-right: 10px;">快速配置：</span>
          <el-button 
            v-for="template in permissionTemplates" 
            :key="template.name"
            size="small"
            @click="applyTemplate(template)"
          >
            {{ template.name }}
          </el-button>
        </div>
        
        <el-alert type="warning" :closable="false">
          提示：点击上方按钮快速应用权限模板，也可以手动勾选下方权限树进行微调
        </el-alert>
      </div>
      
      <el-tree
        ref="permissionTreeRef"
        :data="permissionTree"
        show-checkbox
        node-key="id"
        :props="{ children: 'children', label: 'permissionName' }"
        :default-checked-keys="selectedPermissions"
        default-expand-all
      >
        <template #default="{ node, data }">
          <span>
            {{ data.permissionName }}
            <el-tag v-if="data.permissionType === 1" type="primary" size="small" style="margin-left: 8px;">菜单</el-tag>
            <el-tag v-else type="success" size="small" style="margin-left: 8px;">按钮</el-tag>
            <span style="color: #999; margin-left: 8px; font-size: 12px;">({{ data.permissionCode }})</span>
          </span>
        </template>
      </el-tree>
      
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePermissionSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, UploadFile } from 'element-plus'
import { getUserPage, createUser, updateUser, deleteUser, importUsers, downloadUserTemplate, resetPassword } from '@/api/user'
import { getAllRoles, getUserRoles, assignRoles, getRoleList, createRole, updateRole, deleteRole } from '@/api/role'
import { getPermissionTree, getRolePermissions, assignPermissions } from '@/api/permission'

const activeTab = ref('user')
const loading = ref(false)
const userList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const roleDialogVisible = ref(false)
const passwordDialogVisible = ref(false)
const dialogMode = ref<'add' | 'edit'>('add')
const formRef = ref<FormInstance>()
const currentUserId = ref<number>()
const allRoles = ref([])
const selectedRoles = ref<number[]>([])
const newPassword = ref('')

// 角色管理相关
const roleList = ref([])
const roleTotal = ref(0)
const roleFormDialogVisible = ref(false)
const roleDialogMode = ref<'add' | 'edit'>('add')
const permissionDialogVisible = ref(false)
const permissionTreeRef = ref()
const permissionTree = ref([])
const selectedPermissions = ref<number[]>([])
const currentRoleId = ref<number>()
const currentRoleName = ref('')

const roleQueryParams = reactive({
  pageNum: 1,
  pageSize: 10
})

const roleForm = reactive({
  id: null,
  roleCode: '',
  roleName: '',
  description: '',
  status: 1
})

// 权限模板
const permissionTemplates = [
  {
    name: '系统管理员',
    description: '拥有所有权限',
    permissions: ['all']
  },
  {
    name: '资产管理员',
    description: '资产、领用、维修、报废、人员管理权限',
    permissions: ['dashboard', 'asset', 'borrow', 'maintenance', 'scrap', 'employee']
  },
  {
    name: '普通用户',
    description: '仅查看和借用权限',
    permissions: ['dashboard', 'asset:view', 'borrow:view', 'borrow:add']
  }
]

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  username: '',
  realName: '',
  status: undefined
})

const userForm = reactive({
  username: '',
  password: '',
  realName: '',
  email: '',
  phone: '',
  status: 1
})

const formRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

const fetchUserList = async () => {
  loading.value = true
  try {
    const res = await getUserPage(queryParams)
    userList.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const fetchRoleList = async () => {
  try {
    const res = await getRoleList(roleQueryParams)
    roleList.value = res.data.list || res.data.records || []
    roleTotal.value = res.data.total || 0
  } catch (error) {
    ElMessage.error('获取角色列表失败')
  }
}

const handleSearch = () => {
  queryParams.pageNum = 1
  fetchUserList()
}

const handleReset = () => {
  queryParams.username = ''
  queryParams.realName = ''
  queryParams.status = undefined
  queryParams.pageNum = 1
  fetchUserList()
}

const handleAdd = () => {
  dialogMode.value = 'add'
  Object.assign(userForm, {
    username: '',
    password: '',
    realName: '',
    email: '',
    phone: '',
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogMode.value = 'edit'
  currentUserId.value = row.id
  Object.assign(userForm, {
    username: row.username,
    realName: row.realName,
    email: row.email,
    phone: row.phone,
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
          await createUser(userForm)
        } else {
          await updateUser(currentUserId.value!, userForm)
        }
        ElMessage.success('操作成功')
        dialogVisible.value = false
        fetchUserList()
      } catch (error) {
        console.error(error)
      }
    }
  })
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该用户吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    fetchUserList()
  })
}

const handleAssignRole = async (row: any) => {
  currentUserId.value = row.id
  try {
    // 获取所有角色
    const rolesRes = await getAllRoles()
    allRoles.value = rolesRes.data
    
    // 获取用户当前角色
    const userRolesRes = await getUserRoles(row.id)
    selectedRoles.value = userRolesRes.data.map((r: any) => r.id)
    
    roleDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取角色信息失败')
  }
}

const handleRoleSubmit = async () => {
  try {
    await assignRoles({
      userId: currentUserId.value,
      roleIds: selectedRoles.value
    })
    ElMessage.success('角色分配成功')
    roleDialogVisible.value = false
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '角色分配失败')
  }
}

const handleResetPassword = (row: any) => {
  currentUserId.value = row.id
  newPassword.value = ''
  passwordDialogVisible.value = true
}

const handleUnlock = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要解锁用户"${row.username}"吗？解锁后该用户可以立即登录。`,
      '解锁确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await fetch(`/api/auth/unlock/${row.username}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    if (response.ok) {
      ElMessage.success('用户解锁成功')
    } else {
      ElMessage.error('用户解锁失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('用户解锁失败')
    }
  }
}

const handlePasswordSubmit = async () => {
  if (!newPassword.value || newPassword.value.length < 6) {
    ElMessage.error('密码至少6位')
    return
  }
  
  try {
    await resetPassword(currentUserId.value!, newPassword.value)
    ElMessage.success('密码重置成功')
    passwordDialogVisible.value = false
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '密码重置失败')
  }
}

const handleImport = async (file: UploadFile) => {
  if (!file.raw) return
  
  try {
    const res = await importUsers(file.raw)
    const result = res.data
    ElMessage.success(`导入完成！成功${result.success}条，失败${result.fail}条`)
    if (result.message) {
      ElMessageBox.alert(result.message, '导入详情', {
        confirmButtonText: '确定',
        type: result.fail > 0 ? 'warning' : 'success'
      })
    }
    fetchUserList()
  } catch (error) {
    console.error(error)
  }
}

const downloadTemplate = async () => {
  try {
    const response = await downloadUserTemplate()
    
    // 创建 Blob 对象
    const blob = new Blob([response.data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })
    
    // 创建下载链接
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '用户导入模板.xlsx'
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

// 角色管理方法
const handleAddRole = () => {
  roleDialogMode.value = 'add'
  Object.assign(roleForm, {
    id: null,
    roleCode: '',
    roleName: '',
    description: '',
    status: 1
  })
  roleFormDialogVisible.value = true
}

const handleEditRole = (row: any) => {
  roleDialogMode.value = 'edit'
  Object.assign(roleForm, { ...row })
  roleFormDialogVisible.value = true
}

const handleRoleFormSubmit = async () => {
  try {
    if (roleDialogMode.value === 'edit') {
      await updateRole(roleForm.id, roleForm)
      ElMessage.success('更新成功')
    } else {
      await createRole(roleForm)
      ElMessage.success('创建成功')
    }
    roleFormDialogVisible.value = false
    fetchRoleList()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

const handleDeleteRole = (row: any) => {
  ElMessageBox.confirm('确认删除该角色？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteRole(row.id)
      ElMessage.success('删除成功')
      fetchRoleList()
    } catch (error: any) {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  })
}

const handleAssignPermission = async (row: any) => {
  currentRoleId.value = row.id
  currentRoleName.value = row.roleName
  try {
    // 获取权限树
    const treeRes = await getPermissionTree()
    permissionTree.value = treeRes.data
    
    // 获取角色已有权限
    const rolePermsRes = await getRolePermissions(row.id)
    selectedPermissions.value = rolePermsRes.data.map((p: any) => p.id)
    
    permissionDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取权限信息失败')
  }
}

const applyTemplate = (template: any) => {
  if (!permissionTreeRef.value) return
  
  // 清空当前选择
  permissionTreeRef.value.setCheckedKeys([])
  
  if (template.permissions.includes('all')) {
    // 选择所有权限
    const allIds = getAllPermissionIds(permissionTree.value)
    permissionTreeRef.value.setCheckedKeys(allIds)
  } else {
    // 根据权限编码选择
    const selectedIds = getPermissionIdsByCode(permissionTree.value, template.permissions)
    permissionTreeRef.value.setCheckedKeys(selectedIds)
  }
  
  ElMessage.success(`已应用"${template.name}"权限模板`)
}

const getAllPermissionIds = (tree: any[]): number[] => {
  let ids: number[] = []
  tree.forEach(node => {
    ids.push(node.id)
    if (node.children && node.children.length > 0) {
      ids = ids.concat(getAllPermissionIds(node.children))
    }
  })
  return ids
}

const getPermissionIdsByCode = (tree: any[], codes: string[]): number[] => {
  let ids: number[] = []
  tree.forEach(node => {
    // 检查是否匹配权限编码
    const matched = codes.some(code => {
      if (code === node.permissionCode) return true
      // 支持模糊匹配，如 'asset' 匹配 'asset:add', 'asset:edit' 等
      if (node.permissionCode.startsWith(code + ':')) return true
      return false
    })
    
    if (matched) {
      ids.push(node.id)
    }
    
    if (node.children && node.children.length > 0) {
      ids = ids.concat(getPermissionIdsByCode(node.children, codes))
    }
  })
  return ids
}

const handlePermissionSubmit = async () => {
  try {
    const checkedKeys = permissionTreeRef.value.getCheckedKeys()
    const halfCheckedKeys = permissionTreeRef.value.getHalfCheckedKeys()
    const allKeys = [...checkedKeys, ...halfCheckedKeys]
    
    await assignPermissions({
      roleId: currentRoleId.value,
      permissionIds: allKeys
    })
    ElMessage.success('权限分配成功')
    permissionDialogVisible.value = false
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '权限分配失败')
  }
}

onMounted(() => {
  fetchUserList()
  fetchRoleList()
})
</script>

<style scoped>
.user-management {
  padding: 20px;
}
</style>
