<template>
  <el-header class="navbar">
    <div class="navbar-inner">
      <router-link to="/" class="logo">校园社区</router-link>
      <div class="nav-links">
        <router-link to="/">首页</router-link>
        <template v-if="auth.isLoggedIn">
          <router-link to="/post/create">发帖</router-link>
          <router-link to="/profile">个人中心</router-link>
          <router-link v-if="auth.isAdmin" to="/categories">分类管理</router-link>
          <el-dropdown @command="handleCommand" style="margin-left: 12px;">
            <span class="user-info">
              <el-avatar :size="32" :src="auth.user?.avatar" />
              <span style="margin-left: 6px;">{{ auth.user?.nickname || auth.user?.username }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <router-link to="/login">登录</router-link>
        </template>
      </div>
    </div>
  </el-header>
</template>

<script setup>
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'

const auth = useAuthStore()
const router = useRouter()

function handleCommand(cmd) {
  if (cmd === 'profile') {
    router.push('/profile')
  } else if (cmd === 'logout') {
    auth.logout()
  }
}
</script>

<style scoped>
.navbar {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  position: sticky;
  top: 0;
  z-index: 100;
  padding: 0;
  height: 56px;
}

.navbar-inner {
  max-width: 960px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  padding: 0 16px;
}

.logo {
  font-size: 20px;
  font-weight: 700;
  color: #409eff;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 20px;
}

.nav-links a {
  color: #555;
  font-size: 15px;
}

.nav-links a:hover,
.nav-links a.router-link-exact-active {
  color: #409eff;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
}
</style>
