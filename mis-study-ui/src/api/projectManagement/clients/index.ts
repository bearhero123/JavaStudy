import request from '@/utils/request'

export interface ClientsInfo {
  clientId?: number
  clientName?: string
  clientAddress?: string
  contactName?: string
  contactPosition?: string
  contactPhone?: string
  contactEmail?: string
  clientLevel?: string
  remark?: string
  delFlag?: string
  createBy?: string
  createTime?: string
  updateBy?: string
  updateTime?: string
}

export interface ClientsQuery {
  pageNum: number
  pageSize: number
  clientName?: string
  clientAddress?: string
  contactName?: string
  contactPhone?: string
  clientLevel?: string
}

export interface TableDataInfo<T> {
  code: number
  msg: string
  rows: T[]
  total: number
}

export interface R<T = unknown> {
  code: number
  msg: string
  data?: T
}

export function listClients(query: ClientsQuery) {
  return request.get<unknown, TableDataInfo<ClientsInfo>>('/clients/list', {
    params: query,
  })
}

export function getClients(clientId: number) {
  return request.get<unknown, R<ClientsInfo>>(`/clients/${clientId}`)
}

export function addClients(data: ClientsInfo) {
  return request.post<unknown, R>('/clients', data)
}

export function updateClients(data: ClientsInfo) {
  return request.put<unknown, R>('/clients', data)
}

export function deleteClients(clientId: number) {
  return request.delete<unknown, R>(`/clients/${clientId}`)
}

export function checkClientNameExists(clientName: string, excludeClientId?: number) {
  return request.get<unknown, R<boolean>>('/clients/nameExists', {
    params: {
      clientName,
      excludeClientId,
    },
  })
}
