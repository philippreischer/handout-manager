import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import AuthView from '@/views/AuthView.vue'
import HomeView from '@/views/HomeView.vue'
import LoginForm from '@/components/auth/LoginForm.vue'
import RegisterForm from '@/components/auth/RegisterForm.vue'
import ForgotPasswordForm from '@/components/auth/ForgotPasswordForm.vue'
import ResetPasswordForm from '@/components/auth/ResetPasswordForm.vue'

// Tells TypeScript which meta fields exist, so a typo in a route
// definition or in the guard below is a compile error.
declare module 'vue-router' {
  interface RouteMeta {
    requiresAuth?: boolean
    guestOnly?: boolean
  }
}

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: AuthView,
      children: [
        // "/" itself has no page - hand the visitor to the login form.
        { path: '', redirect: { name: 'login' } },
        {
          path: 'login',
          name: 'login',
          component: LoginForm,
          meta: { guestOnly: true },
        },
        {
          path: 'register',
          name: 'register',
          component: RegisterForm,
          meta: { guestOnly: true },
        },
        {
          path: 'forgot-password',
          name: 'forgot-password',
          component: ForgotPasswordForm,
          meta: { guestOnly: true },
        },
        {
          path: 'reset-password',
          name: 'reset-password',
          component: ResetPasswordForm,
        },
      ],
    },
    {
      path: '/home',
      name: 'home',
      component: HomeView,
      meta: { requiresAuth: true },
    },
    {
      path: '/settings',
      name: 'settings',
      component: () => import('@/views/SettingsView.vue'),
      meta: { requiresAuth: true },
    },
  ],
})

// Runs before every navigation, the very first one after mount() included.
router.beforeEach((to) => {
  // The store is read inside the guard, not at module level: when this file
  // is first imported, Pinia is not installed yet.
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return { name: 'login' }
  }

  if (to.meta.guestOnly && authStore.isAuthenticated) {
    return { name: 'home' }
  }

  // Returning nothing lets the navigation through.
})

export default router
