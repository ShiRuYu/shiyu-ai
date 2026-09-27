import { afterEach, describe, expect, it, vi } from 'vitest'
import { redactHeaders, redactSecrets, safeApiRequest } from './safeApi'

describe('safeApiRequest', () => {
  afterEach(() => vi.unstubAllGlobals())

  it('refuses external URLs and paths outside the current /api namespace', async () => {
    const fetchMock = vi.fn()
    vi.stubGlobal('fetch', fetchMock)
    await expect(safeApiRequest('https://example.com/api/users')).rejects.toThrow('/api/**')
    await expect(safeApiRequest('/console/api/runtime')).rejects.toThrow('/api/**')
    expect(fetchMock).not.toHaveBeenCalled()
  })

  it('adds a bearer token only when auth is enabled and uses manual redirects', async () => {
    const fetchMock = vi.fn()
      .mockResolvedValueOnce(new Response('{"data":true}', { status: 200 }))
      .mockResolvedValueOnce(new Response('{"data":true}', { status: 200 }))
    vi.stubGlobal('fetch', fetchMock)

    await safeApiRequest('/api/example', { token: 'business-token' })
    await safeApiRequest('/api/example', { token: 'business-token', attachToken: false })

    expect(fetchMock.mock.calls[0][1].headers.get('Authorization')).toBe('Bearer business-token')
    expect(fetchMock.mock.calls[1][1].headers.has('Authorization')).toBe(false)
    expect(fetchMock.mock.calls[0][1].redirect).toBe('manual')
  })

  it('cancels a response that exceeds the configured byte limit', async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response('123456789', { status: 200 }))
    vi.stubGlobal('fetch', fetchMock)

    await expect(safeApiRequest('/api/example', { maxResponseBytes: 4 })).rejects.toThrow('响应超过')
  })

  it('aborts an in-flight request when the caller cancels it', async () => {
    const fetchMock = vi.fn((_url: string, init: RequestInit) => new Promise<Response>((_resolve, reject) => {
      init.signal?.addEventListener('abort', () => reject(init.signal?.reason), { once: true })
    }))
    vi.stubGlobal('fetch', fetchMock)
    const cancellation = new AbortController()
    const pending = safeApiRequest('/api/example', { signal: cancellation.signal })
    await Promise.resolve()
    cancellation.abort(new DOMException('cancelled', 'AbortError'))

    await expect(pending).rejects.toMatchObject({ name: 'AbortError' })
  })
})

describe('credential redaction', () => {
  it('redacts auth and secret-shaped fields in memory-only history records', () => {
    expect(redactHeaders({ Authorization: 'Bearer secret', Accept: 'application/json' })).toEqual({
      Authorization: '••••••',
      Accept: 'application/json',
    })
    expect(redactSecrets({ accessToken: 'secret', nested: { password: 'pw', count: 2 } })).toEqual({
      accessToken: '••••••', nested: { password: '••••••', count: 2 },
    })
  })
})
