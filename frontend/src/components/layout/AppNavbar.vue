<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/useAuthStore'

const router = useRouter()
const authStore = useAuthStore()

// Route names the navbar links to. Kept as data so adding a page
// means adding one entry instead of another block of markup.
const navItems = [
  { name: 'home', label: 'Home' },
  { name: 'login', label: 'Login' },
  { name: 'register', label: 'Register' },
]

async function handleLogout() {
  try {
    await authStore.logout()
  } finally {
    // Leave the protected area even if the server call failed -
    // the store dropped the token either way.
    await router.push({ name: 'login' })
  }
}
</script>

<template>
  <nav class="flex h-14 items-center gap-6 border-b border-slate-700 bg-slate-800 px-4">
    <span class="font-semibold text-slate-100">handout-manager</span>

    <div class="flex gap-1">
      <RouterLink
        v-for="item in navItems"
        :key="item.name"
        :to="{ name: item.name }"
        class="rounded-md px-3 py-1.5 text-sm text-slate-400
               hover:bg-slate-700 hover:text-slate-100"
        active-class="bg-slate-700 text-slate-100"
      >
        {{ item.label }}
      </RouterLink>
    </div>

    <!-- Pushes everything after it to the right edge. -->
    <div class="ml-auto">
      <button
        v-if="authStore.isAuthenticated"
        type="button"
        class="rounded-md bg-slate-700 px-3 py-1.5 text-sm font-medium text-slate-100
               hover:bg-slate-600"
        @click="handleLogout"
      >
        Sign out
      </button>
    </div>
  </nav>
</template>