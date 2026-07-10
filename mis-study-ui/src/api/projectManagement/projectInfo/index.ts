import request from '@/utils/request'

export interface ProjectInfo {
  projectId?: number
  projectNumber?: string
  projectAbbreviation?: string
  projectNameCn?: string
  projectNameEn?: string
  projectType?: string
  projectStatus?: string
  pmPersonId?: number
  pmName?: string
  clientId?: number
  clientName?: string
  projectScale?: number
  currency?: string
  businessArea?: string
  contractStatus?: string
  startDate?: string
  endDate?: string
  remark?: string
  delFlag?: string
  createBy?: string
  createTime?: string
  updateBy?: string
  updateTime?: string
}

export interface ProjectInfoQuery {
  pageNum: number
  pageSize: number
  projectNumber?: string
  projectNameCn?: string
  projectType?: string
  clientName?: string
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

export function listProjectInfo(query: ProjectInfoQuery) {
  return request.get<unknown, TableDataInfo<ProjectInfo>>('/projectInfo/list', {
    params: query,
  })
}

export function getProjectInfo(projectId: number) {
  return request.get<unknown, R<ProjectInfo>>(`/projectInfo/${projectId}`)
}

export function addProjectInfo(data: ProjectInfo) {
  return request.post<unknown, R>('/projectInfo', data)
}

export function updateProjectInfo(data: ProjectInfo) {
  return request.put<unknown, R>('/projectInfo', data)
}

export function deleteProjectInfo(projectId: number) {
  return request.delete<unknown, R>(`/projectInfo/${projectId}`)
}