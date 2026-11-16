<template>
  <div class="setting-page">
    <section class="setting-grid">
      <!-- 左侧导航 -->
      <aside class="card nav-card">
        <div class="nav-head">系统设置</div>
        <ul class="nav-list">
          <li
            v-for="item in navs"
            :key="item.key"
            class="nav-item"
            :class="{ active: active === item.key }"
            @click="active = item.key"
          >
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.label }}</span>
          </li>
        </ul>
      </aside>

      <!-- 右侧内容 -->
      <div class="card content-card">
        <!-- 个人资料 -->
        <template v-if="active === 'profile'">
          <div class="content-head">
            <h3>个人资料</h3>
            <span class="content-sub">修改后仅在本机生效（账号信息待用户中心接口接入）</span>
          </div>

          <div class="profile-banner">
            <div class="profile-avatar">{{ (profile.realname || '医').slice(0, 1) }}</div>
            <div>
              <p class="profile-name">{{ profile.realname || '医生' }}</p>
              <p class="profile-code">医生编码：{{ doctor.doccode || '—' }}</p>
            </div>
            <span class="tag tag-info">{{ deptLabel(profile.deptno) }}</span>
          </div>

          <el-form label-position="top" class="setting-form">
            <div class="form-grid">
              <el-form-item label="医生编码">
                <el-input :model-value="doctor.doccode" disabled />
              </el-form-item>
              <el-form-item label="真实姓名">
                <el-input v-model="profile.realname" placeholder="请输入姓名" />
              </el-form-item>
              <el-form-item label="所属科室">
                <el-select v-model="profile.deptno" class="full">
                  <el-option label="检验科" :value="1" />
                  <el-option label="内科" :value="2" />
                  <el-option label="外科" :value="3" />
                </el-select>
              </el-form-item>
              <el-form-item label="性别">
                <el-radio-group v-model="profile.sex">
                  <el-radio :value="1">男</el-radio>
                  <el-radio :value="0">女</el-radio>
                </el-radio-group>
              </el-form-item>
            </div>
          </el-form>

          <div class="content-actions">
            <el-button type="primary" :loading="savingProfile" @click="saveProfile">保存修改</el-button>
          </div>
        </template>

        <!-- 账号安全 -->
        <template v-else-if="active === 'security'">
          <div class="content-head">
            <h3>账号安全</h3>
            <span class="content-sub">定期修改密码可以提升账号安全性</span>
          </div>

          <div class="security-tip">
            <el-icon><Lock /></el-icon>
            当前账号：{{ doctor.doccode || '—' }}，修改成功后请使用新密码重新登录。
          </div>

          <el-form label-position="top" class="setting-form narrow">
            <el-form-item label="原密码">
              <el-input v-model="pwd.old" type="password" show-password placeholder="请输入原密码" />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="pwd.new" type="password" show-password placeholder="至少 6 位" />
            </el-form-item>
            <el-form-item label="确认新密码">
              <el-input v-model="pwd.confirm" type="password" show-password placeholder="请再次输入新密码" />
            </el-form-item>
          </el-form>

          <div class="content-actions">
            <el-button type="primary" :loading="savingPwd" @click="savePassword">保存修改</el-button>
          </div>
        </template>

        <!-- 通知设置 -->
        <template v-else-if="active === 'notify'">
          <div class="content-head">
            <h3>通知设置</h3>
            <span class="content-sub">设置后保存在本机，用于演示</span>
          </div>

          <div class="switch-list">
            <div v-for="item in notifyOptions" :key="item.key" class="switch-row">
              <div class="switch-info">
                <p>{{ item.label }}</p>
                <small>{{ item.desc }}</small>
              </div>
              <el-switch v-model="notify[item.key]" @change="saveNotify" />
            </div>
          </div>
        </template>

        <!-- 关于系统 -->
        <template v-else>
          <div class="content-head">
            <h3>关于系统</h3>
          </div>

          <div class="about-box">
            <div class="about-logo">熙</div>
            <div>
              <h4>熙心健康体检管理系统 · 医生端</h4>
              <p>版本 v1.0.0</p>
            </div>
          </div>

          <div class="about-grid">
            <div class="about-item"><span>后端</span><strong>Spring Boot 2.6.4 + MyBatis-Plus 3.5.5</strong></div>
            <div class="about-item"><span>数据库</span><strong>MySQL 8</strong></div>
            <div class="about-item"><span>前端</span><strong>Vue 3 + Element Plus + Vue Router</strong></div>
            <div class="about-item"><span>负责模块</span><strong>医生端体检报告 / 套餐 / 日程 / 消息</strong></div>
          </div>

          <p class="copyright">© 2026 熙心健康体检管理系统 · 仅供课程实训使用</p>
        </template>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getSessionStorage, setSessionStorage } from '@/common'
import { updateDoctor, updatePassword } from '@/api/doctor'

const doctor = getSessionStorage('doctor') || {}
const active = ref('profile')

const navs = [
  { key: 'profile', label: '个人资料', icon: 'User' },
  { key: 'security', label: '账号安全', icon: 'Lock' },
  { key: 'notify', label: '通知设置', icon: 'Bell' },
  { key: 'about', label: '关于系统', icon: 'InfoFilled' }
]

const profile = reactive({
  realname: doctor.realname || '',
  deptno: doctor.deptno || 2,
  sex: doctor.sex === 0 ? 0 : 1
})

const pwd = reactive({ old: '', new: '', confirm: '' })

const savingProfile = ref(false)
const savingPwd = ref(false)

const notify = reactive({
  appointment: true,
  report: true,
  abnormal: true,
  system: false
})

