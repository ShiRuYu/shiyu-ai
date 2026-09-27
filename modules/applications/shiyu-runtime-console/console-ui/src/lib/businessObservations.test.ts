import { describe, expect, it } from 'vitest'
import { findKnowledgeJobListOperation, isEmptyBusinessCollection, parseKnowledgeJobPage } from './businessObservations'

describe('business observations', () => {
  it('finds the existing knowledge ingestion job list from the project OpenAPI document', () => {
    const operations = [
      { method: 'GET', path: '/api/knowledge/ingestion-jobs/{id}', tags: ['知识任务'] },
      { method: 'GET', path: '/api/knowledge/ingestion-jobs', tags: ['知识任务'] },
      { method: 'POST', path: '/api/knowledge/ingestion-jobs', tags: ['知识任务'] },
    ]

    expect(findKnowledgeJobListOperation(operations)?.path).toBe('/api/knowledge/ingestion-jobs')
  })

  it('parses the existing Result<PageData<JobView>> response without exposing error details', () => {
    expect(parseKnowledgeJobPage(JSON.stringify({
      success: true,
      data: {
        total: 25,
        items: [{ id: 42, jobKey: 'job-42', status: 'PROCESSING', stage: 'EMBEDDING', progress: 60 }],
      },
    }))).toEqual({
      total: 25,
      items: [{ id: '42', jobKey: 'job-42', status: 'PROCESSING', stage: 'EMBEDDING', progress: 60 }],
    })
    expect(parseKnowledgeJobPage('{"success":false}')).toBeNull()
  })

  it('recognizes empty arrays and paged collections for model-configuration observations', () => {
    expect(isEmptyBusinessCollection('{"success":true,"data":[]}')).toBe(true)
    expect(isEmptyBusinessCollection('{"success":true,"data":{"total":0,"items":[]}}')).toBe(true)
    expect(isEmptyBusinessCollection('{"success":true,"data":{"total":1,"items":[{}]}}')).toBe(false)
  })
})
