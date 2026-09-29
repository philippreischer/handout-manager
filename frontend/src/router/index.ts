import { createRouter, createWebHistory } from 'vue-router'
import AuthView from '@/views/AuthView.vue'
import HomeView from '@/views/HomeView.vue'
import LoginForm from '@/components/auth/LoginForm.vue'
import RegisterForm from '@/components/auth/RegisterForm.vue'

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            component: AuthView,
            children: [
                { path: 'login', name: 'login', component: LoginForm },
                { path: 'register', name: 'register', component: RegisterForm },
            ],
        },
        {
            path: '/home',
            name: 'home',
            component: HomeView,
            meta: { requiresAuth: true },
        },
        {
            path: '/',
            redirect: { name: 'home' },
        },
    ],
})

export default router
