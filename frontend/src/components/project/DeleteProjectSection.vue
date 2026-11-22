<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useProjectStore } from '@/stores/projectStore'
import { extractErrorMessage } from '@/api/errorMessage'
import type { ProjectResponse } from '@/types/project'
import ProjectNameField from '@/components/ui/ProjectNameField.vue'

const props = defineProps<{
  project: ProjectResponse
}>()

const router = useRouter()
const projectStore = useProjectStore()

const confirmation = ref('')
const isSubmitting = ref(false)
const errorMessage = ref<string | null>(null)

const isConfirmed = computed(() => confirmation.value === props.project.name)

async function handleSubmit() {
  errorMessage.value = null
  isSubmitting.value = true

  try {
    await projectStore.deleteProject(props.project.id)
    await router.push({ name: 'home' })
  } catch (error) {
    errorMessage.value = extractErrorMessage(error, 'Could not delete project.')
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <section class="rounded-xl border border-red-900 bg-slate-800 p-6 shadow-xl">
    <h3 class="mb-2 text-lg font-semibold text-red-400">Delete project</h3>
    <p class="mb-4 text-sm text-slate-400">
      This permanently deletes the project and everything in it. This cannot be undone.
    </p>

    <form class="space-y-4" @submit.prevent="handleSubmit">
      <ProjectNameField
        id="delete-project-confirmation"
        v-model="confirmation"
        :label="`Type &quot;${project.name}&quot; to confirm`"
      />

      <p v-if="errorMessage" role="alert" class="text-sm text-red-400">{{ errorMessage }}</p>

      <button
        type="submit"
        :disabled="!isConfirmed || isSubmitting"
        class="rounded-md bg-red-600 px-4 py-2 font-medium text-white hover:bg-red-500 disabled:cursor-not-allowed disabled:opacity-50"
      >
        {{ isSubmitting ? 'Deleting…' : 'Delete project' }}
      </button>
    </form>
  </section>
</template>