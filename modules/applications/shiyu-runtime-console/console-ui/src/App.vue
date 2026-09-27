<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { ProjectApiClient } from './lib/projectApi'
import { findKnowledgeJobListOperation, isEmptyBusinessCollection, parseKnowledgeJobPage, type KnowledgeJobObservation } from './lib/businessObservations'

type Section = 'overview' | 'logs' | 'config' | 'api'
type RuntimeStatus = { version: string; mode: string; pid: number; port: number; uptimeMillis: number; appHome: string; health: string; healthDetail: string; managed: boolean }
type MetricSnapshot = { capturedAt: string; values: Record<string, number> }
type ConfigField = { key: string; label: string; type: string; applyMode: string; sensitive: boolean; source: string; editable: boolean; effectiveValue: string; savedValue: string; configured: boolean; supported: boolean; unsupportedReason: string }
type LogFile = { name: string; label: string; sizeBytes: number; modifiedAt: string }
type Operation = { key: string; path: string; method: string; tags: string[]; summary: string; operation: Record<string, any>; supported: boolean; unsupportedReason: string }

const api = new ProjectApiClient()
const navigation: Array<{ id: Section; label: string; detail: string; icon: string }> = [
  { id: 'overview', label: '运行概览', detail: 'Runtime', icon: '◉' },
  { id: 'logs', label: '日志查看', detail: 'Logs', icon: '≋' },
  { id: 'config', label: '配置管理', detail: 'Settings', icon: '⌘' },
  { id: 'api', label: '接口工作台', detail: 'API Lab', icon: '⇄' },
]
const section = ref<Section>('overview')
const authenticated = ref(false)
const csrf = ref('')
const authError = ref('')
const flash = ref('')
const busy = ref(false)
const runtime = ref<RuntimeStatus | null>(null)
const snapshots = ref<MetricSnapshot[]>([])
const configVersion = ref(0)
const configFields = ref<ConfigField[]>([])
const configDraft = ref<Record<string, string>>({})
const secretDraft = ref<Record<string, string>>({})
const secretAction = ref<Record<string, 'keep' | 'replace' | 'clear'>>({})
const logs = ref<LogFile[]>([])
const activeLog = ref('info.log')
const logLines = ref<string[]>([])
const logCursor = ref('')
const logFilter = ref('ALL')
const logQuery = ref('')
const logPaused = ref(false)
const logLoading = ref(false)
const logRotated = ref(false)
const openApiError = ref('')
const openApiDoc = ref<Record<string, any> | null>(null)
const apiSearch = ref('')
const apiTag = ref('全部接口')
const selectedKey = ref('')
const pathValues = ref<Record<string, string>>({})
const queryValues = ref<Record<string, string>>({})
const headerValues = ref<Record<string, string>>({})
const requestBody = ref('{}')
const attachBusinessToken = ref(true)
const requestTimeout = ref(20_000)
const response = ref<{ status: number; elapsedMs: number; headers: Record<string, string>; body: string } | null>(null)
const requestError = ref('')
const responseTab = ref<'body' | 'headers' | 'business'>('body')
const history = ref<Array<{ method: string; path: string; at: string; status: number | string; elapsedMs: number | null }>>([])
const loginName = ref('')
const loginPassword = ref('')
const businessLoginBusy = ref(false)
const businessLoginError = ref('')
const modelStatus = ref('尚未检查')
const knowledgeStatus = ref('尚未检查')
const knowledgeTasks = ref<KnowledgeJobObservation[]>([])
const knowledgeTaskTotal = ref(0)
const businessStatusBusy = ref(false)
const operations = computed<Operation[]>(() => {
  const paths = openApiDoc.value?.paths as Record<string, Record<string, any>> | undefined
  if (!paths) return []
  const methods = new Set(['get', 'post', 'put', 'patch', 'delete', 'options', 'head'])
  const list: Operation[] = []
  for (const [path, item] of Object.entries(paths)) {
    if (!path.startsWith('/api/')) continue
    for (const [method, operation] of Object.entries(item)) {
      if (!methods.has(method.toLowerCase())) continue
      const media = Object.keys(operation.requestBody?.content ?? {})
      const stream = path.toLowerCase().includes('/stream') || path.toLowerCase().includes('/ws')
        || JSON.stringify(operation.responses ?? {}).includes('text/event-stream')
      const multipart = media.some(type => type.toLowerCase().includes('multipart'))
      list.push({
        key: method.toUpperCase() + ' ' + path, path, method: method.toUpperCase(),
        tags: Array.isArray(operation.tags) ? operation.tags : ['未分类'],
        summary: operation.summary ?? operation.operationId ?? '接口操作',
        operation, supported: !stream && !multipart,
        unsupportedReason: stream ? '流式 / WebSocket 响应暂不支持' : multipart ? 'multipart 文件请求暂不支持' : '',
      })
    }
  }
  return list.sort((a, b) => a.path.localeCompare(b.path) || a.method.localeCompare(b.method))
})
const tags = computed(() => ['全部接口', ...new Set(operations.value.flatMap(item => item.tags))])
const filteredOperations = computed(() => operations.value.filter(item =>
  (apiTag.value === '全部接口' || item.tags.includes(apiTag.value))
  && (!apiSearch.value || (item.path + ' ' + item.summary + ' ' + item.tags.join(' ')).toLowerCase().includes(apiSearch.value.toLowerCase())),
))
const selectedOperation = computed(() => operations.value.find(item => item.key === selectedKey.value) ?? null)
const heapMetric = computed(() => snapshots.value.at(-1)?.values['jvm.heap.used'] ?? -1)
const cpuMetric = computed(() => snapshots.value.at(-1)?.values['process.cpu.percent'] ?? -1)
const requestMetric = computed(() => snapshots.value.at(-1)?.values['http.requests'] ?? 0)
const latencyMetric = computed(() => snapshots.value.at(-1)?.values['http.average-latency-ms'] ?? 0)
const heapPercent = computed(() => {
  const sample = snapshots.value.at(-1)?.values
  if (!sample || sample['jvm.heap.max'] <= 0) return 0
  return Math.min(100, Math.round(sample['jvm.heap.used'] / sample['jvm.heap.max'] * 100))
})
const uptimeLabel = computed(() => formatDuration(runtime.value?.uptimeMillis ?? 0))
const chartPoints = computed(() => {
  const values = snapshots.value.slice(-48)
  if (values.length < 2) return '0,64 100,64'
  const max = Math.max(1, ...values.map(item => item.values['jvm.heap.used'] ?? 0))
  return values.map((item, index) => (index / (values.length - 1) * 100) + ',' + (64 - (item.values['jvm.heap.used'] ?? 0) / max * 54)).join(' ')
})

