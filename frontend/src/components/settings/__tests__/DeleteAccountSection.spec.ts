import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import DeleteAccountSection from '../DeleteAccountSection.vue'

const PASSWORD = 'pass1234'

const { deleteAccount, push } = vi.hoisted(() => ({
  deleteAccount: vi.fn<(password: string) => Promise<void>>(),
  push: vi.fn<(to: unknown) => Promise<void>>(),
}))

vi.mock('@/stores/userStore', () => ({
  useUserStore: () => ({ deleteAccount }),
}))

vi.mock('vue-router', () => ({
  useRouter: () => ({ push }),
}))

async function fillAndSubmit() {
  const wrapper = mount(DeleteAccountSection)

  await wrapper.get('#settings-delete-password').setValue(PASSWORD)
  await wrapper.get('form').trigger('submit')
  await flushPromises()

  return wrapper
}

describe('DeleteAccountSection', () => {
  beforeEach(() => {
    deleteAccount.mockReset()
    push.mockReset()
  })

  it('passes the password to the store', async () => {
    deleteAccount.mockResolvedValue(undefined)

    await fillAndSubmit()

    expect(deleteAccount).toHaveBeenCalledWith(PASSWORD)
  })

  it('sends the user to the login page after the account is deleted', async () => {
    deleteAccount.mockResolvedValue(undefined)

    await fillAndSubmit()

    // The query parameter carries the confirmation across the page change; store
    // state would not survive it.
    expect(push).toHaveBeenCalledWith({ name: 'login', query: { deleted: '1' } })
  })

  it('shows the error and stays on the page when the deletion is rejected', async () => {
    deleteAccount.mockRejectedValue(new Error('rejected'))

    const wrapper = await fillAndSubmit()

    expect(wrapper.text()).toContain('Could not delete account.')
    expect(push).not.toHaveBeenCalled()
  })
})