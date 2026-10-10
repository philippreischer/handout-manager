import { createApp } from 'vue'
import { createPinia } from 'pinia'

import './assets/main.css'

import App from './App.vue'
import router from './router'
import { useAuthStore } from '@/stores/authStore.ts'
import { useUserStore } from '@/stores/userStore.ts'
import { setupInterceptors } from '@/api/interceptors'

async function bootstrap() {
  const app = createApp(App)
  app.use(createPinia())
  setupInterceptors()

  // Must run before app.use(router): installing the router starts the first
  // navigation, and its guard would see no token yet.
  const authStore = useAuthStore()
  await authStore.restoreSession()

  if (authStore.isAuthenticated) {
    try {
      await useUserStore().loadUser()
    } catch {
      // A failed profile request must not keep the app from mounting.
      // The views render without a user and can retry.
    }
  }

  app.use(router)
  await router.isReady()
  app.mount('#app')
}

void bootstrap()