async function consoleRequest<T>(path: string, method = 'GET', body?: unknown): Promise<T> {
  const headers = new Headers()
  if (body !== undefined) headers.set('Content-Type', 'application/json')
  if (method !== 'GET' && method !== 'HEAD') headers.set('X-ShiYu-Console-CSRF', csrf.value)
  const result = await fetch(path, { method, credentials: 'same-origin', headers, body: body === undefined ? undefined : JSON.stringify(body), redirect: 'manual' })
  const raw = await result.text()
  let value: any = raw
  try { value = raw ? JSON.parse(raw) : null } catch { /* readable non-JSON failure */ }
  if (result.status === 401) {
    authenticated.value = false
    throw new Error('本机控制会话已失效，请使用启动器重新打开浏览器授权链接；IDEA 模式请重新运行启动类。')
  }
  if (!result.ok) throw new Error(value?.message ?? value?.error ?? '请求失败（HTTP ' + result.status + '）')
  return value as T
}

async function authorize(): Promise<boolean> {
  const grant = new URLSearchParams(window.location.hash.slice(1)).get('grant')
  if (grant) {
    window.history.replaceState(null, document.title, window.location.pathname + window.location.search)
    try {
      const result = await fetch('/console/api/session/exchange', {
        method: 'POST', credentials: 'same-origin', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ grant }), redirect: 'manual',
      })
      const value = await result.json()
      if (!result.ok) throw new Error(value?.message ?? '授权链接已失效或已使用')
      csrf.value = value.csrfToken
      authenticated.value = true
      return true
    } catch (error) {
      authError.value = error instanceof Error ? error.message : '无法兑换本机授权链接'
      return false
    }
  }
  try {
    const session = await consoleRequest<{ authenticated: boolean; csrfToken: string }>('/console/api/session')
    csrf.value = session.csrfToken
    authenticated.value = true
    return true
  } catch { return false }
}

async function refreshRuntime(): Promise<void> {
  try {
    const [status, metrics] = await Promise.all([
      consoleRequest<RuntimeStatus>('/console/api/runtime'),
      consoleRequest<{ samples: MetricSnapshot[] }>('/console/api/metrics'),
    ])
    runtime.value = status
    snapshots.value = metrics.samples
  } catch (error) {
    if (authenticated.value) flashMessage(error instanceof Error ? error.message : '运行状态暂不可用')
  }
}

async function loadConfiguration(): Promise<void> {
  try {
    const config = await consoleRequest<{ version: number; fields: ConfigField[] }>('/console/api/config')
    configVersion.value = config.version
    configFields.value = config.fields
    const values: Record<string, string> = {}
    for (const field of config.fields) {
      if (!field.sensitive) values[field.key] = field.savedValue || field.effectiveValue || ''
      if (field.sensitive) secretAction.value[field.key] = 'keep'
    }
    configDraft.value = values
  } catch (error) { flashMessage(error instanceof Error ? error.message : '配置读取失败') }
}

async function saveConfiguration(andApply = false): Promise<void> {
  busy.value = true
  try {
    const secrets: Record<string, { action: string; value?: string }> = {}
    for (const field of configFields.value.filter(item => item.sensitive)) {
      const action = secretAction.value[field.key] ?? 'keep'
      secrets[field.key] = action === 'replace' ? { action, value: secretDraft.value[field.key] } : { action }
    }
    const result = await consoleRequest<{ message: string }>('/console/api/config', 'POST', {
      expectedVersion: configVersion.value, values: configDraft.value, secrets,
    })
    flashMessage(result.message)
    await loadConfiguration()
    if (andApply && runtime.value?.managed) {
      await consoleRequest('/console/api/lifecycle/restart', 'POST', {})
      flashMessage('已向 Windows 启动器发送受控重启请求。')
    }
  } catch (error) { flashMessage(error instanceof Error ? error.message : '配置保存失败') }
  finally { busy.value = false }
}

async function restoreLastApplied(): Promise<void> {
  try {
    const result = await consoleRequest<{ message: string }>('/console/api/config/restore-last-applied', 'POST', { expectedVersion: configVersion.value })
    flashMessage(result.message)
    await loadConfiguration()
  } catch (error) { flashMessage(error instanceof Error ? error.message : '恢复失败') }
}

async function loadLogFiles(): Promise<void> {
  try {
    const result = await consoleRequest<{ files: LogFile[] }>('/console/api/logs/files')
    logs.value = result.files
    if (!logs.value.some(file => file.name === activeLog.value) && logs.value.length) activeLog.value = logs.value[0].name
  } catch (error) { flashMessage(error instanceof Error ? error.message : '日志目录暂不可用') }
}

async function loadLog(reset = false): Promise<void> {
  if (logPaused.value || logLoading.value || !activeLog.value) return
  logLoading.value = true
  try {
    const params = new URLSearchParams({ file: activeLog.value, level: logFilter.value, query: logQuery.value, maxBytes: '65536' })
    if (!reset && logCursor.value) params.set('cursor', logCursor.value)
    const chunk = await consoleRequest<{ lines: string[]; nextCursor: string; rotated: boolean }>('/console/api/logs/tail?' + params)
    logRotated.value = chunk.rotated
    logCursor.value = chunk.nextCursor ?? ''
    logLines.value = reset || chunk.rotated ? chunk.lines : [...logLines.value, ...chunk.lines].slice(-4000)
  } catch (error) { flashMessage(error instanceof Error ? error.message : '日志读取失败') }
  finally { logLoading.value = false }
}

