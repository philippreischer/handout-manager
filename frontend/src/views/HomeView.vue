<script setup lang="ts">
import { onMounted, ref } from 'vue'
import client from '@/api/httpClient'
import { extractErrorMessage } from '@/api/errorMessage'

const userId = ref<number | null>(null)
const errorMessage = ref<string | null>(null)

async function loadProfile() {
  errorMessage.value = null

  try {
    const response = await client.get<number>('/api/me')
    userId.value = response.data
  } catch (error) {
    errorMessage.value = extractErrorMessage(error, 'Could not load profile.')
  }
}

onMounted(loadProfile)
</script>

<template>
  <div class="flex min-h-[calc(100vh-3.5rem)] items-center justify-center p-4">
    <div class="w-full max-w-md rounded-xl bg-slate-800 p-8 shadow-xl">
      <h1 class="mb-6 text-2xl font-bold text-slate-100">Signed in</h1>

      <p v-if="userId !== null" class="text-slate-300">
        Your user id: <span class="font-mono text-emerald-400">{{ userId }}</span>
      </p>

      <p v-else-if="errorMessage" class="text-sm text-red-400">
        {{ errorMessage }}
      </p>

      <p v-else class="text-slate-500">Loading…</p>

      <button
        type="button"
        class="mt-6 w-full rounded-md bg-slate-700 px-4 py-2 font-medium text-slate-100 hover:bg-slate-600"
        @click="loadProfile"
      >
        Reload profile
      </button>
    </div>
  </div>
</template>
