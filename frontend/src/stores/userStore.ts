import { defineStore } from 'pinia'
import { ref } from 'vue'
import client from '@/api/httpClient'
import { useAuthStore } from '@/stores/authStore'
import type { UserResponse } from '@/types/user'
import type { TokenResponse } from '@/types/auth'

export const useUserStore = defineStore('user', () => {
  const user = ref<UserResponse | null>(null)

  async function loadUser() {
    const response = await client.get<UserResponse>('/api/users/me')
    user.value = response.data
  }

  async function updateUserName(password: string, userName: string) {
    const response = await client.patch<UserResponse>('/api/users/me/username', {
      password,
      userName,
    })
    user.value = response.data
  }

  async function changePassword(password: string, newPassword: string) {
    const authStore = useAuthStore()
    const response = await client.patch<TokenResponse>('/api/users/me/password', {
      password,
      newPassword,
    })
    authStore.setAccessToken(response.data.accessToken)
  }

  async function deleteAccount(password: string) {
    const authStore = useAuthStore()
    await client.delete('/api/users/me', { data: { password } })
    clearUser()
    authStore.clearSession()
  }

  function clearUser() {
    user.value = null
  }

  return {
    user,
    loadUser,
    updateUserName,
    changePassword,
    deleteAccount,
    clearUser,
  }
})
