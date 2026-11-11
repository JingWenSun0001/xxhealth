<template>
  <div class="auth">
    <div class="auth-brand">
      <div class="logo"><i class="fa fa-heartbeat"></i></div>
      <h1>熙心健康体检</h1>
      <p>专业体检 · 在线预约 · 报告查询</p>
    </div>

    <div class="auth-card">
      <div v-if="error" class="alert">{{ error }}</div>

      <div class="field">
        <label>手机号</label>
        <input
          v-model.trim="userid"
          class="input"
          maxlength="11"
          inputmode="numeric"
          placeholder="请输入 11 位手机号"
          @keyup.enter="login"
        />
      </div>

      <div class="field">
        <label>密码</label>
        <input
          v-model="password"
          class="input"
          type="password"
          placeholder="请输入密码"
          @keyup.enter="login"
        />
      </div>

      <button class="btn btn-primary btn-block" :disabled="loading" @click="login">
        {{ loading ? '登录中…' : '登 录' }}
      </button>

      <p class="tip">测试账号：13800000001 / 123456</p>
      <p class="switch">还没有账号？<router-link to="/register">去注册</router-link></p>
    </div>
  </div>
</template>

<script>
import request from '@/utils/request'
import { setSessionStorage } from '@/common'

export default {
  name: 'Login',
  data() {
    return {
      userid: '',
      password: '',
      error: '',
      loading: false
    }
  },
  methods: {
    async login() {
      this.error = ''
      if (!/^\d{11}$/.test(this.userid)) {
        this.error = '请输入 11 位手机号'
        return
      }
      if (!this.password) {
        this.error = '请输入密码'
        return
      }
      this.loading = true
      try {
        const res = await request.post('/users/getUsersByUserIdByPass', {
          userid: this.userid,
          password: this.password
        })
        const user = res.data
        if (!user || !user.userid) {
          this.error = '手机号或密码错误'
          return
        }
        setSessionStorage('users', user)
        this.$router.replace('/index')
      } catch (e) {
        this.error = '网络异常，请稍后重试'
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.auth {
  min-height: 100vh;
  padding: 0 24px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: linear-gradient(160deg, #12a874 0%, #0b7d56 55%, #076447 100%);
}
.auth-brand {
  margin-bottom: 26px;
  text-align: center;
  color: #fff;
}
.logo {
  width: 64px;
  height: 64px;
  margin: 0 auto 14px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.16);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
}
.auth-brand h1 {
  font-size: 24px;
  letter-spacing: 2px;
}
.auth-brand p {
  margin-top: 8px;
  font-size: 13px;
  opacity: 0.85;
}
.auth-card {
  padding: 22px;
  background: #fff;
  border-radius: 18px;
  box-shadow: 0 14px 34px rgba(4, 60, 40, 0.22);
}
.tip {
  margin-top: 14px;
  text-align: center;
  color: #9aa4b2;
  font-size: 12px;
}
.switch {
  margin-top: 10px;
  text-align: center;
  color: #6b7787;
  font-size: 13px;
}
.switch a {
  color: #10a06a;
  font-weight: 600;
}
</style>
