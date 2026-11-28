<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useProjectStore } from '@/stores/projectStore'
import { extractErrorMessage } from '@/api/errorMessage'
import ProjectNameField from '@/components/ui/ProjectNameField.vue'
import ProjectNickNameField from '@/components/ui/ProjectNickNameField.vue'
import ProjectGameSystemField from '@/components/ui/ProjectGameSystemField.vue'
import ProjectDescriptionField from '@/components/ui/ProjectDescriptionField.vue'


const router = useRouter()
const projectStore = useProjectStore()

const emit = defineEmits<{ cancel: [] }>()
const name = ref('')
const gameSystem = ref('')
const description = ref('')
const nickName = ref('')
const errorMessage = ref<string | null>(null)
const isSubmitting = ref(false)

async function handleSubmit() {
  errorMessage.value = null
  isSubmitting.value = true

  try {
    const project = await projectStore.createProject(name.value, gameSystem.value, description.value, nickName.value)
    await router.push({ name: 'project', params: { projectId: project.id } })
  } catch (error) {
    errorMessage.value = extractErrorMessage(error, 'Create project failed.')
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <form class="space-y-5" @submit.prevent="handleSubmit">
    <h2 id="create-project-heading" class="text-lg font-semibold text-slate-200">Create project</h2>

    <ProjectNameField id="create-project-name" v-model="name" />

    <ProjectGameSystemField id="create-project-game-system" v-model="gameSystem" />

    <ProjectDescriptionField id="create-project-description" v-model="description" />

    <ProjectNickNameField id="create-project-nickname" v-model="nickName" />


    <p v-if="errorMessage" role="alert" class="text-sm text-red-400">
      {{ errorMessage }}
    </p>

    <button
      type="submit"
      :disabled="isSubmitting"
      class="w-full rounded-md bg-emerald-600 px-4 py-2 font-medium text-white hover:bg-emerald-500 disabled:cursor-not-allowed disabled:opacity-50"
    >
      {{ isSubmitting ? 'Creating…' : 'Create project' }}
    </button>
    <button
      type="button"
      class="w-full rounded-md bg-slate-700 px-4 py-2 font-medium text-slate-100 hover:bg-slate-600"
      @click="emit('cancel')"
    >
      Cancel
    </button>
  </form>
</template>
