import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import client from '@/api/httpClient'
import { useProjectStore } from '@/stores/projectStore'
import type { ProjectResponse } from '@/types/project'

const clientMock = vi.hoisted(() => ({
  get: vi.fn<typeof client.get>(),
  post: vi.fn<typeof client.post>(),
  patch: vi.fn<typeof client.patch>(),
  delete: vi.fn<typeof client.delete>(),
}))

vi.mock('@/api/httpClient', () => ({ default: clientMock }))

const PROJECT_NAME = 'Curse of Strahd'
const GAME_SYSTEM = 'Dungeons & Dragons 5e'
const DESCRIPTION = 'A barony lost in fog.'
const NICK_NAME = 'Strahd von Zarovich'
const NEW_PROJECT_NAME = 'House of Lament'

const FIRST_PROJECT: ProjectResponse = {
  id: 7,
  name: 'Tomb of Annihilation',
  gameSystem: null,
  description: null,
  membershipRole: 'PLAYER',
}
const SECOND_PROJECT: ProjectResponse = {
  id: 12,
  name: PROJECT_NAME,
  gameSystem: null,
  description: null,
  membershipRole: 'GAME_MASTER',
}

// Only the part of an Axios response the store reads.
function axiosResponse<T>(data: T) {
  return { data } as never
}

describe('projectStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('sends name, game system, description and nickname in this order and adds the created project', async () => {
    const created: ProjectResponse = {
      id: 20,
      name: PROJECT_NAME,
      gameSystem: GAME_SYSTEM,
      description: DESCRIPTION,
      membershipRole: 'GAME_MASTER',
    }
    clientMock.post.mockResolvedValue(axiosResponse(created))
    const store = useProjectStore()

    const result = await store.createProject(PROJECT_NAME, GAME_SYSTEM, DESCRIPTION, NICK_NAME)

    expect(clientMock.post).toHaveBeenCalledWith('/api/projects', {
      name: PROJECT_NAME,
      gameSystem: GAME_SYSTEM,
      description: DESCRIPTION,
      nickName: NICK_NAME,
    })
    expect(result).toEqual(created)
    expect(store.projects).toEqual([created])
  })

  it('replaces the renamed project by id, not by position', async () => {
    const renamed: ProjectResponse = { ...SECOND_PROJECT, name: NEW_PROJECT_NAME }
    clientMock.patch.mockResolvedValue(axiosResponse(renamed))
    const store = useProjectStore()
    store.projects = [FIRST_PROJECT, SECOND_PROJECT]

    await store.renameProject(SECOND_PROJECT.id, NEW_PROJECT_NAME)

    expect(clientMock.patch).toHaveBeenCalledWith('/api/projects/12/name', { name: NEW_PROJECT_NAME })
    expect(store.projects).toEqual([FIRST_PROJECT, renamed])
    expect(store.currentProject).toEqual(renamed)
  })

  it('leaves the list untouched when the renamed project is not in it', async () => {
    const renamed: ProjectResponse = { ...SECOND_PROJECT, name: NEW_PROJECT_NAME }
    clientMock.patch.mockResolvedValue(axiosResponse(renamed))
    const store = useProjectStore()
    store.projects = [FIRST_PROJECT]

    await store.renameProject(SECOND_PROJECT.id, NEW_PROJECT_NAME)

    expect(store.projects).toEqual([FIRST_PROJECT])
    // toEqual ignores non-index keys on arrays; a write to index -1 would only show up here.
    expect(Object.keys(store.projects)).toEqual(['0'])
    expect(store.currentProject).toEqual(renamed)
  })

  it('removes only the deleted project', async () => {
    clientMock.delete.mockResolvedValue(axiosResponse(undefined))
    const store = useProjectStore()
    store.projects = [FIRST_PROJECT, SECOND_PROJECT]
    store.currentProject = FIRST_PROJECT

    await store.deleteProject(FIRST_PROJECT.id)

    expect(clientMock.delete).toHaveBeenCalledWith('/api/projects/7')
    expect(store.projects).toEqual([SECOND_PROJECT])
    expect(store.currentProject).toBeNull()
  })

  it('keeps list and current project when deleting fails', async () => {
    clientMock.delete.mockRejectedValue(new Error('Forbidden'))
    const store = useProjectStore()
    store.projects = [FIRST_PROJECT, SECOND_PROJECT]
    store.currentProject = FIRST_PROJECT

    await expect(store.deleteProject(FIRST_PROJECT.id)).rejects.toThrow('Forbidden')

    expect(store.projects).toEqual([FIRST_PROJECT, SECOND_PROJECT])
    expect(store.currentProject).toEqual(FIRST_PROJECT)
  })

  it('clears list and current project', () => {
    const store = useProjectStore()
    store.projects = [FIRST_PROJECT, SECOND_PROJECT]
    store.currentProject = FIRST_PROJECT

    store.clearProjects()

    expect(store.projects).toEqual([])
    expect(store.currentProject).toBeNull()
  })

  it('sends the game system to its own endpoint and replaces the project', async () => {
    const updated: ProjectResponse = { ...SECOND_PROJECT, gameSystem: GAME_SYSTEM }
    clientMock.patch.mockResolvedValue(axiosResponse(updated))
    const store = useProjectStore()
    store.projects = [FIRST_PROJECT, SECOND_PROJECT]

    await store.updateGameSystem(SECOND_PROJECT.id, GAME_SYSTEM)

    expect(clientMock.patch).toHaveBeenCalledWith('/api/projects/12/game-system', { gameSystem: GAME_SYSTEM })
    expect(store.projects).toEqual([FIRST_PROJECT, updated])
    expect(store.currentProject).toEqual(updated)
  })

  it('sends the description to its own endpoint and replaces the project', async () => {
    const updated: ProjectResponse = { ...SECOND_PROJECT, description: DESCRIPTION }
    clientMock.patch.mockResolvedValue(axiosResponse(updated))
    const store = useProjectStore()
    store.projects = [FIRST_PROJECT, SECOND_PROJECT]

    await store.updateDescription(SECOND_PROJECT.id, DESCRIPTION)

    expect(clientMock.patch).toHaveBeenCalledWith('/api/projects/12/description', { description: DESCRIPTION })
    expect(store.projects).toEqual([FIRST_PROJECT, updated])
    expect(store.currentProject).toEqual(updated)
  })
})