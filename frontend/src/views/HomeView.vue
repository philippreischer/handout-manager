<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useUserStore } from '@/stores/userStore'
import { extractErrorMessage } from '@/api/errorMessage'

const userStore = useUserStore()
const errorMessage = ref<string | null>(null)

// The string becomes a Date only here, for display. Keeping the raw value in
// the store means it still matches what the server sent.
const memberSince = computed(() =>
  userStore.user ? new Date(userStore.user.createdAt).toLocaleDateString() : '',
)

async function loadProfile() {
  errorMessage.value = null

  try {
    await userStore.loadUser()
  } catch (error) {
    errorMessage.value = extractErrorMessage(error, 'Could not load profile.')
  }
}
    
onMounted(() => {
  // Bootstrap already loaded the user on startup. Only fetch when it is missing,
  // which happens if that request failed.
  if (userStore.user === null) {
    void loadProfile()
  }
})

</script>

<template>
  <!-- 3.5rem is the navbar height (h-14), so the card centers in what is left. -->
  <div class="flex min-h-[calc(100vh-3.5rem)] items-center justify-center p-4">
    <div class="w-full max-w-md rounded-xl bg-slate-800 p-8 shadow-xl">
      <h1 class="mb-6 text-2xl font-bold text-slate-100">Your account</h1>

      <dl v-if="userStore.user" class="space-y-3 text-sm">
        <div class="flex justify-between gap-4">
          <dt class="text-slate-400">Username</dt>
          <dd class="text-slate-100">{{ userStore.user.userName }}</dd>
        </div>
        <div class="flex justify-between gap-4">
          <dt class="text-slate-400">Email</dt>
          <dd class="text-slate-100">{{ userStore.user.email }}</dd>
        </div>
        <div class="flex justify-between gap-4">
          <dt class="text-slate-400">Member since</dt>
          <dd class="text-slate-100">{{ memberSince }}</dd>
        </div>
      </dl>

      <p v-else-if="errorMessage" class="text-sm text-red-400">
        {{ errorMessage }}
      </p>

      <p v-else class="text-slate-500">Loading…</p>

      <button
        type="button"
        class="mt-6 w-full rounded-md bg-slate-700 px-4 py-2 font-medium text-slate-100
               hover:bg-slate-600"
        @click="loadProfile"
      >
        Reload profile
      </button>
    </div>
  </div>
</template>
