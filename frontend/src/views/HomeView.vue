<script setup lang="ts">
import { onMounted, ref } from 'vue'
import client from '@/api/httpClient'
import { extractErrorMessage } from '@/api/errorMessage'

const userId = ref<number | null>(null)
const errorMessage = ref<string | null>(null)

onMounted(async () => {
  try {
    const response = await client.get<number>('/api/me')
    userId.value = response.data
  } catch (error) {
    errorMessage.value = extractErrorMessage(error, 'Could not load profile.')
  }
})
</script>

<template>
  <!-- 3.5rem is the navbar height (h-14), so the card centers in what is left. -->
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
    </div>
  </div>
</template>