const notifyOptions = [
  { key: 'appointment', label: '预约提醒', desc: '有新预约时提醒我' },
  { key: 'report', label: '报告通知', desc: '体检报告生成时提醒我' },
  { key: 'abnormal', label: '异常提醒', desc: '检查结果异常时提醒我' },
  { key: 'system', label: '系统通知', desc: '系统维护、版本更新等通知' }
]

function deptLabel(deptno) {
  return { 1: '检验科', 2: '内科', 3: '外科' }[deptno] || '科室未设置'
}

async function saveProfile() {
  if (!profile.realname || !profile.realname.trim()) {
    ElMessage.warning('请输入真实姓名')
    return
  }
  savingProfile.value = true
  try {
    await updateDoctor({
      docid: doctor.docid,
      realname: profile.realname.trim(),
      sex: profile.sex,
      deptno: profile.deptno
    })
    const data = {
      ...doctor,
      realname: profile.realname.trim(),
      deptno: profile.deptno,
      sex: profile.sex
    }
    setSessionStorage('doctor', data)
    ElMessage.success('个人资料已保存')
  } catch (e) {
    // 拦截器已提示
  } finally {
    savingProfile.value = false
  }
}

async function savePassword() {
  if (!pwd.old || !pwd.new || !pwd.confirm) {
    ElMessage.warning('请完整填写密码信息')
    return
  }
  if (pwd.new.length < 6) {
    ElMessage.warning('新密码至少 6 位')
    return
  }
  if (pwd.new !== pwd.confirm) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  savingPwd.value = true
  try {
    await updatePassword({
      docid: doctor.docid,
      oldPassword: pwd.old,
      newPassword: pwd.new
    })
    ElMessage.success('密码修改成功')
    pwd.old = ''
    pwd.new = ''
    pwd.confirm = ''
  } catch (e) {
    // 拦截器已提示（原密码错误等）
  } finally {
    savingPwd.value = false
  }
}

function loadNotify() {
  try {
    const raw = localStorage.getItem('xxhealth:notify')
    if (raw) Object.assign(notify, JSON.parse(raw))
  } catch (e) {
    // ignore
  }
}

function saveNotify() {
  try {
    localStorage.setItem('xxhealth:notify', JSON.stringify(notify))
  } catch (e) {
    // ignore
  }
  ElMessage.success('通知设置已保存')
}

onMounted(loadNotify)
</script>

<style scoped>
.setting-grid {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  gap: 16px;
  align-items: start;
}

.nav-card {
  padding: 16px 12px;
}

.nav-head {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-1);
  padding: 0 10px 12px;
}

.nav-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 11px 12px;
  border-radius: 10px;
  font-size: 13px;
  color: var(--text-2);
  cursor: pointer;
  border: 1px solid transparent;
}

.nav-item:hover {
  background: #f7faff;
}

.nav-item.active {
  background: var(--brand-light);
  color: var(--brand);
  font-weight: 600;
  border-color: #cfe0ff;
}

.content-card {
  padding: 22px 26px 26px;
  min-height: 520px;
}

.content-head {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 20px;
}

.content-head h3 {
  font-size: 17px;
  font-weight: 800;
  color: var(--text-1);
}

.content-sub {
  font-size: 12px;
  color: var(--text-3);
}

.profile-banner {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px 20px;
  border-radius: 16px;
  background: linear-gradient(120deg, #f3f8ff, #eaf2ff);
  border: 1px solid #e1ebff;
  margin-bottom: 22px;
}

.profile-avatar {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: linear-gradient(135deg, #5aa0ff, #2f6bff);
  color: #fff;
  font-size: 22px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.profile-name {
  font-size: 16px;
  font-weight: 800;
  color: var(--text-1);
}

.profile-code {
  margin-top: 5px;
  font-size: 12px;
  color: var(--text-3);
}

.profile-banner .tag {
  margin-left: auto;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 26px;
  max-width: 720px;
}

.setting-form.narrow {
  max-width: 420px;
}

.full {
  width: 100%;
}

.content-actions {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
  max-width: 720px;
}

.security-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  border-radius: 12px;
  background: #fff8ee;
  border: 1px solid #ffe6c7;
  color: #b26b00;
  font-size: 12.5px;
  margin-bottom: 22px;
}

.switch-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-width: 620px;
}

.switch-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 18px;
  border-radius: 14px;
  background: #f8fafe;
  border: 1px solid var(--line);
}

.switch-info p {
  font-size: 13.5px;
  font-weight: 600;
  color: var(--text-1);
}

.switch-info small {
  display: block;
  margin-top: 4px;
  font-size: 11.5px;
  color: var(--text-3);
}

.about-box {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px 20px;
  border-radius: 16px;
  background: #f8fafe;
  border: 1px solid var(--line);
}

.about-logo {
  width: 54px;
  height: 54px;
  border-radius: 15px;
  background: linear-gradient(135deg, #2f6bff, #6aa1ff);
  color: #fff;
  font-size: 24px;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
}

.about-box h4 {
  font-size: 15px;
  font-weight: 800;
  color: var(--text-1);
}

.about-box p {
  margin-top: 5px;
  font-size: 12px;
  color: var(--text-3);
}

.about-grid {
  margin-top: 18px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px 20px;
}

.about-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-radius: 12px;
  background: #f8fafe;
  font-size: 12.5px;
  color: var(--text-2);
}

.about-item strong {
  color: var(--text-1);
  font-weight: 600;
}

.copyright {
  margin-top: 26px;
  font-size: 11.5px;
  color: var(--text-3);
}
</style>
