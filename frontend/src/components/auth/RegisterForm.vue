<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { extractErrorMessage } from '@/api/errorMessage'

const router = useRouter()
const authStore = useAuthStore()

const email = ref('')
const password = ref('')
const userName = ref('')
const errorMessage = ref<string | null>(null)
const isSubmitting = ref(false)

async function handleSubmit() {
  errorMessage.value = null
  isSubmitting.value = true

  try {
    await authStore.register(email.value, password.value, userName.value)
    // Registration does not sign the user in - the backend answers 201 without tokens.
    await router.push({ name: 'login' })
  } catch (error) {
    errorMessage.value = extractErrorMessage(error, 'Registration failed.')
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <form class="space-y-5" @submit.prevent="handleSubmit">
    <h2 class="text-lg font-semibold text-slate-200">Create account</h2>

    <div>
      <label for="userName" class="mb-1 block text-sm text-slate-400">Username</label>
      <input
        id="userName"
        v-model="userName"
        type="text"
        required
        autocomplete="username"
        class="w-full rounded-md border border-slate-600 bg-slate-900 px-3 py-2 text-slate-100 placeholder-slate-500 focus:border-emerald-500 focus:outline-none"
      />
    </div>

    <div>
      <label for="registerEmail" class="mb-1 block text-sm text-slate-400">Email</label>
      <input
        id="registerEmail"
        v-model="email"
        type="email"
        required
        autocomplete="email"
        class="w-full rounded-md border border-slate-600 bg-slate-900 px-3 py-2 text-slate-100 placeholder-slate-500 focus:border-emerald-500 focus:outline-none"
      />
    </div>

    <div>
      <label for="registerPassword" class="mb-1 block text-sm text-slate-400"> Password </label>
      <input
        id="registerPassword"
        v-model="password"
        type="password"
        required
        minlength="8"
        autocomplete="new-password"
        class="w-full rounded-md border border-slate-600 bg-slate-900 px-3 py-2 text-slate-100 placeholder-slate-500 focus:border-emerald-500 focus:outline-none"
      />
      <p class="mt-1 text-xs text-slate-500">At least 8 characters.</p>
    </div>

    <p v-if="errorMessage" class="text-sm text-red-400">
      {{ errorMessage }}
    </p>

    <button
      type="submit"
      :disabled="isSubmitting"
      class="w-full rounded-md bg-emerald-600 px-4 py-2 font-medium text-white hover:bg-emerald-500 disabled:cursor-not-allowed disabled:opacity-50"
    >
      {{ isSubmitting ? 'Creating…' : 'Create account' }}
    </button>

    <p class="text-center text-sm text-slate-400">
      Already registered?
      <RouterLink :to="{ name: 'login' }" class="text-emerald-400 hover:underline">
        Sign in
      </RouterLink>
    </p>
  </form>
</template>
