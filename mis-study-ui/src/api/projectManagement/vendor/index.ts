import request from '@/utils/request'

export interface VendorInfo {
  vendorId?: number
  vendorName?: string
  contactName?: string
  contactPosition?: string
  contactPhone?: string
  contactEmail?: string
  vendorRatings?: string
  vendorAddr?: string
  remark?: string
  delFlag?: string
  createBy?: string
  createTime?: string
  updateBy?: string
  updateTime?: string
}

export interface VendorQuery {
  pageNum: number
  pageSize: number
  vendorName?: string
  contactName?: string
  contactPhone?: string
  vendorRatings?: string
  vendorAddr?: string
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

export function listVendor(query: VendorQuery) {
  return request.get<unknown, TableDataInfo<VendorInfo>>('/vendor/list', {
    params: query,
  })
}

export function getVendor(vendorId: number) {
  return request.get<unknown, R<VendorInfo>>(`/vendor/${vendorId}`)
}

export function addVendor(data: VendorInfo) {
  return request.post<unknown, R>('/vendor', data)
}

export function updateVendor(data: VendorInfo) {
  return request.put<unknown, R>('/vendor', data)
}

export function deleteVendor(vendorId: number) {
  return request.delete<unknown, R>(`/vendor/${vendorId}`)
}

export function checkVendorNameExists(vendorName: string, excludeVendorId?: number) {
  return request.get<unknown, R<boolean>>('/vendor/nameExists', {
    params: {
      vendorName,
      excludeVendorId,
    },
  })
}
