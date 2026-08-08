// 登录
export interface loginQueryInterface {
  // 邮箱
  email: string;
  // 验证码
  code: string;
}
// 获取邮箱验证码
export interface SmsCodeQueryInterface {
  // 邮箱
  email: string;
}

// 用户信息
export interface UserInfoInterface {
  // 头像
  avatarUrl: string;
  // 用户名
  nickname: string;
  // 邮箱
  email?: string;
  // 手机号
  phone?: string;
}
// 用户state
export interface UserStateInterface {
  // 用户信息
  userInfo: UserInfoInterface | null;
  // token
  token: string | null;
}
