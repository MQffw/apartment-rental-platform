<template>
  <SearchBar :confirm-callback="confirmCallback"></SearchBar>

  <div class="main-container">
    <PullDownRefreshContainer
      :request="getRoomListHandler"
      ref="pullDownRefreshContainerRef"
      class="min-h-[70vh]"
    >
      <template v-if="roomList?.length">
        <RoomCard
          v-for="item in roomList"
          :key="item.id"
          :data="item"
        ></RoomCard>
      </template>
    </PullDownRefreshContainer>
  </div>
</template>

<script setup lang="ts" name="Search">
import SearchBar from "@/components/SearchBar/SearchBar.vue";
import type { RoomInterface, RoomListQueryInterface } from "@/api/search/types";
import { ref } from "vue";
import { getRoomList } from "@/api/search";
import PullDownRefreshContainer from "@/components/PullDownRefreshContainer/PullDownRefreshContainer.vue";
import type { ReqPage } from "@/api/types";
import RoomCard from "@/components/RoomCard/RoomCard.vue";

// 搜索参数
const searchParams = ref<Partial<RoomListQueryInterface>>({});
const pullDownRefreshContainerRef =
  ref<InstanceType<typeof PullDownRefreshContainer>>();
// 房间列表
const roomList = ref<RoomInterface[]>([]);
// 点击查找，搜索条件
function confirmCallback(params: RoomListQueryInterface) {
  let targetObi = {};
  for (let key in params) {
    if ((params as any)[key] !== "") {
      (targetObi as any)[key] = (params as any)[key];
    }
  }
  searchParams.value = targetObi;
  pullDownRefreshContainerRef.value?.onRefresh();
}
// 分页获取房间的信息
async function getRoomListHandler(pageInfo: ReqPage) {
  const { data } = await getRoomList({ ...searchParams.value, ...pageInfo });
  if (pageInfo.current === 1) {
    roomList.value = data.records;
  } else {
    roomList.value = [...roomList.value, ...data.records];
  }
  pullDownRefreshContainerRef.value?.setFinished(
    roomList.value.length >= data.total
  );
}
</script>

<style scoped lang="less">
</style>
