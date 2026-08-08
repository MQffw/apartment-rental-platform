import http from '@/utils/http'

export interface RepairRequest {
  id: number
  userId: number
  roomId: number
  apartmentId: number
  title: string
  description: string
  images: string
  status: number
  createTime: string
}

export interface RepairCreateDto {
  title: string
  description: string
  images?: string
}

// 提交报修
export function submitRepair(data: RepairCreateDto) {
  return http.post('/app/repair/save', data)
}

// 查询我的报修列表
export function getRepairList() {
  return http.get<RepairRequest[]>('/app/repair/list')
}
