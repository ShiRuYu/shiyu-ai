import { afterEach, describe, expect, it, vi } from 'vitest'
import { ProjectApiClient } from './projectApi'

describe('ProjectApiClient', () => {
  afterEach(() => vi.unstubAllGlobals())

  it('refreshes a token but never replays a write request', async () => {
    const fetchMock = vi.fn()
      .mockResolvedValueOnce(new Response('{"success":true,"data":{"accessToken":"old"}}', { status: 200 }))
      .mockResolvedValueOnce(new Response('{"message":"expired"}', { status: 401 }))
      .mockResolvedValueOnce(new Response('{"success":true,"data":"new"}', { status: 200 }))
    vi.stubGlobal('fetch', fetchMock)
    const api = new ProjectApiClient()

    await api.login('operator', 'password')
    const response = await api.request('/api/example', { method: 'POST', body: '{"write":true}' })

    expect(response.status).toBe(401)
    expect(fetchMock).toHaveBeenCalledTimes(3)
    expect(api.authenticated).toBe(true)
    expect(fetchMock.mock.calls[1][1].headers.get('Authorization')).toBe('Bearer old')
  })

  it('deduplicates concurrent refreshes and retries safe reads once', async () => {
    const fetchMock = vi.fn()
      .mockResolvedValueOnce(new Response('{"success":true,"data":{"accessToken":"old"}}', { status: 200 }))
      .mockResolvedValueOnce(new Response('{}', { status: 401 }))
      .mockResolvedValueOnce(new Response('{}', { status: 401 }))
      .mockResolvedValueOnce(new Response('{"success":true,"data":"new"}', { status: 200 }))
      .mockResolvedValueOnce(new Response('{"data":1}', { status: 200 }))
      .mockResolvedValueOnce(new Response('{"data":2}', { status: 200 }))
    vi.stubGlobal('fetch', fetchMock)
    const api = new ProjectApiClient()
    await api.login('operator', 'password')

    const responses = await Promise.all([
      api.request('/api/first'),
      api.request('/api/second'),
    ])

    expect(responses.map(item => item.ok)).toEqual([true, true])
    expect(fetchMock.mock.calls.filter(([url]) => url === '/api/iam/auth/refresh')).toHaveLength(1)
    expect(fetchMock).toHaveBeenCalledTimes(6)
  })
})
