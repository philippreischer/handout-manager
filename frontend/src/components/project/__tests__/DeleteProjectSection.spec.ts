import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import DeleteProjectSection from '@/components/project/DeleteProjectSection.vue'
import type { ProjectResponse } from '@/types/project'

const { storeMock, routerMock } = vi.hoisted(() => ({
  storeMock: { deleteProject: vi.fn<(projectId: number) => Promise<void>>() },
  routerMock: { push: vi.fn<(to: unknown) => Promise<void>>() },
}))

vi.mock('@/stores/projectStore', () => ({ useProjectStore: () => storeMock }))
vi.mock('vue-router', () => ({ useRouter: () => routerMock }))

const PROJECT: ProjectResponse = { id: 12, name: 'Curse of Strahd', membershipRole: 'GAME_MASTER' }
const WRONG_CASE_NAME = 'curse of strahd'

function mountSection() {
  return mount(DeleteProjectSection, { props: { project: PROJECT } })
}

function submitButton(wrapper: ReturnType<typeof mountSection>) {
  return wrapper.get<HTMLButtonElement>('button[type="submit"]')
}

describe('DeleteProjectSection', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('keeps the button disabled until the name matches exactly, case included', async () => {
    const wrapper = mountSection()
    expect(submitButton(wrapper).element.disabled).toBe(true)

    await wrapper.get('#delete-project-confirmation').setValue(WRONG_CASE_NAME)
    expect(submitButton(wrapper).element.disabled).toBe(true)

    await wrapper.get('#delete-project-confirmation').setValue(PROJECT.name)
    expect(submitButton(wrapper).element.disabled).toBe(false)
  })

  it('deletes the project and navigates home on success', async () => {
    storeMock.deleteProject.mockResolvedValue()
    const wrapper = mountSection()

    await wrapper.get('#delete-project-confirmation').setValue(PROJECT.name)
    await wrapper.get('form').trigger('submit')
    await flushPromises()

    expect(storeMock.deleteProject).toHaveBeenCalledWith(PROJECT.id)
    expect(routerMock.push).toHaveBeenCalledWith({ name: 'home' })
  })

  it('shows an error and does not navigate when deleting fails', async () => {
    storeMock.deleteProject.mockRejectedValue(new Error('Forbidden'))
    const wrapper = mountSection()

    await wrapper.get('#delete-project-confirmation').setValue(PROJECT.name)
    await wrapper.get('form').trigger('submit')
    await flushPromises()

    expect(routerMock.push).not.toHaveBeenCalled()
    expect(wrapper.find('[role="alert"]').exists()).toBe(true)
  })
})