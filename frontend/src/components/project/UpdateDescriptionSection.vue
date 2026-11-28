<script setup lang="ts">
import { ref } from 'vue'
import { useProjectStore } from '@/stores/projectStore'
import { extractErrorMessage } from '@/api/errorMessage'
import type { ProjectResponse } from '@/types/project'
import ProjectDescriptionField from '@/components/ui/ProjectDescriptionField.vue'

const projectStore = useProjectStore()

const props = defineProps<{
  project: ProjectResponse
}>()

const description = ref(props.project.description?? '')
const isSubmitting = ref(false)
const errorMessage = ref<string | null>(null)
const isSaved = ref(false)

async function handleSubmit() {
  errorMessage.value = null
  isSaved.value = false
  isSubmitting.value = true

  try {
    await projectStore.updateDescription(props.project.id, description.value)
    isSaved.value = true
  } catch (error) {
    errorMessage.value = extractErrorMessage(error, 'Could not update description.')
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <section class="rounded-xl bg-slate-800 p-6 shadow-xl">
    <h3 class="mb-4 text-lg font-semibold text-slate-100">Change description</h3>

    <form class="space-y-4" @submit.prevent="handleSubmit">
      
        <ProjectDescriptionField id="update-description" v-model="description" />

      <p v-if="errorMessage" role="alert" class="text-sm text-red-400">{{ errorMessage }}</p>
      <p v-else-if="isSaved" role="status" class="text-sm text-emerald-400">Description updated.</p>

      <button
        type="submit"
        :disabled="isSubmitting"
        class="rounded-md bg-emerald-600 px-4 py-2 font-medium text-white hover:bg-emerald-500 disabled:cursor-not-allowed disabled:opacity-50"
      >
        {{ isSubmitting ? 'Saving…' : 'Save description' }}
      </button>
    </form>

  </section>
</template>