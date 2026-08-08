import http from "@/utils/http";
import type {
  loginQueryInterface,
  SmsCodeQueryInterface,
  UserInfoInterface
} from "@/api/user/types";

/**
 * @description 验证码登录
 * @param params
 */
export function login(params: loginQueryInterface) {
  return http.post<string>(`/app/login`, params);
}

/**
 * @description 密码登录
 * @param params
 */
export function loginByPassword(params: { email: string; password: string }) {
  return http.post<string>(`/app/loginByPassword`, params);
}

/**
 * @description 获取邮箱验证码
 * @param params
 */
export function getEmailCode(params: SmsCodeQueryInterface) {
  return http.get(`/app/login/getCode`, params);
}

/**
 * @description 设置密码
 */
export function setPassword(password: string) {
  return http.post(`/app/setPassword?password=${encodeURIComponent(password)}`);
}

/**
 * @description 检查是否已设置密码
 */
export function hasPassword() {
  return http.get<boolean>(`/app/hasPassword`);
}

/**
 * @description 获取用户信息
 */
export function getUserInfo() {
  return http.get<UserInfoInterface>(`/app/info`);
}

/**
 * @description 更新用户资料
 */
export function updateProfile(params: Partial<UserInfoInterface>) {
  return http.post(`/app/user/updateProfile`, params);
}

/**
 * @description 获取通知列表
 */
export function getNotificationList() {
  return http.get<NotificationInterface[]>(`/app/notification/list`);
}

/**
 * @description 获取未读通知数量
 */
export function getUnreadCount() {
  return http.get<number>(`/app/notification/unreadCount`);
}

/**
 * @description 标记通知已读
 */
export function markNotificationAsRead(id: number | string) {
  return http.post(`/app/notification/markAsRead?id=${id}`);
}

/**
 * @description 标记所有通知已读
 */
export function markAllNotificationsAsRead() {
  return http.post(`/app/notification/markAllAsRead`);
}

export interface NotificationInterface {
  id: number;
  userId: number;
  title: string;
  content: string;
  type: number;
  isRead: number;
  relatedId: number;
  createTime: string;
}
