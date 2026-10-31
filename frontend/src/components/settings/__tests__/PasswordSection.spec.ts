import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import PasswordSection from '../PasswordSection.vue'

const { changePassword } = vi.hoisted(() => ({
  changePassword: vi.fn<(password: string, newPassword: string) => Promise<void>>(),
}))

// The store module itself is replaced, so no Pinia instance is needed here.
vi.mock('@/stores/userStore', () => ({
  useUserStore: () => ({ changePassword }),
}))

const CURRENT = 'oldpass1234'
const NEXT = 'newpass1234'

async function fillAndSubmit() {
  const wrapper = mount(PasswordSection)

  await wrapper.get('#settings-current-password').setValue(CURRENT)
  await wrapper.get('#settings-new-password').setValue(NEXT)
  await wrapper.get('form').trigger('submit')
  await flushPromises()

  return wrapper
}

function valueOf(wrapper: Awaited<ReturnType<typeof fillAndSubmit>>, selector: string) {
  return (wrapper.get(selector).element as HTMLInputElement).value
}

describe('PasswordSection', () => {
  beforeEach(() => {
    changePassword.mockReset()
  })

  it('passes the current and the new password to the store in that order', async () => {
    changePassword.mockResolvedValue(undefined)

    await fillAndSubmit()

    // Both parameters are strings: swapping them compiles and would send the new
    // password as the current one.
    expect(changePassword).toHaveBeenCalledWith(CURRENT, NEXT)
  })

  it('clears both fields and confirms after a successful change', async () => {
    changePassword.mockResolvedValue(undefined)

    const wrapper = await fillAndSubmit()

    expect(wrapper.text()).toContain('Password changed')
    expect(valueOf(wrapper, '#settings-current-password')).toBe('')
    expect(valueOf(wrapper, '#settings-new-password')).toBe('')
  })

  it('shows the error and not the confirmation when the change is rejected', async () => {
    changePassword.mockRejectedValue(new Error('rejected'))

    const wrapper = await fillAndSubmit()

    // The fallback text comes from the real extractErrorMessage.
    expect(wrapper.text()).toContain('Could not change password.')
    expect(wrapper.text()).not.toContain('Password changed')
  })
})