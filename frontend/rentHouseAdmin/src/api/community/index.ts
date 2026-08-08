import http from '@/utils/http'
import type { PageRes } from '@/api/types'

// ===== 报修管理 =====
export function getRepairPage(params: { pageNum: number; pageSize: number; status?: number; apartmentId?: number }) {
  return http.get<PageRes<any>>('/admin/repair/page', {
    current: params.pageNum,
    size: params.pageSize,
    status: params.status,
    apartmentId: params.apartmentId
  })
}

export function updateRepairStatus(id: number, status: number) {
  return http.post('/admin/repair/updateStatus', null, { params: { id, status } })
}

// ===== 帖子管理 =====
export function getPostPage(params: { pageNum: number; pageSize: number; status?: number }) {
  return http.get<PageRes<any>>('/admin/post/page', {
    current: params.pageNum,
    size: params.pageSize,
    status: params.status
  })
}

export function updatePostStatus(id: number, status: number) {
  return http.post('/admin/post/updateStatus', null, { params: { id, status } })
}

export function deletePost(id: number) {
  return http.delete(`/admin/post/${id}`)
}

export function getPostStatistics() {
  return http.get<any>('/admin/post/statistics')
}

// ===== 评论管理 =====
export function getCommentPage(params: { pageNum: number; pageSize: number; postId?: number }) {
  return http.get<PageRes<any>>('/admin/post/comment/page', {
    current: params.pageNum,
    size: params.pageSize,
    postId: params.postId
  })
}

export function deleteComment(id: number) {
  return http.delete(`/admin/post/comment/${id}`)
}

// ===== 公告发送 =====
export function sendAnnouncement(data: { title: string; content: string; target: string }) {
  return http.post('/admin/announcement/send', data)
}
