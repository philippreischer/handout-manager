<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { extractErrorMessage } from '@/api/errorMessage'
import EmailField from '@/components/ui/EmailField.vue'
import PasswordField from '@/components/ui/PasswordField.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const email = ref('')
const password = ref('')
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

    <p v-if="accountDeleted" class="text-sm text-emerald-400">Your account has been deleted.</p>

    <p v-else-if="passwordWasReset" class="text-sm text-emerald-400">
      Your password has been changed. Sign in with the new one.
    </p>

    <EmailField id="email" v-model="email" />

    <PasswordField id="password" v-model="password" />

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
      <RouterLink :to="{ name: 'forgot-password' }" class="text-emerald-400 hover:underline">
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
