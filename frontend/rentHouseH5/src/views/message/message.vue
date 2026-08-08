<template>
  <div class="msg-container" :class="{ dark: isDark }">
    <!-- Header: only action button, title handled by NavBar -->
    <div class="msg-header">
      <div></div>
      <div
        v-if="notificationList.length > 0 && unreadCount > 0"
        class="msg-header-action"
        @click="handleMarkAllRead"
      >
        全部已读
      </div>
    </div>

    <!-- Notification list -->
    <div v-if="notificationList.length > 0" class="msg-list">
      <div
        v-for="item in notificationList"
        :key="item.id"
        class="msg-item"
        :class="{ unread: item.isRead === 0 }"
        @click="handleRead(item)"
      >
        <div class="msg-item-icon" :class="'type-' + item.type">
          <van-icon :name="typeIcon(item.type)" size="20" />
        </div>
        <div class="msg-item-body">
          <div class="msg-item-top">
            <span class="msg-item-title">{{ item.title }}</span>
            <van-tag v-if="item.isRead === 0" type="danger" size="mini" round>未读</van-tag>
          </div>
          <div class="msg-item-content">{{ item.content }}</div>
          <div class="msg-item-time">{{ formatTime(item.createTime) }}</div>
        </div>
      </div>
    </div>

    <!-- Empty state -->
    <van-empty v-else description="暂无消息" image="search" />
  </div>
</template>

<script setup lang="ts" name="Message">
import { ref, computed, onMounted } from "vue";
import {
  getNotificationList,
  markNotificationAsRead,
  markAllNotificationsAsRead
} from "@/api/user";
import { useDarkMode } from "@/hooks/useToggleDarkMode";

const isDark = computed(() => useDarkMode());

interface NotificationItem {
  id: number;
  userId: number;
  title: string;
  content: string;
  type: number;
  isRead: number;
  relatedId: number;
  createTime: string;
}

const notificationList = ref<NotificationItem[]>([]);

const unreadCount = computed(() =>
  notificationList.value.filter(n => n.isRead === 0).length
);

function typeIcon(type: number): string {
  const map: Record<number, string> = {
    1: "clock-o",
    2: "exchange",
    3: "bullhorn-o",
    4: "orders-o",
    5: "bill-o",
    6: "good-job-o",
    7: "chat-o"
  };
  return map[type] || "info-o";
}

function formatTime(time: string): string {
  if (!time) return "";
  return time.substring(5, 16);
}

async function fetchNotifications() {
  try {
    const { data } = await getNotificationList();
    notificationList.value = data || [];
  } catch (e) {
    console.error("fetch notifications failed", e);
  }
}

async function handleRead(item: NotificationItem) {
  if (item.isRead === 0) {
    await markNotificationAsRead(item.id);
    item.isRead = 1;
  }
}

async function handleMarkAllRead() {
  await markAllNotificationsAsRead();
  notificationList.value.forEach(n => (n.isRead = 1));
}

onMounted(() => {
  fetchNotifications();
});
</script>

<style scoped lang="less">
.msg-container {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 80px;
}

.dark {
  background: #1a1a1a;
}

.msg-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 20px 4px;
  position: sticky;
  top: 46px;
  z-index: 10;
  background: #f5f7fa;

  .dark & {
    background: #1a1a1a;
  }
}

.msg-header-action {
  font-size: 13px;
  color: #277941;
  padding: 4px 12px;
  border-radius: 12px;
  background: rgba(39, 121, 65, 0.08);

  .dark & {
    color: #34c759;
    background: rgba(52, 199, 89, 0.12);
  }

  &:active {
    background: rgba(39, 121, 65, 0.15);
  }
}

.msg-list {
  padding: 0 12px;
}

.msg-item {
  display: flex;
  align-items: flex-start;
  padding: 14px 16px;
  margin-bottom: 8px;
  background: #fff;
  border-radius: 12px;
  transition: all 0.2s ease;

  .dark & {
    background: #2c2c2c;
  }

  &:active {
    transform: scale(0.98);
  }

  &.unread {
    border-left: 3px solid #277941;

    .dark & {
      border-left-color: #34c759;
    }
  }
}

.msg-item-icon {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;

  &.type-1 {
    background: rgba(245, 108, 108, 0.1);
    color: #f56c6c;
  }
  &.type-2 {
    background: rgba(64, 158, 255, 0.1);
    color: #409eff;
  }
  &.type-3 {
    background: rgba(230, 162, 60, 0.1);
    color: #e6a23c;
  }
  &.type-4 {
    background: rgba(39, 121, 65, 0.1);
    color: #277941;

    .dark & {
      background: rgba(52, 199, 89, 0.12);
      color: #34c759;
    }
  }
  &.type-5 {
    background: rgba(155, 89, 182, 0.1);
    color: #9b59b6;

    .dark & {
      background: rgba(155, 89, 182, 0.15);
      color: #bb8fce;
    }
  }
  &.type-6 {
    background: rgba(231, 76, 60, 0.1);
    color: #e74c3c;

    .dark & {
      background: rgba(231, 76, 60, 0.15);
      color: #f1948a;
    }
  }
  &.type-7 {
    background: rgba(52, 152, 219, 0.1);
    color: #3498db;

    .dark & {
      background: rgba(52, 152, 219, 0.15);
      color: #85c1e9;
    }
  }
}

.msg-item-body {
  flex: 1;
  min-width: 0;
}

.msg-item-top {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.msg-item-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;

  .dark & {
    color: #e0e0e0;
  }
}

.msg-item-content {
  font-size: 13px;
  color: #999;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;

  .dark & {
    color: #888;
  }
}

.msg-item-time {
  font-size: 12px;
  color: #c0c0c0;
  margin-top: 6px;

  .dark & {
    color: #666;
  }
}
</style>
