import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import CreateProjectForm from '@/components/project/CreateProjectForm.vue'
import type { ProjectResponse } from '@/types/project'

const { storeMock, routerMock } = vi.hoisted(() => ({
  storeMock: {
    createProject: vi.fn<(name: string, nickName: string) => Promise<ProjectResponse>>(),
  },
  routerMock: { push: vi.fn<(to: unknown) => Promise<void>>() },
}))

vi.mock('@/stores/projectStore', () => ({ useProjectStore: () => storeMock }))
vi.mock('vue-router', () => ({ useRouter: () => routerMock }))

const PROJECT_NAME = 'Curse of Strahd'
const NICK_NAME = 'Strahd von Zarovich'
const CREATED_PROJECT: ProjectResponse = { id: 99, name: PROJECT_NAME, membershipRole: 'GAME_MASTER' }

async function fillAndSubmit(wrapper: ReturnType<typeof mount>) {
  await wrapper.get('#create-project-name').setValue(PROJECT_NAME)
  await wrapper.get('#create-project-nickname').setValue(NICK_NAME)
  await wrapper.get('form').trigger('submit')
  await flushPromises()
}

describe('CreateProjectForm', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('passes name and nickname to the store in this order', async () => {
    storeMock.createProject.mockResolvedValue(CREATED_PROJECT)
    const wrapper = mount(CreateProjectForm)

    await fillAndSubmit(wrapper)

    expect(storeMock.createProject).toHaveBeenCalledWith(PROJECT_NAME, NICK_NAME)
  })

  it('navigates to the project returned by the store', async () => {
    storeMock.createProject.mockResolvedValue(CREATED_PROJECT)
    const wrapper = mount(CreateProjectForm)

    await fillAndSubmit(wrapper)

    expect(routerMock.push).toHaveBeenCalledWith({
      name: 'project',
      params: { projectId: CREATED_PROJECT.id },
    })
  })

  it('shows an error and does not navigate when creating fails', async () => {
    storeMock.createProject.mockRejectedValue(new Error('Server error'))
    const wrapper = mount(CreateProjectForm)

    await fillAndSubmit(wrapper)

    expect(routerMock.push).not.toHaveBeenCalled()
    expect(wrapper.find('[role="alert"]').exists()).toBe(true)
  })

  it('emits cancel from a button that cannot submit the form', async () => {
    const wrapper = mount(CreateProjectForm)

    const cancelButton = wrapper.findAll('button').find((button) => button.text() === 'Cancel')

    expect(cancelButton!.attributes('type')).toBe('button')

    await cancelButton!.trigger('click')
    expect(wrapper.emitted('cancel')).toHaveLength(1)
  })
})