<script setup lang="ts">
import { ref } from 'vue'
import { useUserStore } from '@/stores/userStore'
import { extractErrorMessage } from '@/api/errorMessage'

const userStore = useUserStore()

const password = ref('')
const newPassword = ref('')
const showPasswords = ref(false)
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
      <div>
        <label for="current-password" class="mb-1 block text-sm text-slate-400">
          Current password
        </label>
        <input
          id="current-password"
          v-model="password"
          :type="showPasswords ? 'text' : 'password'"
          required
          autocomplete="current-password"
          class="w-full rounded-md border border-slate-600 bg-slate-900 px-3 py-2 text-slate-100 focus:border-emerald-500 focus:outline-none"
        />
      </div>

      <div>
        <label for="new-password" class="mb-1 block text-sm text-slate-400">
          New password
        </label>
        <div class="relative">
          <input
            id="new-password"
            v-model="newPassword"
            :type="showPasswords ? 'text' : 'password'"
            required
            minlength="8"
            maxlength="72"
            autocomplete="new-password"
            class="w-full rounded-md border border-slate-600 bg-slate-900 px-3 py-2 pr-16 text-slate-100 focus:border-emerald-500 focus:outline-none"
          />
        
          <button
            type="button"
            class="absolute top-1/2 right-2 -translate-y-1/2 text-xs text-slate-400 hover:text-slate-200"
            @click="showPasswords = !showPasswords"
          >
            {{ showPasswords ? 'Hide' : 'Show' }}
          </button>
        </div>
      </div>

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