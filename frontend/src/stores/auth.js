import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '../utils/request'
import router from '../router'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  const isLoggedIn = computed(() => !!user.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  async function login(username, password) {
    const res = await request.post('/auth/login', { username, password })
    const { accessToken, refreshToken, userInfo } = res.data
    localStorage.setItem('accessToken', accessToken)
    localStorage.setItem('refreshToken', refreshToken)
    localStorage.setItem('user', JSON.stringify(userInfo))
    user.value = userInfo
    return res
  }

  async function register(data) {
    return await request.post('/auth/register', data)
  }

  async function fetchUser() {
    const res = await request.get('/auth/me')
    user.value = res.data
    localStorage.setItem('user', JSON.stringify(res.data))
  }

  async function logout() {
    try {
      await request.post('/auth/logout')
    } catch (e) {
      // ignore
    }
    localStorage.clear()
    user.value = null
    router.push('/login')
  }

  return { user, isLoggedIn, isAdmin, login, register, fetchUser, logout }
})
