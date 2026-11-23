<script setup lang="ts">
import { ref } from 'vue'
import { useProjectStore } from '@/stores/projectStore'
import { extractErrorMessage } from '@/api/errorMessage'
import type { ProjectResponse } from '@/types/project'
import ProjectNameField from '@/components/ui/ProjectNameField.vue'

const projectStore = useProjectStore();

const props = defineProps<{
  project: ProjectResponse
}>()

const name = ref(props.project.name)
const isSubmitting = ref(false)
const errorMessage = ref<string | null>(null)
const isSaved = ref(false)

async function handleSubmit() {
  errorMessage.value = null
  isSaved.value = false
  isSubmitting.value = true

  try {
    await projectStore.renameProject(props.project.id, name.value)
    isSaved.value = true
  } catch (error) {
    errorMessage.value = extractErrorMessage(error, 'Could not update project name.')
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <section class="rounded-xl bg-slate-800 p-6 shadow-xl">
    <h3 class="mb-4 text-lg font-semibold text-slate-100">Change project name</h3>

    <form class="space-y-4" @submit.prevent="handleSubmit">
      
        <ProjectNameField id="rename-project-name" v-model="name" />

      <p v-if="errorMessage" role="alert" class="text-sm text-red-400">{{ errorMessage }}</p>
      <p v-else-if="isSaved" role="status" class="text-sm text-emerald-400">Project name updated.</p>

      <button
        type="submit"
        :disabled="isSubmitting"
        class="rounded-md bg-emerald-600 px-4 py-2 font-medium text-white hover:bg-emerald-500 disabled:cursor-not-allowed disabled:opacity-50"
      >
        {{ isSubmitting ? 'Saving…' : 'Save project name' }}
      </button>
    </form>

  </section>
</template>