package com.atguigu.lease.web.app.service;

import com.atguigu.lease.model.entity.Notification;
import com.atguigu.lease.model.enums.NotificationType;
import com.baomidou.mybatisplus.extension.service.IService;

public interface NotificationService extends IService<Notification> {
    void sendNotification(Long userId, String title, String content, NotificationType type, Long relatedId);
}
