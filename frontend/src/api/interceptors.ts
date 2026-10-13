import type { AxiosError, InternalAxiosRequestConfig } from 'axios'
import client from '@/api/httpClient'
import router from '@/router'
import { useAuthStore } from '@/stores/authStore'

type RetriableConfig = InternalAxiosRequestConfig & { _isRetry?: boolean }

let refreshPromise: Promise<void> | null = null

function refreshOnce(): Promise<void> {
  if (!refreshPromise) {
    refreshPromise = useAuthStore()
      .restoreSession()
      .finally(() => {
        refreshPromise = null
      })
  }

  return refreshPromise
}

export function setupInterceptors() {
  client.interceptors.request.use((config) => {
    const authStore = useAuthStore()

    if (authStore.accessToken) {
      config.headers.Authorization = `Bearer ${authStore.accessToken}`
    }

    return config
  })

  client.interceptors.response.use(
    (response) => response,
    async (error: AxiosError) => {
      const config = error.config as RetriableConfig | undefined

      if (error.response?.status !== 401 || !config || config._isRetry) {
        return Promise.reject(error)
      }

      if (config.url?.startsWith('/api/auth/')) {
        return Promise.reject(error)
      }

      config._isRetry = true

      await refreshOnce()

      if (!useAuthStore().isAuthenticated) {
        await router.push({ name: 'login' })
        return Promise.reject(error)
      }

      return client(config)
    },
  )
}
