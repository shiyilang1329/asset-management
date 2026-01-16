<template>
  <div class="login-container">
    <!-- 左侧Logo和标题区域 -->
    <div class="login-left">
      <div class="logo-section">
        <div class="logo-wrapper">
          <img src="/logo.png" alt="Logo" class="logo" />
        </div>
        <h1 class="system-title">资产管理系统</h1>
        <p class="system-subtitle">Asset Management System</p>
      </div>
    </div>

    <!-- 右侧登录表单区域 -->
    <div class="login-right">
      <div class="login-box">
        <div class="login-header">
          <h2 class="login-title">欢迎登录</h2>
          <p class="login-desc">请输入您的账号和密码</p>
        </div>
        
        <el-form :model="loginForm" :rules="rules" ref="formRef" class="login-form">
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入账号"
              prefix-icon="User"
              size="large"
              clearable
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="Lock"
              size="large"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-form-item prop="captchaCode">
            <div class="captcha-wrapper">
              <el-input
                v-model="loginForm.captchaCode"
                placeholder="请输入验证码"
                prefix-icon="Key"
                size="large"
                clearable
                maxlength="4"
                @keyup.enter="handleLogin"
              />
              <div class="captcha-image" @click="refreshCaptcha">
                <img v-if="captchaImage" :src="captchaImage" alt="验证码" />
                <span v-else class="captcha-loading">加载中...</span>
              </div>
            </div>
          </el-form-item>
          <el-form-item class="remember-item">
            <el-checkbox v-model="rememberMe">记住密码</el-checkbox>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="large" class="login-btn" @click="handleLogin">
              登 录
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="login-footer">
          <span class="copyright">© 2023 资产管理系统 All Rights Reserved</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { login, getCaptcha } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const formRef = ref<FormInstance>()
const userStore = useUserStore()

const loginForm = reactive({
  username: '',
  password: '',
  captchaId: '',
  captchaCode: ''
})

const captchaImage = ref('')
const rememberMe = ref(false)

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captchaCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 4, message: '验证码为4位数字', trigger: 'blur' }
  ]
}

// 获取验证码
const refreshCaptcha = async () => {
  try {
    const res = await getCaptcha()
    loginForm.captchaId = res.data.captchaId
    captchaImage.value = res.data.captchaImage
  } catch (error) {
    console.error('获取验证码失败', error)
    ElMessage.error('获取验证码失败')
  }
}

const handleLogin = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = await login(loginForm)
        userStore.setUserInfo(res.data)
        ElMessage.success('登录成功')
        router.push('/')
      } catch (error: any) {
        console.error('登录失败', error)
        // 登录失败后刷新验证码
        refreshCaptcha()
        loginForm.captchaCode = ''
      }
    }
  })
}

// 页面加载时获取验证码
onMounted(() => {
  refreshCaptcha()
})
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
  position: relative;
  overflow: hidden;
}

/* 背景装饰 */
.login-container::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -10%;
  width: 80%;
  height: 150%;
  background: rgba(255, 255, 255, 0.03);
  transform: rotate(-15deg);
  z-index: 1;
}

/* 左侧Logo区域 */
.login-left {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 2;
  padding: 40px;
}

.logo-section {
  text-align: center;
  color: white;
  animation: fadeInLeft 0.8s ease-out;
}

