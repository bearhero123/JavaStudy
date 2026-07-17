import request from '@/utils/request'

export interface SysPost {
  postId?: number
  postCode?: string
  postName?: string
  postSort?: number
  status?: string
  remark?: string
  createBy?: string
  createTime?: string
  updateBy?: string
  updateTime?: string
  delFlag?: string
}

export interface PostQuery {
  pageNum: number
  pageSize: number
  postCode?: string
  postName?: string
  status?: string
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

export function listPost(query: PostQuery) {
  return request.get<unknown, TableDataInfo<SysPost>>('/post/list', {
    params: query,
  })
}

export function getPost(postId: number) {
  return request.get<unknown, R<SysPost>>(`/post/${postId}`)
}

export function listPostOptions() {
  return request.get<unknown, R<SysPost[]>>('/post/options')
}

export function addPost(data: SysPost) {
  return request.post<unknown, R>('/post', data)
}

export function updatePost(data: SysPost) {
  return request.put<unknown, R>('/post', data)
}

export function deletePost(postId: number) {
  return request.delete<unknown, R>(`/post/${postId}`)
}
