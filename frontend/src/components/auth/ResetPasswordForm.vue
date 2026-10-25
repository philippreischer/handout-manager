<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { extractErrorMessage } from '@/api/errorMessage'
import PasswordField from '@/components/ui/PasswordField.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// The value can be a string, an array or null depending on the URL, so narrow
// it to a string before using it.
const token = computed(() => (typeof route.query.token === 'string' ? route.query.token : ''))

const newPassword = ref('')
const errorMessage = ref<string | null>(null)
const isSubmitting = ref(false)

async function handleSubmit() {
  errorMessage.value = null
  isSubmitting.value = true

  try {
    await authStore.confirmPasswordReset(token.value, newPassword.value)
    await router.push({ name: 'login', query: { reset: '1' } })
  } catch (error) {
    errorMessage.value = extractErrorMessage(error, 'Could not set the new password.')
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <div class="space-y-5">
    <h2 class="text-lg font-semibold text-slate-200">Choose a new password</h2>

    <!-- No token in the URL at all: there is nothing to submit, so no form. -->
    <template v-if="!token">
      <p class="text-sm text-red-400">This link is incomplete. Request a new one.</p>

      <p class="text-center text-sm text-slate-400">
        <RouterLink :to="{ name: 'forgot-password' }" class="text-emerald-400 hover:underline">
          Request a new link
        </RouterLink>
      </p>
    </template>

    <form v-else class="space-y-5" @submit.prevent="handleSubmit">
      <PasswordField
        id="reset-new-password"
        label="New password"
        v-model="newPassword"
        purpose="new"
      />

      <p v-if="errorMessage" class="text-sm text-red-400">
        {{ errorMessage }}
      </p>

      <button
        type="submit"
        :disabled="isSubmitting"
        class="w-full rounded-md bg-emerald-600 px-4 py-2 font-medium text-white hover:bg-emerald-500 disabled:cursor-not-allowed disabled:opacity-50"
      >
        {{ isSubmitting ? 'Saving…' : 'Set new password' }}
      </button>

      <p class="text-center text-sm text-slate-400">
        <RouterLink :to="{ name: 'forgot-password' }" class="text-emerald-400 hover:underline">
          Request a new link
        </RouterLink>
      </p>
    </form>
  </div>
</template>
