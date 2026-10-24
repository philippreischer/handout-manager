<script setup lang="ts">
  import { ref } from 'vue'
  import { useUserStore } from '@/stores/userStore'
  import { extractErrorMessage } from '@/api/errorMessage'
  import UserNameField from '@/components/ui/UserNameField.vue'
  import PasswordField from '@/components/ui/PasswordField.vue'

  const userStore = useUserStore()

  const userName = ref(userStore.user?.userName ?? '')
  const password = ref('')
  const isSubmitting = ref(false)
  const errorMessage = ref<string | null>(null)
  const isSaved = ref(false)

  async function handleSubmit() {
    errorMessage.value = null
    isSaved.value = false
    isSubmitting.value = true

    try {
      await userStore.updateUserName(password.value, userName.value)
      isSaved.value = true
      password.value = ''
    } catch (error) {
      errorMessage.value = extractErrorMessage(error, 'Could not update username.')
    } finally {
      isSubmitting.value = false
    }
  }
</script>

<template>
  <section class="rounded-xl bg-slate-800 p-6 shadow-xl">
    <h2 class="mb-4 text-lg font-semibold text-slate-100">Username</h2>

    <form class="space-y-4" @submit.prevent="handleSubmit">
      
      <UserNameField id="settings-username" v-model="userName" />

      <PasswordField id="settings-username-password" v-model="password" />
      
      <p v-if="errorMessage" class="text-sm text-red-400">{{ errorMessage }}</p>
      <p v-else-if="isSaved" class="text-sm text-emerald-400">Username updated.</p>

      <button
        type="submit"
        :disabled="isSubmitting"
        class="rounded-md bg-emerald-600 px-4 py-2 font-medium text-white hover:bg-emerald-500 disabled:cursor-not-allowed disabled:opacity-50"
      >
        {{ isSubmitting ? 'Saving…' : 'Save username' }}
      </button>
    </form>
  </section>
</template>