import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import RenameProjectSection from '@/components/project/RenameProjectSection.vue'
import type { ProjectResponse } from '@/types/project'

const { storeMock } = vi.hoisted(() => ({
  storeMock: {
    renameProject: vi.fn<(projectId: number, name: string) => Promise<void>>(),
  },
}))

vi.mock('@/stores/projectStore', () => ({ useProjectStore: () => storeMock }))

const PROJECT: ProjectResponse = {
  id: 12,
  name: 'Curse of Strahd',
  gameSystem: null,
  description: null,
  membershipRole: 'GAME_MASTER',
}

const NEW_PROJECT_NAME = 'House of Lament'

async function renameTo(wrapper: ReturnType<typeof mount>, name: string) {
  await wrapper.get('#rename-project-name').setValue(name)
  await wrapper.get('form').trigger('submit')
  await flushPromises()
}

describe('RenameProjectSection', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('prefills the current name and sends id and new name to the store', async () => {
    storeMock.renameProject.mockResolvedValue()
    const wrapper = mount(RenameProjectSection, { props: { project: PROJECT } })

    expect(wrapper.get<HTMLInputElement>('#rename-project-name').element.value).toBe(PROJECT.name)

    await renameTo(wrapper, NEW_PROJECT_NAME)

    expect(storeMock.renameProject).toHaveBeenCalledWith(PROJECT.id, NEW_PROJECT_NAME)
  })

  it('shows a confirmation after a successful rename', async () => {
    storeMock.renameProject.mockResolvedValue()
    const wrapper = mount(RenameProjectSection, { props: { project: PROJECT } })

    await renameTo(wrapper, NEW_PROJECT_NAME)

    expect(wrapper.get('[role="status"]').text()).toBe('Project name updated.')
  })

  it('shows an error and no confirmation when renaming fails', async () => {
    storeMock.renameProject.mockRejectedValue(new Error('Forbidden'))
    const wrapper = mount(RenameProjectSection, { props: { project: PROJECT } })

    await renameTo(wrapper, NEW_PROJECT_NAME)

    expect(wrapper.find('[role="alert"]').exists()).toBe(true)
    expect(wrapper.find('[role="status"]').exists()).toBe(false)
  })
})