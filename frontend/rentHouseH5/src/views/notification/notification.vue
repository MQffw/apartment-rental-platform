<template>
  <div class="page-container">
    <div v-if="notificationList.length === 0" class="flex justify-center items-center h-[60vh]">
      <van-empty description="暂无通知" />
    </div>
    <van-cell-group v-else inset>
      <van-cell
        v-for="item in notificationList"
        :key="item.id"
        :title="item.title"
        :label="item.content"
        :value="formatTime(item.createTime)"
        :class="{ 'font-bold': item.isRead === 0 }"
        @click="handleRead(item)"
      >
        <template #right-icon>
          <van-tag v-if="item.isRead === 0" type="danger" size="small">未读</van-tag>
        </template>
      </van-cell>
    </van-cell-group>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import {
  getNotificationList,
  markNotificationAsRead,
  markAllNotificationsAsRead
} from "@/api/user";
import type { NotificationInterface } from "@/api/user";
import { showConfirmDialog } from "vant";

const notificationList = ref<NotificationInterface[]>([]);

async function fetchNotifications() {
  const { data } = await getNotificationList();
  notificationList.value = data;
}

function formatTime(time: string) {
  if (!time) return "";
  return time.substring(5, 16);
}

async function handleRead(item: NotificationInterface) {
  if (item.isRead === 0) {
    await markNotificationAsRead(item.id);
    item.isRead = 1;
  }
}

onMounted(() => {
  fetchNotifications();
});
</script>

<style scoped lang="less">
.font-bold {
  font-weight: bold;
}
</style>
