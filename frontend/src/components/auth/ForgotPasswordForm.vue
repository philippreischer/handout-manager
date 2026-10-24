<script setup lang="ts">
import { ref } from 'vue'
import { useAuthStore } from '@/stores/authStore'
import { extractErrorMessage } from '@/api/errorMessage'
import EmailField from '@/components/ui/EmailField.vue'

const authStore = useAuthStore()

const email = ref('')
const errorMessage = ref<string | null>(null)
const isSubmitting = ref(false)
const isSent = ref(false)

async function handleSubmit() {
  errorMessage.value = null
  isSubmitting.value = true

  try {
    await authStore.requestPasswordReset(email.value)
    isSent.value = true
  } catch (error) {
    errorMessage.value = extractErrorMessage(error, 'Could not send the reset link.')
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <form class="space-y-5" @submit.prevent="handleSubmit">
    <h2 class="text-lg font-semibold text-slate-200">Forgot your password?</h2>

    <p class="text-sm text-slate-400">
      Enter your email address and we will send you a link to choose a new password.
    </p>

    <EmailField id="reset-email" v-model="email" />

    <p v-if="errorMessage" class="text-sm text-red-400">
      {{ errorMessage }}
    </p>

    <!--
      Deliberately says nothing about whether the address is registered - the
      server answers the same way either way, and so does this message.
    -->
    <p v-else-if="isSent" class="text-sm text-emerald-400">
      If an account exists for this address, a link is on its way. The link is valid
      for 30 minutes.
    </p>

    <button
      type="submit"
      :disabled="isSubmitting"
      class="w-full rounded-md bg-emerald-600 px-4 py-2 font-medium text-white hover:bg-emerald-500 disabled:cursor-not-allowed disabled:opacity-50"
    >
      {{ isSubmitting ? 'Sending…' : 'Send reset link' }}
    </button>

    <p class="text-center text-sm text-slate-400">
      <RouterLink :to="{ name: 'login' }" class="text-emerald-400 hover:underline">
        Back to sign in
      </RouterLink>
    </p>
  </form>
</template>