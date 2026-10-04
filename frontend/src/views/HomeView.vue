<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import client from '@/api/httpClient'
import { extractErrorMessage } from '@/api/errorMessage'
import type { UserResponse } from '@/types/user'

const user = ref<UserResponse | null>(null)
const errorMessage = ref<string | null>(null)

// The string becomes a Date only here, for display. Keeping the raw value in
// the ref means it still matches what the server sent.
const memberSince = computed(() =>
  user.value ? new Date(user.value.createdAt).toLocaleDateString() : '',
)

async function loadProfile() {
  errorMessage.value = null

  try {
    const response = await client.get<UserResponse>('/api/users/me')
    user.value = response.data
  } catch (error) {
    errorMessage.value = extractErrorMessage(error, 'Could not load profile.')
  }
}

onMounted(loadProfile)
</script>

<template>
  <!-- 3.5rem is the navbar height (h-14), so the card centers in what is left. -->
  <div class="flex min-h-[calc(100vh-3.5rem)] items-center justify-center p-4">
    <div class="w-full max-w-md rounded-xl bg-slate-800 p-8 shadow-xl">
      <h1 class="mb-6 text-2xl font-bold text-slate-100">Your account</h1>

      <dl v-if="user" class="space-y-3 text-sm">
        <div class="flex justify-between gap-4">
          <dt class="text-slate-400">Username</dt>
          <dd class="text-slate-100">{{ user.userName }}</dd>
        </div>
        <div class="flex justify-between gap-4">
          <dt class="text-slate-400">Email</dt>
          <dd class="text-slate-100">{{ user.email }}</dd>
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
