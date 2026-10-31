// src/components/settings/__tests__/UserNameSection.spec.ts
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import UserNameSection from '../UserNameSection.vue'

const USER_NAME = 'Max'
const NEW_USER_NAME = 'Moritz'
const PASSWORD = 'pass1234'

const { updateUserName } = vi.hoisted(() => ({
  updateUserName: vi.fn<(password: string, userName: string) => Promise<void>>(),
}))

// A factory, not a fixed object: the component reads user at setup time, so each
// mount must get its own.
vi.mock('@/stores/userStore', () => ({
  useUserStore: () => ({ user: { userName: USER_NAME }, updateUserName }),
}))

async function fillAndSubmit() {
  const wrapper = mount(UserNameSection)

  await wrapper.get('#settings-username').setValue(NEW_USER_NAME)
  await wrapper.get('#settings-username-password').setValue(PASSWORD)
  await wrapper.get('form').trigger('submit')
  await flushPromises()

  return wrapper
}

describe('UserNameSection', () => {
  beforeEach(() => {
    updateUserName.mockReset()
  })

  it('passes the password and the new username to the store in that order', async () => {
    updateUserName.mockResolvedValue(undefined)

    await fillAndSubmit()

    // Both parameters are strings: swapping them compiles and would send the
    // username as the password.
    expect(updateUserName).toHaveBeenCalledWith(PASSWORD, NEW_USER_NAME)
  })

  it('clears the password and confirms after a successful change', async () => {
    updateUserName.mockResolvedValue(undefined)

    const wrapper = await fillAndSubmit()

    expect(wrapper.text()).toContain('Username updated')
    expect((wrapper.get('#settings-username-password').element as HTMLInputElement).value).toBe('')
  })

  it('shows the error and not the confirmation when the change is rejected', async () => {
    updateUserName.mockRejectedValue(new Error('rejected'))

    const wrapper = await fillAndSubmit()

    expect(wrapper.text()).toContain('Could not update username.')
    expect(wrapper.text()).not.toContain('Username updated')
  })
})