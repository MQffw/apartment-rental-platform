<template>
  <div class="my-room-page" :class="{ dark: isDark }">
    <!-- 无租约状态 -->
    <div v-if="!hasAgreement" class="empty-state">
      <van-empty
        image="search"
        description="暂无在租房间"
      >
        <van-button type="primary" round size="small" @click="router.push('/search')">
          去找房
        </van-button>
      </van-empty>
    </div>

    <!-- 房间列表：点击进入房间详情 -->
    <div v-else class="room-list">
      <div
        v-for="(item, idx) in agreementList"
        :key="item.id"
        class="room-card"
        :style="{ animationDelay: `${idx * 0.06}s` }"
        @click="goRoomDetail(item)"
      >
        <div class="room-card-icon">
          <SvgIcon name="合同" :size="24" />
        </div>
        <div class="room-main">
          <div class="room-name">{{ item.apartmentName || '公寓' }} · {{ item.roomNumber }}</div>
          <div class="room-term">{{ item.leaseStartDate }} ~ {{ item.leaseEndDate }}</div>
        </div>
        <div class="room-right">
          <div class="room-rent">¥{{ item.rent }}<span class="rent-unit">/月</span></div>
          <van-tag type="success" size="medium">{{ item.statusText || '已签约' }}</van-tag>
        </div>
        <van-icon name="arrow" class="room-arrow" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts" name="MyRoom">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useDarkMode } from '@/hooks/useToggleDarkMode'
import SvgIcon from '@/components/SvgIcon/index.vue'
import { getMyAgreementList } from '@/api/search'

const router = useRouter()
const isDark = computed(() => useDarkMode())

const hasAgreement = ref(false)
const agreementList = ref<any[]>([])

async function fetchMyRoom() {
  try {
    const { data } = await getMyAgreementList()
    if (data && data.length > 0) {
      const signed = data.filter((a: any) => a.leaseStatus === 2)
      agreementList.value = signed.length > 0 ? signed : data
      hasAgreement.value = true
    }
  } catch (e) {
    console.error('获取房间信息失败', e)
  }
}

// 进入房间详情（租约信息、缴费、报修等）
function goRoomDetail(item: any) {
  router.push({ path: '/myRoomDetail', query: { id: item.id } })
}

onMounted(() => {
  fetchMyRoom()
})
</script>

<style scoped lang="less">
@keyframes fade-up {
  from {
    opacity: 0;
    transform: translateY(14px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.my-room-page {
  min-height: calc(100vh - 50px);
  background: #f5f7fa;
  transition: background 0.3s ease;

  &.dark {
    background: #1a1a1a;
  }
}

// ========== 房间列表 ==========
.room-list {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.room-card {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #fff;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  animation: fade-up 0.45s cubic-bezier(0.22, 0.61, 0.36, 1) both;
  transition: transform 0.15s ease, box-shadow 0.15s ease;

  &:active {
    transform: scale(0.98);
    box-shadow: 0 1px 6px rgba(0, 0, 0, 0.08);
  }

  .dark & {
    background: #2c2c2c;
  }
}

.room-card-icon {
  flex-shrink: 0;
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: linear-gradient(135deg, #277941, #34c759);
  box-shadow: 0 2px 8px rgba(52, 199, 89, 0.3);
}

.room-main {
  flex: 1;
  min-width: 0;

  .room-name {
    font-size: 16px;
    font-weight: 600;
    color: #333;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;

    .dark & { color: #e0e0e0; }
  }

  .room-term {
    font-size: 12px;
    color: #999;
    margin-top: 6px;

    .dark & { color: #888; }
  }
}

.room-right {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;

  .room-rent {
    font-size: 17px;
    font-weight: 700;
    color: #e74c3c;

    .rent-unit {
      font-size: 11px;
      font-weight: 400;
      color: #999;
    }
  }
}

.room-arrow {
  flex-shrink: 0;
  font-size: 14px;
  color: #c0c0c0;

  .dark & { color: #666; }
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 50px);
}
</style>
