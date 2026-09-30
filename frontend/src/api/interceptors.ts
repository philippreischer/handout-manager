import client from '@/api/httpClient'
import { useAuthStore } from '@/stores/useAuthStore'
 

export function setupInterceptors() {
  client.interceptors.request.use((config) => {
    const authStore = useAuthStore()
 
    if (authStore.accessToken) {
      config.headers.Authorization = `Bearer ${authStore.accessToken}`
    }
 
    return config
  })
}