import { safeApiRequest, type SafeRequestOptions, type SafeResponse } from './safeApi'

interface BusinessEnvelope<T> {
  success?: boolean
  code?: number | string
  message?: string
  data?: T
}

export class ProjectApiClient {
  private accessToken = ''
  private refreshInFlight: Promise<boolean> | null = null

  get authenticated(): boolean {
    return Boolean(this.accessToken)
  }

  async login(username: string, password: string): Promise<void> {
    const response = await safeApiRequest('/api/iam/auth/login', {
      method: 'POST',
      attachToken: false,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password }),
    })
    const envelope = parseEnvelope<{ accessToken?: string }>(response)
    const token = envelope.data?.accessToken
    if (!response.ok || envelope.success === false || !token) {
      throw new Error(envelope.message || '登录失败，请检查账号、密码和业务权限')
    }
    this.accessToken = token
  }

  clearToken(): void {
    this.accessToken = ''
  }

  async request(path: string, options: SafeRequestOptions = {}): Promise<SafeResponse> {
    const originalToken = this.accessToken
    const requestOptions = { ...options, token: originalToken, attachToken: options.attachToken !== false }
    const response = await safeApiRequest(path, requestOptions)
    const method = (options.method ?? 'GET').toUpperCase()
    if (response.status !== 401 || !originalToken) return response

    const refreshed = await this.refreshOnce(originalToken)
    if (!refreshed) return response
    if (method !== 'GET' && method !== 'HEAD') return response
    return safeApiRequest(path, { ...options, token: this.accessToken, attachToken: options.attachToken !== false })
  }

  private async refreshOnce(token: string): Promise<boolean> {
    if (this.accessToken !== token) return Boolean(this.accessToken)
    if (!this.refreshInFlight) {
      this.refreshInFlight = this.refreshToken(token)
    }
    try {
      return await this.refreshInFlight
    } finally {
      this.refreshInFlight = null
    }
  }

  private async refreshToken(oldToken: string): Promise<boolean> {
    try {
      const response = await safeApiRequest('/api/iam/auth/refresh', {
        method: 'POST',
        attachToken: false,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ accessToken: oldToken }),
      })
      const envelope = parseEnvelope<string>(response)
      if (!response.ok || envelope.success === false || typeof envelope.data !== 'string' || !envelope.data) {
        this.clearToken()
        return false
      }
      this.accessToken = envelope.data
      return true
    } catch {
      this.clearToken()
      return false
    }
  }
}

function parseEnvelope<T>(response: SafeResponse): BusinessEnvelope<T> {
  try {
    return JSON.parse(response.body) as BusinessEnvelope<T>
  } catch {
    return { success: false, message: `接口返回非 JSON 内容（HTTP ${response.status}）` }
  }
}
