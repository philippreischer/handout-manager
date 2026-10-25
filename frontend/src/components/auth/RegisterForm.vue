<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { extractErrorMessage } from '@/api/errorMessage'
import UserNameField from '@/components/ui/UserNameField.vue'
import EmailField from '@/components/ui/EmailField.vue'
import PasswordField from '@/components/ui/PasswordField.vue'

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

    <UserNameField id="username" v-model="userName" />

    <EmailField id="email" v-model="email" />

    <PasswordField id="password" v-model="password" purpose="new" />

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
