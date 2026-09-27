export interface OpenApiOperationReference {
  method: string
  path: string
  tags?: string[]
}

export interface KnowledgeJobObservation {
  id: string
  jobKey: string
  status: string
  stage: string
  progress: number | null
}

export interface KnowledgeJobPage {
  total: number
  items: KnowledgeJobObservation[]
}

export function findKnowledgeJobListOperation<T extends OpenApiOperationReference>(
  operations: T[],
): T | undefined {
  return operations.find(operation =>
    operation.method.toUpperCase() === 'GET'
    && /knowledge/i.test(operation.path + ' ' + (operation.tags ?? []).join(' '))
    && /(job|task|ingestion)/i.test(operation.path)
    && !/[{}]/.test(operation.path),
  )
}

export function parseKnowledgeJobPage(body: string): KnowledgeJobPage | null {
  try {
    const envelope = JSON.parse(body)
    const page = envelope?.data
    if (envelope?.success === false || !page || !Array.isArray(page.items)) return null
    const items = page.items.map((item: Record<string, unknown>, index: number) => ({
      id: String(item.id ?? item.jobKey ?? index),
      jobKey: typeof item.jobKey === 'string' ? item.jobKey : String(item.id ?? ''),
      status: typeof item.status === 'string' ? item.status : '未知',
      stage: typeof item.stage === 'string' ? item.stage : '',
      progress: typeof item.progress === 'number' && Number.isFinite(item.progress) ? item.progress : null,
    }))
    return {
      total: typeof page.total === 'number' && Number.isFinite(page.total) ? page.total : items.length,
      items,
    }
  } catch {
    return null
  }
}

export function isEmptyBusinessCollection(body: string): boolean {
  try {
    const data = JSON.parse(body)?.data
    if (Array.isArray(data)) return data.length === 0
    return Boolean(data && Array.isArray(data.items) && data.items.length === 0)
  } catch {
    return false
  }
}
