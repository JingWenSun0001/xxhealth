<template>
  <div class="app">
    <div v-if="user" class="profile">
      <div class="avatar">{{ avatarText }}</div>
      <div class="profile-main">
        <h2>{{ user.realname }}</h2>
        <p>{{ user.userid }}</p>
      </div>
      <span class="tag">{{ userTypeText }}</span>
    </div>

    <div v-if="user" class="card info">
      <div class="row"><span>手机号</span><b>{{ user.userid }}</b></div>
      <div class="row"><span>性别</span><b>{{ sexText }}</b></div>
      <div class="row"><span>身份证号</span><b>{{ idcardMask }}</b></div>
      <div class="row"><span>出生日期</span><b>{{ user.birthday || '未填写' }}</b></div>
    </div>

    <div class="card menu">
      <router-link to="/messages" class="menu-item">
        <i class="fa fa-bell-o"></i><span>健康消息</span><i class="fa fa-angle-right arrow"></i>
      </router-link>
      <router-link to="/appointmentList" class="menu-item">
        <i class="fa fa-calendar-check-o"></i><span>我的预约</span><i class="fa fa-angle-right arrow"></i>
      </router-link>
      <router-link to="/reportList" class="menu-item">
        <i class="fa fa-file-text-o"></i><span>体检报告</span><i class="fa fa-angle-right arrow"></i>
      </router-link>
      <router-link to="/appointment" class="menu-item">
        <i class="fa fa-plus-circle"></i><span>预约体检</span><i class="fa fa-angle-right arrow"></i>
      </router-link>
    </div>

    <button class="btn btn-block logout" @click="logout">退出登录</button>

    <TabBar />
  </div>
</template>

<script>
import { getSessionStorage, removeSessionStorage } from '@/common'
import TabBar from '@/components/TabBar.vue'

export default {
  name: 'Personal',
  components: { TabBar },
  data() {
    return { user: null }
  },
  computed: {
    avatarText() {
      return this.user && this.user.realname ? this.user.realname.charAt(0) : '用'
    },
    sexText() {
      return this.user && this.user.sex === 1 ? '男' : '女'
    },
    idcardMask() {
      const id = this.user && this.user.identitycard ? this.user.identitycard : ''
      if (id.length < 10) return id || '未填写'
      return id.slice(0, 6) + '********' + id.slice(-4)
    },
    userTypeText() {
      const map = { 1: '普通用户', 2: '东软员工', 3: '其他' }
      return (this.user && map[this.user.usertype]) || '普通用户'
    }
  },
  created() {
    this.user = getSessionStorage('users')
    if (!this.user) {
      this.$router.replace('/login')
    }
  },
  methods: {
    logout() {
      if (!window.confirm('确定要退出登录吗？')) return
      removeSessionStorage('users')
      this.$router.replace('/login')
    }
  }
}
</script>

<style scoped>
.profile {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 38px 20px 46px;
  background: linear-gradient(135deg, #12a874, #0b7d56);
  color: #fff;
}
.avatar {
  width: 58px;
  height: 58px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 600;
}
.profile-main {
  flex: 1;
}
.profile-main h2 {
  font-size: 19px;
}
.profile-main p {
  margin-top: 4px;
  font-size: 13px;
  opacity: 0.85;
}
.profile .tag {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
}
.info {
  margin-top: -28px;
}
.row {
  display: flex;
  justify-content: space-between;
  padding: 11px 0;
  border-bottom: 1px solid #f2f5f8;
  font-size: 14px;
}
.row:last-child {
  border-bottom: none;
}
.row span {
  color: #8a94a6;
}
.menu {
  padding: 4px 16px;
}
.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  height: 50px;
  border-bottom: 1px solid #f2f5f8;
  color: #1f2d3d;
  font-size: 15px;
}
.menu-item:last-child {
  border-bottom: none;
}
.menu-item > i:first-child {
  width: 22px;
  color: #10a06a;
  font-size: 17px;
  text-align: center;
}
.menu-item span {
  flex: 1;
}
.menu-item .arrow {
  color: #c3cad4;
}
.logout {
  width: calc(100% - 32px);
  margin: 20px 16px 0;
  background: #fff;
  color: #e5484d;
}
</style>
