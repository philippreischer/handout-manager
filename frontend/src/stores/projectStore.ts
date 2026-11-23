import { defineStore } from 'pinia'
import { ref } from 'vue'
import client from '@/api/httpClient'
import type { ProjectResponse } from '@/types/project'

export const useProjectStore = defineStore('project', () => {
  const projects = ref<ProjectResponse[]>([])
  const currentProject = ref<ProjectResponse | null>(null)

  async function loadProjects(){
    const response = await client.get<ProjectResponse[]>('/api/projects')
    projects.value = response.data
  }

  async function loadProject(projectId: number){
    const response = await client.get<ProjectResponse>(`/api/projects/${projectId}`)
    currentProject.value = response.data
  }

  async function createProject(name: string, nickName: string){
    const response = await client.post<ProjectResponse>('/api/projects', { name, nickName })
    projects.value.push(response.data)
    currentProject.value = response.data
    return response.data
  }

  async function renameProject(projectId: number, name: string) {
    const response = await client.patch<ProjectResponse>(`/api/projects/${projectId}/name`, { name })
    const index = projects.value.findIndex((project) => project.id === projectId)
    if (index !== -1) {
      projects.value[index] = response.data
    }
    currentProject.value = response.data
  }

  async function deleteProject(projectId: number) {
    await client.delete(`/api/projects/${projectId}`)
    projects.value = projects.value.filter((project) => project.id !== projectId)
    currentProject.value = null
  }

  function clearProjects() {
    projects.value = []
    currentProject.value = null
  }

  return {
    projects,
    currentProject,
    loadProjects,
    loadProject,
    createProject,
    renameProject,
    deleteProject,
    clearProjects,
    
  }
})