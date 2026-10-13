import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import client from '@/api/httpClient'
import { useUserStore } from '@/stores/userStore'
import type { TokenResponse } from '@/types/auth'

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref<string | null>(null)

  const isAuthenticated = computed(() => accessToken.value !== null)

  async function register(email: string, password: string, userName: string) {
    await client.post('/api/auth/register', { email, password, userName })
  }

  async function login(email: string, password: string) {
    const response = await client.post<TokenResponse>('/api/auth/login', {
      email,
      password,
    })

    setAccessToken(response.data.accessToken)
  }

  async function logout() {
    const userStore = useUserStore()

    try {
      await client.post('/api/auth/logout')
    } finally {
      clearSession()
      userStore.clearUser()
    }
  }

  async function restoreSession() {
    try {
      const response = await client.post<TokenResponse>('/api/auth/refresh')
      setAccessToken(response.data.accessToken)
    } catch {
      clearSession()
    }
  }

  function setAccessToken(token: string) {
    accessToken.value = token
  }

  function clearSession() {
    accessToken.value = null
  }

  return {
    accessToken,
    isAuthenticated,
    register,
    login,
    logout,
    restoreSession,
    setAccessToken,
    clearSession,
  }
})
