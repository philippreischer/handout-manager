import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import type { RouteLocationRaw } from 'vue-router'
import {
  AxiosError,
  AxiosHeaders,
  type AxiosAdapter,
  type AxiosResponse,
  type InternalAxiosRequestConfig,
} from 'axios'

// Hoisted so the factory below can reach it: vi.mock is moved to the top of
// the file, above any plain const declaration.
const { pushMock } = vi.hoisted(() => ({
  pushMock: vi.fn<(to: RouteLocationRaw) => Promise<void>>(),
}))

vi.mock('@/router', () => ({ default: { push: pushMock } }))

import client from '@/api/httpClient'
import { setupInterceptors } from '@/api/interceptors'
import { useAuthStore } from '@/stores/useAuthStore'

let refreshCount = 0
let accessTokenIsValid = false
let refreshSucceeds = true

function ok<T>(config: InternalAxiosRequestConfig, data: T): AxiosResponse<T> {
  return { data, status: 200, statusText: 'OK', headers: new AxiosHeaders(), config }
}

function unauthorized(config: InternalAxiosRequestConfig): AxiosError {
  const response: AxiosResponse = {
    data: { detail: 'Unauthorized' },
    status: 401,
    statusText: 'Unauthorized',
    headers: new AxiosHeaders(),
    config,
  }

  return new AxiosError('401', AxiosError.ERR_BAD_REQUEST, config, undefined, response)
}

// Replaces the layer that would talk to the network. The interceptors run
// around it, exactly as they do against a real server.
const adapter: AxiosAdapter = async (config) => {
  if (config.url === '/api/auth/refresh') {
    refreshCount += 1

    if (!refreshSucceeds) {
      throw unauthorized(config)
    }

    accessTokenIsValid = true
    return ok(config, { accessToken: `access-token-${refreshCount}` })
  }

  if (!accessTokenIsValid) {
    throw unauthorized(config)
  }

  return ok(config, 1)
}

describe('interceptors', () => {
  beforeEach(() => {
    setActivePinia(createPinia())

    client.interceptors.request.clear()
    client.interceptors.response.clear()
    client.defaults.adapter = adapter

    refreshCount = 0
    accessTokenIsValid = false
    refreshSucceeds = true
    pushMock.mockClear()

    setupInterceptors()
  })

  it('refreshes only once when two requests fail at the same time', async () => {
    useAuthStore().accessToken = 'expired-access-token'

    const [first, second] = await Promise.all([client.get('/api/me'), client.get('/api/me')])

    expect(refreshCount).toBe(1)
    expect(first.status).toBe(200)
    expect(second.status).toBe(200)
  })

  it('does not refresh while the access token is still valid', async () => {
    accessTokenIsValid = true
    useAuthStore().accessToken = 'valid-access-token'

    const response = await client.get('/api/me')

    expect(refreshCount).toBe(0)
    expect(response.status).toBe(200)
  })

  it('clears the session and redirects when the refresh fails', async () => {
    refreshSucceeds = false
    const authStore = useAuthStore()
    authStore.accessToken = 'expired-access-token'

    await expect(client.get('/api/me')).rejects.toBeInstanceOf(AxiosError)

    expect(authStore.accessToken).toBeNull()
    expect(pushMock).toHaveBeenCalledWith({ name: 'login' })
  })
})
