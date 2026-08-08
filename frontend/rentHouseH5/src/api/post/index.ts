import http from '@/utils/http'

export interface PostItem {
  id: number
  userId: number
  nickname: string
  avatarUrl: string
  apartmentId: number | null
  roomId: number | null
  title: string
  content: string
  images: string
  rating: number
  likeCount: number
  commentCount: number
  isLiked: boolean
  createTime: string
}

export interface PostComment {
  id: number
  postId: number
  userId: number
  nickname: string
  avatarUrl: string
  parentId: number
  replyToId: number
  replyToNickname: string
  content: string
  likeCount: number
  createTime: string
}

export interface PostCreateDto {
  title: string
  content: string
  images?: string
  rating?: number
  apartmentId?: number
  roomId?: number
}

export interface CommentCreateDto {
  postId: number
  content: string
  parentId?: number
  replyToId?: number
}

// 帖子列表
export function getPostList(params: { current: number; size: number; sort?: string }) {
  return http.get('/app/post/page', params)
}

// 帖子详情
export function getPostDetail(id: number) {
  return http.get(`/app/post/detail?id=${id}`)
}

// 发布帖子
export function createPost(data: PostCreateDto) {
  return http.post('/app/post/create', data)
}

// 点赞/取消点赞
export function toggleLike(postId: number) {
  return http.post(`/app/post/like/toggle/${postId}`)
}

// 获取评论列表
export function getCommentList(postId: number) {
  return http.get(`/app/post/comment/list/${postId}`)
}

// 发布评论
export function createComment(data: CommentCreateDto) {
  return http.post('/app/post/comment/create', data)
}

// 我的帖子
export function getMyPosts() {
  return http.get('/app/post/myPosts')
}
