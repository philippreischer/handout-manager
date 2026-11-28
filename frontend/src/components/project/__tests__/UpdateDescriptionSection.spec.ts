import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import UpdateDescriptionSection from '@/components/project/UpdateDescriptionSection.vue'
import type { ProjectResponse } from '@/types/project'

const { storeMock } = vi.hoisted(() => ({
  storeMock: {
    updateDescription: vi.fn<(projectId: number, description: string) => Promise<void>>(),
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
const NEW_DESCRIPTION = 'Arkham, 1923.\n\nA professor has gone missing.'

async function submitDescription(wrapper: ReturnType<typeof mount>, description: string) {
  await wrapper.get('#update-description').setValue(description)
  await wrapper.get('form').trigger('submit')
  await flushPromises()
}

describe('UpdateDescriptionSection', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('starts empty for a project without description and sends id and new value', async () => {
    storeMock.updateDescription.mockResolvedValue()
    const wrapper = mount(UpdateDescriptionSection, { props: { project: PROJECT } })

    expect(wrapper.get<HTMLTextAreaElement>('#update-description').element.value).toBe('')

    await submitDescription(wrapper, NEW_DESCRIPTION)

    expect(storeMock.updateDescription).toHaveBeenCalledWith(PROJECT.id, NEW_DESCRIPTION)
  })

  it('shows an error and no confirmation when updating fails', async () => {
    storeMock.updateDescription.mockRejectedValue(new Error('Forbidden'))
    const wrapper = mount(UpdateDescriptionSection, { props: { project: PROJECT } })

    await submitDescription(wrapper, NEW_DESCRIPTION)

    expect(wrapper.find('[role="alert"]').exists()).toBe(true)
    expect(wrapper.find('[role="status"]').exists()).toBe(false)
  })
})