async function loadOpenApi(): Promise<void> {
  openApiError.value = ''
  try {
    const result = await fetch('/v3/api-docs', { credentials: 'same-origin', redirect: 'manual' })
    if (!result.ok) throw new Error('OpenAPI 文档不可用（HTTP ' + result.status + '）')
    openApiDoc.value = await result.json()
    if (!selectedKey.value && operations.value.length) selectOperation(operations.value[0])
  } catch (error) { openApiError.value = error instanceof Error ? error.message : 'OpenAPI 文档加载失败' }
}

function selectOperation(operation: Operation): void {
  selectedKey.value = operation.key
  pathValues.value = Object.fromEntries([...operation.path.matchAll(/\{([^}]+)\}/g)].map(match => [match[1], '']))
  queryValues.value = Object.fromEntries((operation.operation.parameters ?? []).filter((item: any) => item.in === 'query').map((item: any) => [item.name, '']))
  headerValues.value = Object.fromEntries((operation.operation.parameters ?? []).filter((item: any) => item.in === 'header').map((item: any) => [item.name, '']))
  response.value = null
  requestError.value = ''
  requestBody.value = '{}'
}

function parameters(location: string): Array<{ name: string; required: boolean; description?: string }> {
  return (selectedOperation.value?.operation.parameters ?? []).filter((item: any) => item.in === location && !item.$ref)
}

async function executeOperation(): Promise<void> {
  const operation = selectedOperation.value
  if (!operation?.supported) return
  requestError.value = ''
  response.value = null
  let path = operation.path.replace(/\{([^}]+)\}/g, (_match, name: string) => encodeURIComponent(pathValues.value[name] ?? ''))
  const query = new URLSearchParams()
  for (const [key, value] of Object.entries(queryValues.value)) if (value !== '') query.set(key, value)
  if (query.size) path += '?' + query.toString()
  const headers = Object.fromEntries(Object.entries(headerValues.value).filter(([, value]) => value !== ''))
  const hasBody = ['POST', 'PUT', 'PATCH', 'DELETE'].includes(operation.method) && Boolean(operation.operation.requestBody)
  if (hasBody) headers['Content-Type'] = 'application/json'
  const body = hasBody ? requestBody.value : undefined
  busy.value = true
  responseTab.value = 'body'
  const controller = new AbortController()
  activeRequestController = controller
  try {
    const result = await api.request(path, { method: operation.method, headers, body, attachToken: attachBusinessToken.value, timeoutMs: requestTimeout.value, signal: controller.signal })
    response.value = result
    history.value.unshift({ method: operation.method, path: operation.path, at: new Date().toLocaleTimeString(), status: result.status, elapsedMs: result.elapsedMs })
    history.value = history.value.slice(0, 30)
    if (result.status === 401 && api.authenticated) requestError.value = '原业务 Token 已失效。安全 GET 已重试；写请求未自动重放，请确认后手动重试。'
  } catch (error) {
    requestError.value = error instanceof DOMException && error.name === 'AbortError'
      ? '请求已取消。'
      : error instanceof Error ? error.message : '接口调用失败'
    history.value.unshift({ method: operation.method, path: operation.path, at: new Date().toLocaleTimeString(), status: 'ERR', elapsedMs: null })
    history.value = history.value.slice(0, 30)
  } finally { busy.value = false; activeRequestController = null }
}

function cancelRequest(): void {
  activeRequestController?.abort(new DOMException('用户取消请求', 'AbortError'))
}

function businessErrorSummary(): string {
  if (!response.value) return '尚无响应。'
  try {
    const value = JSON.parse(response.value.body)
    const details: Record<string, unknown> = { httpStatus: response.value.status }
    if (value && typeof value === 'object') {
      for (const key of ['code', 'success', 'message', 'error']) {
        if (value[key] !== undefined) details[key] = value[key]
      }
    }
    return JSON.stringify(details, null, 2)
  } catch {
    return JSON.stringify({ httpStatus: response.value.status, message: '响应正文不是 JSON。' }, null, 2)
  }
}

async function requestLifecycle(action: 'stop' | 'restart'): Promise<void> {
  const label = action === 'restart' ? '重启' : '停止'
  if (!window.confirm(`确认${label}由 Windows 启动器托管的 ShiYu 后端吗？`)) return
  try {
    const result = await consoleRequest<{ message: string }>(`/console/api/lifecycle/${action}`, 'POST', {})
    flashMessage(result.message)
  } catch (error) { flashMessage(error instanceof Error ? error.message : `${label}请求失败`) }
}

async function loginBusiness(): Promise<void> {
  businessLoginBusy.value = true
  businessLoginError.value = ''
  try {
    await api.login(loginName.value, loginPassword.value)
    loginPassword.value = ''
    await refreshBusinessStatus()
  } catch (error) { businessLoginError.value = error instanceof Error ? error.message : '业务登录失败' }
  finally { businessLoginBusy.value = false }
}

function logoutBusiness(): void {
  api.clearToken()
  loginPassword.value = ''
  modelStatus.value = '需要业务登录'
  knowledgeStatus.value = '需要业务登录'
  knowledgeTasks.value = []
  knowledgeTaskTotal.value = 0
}

