<script setup lang="ts">
import RenameProjectSection from '@/components/project/RenameProjectSection.vue'
import UpdateGameSystemSection from '@/components/project/UpdateGameSystemSection.vue'
import UpdateDescriptionSection from '@/components/project/UpdateDescriptionSection.vue'
import DeleteProjectSection from '@/components/project/DeleteProjectSection.vue'
import type { ProjectResponse } from '@/types/project'

defineProps<{
  project: ProjectResponse
}>()
</script>

<template>
  <section id="project-settings" class="space-y-6 border-t border-slate-700 pt-6">
    <h2 class="text-xl font-semibold text-slate-100">Settings</h2>
    <RenameProjectSection :project="project" />
    <UpdateGameSystemSection :project="project" />
    <UpdateDescriptionSection :project="project" />
    <DeleteProjectSection :project="project" />
  </section>
</template>