import request from '@/utils/request'

export interface SysDept {
  deptId?: number
  parentId?: number
  deptName?: string
  orderNum?: number
  status?: string
  remark?: string
  createBy?: string
  createTime?: string
  updateBy?: string
  updateTime?: string
  delFlag?: string
  parentName?: string
  children?: SysDept[]
}

export interface DeptQuery {
  deptName?: string
  status?: string
}

export interface R<T = unknown> {
  code: number
  msg: string
  data?: T
}

export function listDept(query?: DeptQuery) {
  return request.get<unknown, R<SysDept[]>>('/dept/list', {
    params: query,
  })
}

export function getDept(deptId: number) {
  return request.get<unknown, R<SysDept>>(`/dept/${deptId}`)
}

export function getDeptTreeOptions(excludeDeptId?: number) {
  return request.get<unknown, R<SysDept[]>>('/dept/treeOptions', {
    params: excludeDeptId === undefined ? undefined : { excludeDeptId },
  })
}

export function addDept(data: SysDept) {
  return request.post<unknown, R>('/dept', data)
}

export function updateDept(data: SysDept) {
  return request.put<unknown, R>('/dept', data)
}

export function deleteDept(deptId: number) {
  return request.delete<unknown, R>(`/dept/${deptId}`)
}
