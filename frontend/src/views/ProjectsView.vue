<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useProjectStore } from '@/stores/projectStore'
import { extractErrorMessage } from '@/api/errorMessage'
import ProjectCard from '@/components/project/ProjectCard.vue'

const projectStore = useProjectStore()

const isLoading = ref(true)
const errorMessage = ref<string | null>(null)

onMounted(async () => {
  try {
    await projectStore.loadProjects()
  } catch (error) {
    errorMessage.value = extractErrorMessage(error, 'Could not load projects.')
  } finally {
    isLoading.value = false
  }
})
</script>

<template>
  <div class="mx-auto min-h-[calc(100vh-3.5rem)] w-full max-w-3xl space-y-6 p-4">
    <h1 class="pt-4 text-2xl font-bold text-slate-100">Projects</h1>

    <p v-if="isLoading" class="text-slate-400">Loading projects…</p>

    <p v-else-if="errorMessage" role="alert" class="text-red-400">{{ errorMessage }}</p>

    <p v-else-if="projectStore.projects.length === 0" class="text-slate-400">
      You have no projects yet.
    </p>

    <ul v-else class="grid gap-4 sm:grid-cols-1">
      <li v-for="project in projectStore.projects" :key="project.id">
        <ProjectCard :project="project" />
      </li>
    </ul>
  </div>
</template>