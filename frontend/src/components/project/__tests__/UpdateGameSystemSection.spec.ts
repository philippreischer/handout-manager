import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import UpdateGameSystemSection from '@/components/project/UpdateGameSystemSection.vue'
import type { ProjectResponse } from '@/types/project'

const { storeMock } = vi.hoisted(() => ({
  storeMock: {
    updateGameSystem: vi.fn<(projectId: number, gameSystem: string) => Promise<void>>(),
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
const NEW_GAME_SYSTEM = 'Call of Cthulhu 7e'

async function submitGameSystem(wrapper: ReturnType<typeof mount>, gameSystem: string) {
  await wrapper.get('#update-game-system').setValue(gameSystem)
  await wrapper.get('form').trigger('submit')
  await flushPromises()
}

describe('UpdateGameSystemSection', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('starts empty for a project without game system and sends id and new value', async () => {
    storeMock.updateGameSystem.mockResolvedValue()
    const wrapper = mount(UpdateGameSystemSection, { props: { project: PROJECT } })

    expect(wrapper.get<HTMLInputElement>('#update-game-system').element.value).toBe('')

    await submitGameSystem(wrapper, NEW_GAME_SYSTEM)

    expect(storeMock.updateGameSystem).toHaveBeenCalledWith(PROJECT.id, NEW_GAME_SYSTEM)
  })

  it('shows an error and no confirmation when updating fails', async () => {
    storeMock.updateGameSystem.mockRejectedValue(new Error('Forbidden'))
    const wrapper = mount(UpdateGameSystemSection, { props: { project: PROJECT } })

    await submitGameSystem(wrapper, NEW_GAME_SYSTEM)

    expect(wrapper.find('[role="alert"]').exists()).toBe(true)
    expect(wrapper.find('[role="status"]').exists()).toBe(false)
  })
})