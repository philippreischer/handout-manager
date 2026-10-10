<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/userStore'
import { extractErrorMessage } from '@/api/errorMessage'

const router = useRouter()
const userStore = useUserStore()

// Username section
const newUserName = ref(userStore.user?.userName ?? '')
const userNamePassword = ref('')
const showUserNamePassword = ref(false)
const userNameLoading = ref(false)
const userNameError = ref<string | null>(null)
const userNameSuccess = ref(false)

// Password section
const currentPassword = ref('')
const newPassword = ref('')
const showPasswordFields = ref(false)
const passwordLoading = ref(false)
const passwordError = ref<string | null>(null)
const passwordSuccess = ref(false)

// Deletion section
const deletePassword = ref('')
const showDeletePassword = ref(false)
const deleteLoading = ref(false)
const deleteError = ref<string | null>(null)

async function submitUserName() {
  userNameError.value = null
  userNameSuccess.value = false
  userNameLoading.value = true

  try {
    await userStore.updateUserName(userNamePassword.value, newUserName.value)
    userNameSuccess.value = true
    userNamePassword.value = ''
  } catch (error) {
    userNameError.value = extractErrorMessage(error, 'Could not update username.')
  } finally {
    userNameLoading.value = false
  }
}

async function submitPassword() {
  passwordError.value = null
  passwordSuccess.value = false
  passwordLoading.value = true

  try {
    await userStore.changePassword(currentPassword.value, newPassword.value)
    passwordSuccess.value = true
    currentPassword.value = ''
    newPassword.value = ''
  } catch (error) {
    passwordError.value = extractErrorMessage(error, 'Could not change password.')
  } finally {
    passwordLoading.value = false
  }
}

async function submitDeletion() {
  deleteError.value = null
  deleteLoading.value = true

  try {
    await userStore.deleteAccount(deletePassword.value)
    // The store has cleared the session, so the guest-only guard lets us in.
    await router.push({ name: 'login', query: { deleted: '1' } })
  } catch (error) {
    deleteError.value = extractErrorMessage(error, 'Could not delete account.')
  } finally {
    deleteLoading.value = false
  }
}
</script>

<template>
  <div class="mx-auto min-h-[calc(100vh-3.5rem)] w-full max-w-xl space-y-6 p-4">
    <h1 class="pt-4 text-2xl font-bold text-slate-100">Settings</h1>

    <!-- Username -->
    <section class="rounded-xl bg-slate-800 p-6 shadow-xl">
      <h2 class="mb-4 text-lg font-semibold text-slate-100">Username</h2>

      <form class="space-y-4" @submit.prevent="submitUserName">
        <div>
          <label for="new-username" class="mb-1 block text-sm text-slate-400">
            New username
          </label>
          <input
            id="new-username"
            v-model="newUserName"
            type="text"
            required
            autocomplete="username"
            class="w-full rounded-md border border-slate-700 bg-slate-900 px-3 py-2
                   text-slate-100 focus:border-slate-500 focus:outline-none"
          />
        </div>

        <div>
          <label for="username-password" class="mb-1 block text-sm text-slate-400">
            Current password
          </label>
          <div class="relative">
            <input
              id="username-password"
              v-model="userNamePassword"
              :type="showUserNamePassword ? 'text' : 'password'"
              required
              autocomplete="current-password"
              class="w-full rounded-md border border-slate-700 bg-slate-900 px-3 py-2 pr-16
                     text-slate-100 focus:border-slate-500 focus:outline-none"
            />
            <button
              type="button"
              class="absolute top-1/2 right-2 -translate-y-1/2 text-xs text-slate-400
                     hover:text-slate-200"
              @click="showUserNamePassword = !showUserNamePassword"
            >
              {{ showUserNamePassword ? 'Hide' : 'Show' }}
            </button>
          </div>
        </div>

        <p v-if="userNameError" class="text-sm text-red-400">{{ userNameError }}</p>
        <p v-else-if="userNameSuccess" class="text-sm text-green-400">Username updated.</p>

        <button
          type="submit"
          :disabled="userNameLoading"
          class="rounded-md bg-indigo-600 px-4 py-2 font-medium text-slate-100
                 hover:bg-indigo-500 disabled:opacity-50"
        >
          Save username
        </button>
      </form>
    </section>

    <!-- Password -->
    <section class="rounded-xl bg-slate-800 p-6 shadow-xl">
      <h2 class="mb-4 text-lg font-semibold text-slate-100">Password</h2>

      <form class="space-y-4" @submit.prevent="submitPassword">
        <div>
          <label for="current-password" class="mb-1 block text-sm text-slate-400">
            Current password
          </label>
          <input
            id="current-password"
            v-model="currentPassword"
            :type="showPasswordFields ? 'text' : 'password'"
            required
            autocomplete="current-password"
            class="w-full rounded-md border border-slate-700 bg-slate-900 px-3 py-2
                   text-slate-100 focus:border-slate-500 focus:outline-none"
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
              :type="showPasswordFields ? 'text' : 'password'"
              required
              minlength="8"
              maxlength="72"
              autocomplete="new-password"
              class="w-full rounded-md border border-slate-700 bg-slate-900 px-3 py-2 pr-16
                     text-slate-100 focus:border-slate-500 focus:outline-none"
            />
            <button
              type="button"
              class="absolute top-1/2 right-2 -translate-y-1/2 text-xs text-slate-400
                     hover:text-slate-200"
              @click="showPasswordFields = !showPasswordFields"
            >
              {{ showPasswordFields ? 'Hide' : 'Show' }}
            </button>
          </div>
        </div>

        <p v-if="passwordError" class="text-sm text-red-400">{{ passwordError }}</p>
        <p v-else-if="passwordSuccess" class="text-sm text-green-400">
          Password changed. Other devices have been signed out.
        </p>

        <button
          type="submit"
          :disabled="passwordLoading"
          class="rounded-md bg-indigo-600 px-4 py-2 font-medium text-slate-100
                 hover:bg-indigo-500 disabled:opacity-50"
        >
          Change password
        </button>
      </form>
    </section>

    <!-- Deletion -->
    <section class="rounded-xl border border-red-900 bg-slate-800 p-6 shadow-xl">
      <h2 class="mb-2 text-lg font-semibold text-slate-100">Delete account</h2>
      <p class="mb-4 text-sm text-slate-400">
        This removes your account and everything in it. It cannot be undone.
      </p>

      <form class="space-y-4" @submit.prevent="submitDeletion">
        <div>
          <label for="delete-password" class="mb-1 block text-sm text-slate-400">
            Current password
          </label>
          <div class="relative">
            <input
              id="delete-password"
              v-model="deletePassword"
              :type="showDeletePassword ? 'text' : 'password'"
              required
              autocomplete="current-password"
              class="w-full rounded-md border border-slate-700 bg-slate-900 px-3 py-2 pr-16
                     text-slate-100 focus:border-slate-500 focus:outline-none"
            />
            <button
              type="button"
              class="absolute top-1/2 right-2 -translate-y-1/2 text-xs text-slate-400
                     hover:text-slate-200"
              @click="showDeletePassword = !showDeletePassword"
            >
              {{ showDeletePassword ? 'Hide' : 'Show' }}
            </button>
          </div>
        </div>

        <p v-if="deleteError" class="text-sm text-red-400">{{ deleteError }}</p>

        <button
          type="submit"
          :disabled="deleteLoading"
          class="rounded-md bg-red-600 px-4 py-2 font-medium text-slate-100
                 hover:bg-red-500 disabled:opacity-50"
        >
          Delete account
        </button>
      </form>
    </section>
  </div>
</template>