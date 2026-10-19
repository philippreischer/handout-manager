<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { extractErrorMessage } from '@/api/errorMessage'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const email = ref('')
const password = ref('')
const showPassword = ref(false)
const errorMessage = ref<string | null>(null)
const isSubmitting = ref(false)

const accountDeleted = computed(() => route.query.deleted === '1')

const passwordWasReset = computed(() => route.query.reset === '1')

async function handleSubmit() {
  errorMessage.value = null
  isSubmitting.value = true

  try {
    await authStore.login(email.value, password.value)
    await router.push({ name: 'home' })
  } catch (error) {
    errorMessage.value = extractErrorMessage(error, 'Login failed.')
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <form class="space-y-5" @submit.prevent="handleSubmit">
    <h2 class="text-lg font-semibold text-slate-200">Sign in</h2>

    <p v-if="accountDeleted" class="text-sm text-emerald-400">
      Your account has been deleted.
    </p>

    <p v-else-if="passwordWasReset" class="text-sm text-emerald-400">
      Your password has been changed. Sign in with the new one.
    </p>

    <div>
      <label for="email" class="mb-1 block text-sm text-slate-400">Email</label>
      <input
        id="email"
        v-model="email"
        type="email"
        required
        autocomplete="email"
        class="w-full rounded-md border border-slate-600 bg-slate-900 px-3 py-2 text-slate-100 placeholder-slate-500 focus:border-emerald-500 focus:outline-none"
      />
    </div>

    <div>
      <label for="password" class="mb-1 block text-sm text-slate-400">Password</label>
      <div class="relative">
        <input
          id="password"
          v-model="password"
          :type="showPassword ? 'text' : 'password'"
          required
          autocomplete="current-password"
          class="w-full rounded-md border border-slate-600 bg-slate-900 px-3 py-2 text-slate-100 placeholder-slate-500 focus:border-emerald-500 focus:outline-none"
        />
      
        <button
          type="button"
          class="absolute top-1/2 right-2 -translate-y-1/2 text-xs text-slate-400 hover:text-slate-200"
          @click="showPassword = !showPassword"
        >
          {{ showPassword ? 'Hide' : 'Show' }}
        </button>
      </div>
    </div>
    <p v-if="errorMessage" class="text-sm text-red-400">
      {{ errorMessage }}
    </p>

    <button
      type="submit"
      :disabled="isSubmitting"
      class="w-full rounded-md bg-emerald-600 px-4 py-2 font-medium text-white hover:bg-emerald-500 disabled:cursor-not-allowed disabled:opacity-50"
    >
      {{ isSubmitting ? 'Signing in…' : 'Sign in' }}
    </button>

    <p class="text-center text-sm text-slate-400">
      <RouterLink
        :to="{ name: 'forgot-password' }"
        class="text-emerald-400 hover:underline"
      >
        Forgot your password?
      </RouterLink>
    </p>

    <p class="text-center text-sm text-slate-400">
      No account yet?
      <RouterLink :to="{ name: 'register' }" class="text-emerald-400 hover:underline">
        Register
      </RouterLink>
    </p>
  </form>
</template>
