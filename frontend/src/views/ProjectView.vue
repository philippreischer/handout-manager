<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useProjectStore } from '@/stores/projectStore'
import { extractErrorMessage } from '@/api/errorMessage'
import { formatMembershipRole } from '@/utils/formatMembershipRole'

const NOT_FOUND_MESSAGE = 'Project not found.'

const route = useRoute()
const projectStore = useProjectStore()

const isLoading = ref(true)
const errorMessage = ref<string | null>(null)

const projectId = computed(() => Number(route.params.projectId))
const isValidProjectId = computed(() => Number.isInteger(projectId.value) && projectId.value > 0)


onMounted(async () => {
  if (!isValidProjectId.value) {
    errorMessage.value = NOT_FOUND_MESSAGE
    isLoading.value = false
    return
  }

  try {
    await projectStore.loadProject(projectId.value)
  } catch (error) {
    errorMessage.value = extractErrorMessage(error, NOT_FOUND_MESSAGE)
  } finally {
    isLoading.value = false
  }
})
</script>

<template>
  <div class="mx-auto min-h-[calc(100vh-3.5rem)] w-full max-w-3xl space-y-6 p-4">
    <p v-if="isLoading" class="pt-4 text-slate-400">Loading project…</p>

    <div v-else-if="errorMessage" class="space-y-4 pt-4">
      <p role="alert" class="text-red-400">{{ errorMessage }}</p>
      <RouterLink :to="{ name: 'projects' }" class="text-sm text-slate-300 underline hover:text-slate-100">
        Back to projects
      </RouterLink>
    </div>

    <template v-else-if="projectStore.currentProject">
      <header class="pt-4">
        <h1 class="text-2xl font-bold text-slate-100">{{ projectStore.currentProject.name }}</h1>
        <p class="mt-1 text-slate-400">
          {{ formatMembershipRole(projectStore.currentProject.membershipRole) }}
        </p>
      </header>
    </template>
  </div>
</template>