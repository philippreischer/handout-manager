<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useProjectStore } from '@/stores/projectStore'
import { extractErrorMessage } from '@/api/errorMessage'
import { formatMembershipRole } from '@/utils/formatMembershipRole'
import ProjectSettings from '@/components/project/ProjectSettings.vue'

const NOT_FOUND_MESSAGE = 'Project not found.'

const route = useRoute()
const projectStore = useProjectStore()

const isLoading = ref(true)
const errorMessage = ref<string | null>(null)
const isSettingsOpen = ref(false)

const projectId = computed(() => Number(route.params.projectId))
const isValidProjectId = computed(() => Number.isInteger(projectId.value) && projectId.value > 0)
const isGameMaster = computed(() => projectStore.currentProject?.membershipRole === 'GAME_MASTER')

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
      <header class="flex items-start justify-between gap-4 pt-4">
        <div>
          <h1 class="text-2xl font-bold text-slate-100">{{ projectStore.currentProject.name }}</h1>
          <p class="mt-1 text-slate-400">
            {{ formatMembershipRole(projectStore.currentProject.membershipRole) }}
          </p>
        </div>

        <button
          v-if="isGameMaster"
          type="button"
          aria-label="Project settings"
          aria-controls="project-settings"
          :aria-expanded="isSettingsOpen"
          class="rounded-md p-2 text-slate-400 hover:bg-slate-700 hover:text-slate-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-400"
          @click="isSettingsOpen = !isSettingsOpen"
        >
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            aria-hidden="true"
            class="h-6 w-6 transition-transform"
            :class="{ 'rotate-90': isSettingsOpen }"
          >
            <circle cx="12" cy="12" r="8.5" stroke-width="3" stroke-dasharray="3.34 3.34" />
            <circle cx="12" cy="12" r="5.5" stroke-width="3" />
          </svg>
        </button>
      </header>

      <ProjectSettings
        v-if="isGameMaster && isSettingsOpen"
        :project="projectStore.currentProject"
      />      
    </template>
  </div>
</template>