@keyframes fadeInLeft {
  from {
    opacity: 0;
    transform: translateX(-30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.logo-wrapper {
  margin-bottom: 40px;
}

.logo {
  width: 120px;
  height: 120px;
  object-fit: contain;
  filter: drop-shadow(0 8px 16px rgba(0, 0, 0, 0.2));
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.system-title {
  font-size: 48px;
  font-weight: 700;
  margin-bottom: 16px;
  text-shadow: 2px 2px 8px rgba(0, 0, 0, 0.3);
  letter-spacing: 4px;
}

.system-subtitle {
  font-size: 20px;
  opacity: 0.95;
  text-shadow: 1px 1px 4px rgba(0, 0, 0, 0.2);
  letter-spacing: 2px;
  font-weight: 300;
}

/* 右侧登录表单区域 */
.login-right {
  width: 500px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;
  position: relative;
  z-index: 2;
  box-shadow: -10px 0 30px rgba(0, 0, 0, 0.1);
  animation: fadeInRight 0.8s ease-out;
}

@keyframes fadeInRight {
  from {
    opacity: 0;
    transform: translateX(30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.login-box {
  width: 380px;
  padding: 50px 0;
}

.login-header {
  margin-bottom: 40px;
}

.login-title {
  font-size: 32px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 12px;
}

.login-desc {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.login-form {
  margin-bottom: 20px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 24px;
}

.login-form :deep(.el-input__wrapper) {
  padding: 14px 16px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}

.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 2px 12px rgba(52, 73, 94, 0.15);
}

.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 2px 12px rgba(52, 73, 94, 0.25);
}

/* 验证码样式 */
.captcha-wrapper {
  display: flex;
  gap: 12px;
  align-items: center;
}

.captcha-wrapper :deep(.el-input) {
  flex: 0 0 220px;
  width: 220px;
}

.captcha-image {
  width: 120px;
  height: 40px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  border: 1px solid #dcdfe6;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  transition: all 0.3s;
}

.captcha-image:hover {
  border-color: #34495e;
  box-shadow: 0 2px 8px rgba(52, 73, 94, 0.15);
}

.captcha-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.captcha-loading {
  font-size: 12px;
  color: #909399;
}

.remember-item {
  margin-bottom: 28px !important;
}

.remember-item :deep(.el-checkbox__label) {
  color: #606266;
  font-size: 14px;
}

.login-btn {
  width: 100%;
  padding: 16px 0;
  font-size: 16px;
  font-weight: 500;
  border-radius: 8px;
  letter-spacing: 2px;
  background: linear-gradient(135deg, #34495e 0%, #2c3e50 100%);
  border: none;
  transition: all 0.3s;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(52, 73, 94, 0.4);
  background: linear-gradient(135deg, #2c3e50 0%, #1a252f 100%);
}

.login-btn:active {
  transform: translateY(0);
}

.login-footer {
  text-align: center;
  margin-top: 40px;
  padding-top: 30px;
  border-top: 1px solid #e4e7ed;
}

.copyright {
  font-size: 13px;
  color: #909399;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .login-left {
    display: none;
  }
  
  .login-right {
    width: 100%;
  }
  
  .login-container::before {
    display: none;
  }
}

@media (max-width: 768px) {
  .login-right {
    padding: 20px;
  }
  
  .login-box {
    width: 100%;
    padding: 30px 0;
  }
  
  .login-title {
    font-size: 24px;
  }
  
  .login-desc {
    font-size: 13px;
  }
  
  .captcha-wrapper {
    flex-direction: column;
    gap: 10px;
  }
  
  .captcha-wrapper :deep(.el-input) {
    width: 100%;
  }
  
  .captcha-image {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .login-right {
    padding: 15px;
  }
  
  .login-box {
    padding: 20px 0;
  }
  
  .login-title {
    font-size: 22px;
  }
  
  .login-header {
    margin-bottom: 30px;
  }
  
  .login-form :deep(.el-form-item) {
    margin-bottom: 20px;
  }
  
  .login-btn {
    padding: 14px 0;
    font-size: 15px;
  }
}
</style>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
  position: relative;
  overflow: hidden;
}

/* 背景装饰 */
.login-container::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -10%;
  width: 80%;
  height: 150%;
  background: rgba(255, 255, 255, 0.03);
  transform: rotate(-15deg);
  z-index: 1;
}

/* 左侧Logo区域 */
.login-left {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 2;
  padding: 40px;
}

.logo-section {
  text-align: center;
  color: white;
  animation: fadeInLeft 0.8s ease-out;
}

@keyframes fadeInLeft {
  from {
    opacity: 0;
    transform: translateX(-30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.logo-wrapper {
  margin-bottom: 40px;
}

.logo {
  width: 120px;
  height: 120px;
  object-fit: contain;
  filter: drop-shadow(0 8px 16px rgba(0, 0, 0, 0.2));
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.system-title {
  font-size: 48px;
  font-weight: 700;
  margin-bottom: 16px;
  text-shadow: 2px 2px 8px rgba(0, 0, 0, 0.3);
  letter-spacing: 4px;
}

.system-subtitle {
  font-size: 20px;
  opacity: 0.95;
  text-shadow: 1px 1px 4px rgba(0, 0, 0, 0.2);
  letter-spacing: 2px;
  font-weight: 300;
}

/* 右侧登录表单区域 */
.login-right {
  width: 500px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;
  position: relative;
  z-index: 2;
  box-shadow: -10px 0 30px rgba(0, 0, 0, 0.1);
  animation: fadeInRight 0.8s ease-out;
}

@keyframes fadeInRight {
  from {
    opacity: 0;
    transform: translateX(30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.login-box {
  width: 380px;
  padding: 50px 0;
}

.login-header {
  margin-bottom: 40px;
}

.login-title {
  font-size: 32px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 12px;
}

.login-desc {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.login-form {
  margin-bottom: 20px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 24px;
}

.login-form :deep(.el-input__wrapper) {
  padding: 14px 16px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}

.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 2px 12px rgba(52, 73, 94, 0.15);
}

.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 2px 12px rgba(52, 73, 94, 0.25);
}

.remember-item {
  margin-bottom: 28px !important;
}

.remember-item :deep(.el-checkbox__label) {
  color: #606266;
  font-size: 14px;
}

.login-btn {
  width: 100%;
  padding: 16px 0;
  font-size: 16px;
  font-weight: 500;
  border-radius: 8px;
  letter-spacing: 2px;
  background: linear-gradient(135deg, #34495e 0%, #2c3e50 100%);
  border: none;
  transition: all 0.3s;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(52, 73, 94, 0.4);
  background: linear-gradient(135deg, #2c3e50 0%, #1a252f 100%);
}

.login-btn:active {
  transform: translateY(0);
}

.login-footer {
  text-align: center;
  margin-top: 40px;
  padding-top: 30px;
  border-top: 1px solid #e4e7ed;
}

.copyright {
  font-size: 13px;
  color: #909399;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .login-left {
    display: none;
  }
  
  .login-right {
    width: 100%;
  }
  
  .login-container::before {
    display: none;
  }
}

@media (max-width: 768px) {
  .login-right {
    padding: 20px;
  }
  
  .login-box {
    width: 100%;
    padding: 30px 0;
  }
  
  .login-title {
    font-size: 24px;
  }
  
  .login-desc {
    font-size: 13px;
  }
}

@media (max-width: 480px) {
  .login-right {
    padding: 15px;
  }
  
  .login-box {
    padding: 20px 0;
  }
  
  .login-title {
    font-size: 22px;
  }
  
  .login-header {
    margin-bottom: 30px;
  }
  
  .login-form :deep(.el-form-item) {
    margin-bottom: 20px;
  }
  
  .login-btn {
    padding: 14px 0;
    font-size: 15px;
  }
}
</style>
