import request from '@/utils/request'

export interface SysConfig {
  configId?: number
  configName?: string
  configKey?: string
  configValue?: string
  configType?: string
  remark?: string
  createTime?: string
}

export interface ConfigQuery {
  pageNum: number
  pageSize: number
  configName?: string
  configKey?: string
  configType?: string
}

export interface R<T = unknown> {
  code: number
  msg: string
  data?: T
}

export interface TableDataInfo<T> {
  code: number
  msg: string
  rows: T[]
  total: number
}

export function listConfig(query: ConfigQuery) {
  return request.get<unknown, TableDataInfo<SysConfig>>('/config/list', { params: query })
}

export function getConfig(configId: number) {
  return request.get<unknown, R<SysConfig>>(`/config/${configId}`)
}

export function getConfigValue(configKey: string) {
  return request.get<unknown, R<string>>(`/config/key/${encodeURIComponent(configKey)}`)
}

export function addConfig(data: SysConfig) {
  return request.post<unknown, R>('/config', data)
}

export function updateConfig(data: SysConfig) {
  return request.put<unknown, R>('/config', data)
}

export function deleteConfig(configId: number) {
  return request.delete<unknown, R>(`/config/${configId}`)
}
