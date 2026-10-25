<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/userStore'
import { extractErrorMessage } from '@/api/errorMessage'
import PasswordField from '@/components/ui/PasswordField.vue'

const router = useRouter()
const userStore = useUserStore()

const password = ref('')
const isSubmitting = ref(false)
const errorMessage = ref<string | null>(null)

async function handleSubmit() {
  errorMessage.value = null
  isSubmitting.value = true

  try {
    await userStore.deleteAccount(password.value)

    await router.push({ name: 'login', query: { deleted: '1' } })
  } catch (error) {
    errorMessage.value = extractErrorMessage(error, 'Could not delete account.')
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <section class="rounded-xl border border-red-900 bg-slate-800 p-6 shadow-xl">
    <h2 class="mb-2 text-lg font-semibold text-slate-100">Delete account</h2>
    <p class="mb-4 text-sm text-slate-400">
      This removes your account and everything in it. It cannot be undone.
    </p>

    <form class="space-y-4" @submit.prevent="handleSubmit">
      <PasswordField id="settings-delete-password" v-model="password" />

      <p v-if="errorMessage" class="text-sm text-red-400">{{ errorMessage }}</p>

      <button
        type="submit"
        :disabled="isSubmitting"
        class="rounded-md bg-red-600 px-4 py-2 font-medium text-white hover:bg-red-500 disabled:cursor-not-allowed disabled:opacity-50"
      >
        {{ isSubmitting ? 'Deleting…' : 'Delete account' }}
      </button>
    </form>
  </section>
</template>
