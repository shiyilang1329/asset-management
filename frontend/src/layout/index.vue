<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <img src="/logo.png" alt="Logo" class="logo-img" />
        <span class="logo-text">资产管理系统</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        class="sidebar-menu"
      >
        <el-menu-item index="/dashboard" v-if="hasPermission('dashboard')">
          <el-icon><DataLine /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/asset" v-if="hasPermission('asset')">
          <el-icon><Box /></el-icon>
          <span>资产管理</span>
        </el-menu-item>
        <el-menu-item index="/borrow" v-if="hasPermission('borrow')">
          <el-icon><DocumentCopy /></el-icon>
          <span>领用管理</span>
        </el-menu-item>
        <el-menu-item index="/maintenance" v-if="hasPermission('maintenance')">
          <el-icon><Tools /></el-icon>
          <span>维修管理</span>
        </el-menu-item>
        <el-menu-item index="/scrap" v-if="hasPermission('scrap')">
          <el-icon><Delete /></el-icon>
          <span>报废管理</span>
        </el-menu-item>
        <el-menu-item index="/user" v-if="hasPermission('user')">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/employee" v-if="hasPermission('employee')">
          <el-icon><UserFilled /></el-icon>
          <span>人员管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <div class="datetime-info">
            <span class="welcome-text">你好，{{ userStore.realName || userStore.username || '用户' }}</span>
            <span class="separator">|</span>
            <span class="date-label">今天是：</span>
            <span class="date">{{ currentDate }}</span>
            <span class="time">{{ currentTime }}</span>
          </div>
        </div>
        <div class="header-right">
          <el-dropdown class="user-dropdown">
            <span class="user-info">
              <el-icon><User /></el-icon>
              {{ userStore.realName || userStore.username || '管理员' }}
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="showChangePasswordDialog">
                  <el-icon><Lock /></el-icon>
                  修改密码
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>

    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="400px">
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleChangePassword">确定</el-button>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup lang="ts">
import { computed, ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { changePassword } from '@/api/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const passwordDialogVisible = ref(false)
const passwordFormRef = ref<FormInstance>()
const currentDate = ref('')
const currentTime = ref('')
let timer: number | null = null

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule: any, value: any, callback: any) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const updateDateTime = () => {
  const now = new Date()
  const weekDays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const weekDay = weekDays[now.getDay()]
  
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  const seconds = String(now.getSeconds()).padStart(2, '0')
  
  currentDate.value = `${year}年${month}月${day}日 ${weekDay}`
  currentTime.value = `${hours}:${minutes}:${seconds}`
}

const showChangePasswordDialog = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordDialogVisible.value = true
}

const handleChangePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await changePassword(passwordForm.oldPassword, passwordForm.newPassword)
        ElMessage.success('密码修改成功，请重新登录')
        passwordDialogVisible.value = false
        
        // 清除登录信息并跳转到登录页
        setTimeout(() => {
          userStore.clearUserInfo()
          router.push('/login')
        }, 1500)
      } catch (error: any) {
        ElMessage.error(error.response?.data?.message || '密码修改失败')
      }
    }
  })
}

const handleLogout = () => {
  userStore.clearUserInfo()
  router.push('/login')
}

const hasPermission = (permission: string) => {
  return userStore.hasPermission(permission)
}

onMounted(() => {
  updateDateTime()
  timer = window.setInterval(updateDateTime, 1000)
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8eaf6 100%);
}

/* 侧边栏 */
.sidebar {
  background: linear-gradient(180deg, #2c3e50 0%, #34495e 100%);
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
}

.logo {
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 0 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-img {
  width: 36px;
  height: 36px;
  object-fit: contain;
}

.logo-text {
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 1px;
}

/* 菜单样式 */
.sidebar-menu {
  border: none;
  background: transparent;
  padding: 10px 0;
}

.sidebar-menu :deep(.el-menu-item) {
  color: rgba(255, 255, 255, 0.85);
  margin: 4px 12px;
  border-radius: 8px;
  transition: all 0.3s;
  height: 48px;
  line-height: 48px;
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.15);
  color: #fff;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.sidebar-menu :deep(.el-menu-item .el-icon) {
  font-size: 18px;
  margin-right: 8px;
}

/* 头部 */
.header {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  z-index: 10;
  border-radius: 0 0 16px 16px;
  margin: 0 12px 12px 12px;
}

.header-left {
  display: flex;
  align-items: center;
}

.datetime-info {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
}

.datetime-info .welcome-text {
  color: #303133;
  font-weight: 500;
}

.datetime-info .separator {
  color: #dcdfe6;
  margin: 0 5px;
}

.datetime-info .date-label {
  color: #606266;
}

.datetime-info .date {
  color: #303133;
  font-weight: 500;
}

.datetime-info .time {
  color: #34495e;
  font-weight: 600;
  font-family: 'Arial', sans-serif;
  letter-spacing: 0.5px;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-dropdown {
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 20px;
  background: linear-gradient(135deg, #34495e 0%, #2c3e50 100%);
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
}

.user-info:hover {
  box-shadow: 0 4px 12px rgba(52, 73, 94, 0.3);
  transform: translateY(-1px);
}

.user-info .el-icon {
  font-size: 16px;
}

/* 主内容区 */
.main-content {
  background: transparent;
  padding: 0 12px 12px 12px;
  overflow-y: auto;
}

/* 下拉菜单样式 */
:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
}

:deep(.el-dropdown-menu__item .el-icon) {
  font-size: 16px;
}

/* 滚动条样式 */
.main-content::-webkit-scrollbar {
  width: 8px;
}

.main-content::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.main-content::-webkit-scrollbar-thumb {
  background: linear-gradient(180deg, #34495e 0%, #2c3e50 100%);
  border-radius: 4px;
}

.main-content::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(180deg, #2c3e50 0%, #1a252f 100%);
}
</style>
