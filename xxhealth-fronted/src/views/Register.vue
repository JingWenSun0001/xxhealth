<template>
  <div class="register-page">
    <!-- 左侧深色品牌区 -->
    <aside class="brand-panel">
      <div class="brand-top">
        <div class="logo-badge">
          <svg viewBox="0 0 32 32" width="26" height="26">
            <path d="M16 26s-9-5.2-9-11.5A5.4 5.4 0 0 1 16 10a5.4 5.4 0 0 1 9 4.5C25 20.8 16 26 16 26z" fill="#fff" />
            <path d="M7 16h5l2.2-3.6 2.6 6.4 2-3.2H25" fill="none" stroke="#2f6bff" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
        </div>
        <span class="brand-name">熙心健康</span>
      </div>

      <div class="brand-copy">
        <h1>医生注册中心</h1>
        <p>加入我们，共建智能医疗新生态</p>
      </div>

      <!-- 装饰图形 -->
      <div class="shape shape-poly"></div>
      <div class="shape shape-poly2"></div>
      <div class="shape shape-tri"></div>
      <div class="shape shape-circle"></div>
    </aside>

    <!-- 右侧表单区 -->
    <section class="form-panel">
      <div class="form-card">
        <h2>创建账号</h2>
        <p class="form-sub">请填写以下信息完成医生账号注册</p>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="register-form" @submit.prevent>
          <el-form-item label="用户名" prop="doccode">
            <el-input v-model="form.doccode" placeholder="请输入登录账号" clearable>
              <template #prefix><el-icon><User /></el-icon></template>
            </el-input>
          </el-form-item>

          <el-form-item label="真实姓名" prop="realname">
            <el-input v-model="form.realname" placeholder="请输入真实姓名（如：张医生）" clearable>
              <template #prefix><el-icon><UserFilled /></el-icon></template>
            </el-input>
          </el-form-item>

          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入11位手机号" maxlength="11" clearable>
              <template #prefix><el-icon><Iphone /></el-icon></template>
            </el-input>
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" type="password" placeholder="请设置密码" show-password>
              <template #prefix><el-icon><Lock /></el-icon></template>
            </el-input>
          </el-form-item>

          <el-form-item label="确认密码" prop="confirm">
            <el-input v-model="form.confirm" type="password" placeholder="请再次输入密码" show-password>
              <template #prefix><el-icon><Lock /></el-icon></template>
            </el-input>
          </el-form-item>

          <el-button class="submit-btn" type="primary" :loading="loading" @click="handleRegister">
            {{ loading ? '注册中…' : '注 册' }}
          </el-button>
        </el-form>

        <p class="form-footer">
          已有账号？<a class="link" @click="goLogin">返回登录</a>
        </p>
      </div>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { registerDoctor } from '@/api/doctor'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  doccode: '',
  realname: '',
  phone: '',
  password: '',
  confirm: ''
})

const validateConfirm = (rule, value, callback) => {
  if (!value) return callback(new Error('请再次输入密码'))
  if (value !== form.password) return callback(new Error('两次输入的密码不一致'))
  callback()
}

const rules = {
  doccode: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为 3-20 个字符', trigger: 'blur' }
  ],
  realname: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^\d{11}$/, message: '手机号应为 11 位数字', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请设置密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6-20 位', trigger: 'blur' }
  ],
  confirm: [{ required: true, validator: validateConfirm, trigger: 'blur' }]
}

async function handleRegister() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }

  loading.value = true
  try {
    await registerDoctor({
      doccode: form.doccode.trim(),
      realname: form.realname.trim(),
      phone: form.phone.trim(),
      password: form.password
    })
    ElMessage.success('注册成功，请使用新账号登录')
    router.push({ path: '/doctor/login', query: { doccode: form.doccode.trim() } })
  } catch (e) {
    // 拦截器已提示（用户名已存在等）
  } finally {
    loading.value = false
  }
}

function goLogin() {
  router.push('/doctor/login')
}
</script>

