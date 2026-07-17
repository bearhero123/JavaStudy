import request from '@/utils/request'

export interface SysUser {
  userId?: number
  deptId?: number
  userName?: string
  nickName?: string
  email?: string
  phonenumber?: string
  sex?: string
  password?: string
  status?: string
  remark?: string
  createBy?: string
  createTime?: string
  updateBy?: string
  updateTime?: string
  delFlag?: string
  deptName?: string
  postIds?: number[]
  postNames?: string[]
  roleIds?: number[]
  roleNames?: string[]
}

export interface UserQuery {
  pageNum: number
  pageSize: number
  userName?: string
  nickName?: string
  phonenumber?: string
  status?: string
  deptId?: number
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

export function listUser(query: UserQuery) {
  return request.get<unknown, TableDataInfo<SysUser>>('/user/list', {
    params: query,
  })
}

export function getUser(userId: number) {
  return request.get<unknown, R<SysUser>>(`/user/${userId}`)
}

export function checkUserNameExists(userName: string, excludeUserId?: number) {
  return request.get<unknown, R<boolean>>('/user/nameExists', {
    params: {
      userName,
      excludeUserId,
    },
  })
}

export function addUser(data: SysUser) {
  return request.post<unknown, R>('/user', data)
}

export function updateUser(data: SysUser) {
  return request.put<unknown, R>('/user', data)
}

export function deleteUser(userId: number) {
  return request.delete<unknown, R>(`/user/${userId}`)
}
