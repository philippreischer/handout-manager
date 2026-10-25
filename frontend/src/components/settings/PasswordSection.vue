<script setup lang="ts">
import { ref } from 'vue'
import { useUserStore } from '@/stores/userStore'
import { extractErrorMessage } from '@/api/errorMessage'
import PasswordField from '@/components/ui/PasswordField.vue'

const userStore = useUserStore()

const password = ref('')
const newPassword = ref('')
const isSubmitting = ref(false)
const errorMessage = ref<string | null>(null)
const isChanged = ref(false)

async function handleSubmit() {
  errorMessage.value = null
  isChanged.value = false
  isSubmitting.value = true

  try {
    await userStore.changePassword(password.value, newPassword.value)
    isChanged.value = true
    password.value = ''
    newPassword.value = ''
  } catch (error) {
    errorMessage.value = extractErrorMessage(error, 'Could not change password.')
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <section class="rounded-xl bg-slate-800 p-6 shadow-xl">
    <h2 class="mb-4 text-lg font-semibold text-slate-100">Password</h2>

    <form class="space-y-4" @submit.prevent="handleSubmit">
      <PasswordField id="settings-current-password" v-model="password" />

      <PasswordField
        id="settings-new-password"
        label="New password"
        v-model="newPassword"
        purpose="new"
      />

      <p v-if="errorMessage" class="text-sm text-red-400">{{ errorMessage }}</p>
      <p v-else-if="isChanged" class="text-sm text-emerald-400">
        Password changed. Other devices have been signed out.
      </p>

      <button
        type="submit"
        :disabled="isSubmitting"
        class="rounded-md bg-emerald-600 px-4 py-2 font-medium text-white hover:bg-emerald-500 disabled:cursor-not-allowed disabled:opacity-50"
      >
        {{ isSubmitting ? 'Changing…' : 'Change password' }}
      </button>
    </form>
  </section>
</template>