async function refreshBusinessStatus(): Promise<void> {
  if (!api.authenticated) {
    modelStatus.value = '需要业务登录'
    knowledgeStatus.value = '需要业务登录'
    knowledgeTasks.value = []
    knowledgeTaskTotal.value = 0
    return
  }
  businessStatusBusy.value = true
  const model = operations.value.find(item => item.method === 'GET'
    && /model/i.test(item.path + ' ' + item.tags.join(' '))
    && /models|platforms|providers|config|catalog/i.test(item.path)
    && !/health|probe|test|chat|completion|embedding|invoke|generate/i.test(item.path)
    && !/[{}]/.test(item.path))
  const knowledge = findKnowledgeJobListOperation(operations.value)
  const check = async (item: Operation | undefined): Promise<string> => {
    if (!item) return 'OpenAPI 未提供可直接读取的状态接口'
    const result = await api.request(item.path, { method: 'GET', timeoutMs: 8000 })
    if (result.status === 401) return '登录态已失效，请重新登录'
    if (result.status === 403) return '当前业务账号无权查看'
    if (!result.ok) return '接口不可用（HTTP ' + result.status + '）'
    try {
      JSON.parse(result.body)
      return isEmptyBusinessCollection(result.body) ? '接口正常，尚无配置/记录' : '已读取现有业务接口结果'
    } catch { return '接口正常，返回非 JSON 状态' }
  }
  const readKnowledgeJobs = async (): Promise<string> => {
    knowledgeTasks.value = []
    knowledgeTaskTotal.value = 0
    if (!knowledge) return 'OpenAPI 未提供知识任务列表接口'
    const params = new URLSearchParams({ pageNum: '1', pageSize: '10' })
    const result = await api.request(knowledge.path + '?' + params, { method: 'GET', timeoutMs: 8000 })
    if (result.status === 401) return '登录态已失效，请重新登录'
    if (result.status === 403) return '当前业务账号无权查看'
    if (!result.ok) return '接口不可用（HTTP ' + result.status + '）'
    const page = parseKnowledgeJobPage(result.body)
    if (!page) return '知识任务接口返回格式不可识别'
    knowledgeTasks.value = page.items
    knowledgeTaskTotal.value = page.total
    return page.total === 0 ? '接口正常，暂无知识任务' : `已读取 ${page.items.length} 条 / 共 ${page.total} 条`
  }
  try {
    const [modelResult, knowledgeResult] = await Promise.allSettled([check(model), readKnowledgeJobs()])
    modelStatus.value = modelResult.status === 'fulfilled' ? modelResult.value : '模型状态不可用'
    knowledgeStatus.value = knowledgeResult.status === 'fulfilled' ? knowledgeResult.value : '知识任务状态不可用'
  } finally { businessStatusBusy.value = false }
}

async function logoutConsole(): Promise<void> {
  try { await consoleRequest('/console/api/session/logout', 'POST', {}) } catch { /* clear local state regardless */ }
  authenticated.value = false
  csrf.value = ''
}

async function copyText(text: string): Promise<void> {
  await navigator.clipboard.writeText(text)
  flashMessage('已复制到剪贴板')
}

function downloadLog(): void {
  const url = URL.createObjectURL(new Blob([logLines.value.join('\n')], { type: 'text/plain;charset=utf-8' }))
  const anchor = document.createElement('a')
  anchor.href = url
  anchor.download = 'shiyu-' + activeLog.value + '-' + new Date().toISOString().replaceAll(':', '-') + '.txt'
  anchor.click()
  URL.revokeObjectURL(url)
}

function prettyBody(body: string): string {
  try { return JSON.stringify(JSON.parse(body), null, 2) } catch { return body }
}

function formatBytes(value: number): string {
  if (!Number.isFinite(value) || value < 0) return '—'
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  let size = value
  let unit = 0
  while (size >= 1024 && unit < units.length - 1) { size /= 1024; unit++ }
  return size.toFixed(unit > 1 ? 1 : 0) + ' ' + units[unit]
}

