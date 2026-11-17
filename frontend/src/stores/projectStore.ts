import { defineStore } from 'pinia'
import { ref } from 'vue'
import client from '@/api/httpClient'
import type { ProjectResponse } from '@/types/project'

export const useProjectStore = defineStore('project', () => {
  const projects = ref<ProjectResponse[]>([])

  async function loadProjects(){
    const response = await client.get<ProjectResponse[]>('/api/projects')
    projects.value = response.data

  }

  function clearProjects() {
    projects.value = []
  }

  return {
    projects,
    loadProjects,
    clearProjects
  }
})