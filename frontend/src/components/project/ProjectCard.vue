<script setup lang="ts">
import { computed } from 'vue'
import type { ProjectResponse } from '@/types/project'
import type { MembershipRole } from '@/types/membership'

const props = defineProps<{
  project: ProjectResponse
}>()

const ROLE_LABELS: Record<MembershipRole, string> = {
  GAME_MASTER: 'Game Master',
  PLAYER: 'Player',
}

const roleLabel = computed(() => ROLE_LABELS[props.project.membershipRole])
</script>

<template>
  <RouterLink
    :to="{ name: 'project', params: { projectId: project.id } }"
    class="block rounded-lg border border-slate-700 bg-slate-800 p-4 hover:border-slate-500 hover:bg-slate-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-400"
  >
    <h2 class="text-lg font-semibold text-slate-100">{{ project.name }}</h2>
    <p class="mt-1 text-sm text-slate-400">{{ roleLabel }}</p>
  </RouterLink>
</template>