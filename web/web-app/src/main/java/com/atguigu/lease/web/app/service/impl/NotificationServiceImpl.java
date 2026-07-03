package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.model.entity.Notification;
import com.atguigu.lease.model.enums.NotificationType;
import com.atguigu.lease.web.app.mapper.NotificationMapper;
import com.atguigu.lease.web.app.service.NotificationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification>
        implements NotificationService {

    @Override
    public void sendNotification(Long userId, String title, String content, NotificationType type, Long relatedId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setIsRead(0);
        notification.setRelatedId(relatedId);
        this.save(notification);
    }
}
