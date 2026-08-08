<template>
  <div class="main-container">
    <!-- 清空按钮 -->
    <div v-if="roomList?.length" class="flex justify-end mb-[10px]">
      <van-button size="small" plain type="danger" @click="handleClearAll">
        清空历史
      </van-button>
    </div>

    <PullDownRefreshContainer
      :request="getHistoryListHandler"
      ref="pullDownRefreshContainerRef"
      class="min-h-[70vh]"
    >
      <template v-if="roomList?.length">
        <div v-for="item in roomList" :key="item.id" class="history-item">
          <RoomCard :data="item">
            <template #tags>
              <div class="flex items-center justify-between w-full">
                <span class="text-[12px] --van-gray-6">{{ item.browseTime }}</span>
                <van-icon
                  name="delete-o"
                  size="16"
                  color="#999"
                  @click.stop="handleDelete(item.historyId)"
                />
              </div>
            </template>
          </RoomCard>
        </div>
      </template>
    </PullDownRefreshContainer>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { getHistoryList, deleteHistory, clearAllHistory } from "@/api/search";
import PullDownRefreshContainer from "@/components/PullDownRefreshContainer/PullDownRefreshContainer.vue";
import type { ReqPage } from "@/api/types";
import RoomCard from "@/components/RoomCard/RoomCard.vue";
import type { RoomInterface } from "@/api/search/types";
import { showConfirmDialog, showSuccessToast } from "vant";

const pullDownRefreshContainerRef =
  ref<InstanceType<typeof PullDownRefreshContainer>>();

type RoomInterfaceWithHistory = RoomInterface & { browseTime: string; historyId: number };
const roomList = ref<RoomInterfaceWithHistory[]>([]);

// 分页获取房间的信息
async function getHistoryListHandler(pageInfo: ReqPage) {
  let { data } = await getHistoryList(pageInfo);
  let targetRecords = data.records.map(item => {
    return {
      id: item.roomId,
      roomNumber: item.roomNumber,
      rent: item.rent,
      graphVoList: item.roomGraphVoList,
      labelInfoList: [],
      apartmentInfo: {
        name: item.apartmentName || "",
        provinceName: item.provinceName || "",
        cityName: item.cityName || "",
        districtName: item.districtName || ""
      },
      browseTime: item.browseTime,
      historyId: item.id
    };
  }) as unknown as RoomInterfaceWithHistory[];
  if (pageInfo.current === 1) {
    roomList.value = targetRecords;
  } else {
    roomList.value = [...roomList.value, ...targetRecords];
  }
  pullDownRefreshContainerRef.value?.setFinished(
    roomList.value.length >= data.total
  );
}

// 删除单条
async function handleDelete(historyId: number) {
  await deleteHistory(historyId);
  roomList.value = roomList.value.filter(item => item.historyId !== historyId);
  showSuccessToast("已删除");
}

// 清空全部
async function handleClearAll() {
  try {
    await showConfirmDialog({ title: "确认清空", message: "确定要清空所有浏览历史吗？" });
    await clearAllHistory();
    roomList.value = [];
    showSuccessToast("已清空");
  } catch {
    // 取消
  }
}
</script>

<style scoped lang="less">
.history-item {
  margin-bottom: 8px;
}
</style>