function formatDuration(value: number): string {
  const seconds = Math.max(0, Math.floor(value / 1000))
  const days = Math.floor(seconds / 86400)
  const hours = Math.floor((seconds % 86400) / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  return days ? days + '天 ' + hours + '小时' : hours + '小时 ' + minutes + '分'
}

function methodClass(method: string): string { return 'method-' + method.toLowerCase() }
function flashMessage(message: string): void {
  flash.value = message
  window.setTimeout(() => { if (flash.value === message) flash.value = '' }, 5000)
}

let runtimeTimer = 0
let logTimer = 0
let activeRequestController: AbortController | null = null
watch(section, async value => {
  if (value === 'logs') { await loadLogFiles(); await loadLog(true) }
  if (value === 'config') await loadConfiguration()
  if (value === 'api' && !openApiDoc.value) await loadOpenApi()
})
watch([activeLog, logFilter, logQuery], () => {
  logCursor.value = ''
  logLines.value = []
  if (section.value === 'logs') void loadLog(true)
})
onMounted(async () => {
  const requestedTab = new URLSearchParams(window.location.search).get('tab')
  if (requestedTab === 'config' || requestedTab === 'logs' || requestedTab === 'api') section.value = requestedTab
  if (!await authorize()) return
  await Promise.all([refreshRuntime(), loadConfiguration(), loadLogFiles(), loadOpenApi()])
  runtimeTimer = window.setInterval(() => { void refreshRuntime() }, 5000)
  logTimer = window.setInterval(() => { if (section.value === 'logs') void loadLog() }, 2500)
})
onUnmounted(() => {
  window.clearInterval(runtimeTimer)
  window.clearInterval(logTimer)
  activeRequestController?.abort(new DOMException('页面已关闭', 'AbortError'))
})
</script>

<template>
  <div v-if="!authenticated" class="gate-screen">
    <div class="gate-card">
      <div class="brand-mark large"><span>詩</span><i></i></div>
      <div class="eyebrow">LOCAL RUNTIME · SHIYU</div>
      <h1>本机运行控制台</h1>
      <p>管理凭证仅限本机一次性授权；它与业务登录 Token 完全隔离。</p>
      <div v-if="authError" class="alert danger">{{ authError }}</div>
      <div v-else class="gate-wait"><span class="pulse-dot"></span>等待有效的一次性授权链接</div>
      <div class="gate-foot"><span class="lock-glyph">⌑</span> LOOPBACK ONLY <span>·</span> 会话只保存在当前进程内存</div>
    </div>
  </div>
  <div v-else class="shell">
    <aside class="sidebar">
      <div class="brand-lockup"><div class="brand-mark"><span>詩</span><i></i></div><div><strong>SHIYU</strong><small>RUNTIME CONSOLE</small></div></div>
      <div class="side-label">工作区</div>
      <nav>
        <button v-for="item in navigation" :key="item.id" class="nav-item" :class="{ active: section === item.id }" @click="section = item.id">
          <span class="nav-icon">{{ item.icon }}</span><span class="nav-copy"><b>{{ item.label }}</b><small>{{ item.detail }}</small></span><span v-if="section === item.id" class="nav-caret">›</span>
        </button>
      </nav>
      <div class="sidebar-spacer"></div>
      <div class="local-card"><div class="local-card-top"><span class="pulse-dot"></span><span>LOCAL SESSION</span></div><div class="local-host">127.0.0.1:{{ runtime?.port ?? '—' }}</div><div class="local-caption">管理身份 · 非业务身份</div></div>
      <button class="logout-button" @click="logoutConsole"><span>↗</span> 退出控制台</button>
      <div class="sidebar-version">SHIYU / {{ runtime?.version ?? 'runtime' }} <span>◆</span></div>
    </aside>
    <main class="main-area">
      <header class="topbar">
        <div class="breadcrumb"><span>ShiYu</span><b>/</b><strong>{{ navigation.find(item => item.id === section)?.label }}</strong></div>
        <div class="topbar-right"><span class="system-chip"><i></i> 本机管理会话</span><span class="topbar-divider"></span><button class="icon-button" title="刷新运行状态" @click="refreshRuntime">↻</button></div>
      </header>
      <div v-if="flash" class="flash-message" role="status">{{ flash }}<button @click="flash = ''">×</button></div>

      <section v-if="section === 'overview'" class="page-section overview-page">
        <div class="page-heading"><div><div class="eyebrow">RUNTIME / OVERVIEW</div><h1>运行概览<span class="title-period">.</span></h1><p>当前进程健康状况与近 30 分钟资源观测。</p></div><div class="heading-actions"><span class="poll-indicator"><i></i> 每 5 秒采样</span><button v-if="runtime?.managed" class="button ghost" :disabled="busy" @click="requestLifecycle('restart')">受控重启 ↻</button><button v-if="runtime?.managed" class="button ghost" :disabled="busy" @click="requestLifecycle('stop')">停止</button><button class="button ghost" @click="refreshRuntime">立即刷新 <span>↻</span></button></div></div>
        <div class="runtime-banner"><div class="health-emblem" :class="runtime?.health === 'UP' ? 'healthy' : 'degraded'"><span>{{ runtime?.health === 'UP' ? '✓' : '!' }}</span></div><div class="runtime-title"><div class="eyebrow">APPLICATION HEALTH</div><strong>{{ runtime?.health ?? '检查中' }}</strong><small>{{ runtime?.healthDetail ?? '正在读取运行信息…' }}</small></div><div class="banner-divider"></div><div class="banner-field"><small>启动方式</small><strong>{{ runtime?.mode ?? '—' }}</strong></div><div class="banner-field"><small>进程 PID</small><strong class="mono">{{ runtime?.pid ?? '—' }}</strong></div><div class="banner-field"><small>HTTP 端口</small><strong class="mono">{{ runtime?.port ?? '—' }}</strong></div><div class="banner-time"><small>已运行</small><strong>{{ uptimeLabel }}</strong></div></div>
        <div class="metric-grid">
          <article class="metric-card"><div class="metric-head"><span>堆内存使用</span><span class="metric-symbol mint">◒</span></div><div class="metric-value">{{ formatBytes(heapMetric) }}<small> / {{ formatBytes(snapshots.at(-1)?.values['jvm.heap.max'] ?? -1) }}</small></div><div class="meter-track"><i :style="{ width: heapPercent + '%' }"></i></div><div class="metric-foot"><span>当前 JVM Heap</span><b>{{ heapPercent }}%</b></div></article>
          <article class="metric-card"><div class="metric-head"><span>进程 CPU</span><span class="metric-symbol coral">⌁</span></div><div class="metric-value">{{ cpuMetric >= 0 ? cpuMetric.toFixed(1) + '%' : '—' }}</div><div class="metric-foot spaced"><span>进程使用率</span><span>动态观测</span></div><div class="mini-bars"><i v-for="(sample, index) in snapshots.slice(-24)" :key="index" :style="{ height: Math.max(8, (sample.values['process.cpu.percent'] ?? 0) * 1.2) + '%' }"></i></div></article>
          <article class="metric-card"><div class="metric-head"><span>HTTP 请求量</span><span class="metric-symbol blue">↗</span></div><div class="metric-value">{{ requestMetric }}<small> / 最近窗口</small></div><div class="metric-foot spaced"><span>平均延迟</span><b>{{ latencyMetric.toFixed(1) }} ms</b></div><div class="metric-caption">已排除控制台轮询流量</div></article>
          <article class="metric-card"><div class="metric-head"><span>线程 / GC</span><span class="metric-symbol amber">⌘</span></div><div class="metric-value">{{ snapshots.at(-1)?.values['jvm.threads.live'] ?? '—' }}<small> 活跃线程</small></div><div class="metric-foot spaced"><span>GC 次数</span><b>{{ snapshots.at(-1)?.values['jvm.gc.count'] ?? '—' }}</b></div><div class="metric-caption">累计回收 {{ (snapshots.at(-1)?.values['jvm.gc.time-ms'] ?? 0).toFixed(0) }} ms</div></article>
        </div>
        <div class="content-grid overview-lower">
          <article class="panel chart-panel"><div class="panel-heading"><div><div class="eyebrow">MEMORY TREND</div><h2>堆内存轨迹</h2></div><span class="range-chip">采样点 {{ Math.min(snapshots.length, 360) }}</span></div><div class="chart-wrap"><div class="chart-y-labels"><span>峰值</span><span>50%</span><span>0</span></div><svg class="memory-chart" viewBox="0 0 100 70" preserveAspectRatio="none"><defs><linearGradient id="areaFill" x1="0" x2="0" y1="0" y2="1"><stop offset="0%" stop-color="#67dec1" stop-opacity=".23"/><stop offset="100%" stop-color="#67dec1" stop-opacity="0"/></linearGradient></defs><path :d="'M ' + chartPoints.replaceAll(' ', ' L ') + ' L 100,70 L 0,70 Z'" fill="url(#areaFill)"/><polyline :points="chartPoints" fill="none" stroke="#67dec1" stroke-width=".85" vector-effect="non-scaling-stroke"/></svg><div class="chart-grid"><i></i><i></i><i></i></div></div><div class="chart-caption"><span>30 分钟滚动窗口</span><span><i class="chart-legend"></i> JVM Heap Used</span></div></article>
          <article class="panel detail-panel"><div class="panel-heading"><div><div class="eyebrow">PROCESS DETAILS</div><h2>运行环境</h2></div><span class="detail-icon">⌘</span></div><dl class="detail-list"><div><dt>实际 APP_HOME</dt><dd class="mono path-value">{{ runtime?.appHome ?? '—' }}</dd></div><div><dt>系统内存使用</dt><dd>{{ formatBytes(snapshots.at(-1)?.values['system.memory.used'] ?? -1) }}</dd></div><div><dt>数据盘可用</dt><dd>{{ formatBytes(snapshots.at(-1)?.values['disk.free'] ?? -1) }}</dd></div><div><dt>数据库连接池</dt><dd>{{ snapshots.at(-1)?.values['db.active'] ?? '—' }} <small>活跃</small><span class="subtle-sep">/</span>{{ snapshots.at(-1)?.values['db.idle'] ?? '—' }} <small>空闲</small></dd></div></dl></article>
        </div>
        <div class="content-grid status-grid">
          <article class="panel business-panel">
            <div class="panel-heading"><div><div class="eyebrow">BUSINESS OBSERVATIONS</div><h2>业务侧状态</h2></div><button v-if="api.authenticated" class="text-button" :disabled="businessStatusBusy" @click="refreshBusinessStatus">{{ businessStatusBusy ? '读取中…' : '从现有接口读取 ↻' }}</button></div>
            <div class="business-states">
              <div><span class="state-dot neutral"></span><label>模型目录 / 已有观测</label><strong>{{ api.authenticated ? modelStatus : '需要业务登录' }}</strong></div>
              <div><span class="state-dot neutral"></span><label>知识任务 / 已有记录</label><strong>{{ api.authenticated ? knowledgeStatus : '需要业务登录' }}</strong></div>
            </div>
            <div v-if="api.authenticated && knowledgeTasks.length" class="knowledge-job-list">
              <div class="knowledge-job-list-heading"><span>最近任务</span><small>{{ knowledgeTaskTotal }} 条记录</small></div>
              <div v-for="task in knowledgeTasks" :key="task.id" class="knowledge-job-row">
                <code>{{ task.jobKey || task.id }}</code><span class="job-status">{{ task.status }}</span><small>{{ task.stage || '—' }} · {{ task.progress === null ? '—' : task.progress + '%' }}</small>
              </div>
            </div>
            <p class="quiet-note">不会自动调用模型或触发计费请求。业务状态使用当前登录 Token 与原接口权限。</p>
          </article>
          <article class="panel health-panel"><div class="panel-heading"><div><div class="eyebrow">SYSTEM HEALTH</div><h2>资源与连接</h2></div></div><div class="health-row"><span>数据盘</span><b><i class="health-dot" :class="(snapshots.at(-1)?.values['disk.free'] ?? -1) >= 0 ? 'green' : 'gray'"></i>{{ (snapshots.at(-1)?.values['disk.free'] ?? -1) >= 0 ? '可观测' : '指标缺失' }}</b></div><div class="health-row"><span>数据库连接池</span><b><i class="health-dot" :class="(snapshots.at(-1)?.values['db.max'] ?? -1) >= 0 ? 'green' : 'gray'"></i>{{ (snapshots.at(-1)?.values['db.max'] ?? -1) >= 0 ? '池已初始化' : '未暴露池指标' }}</b></div><div class="health-row"><span>模型健康探测</span><b><i class="health-dot gray"></i>仅展示已有结果</b></div></article>
        </div>
      </section>

      <section v-else-if="section === 'logs'" class="page-section logs-page">
        <div class="page-heading"><div><div class="eyebrow">RUNTIME / LOG STREAM</div><h1>日志查看<span class="title-period">.</span></h1><p>仅读取当前 APP_HOME 下的预定义日志；支持增量尾读与文件轮转检测。</p></div><div class="heading-actions"><button class="button ghost" @click="copyText(logLines.join('\n'))">复制当前内容</button><button class="button primary" @click="downloadLog">导出 .txt <span>↧</span></button></div></div>
        <div class="log-toolbar panel"><div class="select-wrap"><label>日志文件</label><select v-model="activeLog"><option v-for="file in logs" :key="file.name" :value="file.name">{{ file.label }} · {{ file.name }}</option></select></div><div class="select-wrap short"><label>最低级别</label><select v-model="logFilter"><option>ALL</option><option>DEBUG</option><option>INFO</option><option>WARN</option><option>ERROR</option></select></div><div class="select-wrap search-wrap"><label>关键词</label><input v-model="logQuery" placeholder="搜索日志内容…" @keyup.enter="loadLog(true)"/></div><div class="log-actions"><button class="button small ghost" @click="logPaused = !logPaused">{{ logPaused ? '继续' : '暂停' }}</button><button class="button small ghost" @click="loadLog(true)">回到最新 ↻</button></div></div>
        <div class="log-meta"><span><i class="pulse-dot" :class="{ paused: logPaused }"></i>{{ logPaused ? '已暂停' : '实时尾读' }}</span><span>{{ logs.find(item => item.name === activeLog)?.sizeBytes ? formatBytes(logs.find(item => item.name === activeLog)!.sizeBytes) : '等待日志文件' }}</span><span v-if="logRotated" class="rotated-hint">检测到轮转，已从新文件重新读取</span><span class="log-count">{{ logLines.length }} 行已载入</span></div>
        <div class="terminal-panel"><div class="terminal-title"><div class="terminal-lights"><i></i><i></i><i></i></div><span>{{ activeLog }} <em>· APP_HOME/data/log</em></span><span class="terminal-live">{{ logPaused ? 'PAUSED' : 'LIVE' }}</span></div><pre class="log-content"><code><span v-for="(line, index) in logLines" :key="index" class="log-line" :class="line.includes(' ERROR ') ? 'error' : line.includes(' WARN ') ? 'warn' : line.includes(' DEBUG ') ? 'debug' : ''">{{ line }}\n</span><span v-if="!logLines.length" class="empty-terminal">{{ logLoading ? '正在读取…' : '当前筛选条件下没有日志行' }}</span></code></pre></div>
      </section>

      <section v-else-if="section === 'config'" class="page-section config-page">
        <div class="page-heading"><div><div class="eyebrow">RUNTIME / CONFIGURATION</div><h1>配置管理<span class="title-period">.</span></h1><p>白名单字段、来源透明；敏感值仅可替换或清除，保存采用版本校验。</p></div><div class="heading-actions"><span class="version-chip">配置版本 <b>v{{ configVersion }}</b></span><button class="button ghost" @click="restoreLastApplied">恢复上次成功版本</button><button class="button primary" :disabled="busy" @click="saveConfiguration(false)">{{ busy ? '保存中…' : '保存变更' }} <span>↧</span></button></div></div>
        <div class="config-notice"><span class="notice-icon">i</span><div><strong>生效规则</strong><p>“即时生效”由当前进程应用；“重启生效”会显示待重启，不会伪装成已生效。命令行 / 系统属性 / 环境变量优先级高于控制台配置。</p></div></div>
        <div class="config-section"><div class="config-section-heading"><div><div class="eyebrow">LIVE CONTROLS</div><h2>运行时即时项</h2></div><span class="apply-badge immediate">即时生效</span></div><div class="config-table"><div class="config-row header"><span>字段</span><span>当前生效值</span><span>配置来源</span><span>待应用值</span></div><div v-for="field in configFields.filter(item => item.applyMode === 'IMMEDIATE')" :key="field.key" class="config-row"><div class="field-label"><b>{{ field.label }}</b><small class="mono">{{ field.key }}</small></div><code>{{ field.effectiveValue || '—' }}</code><span class="source-pill">{{ field.source }}</span><div class="config-input-cell"><select v-if="field.type === 'level'" v-model="configDraft[field.key]"><option v-for="level in ['TRACE','DEBUG','INFO','WARN','ERROR']" :key="level">{{ level }}</option></select><input v-else v-model="configDraft[field.key]" :type="field.type === 'number' ? 'number' : 'text'" :disabled="!field.editable"/></div></div></div></div>
        <div class="config-section"><div class="config-section-heading"><div><div class="eyebrow">RESTART REQUIRED</div><h2>启动与基础设施</h2></div><span class="apply-badge restart">重启生效</span></div><div class="config-table"><div class="config-row header"><span>字段</span><span>当前生效值</span><span>配置来源</span><span>待应用值</span></div><div v-for="field in configFields.filter(item => item.applyMode === 'RESTART')" :key="field.key" class="config-row" :class="{ unsupported: !field.supported }"><div class="field-label"><b>{{ field.label }}</b><small class="mono">{{ field.key }}</small><small v-if="!field.supported" class="unsupported-reason">{{ field.unsupportedReason }}</small></div><code>{{ field.sensitive ? (field.configured ? '••••••（已设置）' : '未设置') : field.effectiveValue || '—' }}</code><span class="source-pill">{{ field.source }}</span><div v-if="field.sensitive" class="secret-editor"><input v-model="secretDraft[field.key]" type="password" :placeholder="field.configured ? '留空保留已保存值' : '输入新密钥'" :disabled="!field.editable || !field.supported" @input="secretAction[field.key] = secretDraft[field.key] ? 'replace' : 'keep'"/><button class="text-button danger-text" :disabled="!field.configured || !field.editable" @click="secretAction[field.key] = secretAction[field.key] === 'clear' ? 'keep' : 'clear'">{{ secretAction[field.key] === 'clear' ? '撤销清除' : '清除' }}</button></div><div v-else class="config-input-cell"><select v-if="field.type === 'select'" v-model="configDraft[field.key]" :disabled="!field.editable"><option v-for="option in (field.key.includes('database') ? ['h2','mysql','postgresql'] : field.key.includes('vector') ? ['inmemory','jvector','pgvector'] : field.key.includes('event') ? ['in-process','postgres-outbox','kafka'] : field.key.includes('file') ? ['local','s3','minio','aliyun-oss','tencent-cos'] : ['disabled','redis'])" :key="option">{{ option }}</option></select><label v-else-if="field.type === 'boolean'" class="toggle"><input v-model="configDraft[field.key]" type="checkbox" true-value="true" false-value="false" :disabled="!field.editable || !field.supported"/><i></i></label><input v-else v-model="configDraft[field.key]" :type="field.type === 'number' ? 'number' : 'text'" :disabled="!field.editable || !field.supported"/></div></div></div></div>
        <div class="config-footer"><span><i class="health-dot" :class="runtime?.managed ? 'green' : 'amber-dot'"></i>{{ runtime?.managed ? 'Windows 启动器托管，可受控重启应用' : 'IDEA / 直接运行：保存后请手动重启' }}</span><button class="button primary" :disabled="busy || !runtime?.managed" @click="saveConfiguration(true)">保存并应用 <span>↻</span></button></div>
      </section>

      <section v-else class="page-section api-page">
        <div class="page-heading compact-heading"><div><div class="eyebrow">DEVELOPER TOOLS / API WORKBENCH</div><h1>接口工作台<span class="title-period">.</span></h1><p>从当前实例 OpenAPI 加载；请求仅限本实例 /api/**，认证信息不进入历史记录。</p></div><div class="heading-actions"><span class="workbench-limit">JSON · 1 MB · 可取消 · 20s 默认超时</span><button class="button ghost" @click="loadOpenApi">重载 OpenAPI ↻</button></div></div>
        <div v-if="openApiError" class="alert danger">{{ openApiError }}</div>
        <div class="workbench-grid">
          <aside class="api-sidebar panel"><div class="api-sidebar-head"><div class="eyebrow">ENDPOINT CATALOG</div><strong>{{ filteredOperations.length }} <small>个接口</small></strong></div><input v-model="apiSearch" class="catalog-search" placeholder="检索路径 / 描述…"/><select v-model="apiTag" class="tag-select"><option v-for="tag in tags" :key="tag">{{ tag }}</option></select><div class="endpoint-list"><button v-for="item in filteredOperations" :key="item.key" class="endpoint-row" :class="{ selected: selectedKey === item.key, disabled: !item.supported }" @click="selectOperation(item)"><span class="method-pill" :class="methodClass(item.method)">{{ item.method }}</span><span class="endpoint-copy"><b>{{ item.path }}</b><small>{{ item.summary }}</small><em v-if="!item.supported">{{ item.unsupportedReason }}</em></span></button><div v-if="!filteredOperations.length" class="empty-list">没有匹配的接口</div></div></aside>
          <div class="workbench-main">
            <article class="panel auth-panel"><div class="auth-panel-head"><div><div class="eyebrow">BUSINESS IDENTITY</div><strong>{{ api.authenticated ? '业务 Token 已就绪' : '使用项目账号登录' }}</strong><small>本机管理会话不能代替业务 API 权限</small></div><span class="auth-status" :class="{ good: api.authenticated }"><i></i>{{ api.authenticated ? 'BUSINESS AUTH' : 'ANONYMOUS' }}</span></div><div v-if="!api.authenticated" class="login-form"><input v-model="loginName" autocomplete="username" placeholder="用户名"/><input v-model="loginPassword" autocomplete="current-password" type="password" placeholder="密码" @keyup.enter="loginBusiness"/><button class="button primary" :disabled="businessLoginBusy" @click="loginBusiness">{{ businessLoginBusy ? '登录中…' : '登录并提取 Token' }}</button></div><div v-else class="logged-in-row"><span>已登录 · Token 仅存当前页面内存</span><button class="text-button danger-text" @click="logoutBusiness">清除 Token</button><button class="text-button" @click="refreshBusinessStatus">读取业务状态 ↻</button></div><p v-if="businessLoginError" class="inline-error">{{ businessLoginError }}</p></article>
            <article class="panel request-panel"><div v-if="selectedOperation" class="request-heading"><span class="method-pill large-method" :class="methodClass(selectedOperation.method)">{{ selectedOperation.method }}</span><div class="request-title"><h2>{{ selectedOperation.path }}</h2><p>{{ selectedOperation.summary }}</p></div><label class="auth-toggle"><input v-model="attachBusinessToken" type="checkbox"/><span>携带 Bearer</span></label></div><div v-else class="empty-request">选择左侧接口开始调试</div><div v-if="selectedOperation && !selectedOperation.supported" class="alert warning">{{ selectedOperation.unsupportedReason }}，该操作已禁用。</div><template v-if="selectedOperation?.supported"><div v-if="parameters('path').length" class="request-fields"><h3>路径参数</h3><label v-for="param in parameters('path')" :key="param.name"><span>{{ param.name }}<i v-if="param.required">*</i></span><input v-model="pathValues[param.name]" :placeholder="param.description ?? '输入路径参数'"/></label></div><div v-if="parameters('query').length" class="request-fields"><h3>查询参数</h3><label v-for="param in parameters('query')" :key="param.name"><span>{{ param.name }}<i v-if="param.required">*</i></span><input v-model="queryValues[param.name]" :placeholder="param.description ?? '可选'"/></label></div><div v-if="parameters('header').length" class="request-fields"><h3>请求头</h3><label v-for="param in parameters('header')" :key="param.name"><span>{{ param.name }}<i v-if="param.required">*</i></span><input v-model="headerValues[param.name]" :placeholder="param.description ?? '可选'"/></label></div><div v-if="selectedOperation.operation.requestBody" class="body-editor"><div class="editor-label"><h3>JSON 请求体</h3><span>Content-Type: application/json</span></div><textarea v-model="requestBody" spellcheck="false"></textarea></div><div class="request-footer"><label>超时 <input v-model.number="requestTimeout" type="number" min="1000" max="120000" step="1000"/> ms</label><span class="anon-toggle"><input v-model="attachBusinessToken" type="checkbox"/> Bearer {{ api.authenticated ? '自动注入' : '未登录' }}</span><button v-if="busy" class="button ghost execute-button" @click="cancelRequest">取消请求</button><button v-else class="button primary execute-button" @click="executeOperation">发送请求 <span>→</span></button></div></template></article>
            <article v-if="requestError" class="alert warning">{{ requestError }}</article>
            <article v-if="response" class="panel response-panel"><div class="response-heading"><div><div class="eyebrow">RESPONSE</div><strong :class="response.status < 400 ? 'response-good' : 'response-bad'">HTTP {{ response.status }}</strong><span class="response-time">{{ response.elapsedMs }} ms</span></div><button class="text-button" @click="copyText(responseTab === 'body' ? response.body : responseTab === 'headers' ? JSON.stringify(response.headers, null, 2) : businessErrorSummary())">复制当前内容</button></div><div class="response-tabs"><button :class="{ active: responseTab === 'body' }" @click="responseTab = 'body'">正文</button><button :class="{ active: responseTab === 'headers' }" @click="responseTab = 'headers'">响应头</button><button :class="{ active: responseTab === 'business' }" @click="responseTab = 'business'">业务错误码</button></div><pre v-if="responseTab === 'body'"><code>{{ prettyBody(response.body) }}</code></pre><pre v-else-if="responseTab === 'headers'"><code>{{ JSON.stringify(response.headers, null, 2) }}</code></pre><pre v-else><code>{{ businessErrorSummary() }}</code></pre></article>
            <article class="panel history-panel"><div class="history-title"><div><div class="eyebrow">IN-MEMORY HISTORY</div><strong>最近请求</strong></div><button class="text-button" @click="history = []">清除</button></div><div v-if="!history.length" class="history-empty">请求历史只保留在页面内存，密码与 Token 不会记录。</div><div v-for="(item, index) in history" :key="index" class="history-row"><span class="method-pill" :class="methodClass(item.method)">{{ item.method }}</span><code>{{ item.path }}</code><span class="history-code">{{ item.status }}</span><small>{{ item.elapsedMs ?? '—' }} ms · {{ item.at }}</small></div></article>
          </div>
        </div>
      </section>
      <footer class="page-footer"><span>SHIYU RUNTIME CONSOLE</span><span>仅绑定本机访问 · 本机身份不授予业务权限</span></footer>
    </main>
  </div>
</template>
