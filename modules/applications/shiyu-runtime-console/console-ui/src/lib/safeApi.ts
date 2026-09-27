export const MAX_RESPONSE_BYTES = 1_048_576

export interface SafeResponse {
  status: number
  ok: boolean
  headers: Record<string, string>
  body: string
  elapsedMs: number
}

export interface SafeRequestOptions {
  method?: string
  headers?: Record<string, string>
  body?: string
  token?: string
  attachToken?: boolean
  timeoutMs?: number
  signal?: AbortSignal
  maxResponseBytes?: number
}

export async function safeApiRequest(path: string, options: SafeRequestOptions = {}): Promise<SafeResponse> {
  const target = new URL(path, window.location.origin)
  if (target.origin !== window.location.origin || !target.pathname.startsWith('/api/')) {
    throw new Error('接口调试仅允许访问当前实例的 /api/** 路径')
  }
  if (path.startsWith('//') || path.includes('\\')) {
    throw new Error('接口路径格式无效')
  }

  const method = (options.method ?? 'GET').toUpperCase()
  const headers = new Headers(options.headers)
  if (options.attachToken !== false && options.token) {
    headers.set('Authorization', `Bearer ${options.token}`)
  }

  const abort = new AbortController()
  const onAbort = () => abort.abort(options.signal?.reason)
  options.signal?.addEventListener('abort', onAbort, { once: true })
  const timeout = window.setTimeout(() => abort.abort(new DOMException('请求超时', 'TimeoutError')), options.timeoutMs ?? 20_000)
  const started = performance.now()
  try {
    const response = await fetch(target.pathname + target.search, {
      method,
      headers,
      body: method === 'GET' || method === 'HEAD' ? undefined : options.body,
      credentials: 'same-origin',
      redirect: 'manual',
      signal: abort.signal,
    })
    if (response.redirected || response.type === 'opaqueredirect' || response.url && new URL(response.url).origin !== window.location.origin) {
      throw new Error('接口调试禁止跨来源重定向')
    }
    const body = await readBoundedBody(response, options.maxResponseBytes ?? MAX_RESPONSE_BYTES)
    const responseHeaders: Record<string, string> = {}
    response.headers.forEach((value, key) => { responseHeaders[key] = value })
    return {
      status: response.status,
      ok: response.ok,
      headers: responseHeaders,
      body,
      elapsedMs: Math.round(performance.now() - started),
    }
  } finally {
    window.clearTimeout(timeout)
    options.signal?.removeEventListener('abort', onAbort)
  }
}

async function readBoundedBody(response: Response, maximum: number): Promise<string> {
  const contentLength = Number(response.headers.get('content-length'))
  if (Number.isFinite(contentLength) && contentLength > maximum) {
    await response.body?.cancel()
    throw new Error(`响应超过 ${Math.round(maximum / 1024)} KB 限制`)
  }
  if (!response.body) return ''
  const reader = response.body.getReader()
  const chunks: Uint8Array[] = []
  let total = 0
  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    total += value.byteLength
    if (total > maximum) {
      await reader.cancel()
      throw new Error(`响应超过 ${Math.round(maximum / 1024)} KB 限制`)
    }
    chunks.push(value)
  }
  const joined = new Uint8Array(total)
  let cursor = 0
  for (const chunk of chunks) {
    joined.set(chunk, cursor)
    cursor += chunk.byteLength
  }
  return new TextDecoder().decode(joined)
}

export function redactSecrets(value: unknown): unknown {
  if (Array.isArray(value)) return value.map(redactSecrets)
  if (value && typeof value === 'object') {
    return Object.fromEntries(Object.entries(value).map(([key, child]) => [
      key,
      /authorization|token|password|secret|api[-_]?key/i.test(key) ? '••••••' : redactSecrets(child),
    ]))
  }
  return value
}

export function redactHeaders(headers: Record<string, string>): Record<string, string> {
  return Object.fromEntries(Object.entries(headers).map(([key, value]) => [
    key,
    /authorization|token|password|secret|api[-_]?key/i.test(key) ? '••••••' : value,
  ]))
}
