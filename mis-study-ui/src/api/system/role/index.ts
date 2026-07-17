import request from '@/utils/request'

export interface SysRole {
  roleId?: number
  roleName?: string
  roleKey?: string
  roleSort?: number
  status?: string
  remark?: string
  createBy?: string
  createTime?: string
  updateBy?: string
  updateTime?: string
  delFlag?: string
  menuIds?: number[]
}

export interface RoleQuery {
  pageNum: number
  pageSize: number
  roleName?: string
  roleKey?: string
  status?: string
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

export function listRole(query: RoleQuery) {
  return request.get<unknown, TableDataInfo<SysRole>>('/role/list', { params: query })
}

export function listRoleOptions() {
  return request.get<unknown, R<SysRole[]>>('/role/options')
}

export function getRole(roleId: number) {
  return request.get<unknown, R<SysRole>>(`/role/${roleId}`)
}

export function addRole(data: SysRole) {
  return request.post<unknown, R>('/role', data)
}

export function updateRole(data: SysRole) {
  return request.put<unknown, R>('/role', data)
}

export function deleteRole(roleId: number) {
  return request.delete<unknown, R>(`/role/${roleId}`)
}
