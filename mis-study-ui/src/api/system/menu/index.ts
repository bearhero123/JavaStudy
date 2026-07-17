import request from '@/utils/request'

export interface SysMenu {
  menuId?: number
  parentId?: number
  menuName?: string
  orderNum?: number
  path?: string
  component?: string
  menuType?: string
  visible?: string
  status?: string
  perms?: string
  icon?: string
  remark?: string
  createTime?: string
  children?: SysMenu[]
}

export interface MenuQuery {
  menuName?: string
  status?: string
}

export interface RoleMenuTree {
  menus: SysMenu[]
  checkedKeys: number[]
}

export interface R<T = unknown> {
  code: number
  msg: string
  data?: T
}

export function listMenu(query?: MenuQuery) {
  return request.get<unknown, R<SysMenu[]>>('/menu/list', { params: query })
}

export function getMenu(menuId: number) {
  return request.get<unknown, R<SysMenu>>(`/menu/${menuId}`)
}

export function getMenuTreeOptions(excludeMenuId?: number) {
  return request.get<unknown, R<SysMenu[]>>('/menu/treeOptions', {
    params: excludeMenuId === undefined ? undefined : { excludeMenuId },
  })
}

export function getRoleMenuTree(roleId: number) {
  return request.get<unknown, R<RoleMenuTree>>(`/menu/roleTree/${roleId}`)
}

export function addMenu(data: SysMenu) {
  return request.post<unknown, R>('/menu', data)
}

export function updateMenu(data: SysMenu) {
  return request.put<unknown, R>('/menu', data)
}

export function deleteMenu(menuId: number) {
  return request.delete<unknown, R>(`/menu/${menuId}`)
}
