<script setup lang="ts">
import { nextTick, onMounted, ref, useTemplateRef } from 'vue'
import { useProjectStore } from '@/stores/projectStore'
import { extractErrorMessage } from '@/api/errorMessage'
import ProjectCard from '@/components/project/ProjectCard.vue'
import CreateProjectForm from '@/components/project/CreateProjectForm.vue'

const projectStore = useProjectStore()

const isLoading = ref(true)
const errorMessage = ref<string | null>(null)
const createDialog = useTemplateRef<HTMLDialogElement>('createDialog')
const isCreateFormOpen = ref(false)

async function openCreateDialog() {
  isCreateFormOpen.value = true
  await nextTick()
  createDialog.value?.showModal()
}

function closeCreateDialog() {
  createDialog.value?.close()
}

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
    <div class="flex items-center justify-between pt-4">
      <h1 class="text-2xl font-bold text-slate-100">Projects</h1>
      <button
        type="button"
        aria-haspopup="dialog"
        class="rounded-md bg-emerald-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-emerald-500"
        @click="openCreateDialog"
      >
        New project
      </button>
    </div>

    <dialog
      ref="createDialog"
      aria-labelledby="create-project-heading"
      class="m-auto w-full max-w-md rounded-lg border border-slate-700 bg-slate-800 p-6 text-slate-100 backdrop:bg-black/60"
      @close="isCreateFormOpen = false"
    >
      <CreateProjectForm v-if="isCreateFormOpen" @cancel="closeCreateDialog" />
    </dialog>

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