<style scoped>
.register-page {
  width: 100%;
  min-height: 100%;
  display: grid;
  grid-template-columns: 1.05fr 0.95fr;
}

/* ---------- 左侧深色品牌区 ---------- */
.brand-panel {
  position: relative;
  overflow: hidden;
  background: #1d2736;
  color: #fff;
  padding: 54px 64px;
  display: flex;
  flex-direction: column;
}

.brand-top {
  display: flex;
  align-items: center;
  gap: 12px;
  position: relative;
  z-index: 2;
}

.logo-badge {
  width: 44px;
  height: 44px;
  border-radius: 13px;
  background: linear-gradient(135deg, #2f6bff, #4f8bff);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 20px rgba(47, 107, 255, 0.35);
}

.brand-name {
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.brand-copy {
  position: relative;
  z-index: 2;
  margin-top: auto;
  margin-bottom: 150px;
}

.brand-copy h1 {
  font-size: 40px;
  font-weight: 800;
  letter-spacing: 2px;
  line-height: 1.25;
}

.brand-copy p {
  margin-top: 18px;
  font-size: 14px;
  color: #8f9bb0;
  letter-spacing: 1px;
}

/* 装饰图形 */
.shape {
  position: absolute;
  pointer-events: none;
}

.shape-poly {
  right: -60px;
  bottom: 40px;
  width: 460px;
  height: 400px;
  background: rgba(255, 255, 255, 0.045);
  clip-path: polygon(30% 0, 100% 18%, 82% 100%, 0 78%);
  transform: rotate(-6deg);
}

.shape-poly2 {
  right: 40px;
  bottom: -60px;
  width: 340px;
  height: 300px;
  background: rgba(255, 255, 255, 0.035);
  clip-path: polygon(18% 12%, 88% 0, 100% 74%, 24% 100%);
  transform: rotate(8deg);
}

.shape-tri {
  right: 210px;
  bottom: 0;
  width: 150px;
  height: 130px;
  background: rgba(255, 255, 255, 0.04);
  clip-path: polygon(50% 0, 100% 100%, 0 100%);
}

.shape-circle {
  right: 130px;
  bottom: 70px;
  width: 190px;
  height: 190px;
  border-radius: 50%;
  background: rgba(120, 42, 42, 0.55);
}

/* ---------- 右侧表单区 ---------- */
.form-panel {
  background: #eef1f6;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
}

.form-card {
  width: 100%;
  max-width: 470px;
  background: #fff;
  border-radius: 18px;
  box-shadow: 0 18px 50px rgba(31, 58, 110, 0.12);
  padding: 40px 44px 32px;
}

.form-card h2 {
  text-align: center;
  font-size: 22px;
  font-weight: 800;
  color: #1f2a3d;
  letter-spacing: 1px;
}

.form-sub {
  margin-top: 10px;
  margin-bottom: 26px;
  text-align: center;
  font-size: 12.5px;
  color: #93a0b5;
}

.register-form :deep(.el-form-item__label) {
  font-size: 13px;
  color: #4a5568;
  padding-bottom: 4px;
}

.register-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.register-form :deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #e3e8f0 inset;
  padding: 2px 12px;
}

.register-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #2f6bff inset;
}

.submit-btn {
  width: 100%;
  height: 44px;
  margin-top: 8px;
  border-radius: 8px;
  font-size: 15px;
  letter-spacing: 3px;
  background: #2f6bff;
  border-color: #2f6bff;
  box-shadow: 0 10px 22px rgba(47, 107, 255, 0.28);
}

.form-footer {
  margin-top: 22px;
  text-align: center;
  font-size: 12.5px;
  color: #93a0b5;
}

.link {
  color: #2f6bff;
  cursor: pointer;
  font-weight: 600;
}

@media (max-width: 900px) {
  .register-page {
    grid-template-columns: 1fr;
  }
  .brand-panel {
    display: none;
  }
}
</style>
