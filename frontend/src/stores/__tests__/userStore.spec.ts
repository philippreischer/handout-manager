import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import type { AxiosResponse } from 'axios'
import client from '@/api/httpClient'
import { useAuthStore } from '@/stores/authStore'
import { useUserStore } from '@/stores/userStore'
import type { UserResponse } from '@/types/user'

vi.mock('@/api/httpClient', () => ({
  default: {
    get: vi.fn<typeof client.get>(),
    post: vi.fn<typeof client.post>(),
    patch: vi.fn<typeof client.patch>(),
    delete: vi.fn<typeof client.delete>(),
  },
}))

const USER: UserResponse = {
  id: 1,
  email: 'max@mustermann.com',
  userName: 'Max',
  createdAt: '2026-01-15T10:00:00Z',
}

const PASSWORD = 'pass1234'
const NEW_PASSWORD = 'newpass1234'
const NEW_USER_NAME = 'Moritz'
const ACCESS_TOKEN = 'new-access-token'
const WRONG_PASSWORD = 'wrong-password'

function response<T>(data: T) {
  return { data } as AxiosResponse<T>
}

describe('userStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.resetAllMocks()
  })

  it('should store the profile when loading the user', async () => {
    vi.mocked(client.get).mockResolvedValue(response(USER))
    const userStore = useUserStore()

    await userStore.loadUser()

    expect(userStore.user).toEqual(USER)
  })

  it('should take the updated profile from the response when changing the username', async () => {
    const updated = { ...USER, userName: NEW_USER_NAME }
    vi.mocked(client.patch).mockResolvedValue(response(updated))
    const userStore = useUserStore()

    await userStore.updateUserName(PASSWORD, NEW_USER_NAME)

    expect(client.patch).toHaveBeenCalledWith('/api/users/me/username', {
      password: PASSWORD,
      userName: NEW_USER_NAME,
    })
    expect(userStore.user?.userName).toBe(NEW_USER_NAME)
  })

  it('should hand the new token to the auth store when changing the password', async () => {
    vi.mocked(client.patch).mockResolvedValue(response({ accessToken: ACCESS_TOKEN }))
    const userStore = useUserStore()
    const authStore = useAuthStore()

    await userStore.changePassword(PASSWORD, NEW_PASSWORD)

    expect(authStore.accessToken).toBe(ACCESS_TOKEN)
  })

  it('should send the password in the request body when deleting the account', async () => {
    vi.mocked(client.delete).mockResolvedValue(response(null))
    const userStore = useUserStore()

    await userStore.deleteAccount(PASSWORD)

    // Axios puts a DELETE body into the config, not into a second argument.
    expect(client.delete).toHaveBeenCalledWith('/api/users/me', {
      data: { password: PASSWORD },
    })
  })

  it('should clear profile and session when deleting the account', async () => {
    vi.mocked(client.get).mockResolvedValue(response(USER))
    vi.mocked(client.delete).mockResolvedValue(response(null))
    const userStore = useUserStore()
    const authStore = useAuthStore()

    await userStore.loadUser()
    authStore.setAccessToken(ACCESS_TOKEN)

    await userStore.deleteAccount(PASSWORD)

    expect(userStore.user).toBeNull()
    expect(authStore.accessToken).toBeNull()
  })

  it('should not touch the session when the password is wrong', async () => {
    vi.mocked(client.delete).mockRejectedValue(new Error('request failed'))
    const userStore = useUserStore()
    const authStore = useAuthStore()
    authStore.setAccessToken(ACCESS_TOKEN)

    await expect(userStore.deleteAccount(WRONG_PASSWORD)).rejects.toThrow('request failed')

    expect(authStore.accessToken).toBe(ACCESS_TOKEN)
  })
})
