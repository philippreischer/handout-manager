import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import PasswordField from '../PasswordField.vue'

const ID = 'test-password'
const HINT_SELECTOR = `#${ID}-hint`

function mountField(purpose?: 'current' | 'new') {
  return mount(PasswordField, {
    props: { id: ID, modelValue: '', ...(purpose ? { purpose } : {}) },
  })
}

describe('PasswordField', () => {
  it('toggles the input type when the button is clicked', async () => {
    const wrapper = mountField()

    expect(wrapper.get('input').attributes('type')).toBe('password')

    await wrapper.get('button').trigger('click')
    expect(wrapper.get('input').attributes('type')).toBe('text')

    await wrapper.get('button').trigger('click')
    expect(wrapper.get('input').attributes('type')).toBe('password')
  })

  it('passes what the user types up to the parent', async () => {
    const wrapper = mountField()

    await wrapper.get('input').setValue('secret1234')

    // The model travels through FormField first, so this also proves the
    // two-level hand-over works.
    expect(wrapper.emitted('update:modelValue')).toEqual([['secret1234']])
  })

  it('applies the length rule and shows the hint for a new password', () => {
    const wrapper = mountField('new')
    const input = wrapper.get('input')

    expect(input.attributes('minlength')).toBe('8')
    expect(input.attributes('autocomplete')).toBe('new-password')
    expect(input.attributes('aria-describedby')).toBe(`${ID}-hint`)
    expect(wrapper.get(HINT_SELECTOR).text()).toContain('8 characters')
  })

  it('omits the length rule for the current password', () => {
    const wrapper = mountField('current')
    const input = wrapper.get('input')

    // A minimum length here would lock out an account whose password predates
    // the rule, and the browser must offer the saved password, not a new one.
    expect(input.attributes('minlength')).toBeUndefined()
    expect(input.attributes('autocomplete')).toBe('current-password')
    expect(input.attributes('aria-describedby')).toBeUndefined()
    expect(wrapper.find(HINT_SELECTOR).exists()).toBe(false)
  })
})
