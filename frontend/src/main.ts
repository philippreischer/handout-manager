import { createApp } from 'vue'
import { createPinia } from 'pinia'
 
import './assets/main.css'
 
import App from './App.vue'
import router from './router'
import { useAuthStore } from '@/stores/useAuthStore'
import { setupInterceptors } from '@/api/interceptors'
 
async function bootstrap() {
    const app = createApp(App)
 
    app.use(createPinia())

    setupInterceptors()
    
    // Must run before app.use(router): installing the router starts the first
    // navigation, and its guard would see no token yet.
    await useAuthStore().restoreSession()
    
    app.use(router)
    await router.isReady()

    app.mount('#app')
}
 
void bootstrap()
