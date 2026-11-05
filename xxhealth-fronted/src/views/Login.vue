<template>
  <div class="login-page">
    <div class="login-shell">
      <!-- 左侧品牌面板 -->
      <section class="brand-panel">
        <div class="brand-top">
          <div class="logo-badge">
            <svg viewBox="0 0 32 32" width="30" height="30">
              <path d="M16 26s-9-5.2-9-11.5A5.4 5.4 0 0 1 16 10a5.4 5.4 0 0 1 9 4.5C25 20.8 16 26 16 26z" fill="#fff" />
              <path d="M7 16h5l2.2-3.6 2.6 6.4 2-3.2H25" fill="none" stroke="#2f6bff" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
          </div>
          <div>
            <p class="brand-name">熙心健康</p>
            <p class="brand-sub">体检管理系统 · 医生端</p>
          </div>
        </div>

        <div class="brand-copy">
          <h2>专业体检<br />让健康更有保障</h2>
          <p>编辑体检报告、录入检查结果、跟踪异常指标，一站式完成医生端全部工作。</p>
        </div>

        <div class="brand-stats">
          <div class="brand-stat">
            <strong>8,230</strong>
            <span>累计患者</span>
          </div>
          <div class="brand-stat">
            <strong>56</strong>
            <span>今日预约</span>
          </div>
          <div class="brand-stat">
            <strong>12</strong>
            <span>待审核报告</span>
          </div>
        </div>

        <svg class="pulse-line" viewBox="0 0 520 120" preserveAspectRatio="none">
          <polyline
            points="0,70 90,70 120,70 140,34 165,96 190,16 215,70 300,70 330,70 350,44 372,80 392,70 520,70"
            fill="none"
            stroke="rgba(255,255,255,0.55)"
            stroke-width="2.4"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
      </section>

      <!-- 右侧登录表单 -->
      <section class="form-panel">
        <div class="form-head">
          <h3>医生登录</h3>
          <p>请使用医院分配的医生编码与密码登录</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" size="large" @submit.prevent>
          <el-form-item label="医生编码" prop="doccode">
            <el-input v-model="form.doccode" placeholder="请输入医生编码，如 D001" clearable @keyup.enter="handleLogin">
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入登录密码"
              show-password
              @keyup.enter="handleLogin"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-button class="login-btn" type="primary" :loading="loading" @click="handleLogin">
            {{ loading ? '登录中…' : '登 录' }}
          </el-button>
        </el-form>

        <div class="form-footer">
          <span><el-icon><InfoFilled /></el-icon> 账号由体检管理系统统一分配</span>
        </div>
      </section>
    </div>

    <p class="copyright">© {{ year }} 熙心健康体检管理系统 · 医生端</p>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getDoctorByCodeByPass } from '@/api/doctor'
import { setSessionStorage } from '@/common'

const route = useRoute()
const router = useRouter()

const year = new Date().getFullYear()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  doccode: '',
  password: ''
})

const rules = {
  doccode: [{ required: true, message: '请输入医生编码', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }

  loading.value = true
  try {
    const doctor = await getDoctorByCodeByPass({
      doccode: form.doccode.trim(),
      password: form.password
    })
    setSessionStorage('doctor', doctor)
    ElMessage.success(`欢迎回来，${doctor.realname || '医生'}`)
    const redirect = route.query.redirect
    router.push(typeof redirect === 'string' && redirect ? redirect : '/report/create')
  } catch (e) {
    // 错误提示已由 axios 拦截器统一处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  position: relative;
  width: 100%;
  min-height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  background:
    radial-gradient(900px 480px at 12% 8%, #dce9ff 0%, transparent 60%),
    radial-gradient(760px 460px at 92% 92%, #e3edff 0%, transparent 62%),
    #f3f7ff;
}

.login-shell {
  width: 100%;
  max-width: 980px;
  min-height: 560px;
  display: grid;
  grid-template-columns: 1.08fr 0.92fr;
  background: #fff;
  border-radius: 26px;
  overflow: hidden;
  box-shadow: 0 30px 80px rgba(24, 52, 110, 0.16);
}

/* 左侧品牌 */
.brand-panel {
  position: relative;
  overflow: hidden;
  padding: 40px 44px 36px;
  background: linear-gradient(150deg, #2f6bff 0%, #3f7dff 52%, #6aa1ff 100%);
  color: #fff;
  display: flex;
  flex-direction: column;
}

.brand-panel::before,
.brand-panel::after {
  content: '';
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.brand-panel::before {
  width: 300px;
  height: 300px;
  top: -140px;
  right: -110px;
}

.brand-panel::after {
  width: 200px;
  height: 200px;
  bottom: -100px;
  left: -60px;
}

.brand-top {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
  z-index: 1;
}

.logo-badge {
  width: 48px;
  height: 48px;
  border-radius: 15px;
  background: rgba(255, 255, 255, 0.18);
  display: flex;
  align-items: center;
  justify-content: center;
}

.brand-name {
  font-size: 19px;
  font-weight: 800;
  letter-spacing: 1px;
}

.brand-sub {
  margin-top: 3px;
  font-size: 12px;
  opacity: 0.82;
  letter-spacing: 0.6px;
}

.brand-copy {
  position: relative;
  margin-top: 54px;
  z-index: 1;
}

.brand-copy h2 {
  font-size: 34px;
  line-height: 1.32;
  font-weight: 800;
  letter-spacing: 1px;
}

.brand-copy p {
  margin-top: 16px;
  font-size: 13.5px;
  line-height: 1.85;
  max-width: 340px;
  opacity: 0.86;
}

.brand-stats {
  position: relative;
  margin-top: auto;
  display: flex;
  gap: 34px;
  z-index: 1;
}

.brand-stat strong {
  display: block;
  font-size: 24px;
  font-weight: 800;
  letter-spacing: 0.5px;
}

.brand-stat span {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  opacity: 0.78;
}

.pulse-line {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 96px;
  width: 100%;
  height: 110px;
  opacity: 0.8;
}

/* 右侧表单 */
.form-panel {
  padding: 58px 52px 44px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.form-head h3 {
  font-size: 26px;
  font-weight: 800;
  color: var(--text-1);
  letter-spacing: 0.5px;
}

.form-head p {
  margin-top: 10px;
  margin-bottom: 30px;
  font-size: 13px;
  color: var(--text-3);
}

.login-btn {
  width: 100%;
  height: 46px;
  margin-top: 6px;
  font-size: 15px;
  letter-spacing: 4px;
  border-radius: 12px;
  box-shadow: 0 12px 24px rgba(47, 107, 255, 0.28);
}

.form-footer {
  margin-top: 26px;
  font-size: 12px;
  color: var(--text-3);
  display: flex;
  align-items: center;
  gap: 6px;
}

.form-footer span {
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

.copyright {
  margin-top: 26px;
  font-size: 12px;
  color: #9aa7ba;
}

@media (max-width: 900px) {
  .login-shell {
    grid-template-columns: 1fr;
    max-width: 460px;
    min-height: auto;
  }
  .brand-panel {
    display: none;
  }
  .form-panel {
    padding: 42px 32px;
  }
}
</style>
