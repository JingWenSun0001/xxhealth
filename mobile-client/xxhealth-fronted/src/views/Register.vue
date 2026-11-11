<template>
  <div class="app">
    <div class="topbar">
      <button class="back" @click="$router.replace('/login')">
        <i class="fa fa-angle-left"></i>
      </button>
      <h1>注册账号</h1>
    </div>

    <div class="card">
      <div v-if="error" class="alert">{{ error }}</div>
      <div v-if="success" class="alert ok">{{ success }}</div>

      <div class="field">
        <label>手机号</label>
        <input v-model.trim="form.userid" class="input" maxlength="11" inputmode="numeric" placeholder="请输入 11 位手机号" />
      </div>

      <div class="field">
        <label>密码</label>
        <input v-model="form.password" class="input" type="password" placeholder="请设置 6-20 位密码" />
      </div>

      <div class="field">
        <label>确认密码</label>
        <input v-model="confirmPassword" class="input" type="password" placeholder="请再次输入密码" />
      </div>

      <div class="field">
        <label>真实姓名</label>
        <input v-model.trim="form.realname" class="input" maxlength="20" placeholder="请输入真实姓名" />
      </div>

      <div class="field">
        <label>性别</label>
        <div class="radio-group">
          <div class="radio" :class="{ active: form.sex === 1 }" @click="form.sex = 1">男</div>
          <div class="radio" :class="{ active: form.sex === 0 }" @click="form.sex = 0">女</div>
        </div>
      </div>

      <div class="field">
        <label>身份证号</label>
        <input v-model.trim="form.identitycard" class="input" maxlength="18" placeholder="请输入 18 位身份证号" />
      </div>

      <div class="field">
        <label>出生日期</label>
        <input v-model="form.birthday" class="input" type="date" />
      </div>

      <button class="btn btn-primary btn-block" :disabled="loading" @click="register">
        {{ loading ? '提交中…' : '注 册' }}
      </button>

      <p class="switch">已有账号？<router-link to="/login">去登录</router-link></p>
    </div>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'Register',
  data() {
    return {
      form: {
        userid: '',
        password: '',
        realname: '',
        sex: 1,
        identitycard: '',
        birthday: '',
        usertype: 1
      },
      confirmPassword: '',
      error: '',
      success: '',
      loading: false
    }
  },
  methods: {
    validate() {
      if (!/^\d{11}$/.test(this.form.userid)) return '请输入 11 位手机号'
      if (!this.form.password || this.form.password.length < 6) return '密码至少 6 位'
      if (this.form.password !== this.confirmPassword) return '两次输入的密码不一致'
      if (!this.form.realname) return '请输入真实姓名'
      if (this.form.sex !== 0 && this.form.sex !== 1) return '请选择性别'
      if (!/^\d{17}[\dXx]$/.test(this.form.identitycard)) return '请输入 18 位身份证号'
      if (!this.form.birthday) return '请选择出生日期'
      return ''
    },
    async register() {
      this.error = ''
      this.success = ''
      const msg = this.validate()
      if (msg) {
        this.error = msg
        return
      }
      this.loading = true
      try {
        const check = await request.post('/users/getUsersById', { userid: this.form.userid })
        if (check.data && check.data.userid) {
          this.error = '该手机号已注册'
          return
        }
        const res = await request.post('/users/saveUsers', this.form)
        if (res.data === 1) {
          this.success = '注册成功，即将跳转登录页…'
          setTimeout(() => this.$router.replace('/login'), 900)
        } else {
          this.error = '注册失败，该手机号可能已注册'
        }
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
.switch {
  margin-top: 14px;
  text-align: center;
  color: #6b7787;
  font-size: 13px;
}
.switch a {
  color: #10a06a;
  font-weight: 600;
}
</style>
