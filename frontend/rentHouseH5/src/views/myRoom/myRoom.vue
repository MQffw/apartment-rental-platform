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

    <!-- 有租约状态 -->
    <template v-else>
      <van-swipe
        ref="swipeRef"
        class="room-swipe"
        :show-indicators="agreementList.length > 1"
        :loop="agreementList.length > 1"
        :duration="300"
        @change="onSwipeChange"
      >
        <van-swipe-item
          v-for="(item, idx) in agreementList"
          :key="item.id"
        >
          <!-- 头部：房间信息 -->
          <div class="room-header">
            <div class="room-header-bg"></div>
            <div class="room-header-content">
              <div class="room-title">{{ item.apartmentName || '公寓' }} {{ item.roomNumber }}</div>
              <div class="room-subtitle">我的租住空间</div>
            </div>
          </div>

          <div class="room-body">
            <!-- 租约信息卡片 -->
            <div class="info-card">
              <div class="card-title">租约信息</div>
              <div class="info-row">
                <span class="label">租期</span>
                <span class="value">{{ item.leaseStartDate }} ~ {{ item.leaseEndDate }}</span>
              </div>
              <div class="info-row">
                <span class="label">月租</span>
                <span class="value price">¥{{ item.rent }}</span>
              </div>
              <div class="info-row">
                <span class="label">支付方式</span>
                <span class="value">{{ item.paymentTypeName || '月付' }}</span>
              </div>
              <div class="info-row">
                <span class="label">状态</span>
                <van-tag type="success" size="medium">{{ item.statusText || '已签约' }}</van-tag>
              </div>
            </div>

            <!-- 生活服务 -->
            <div class="service-grid">
              <div class="service-item" @click="showPayDialog(1)">
                <div class="service-icon rent">
                  <SvgIcon name="电费" :size="24" />
                </div>
                <span class="service-name">交房租</span>
              </div>
              <div class="service-item" @click="showPayDialog(2)">
                <div class="service-icon electric">
                  <SvgIcon name="电费" :size="24" />
                </div>
                <span class="service-name">交电费</span>
              </div>
              <div class="service-item" @click="showPayDialog(3)">
                <div class="service-icon water">
                  <SvgIcon name="水费" :size="24" />
                </div>
                <span class="service-name">交水费</span>
              </div>
              <div class="service-item" @click="router.push('/repair')">
                <div class="service-icon repair">
                  <SvgIcon name="物业报修" :size="24" />
                </div>
                <span class="service-name">报修</span>
              </div>
            </div>

            <!-- 待缴账单 -->
            <div class="bill-section">
              <div class="section-title">待缴账单</div>
              <div v-if="getBills(idx).pending.length === 0" class="empty-bills">暂无待缴账单</div>
              <div v-else class="bill-list">
                <div
                  v-for="bill in getBills(idx).pending"
                  :key="bill.id"
                  class="bill-item pending"
                >
                  <div class="bill-info">
                    <div class="bill-type">{{ getBillTypeName(bill.billType) }}</div>
                    <div class="bill-date">{{ bill.createTime }}</div>
                  </div>
                  <div class="bill-amount">¥{{ bill.amount }}</div>
                  <van-button size="small" type="primary" plain round @click="handlePay(bill.id, idx)">
                    去支付
                  </van-button>
                </div>
              </div>
            </div>

            <!-- 历史记录 -->
            <div class="bill-section">
              <div class="section-title">缴费历史</div>
              <div v-if="getBills(idx).paid.length === 0" class="empty-bills">暂无缴费记录</div>
              <div v-else class="bill-list">
                <div
                  v-for="bill in getBills(idx).paid"
                  :key="bill.id"
                  class="bill-item paid"
                >
                  <div class="bill-info">
                    <div class="bill-type">{{ getBillTypeName(bill.billType) }}</div>
                    <div class="bill-date">{{ bill.paymentTime || bill.createTime }}</div>
                  </div>
                  <div class="bill-amount">¥{{ bill.amount }}</div>
                  <van-tag type="success" size="medium">已支付</van-tag>
                </div>
              </div>
            </div>
          </div>
        </van-swipe-item>
      </van-swipe>
    </template>

    <!-- 支付弹窗 -->
    <van-dialog
      v-model:show="payDialogVisible"
      title="确认支付"
      show-cancel-button
      :before-close="onPayConfirm"
    >
      <div class="pay-dialog-content">
        <div class="pay-type">{{ getBillTypeName(currentPayType) }}</div>
        <div v-if="currentPayType === 1" class="pay-amount">¥{{ currentAgreement.rent || 0 }}</div>
        <div v-else class="pay-input-row">
          <span>金额：</span>
          <van-field v-model="customAmount" type="digit" placeholder="输入金额" class="pay-input" />
        </div>
      </div>
    </van-dialog>
  </div>
</template>

<script setup lang="ts" name="MyRoom">
import { ref, computed, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showSuccessToast, showFailToast } from 'vant'
import { useDarkMode } from '@/hooks/useToggleDarkMode'
import SvgIcon from '@/components/SvgIcon/index.vue'
import { getMyAgreementList } from '@/api/search'
import http from '@/utils/http'

const router = useRouter()
const isDark = computed(() => useDarkMode())

const hasAgreement = ref(false)
const agreementList = ref<any[]>([])
const currentIndex = ref(0)
const swipeRef = ref()

// 每个房间的账单缓存，避免重复请求
const billsCache = reactive<Record<string, { pending: any[], paid: any[] }>>({})

const currentAgreement = computed(() => agreementList.value[currentIndex.value] || {})

function getBills(idx: number) {
  const id = agreementList.value[idx]?.id
  if (!id) return { pending: [], paid: [] }
  return billsCache[id] || { pending: [], paid: [] }
}

async function loadBills(idx: number) {
  const item = agreementList.value[idx]
  if (!item?.id) return
  if (billsCache[item.id]) return
  try {
    const { data } = await http.get(`/app/paymentRecord/bills?agreementId=${item.id}`)
    if (data) {
      billsCache[item.id] = {
        pending: data.filter((b: any) => b.paymentStatus === 0),
        paid: data.filter((b: any) => b.paymentStatus === 1)
      }
    }
  } catch {
    billsCache[item.id] = { pending: [], paid: [] }
  }
}

function onSwipeChange(idx: number) {
  currentIndex.value = idx
  loadBills(idx)
}

async function fetchMyRoom() {
  try {
    const { data } = await getMyAgreementList()
    if (data && data.length > 0) {
      const signed = data.filter((a: any) => a.leaseStatus === 2)
      agreementList.value = signed.length > 0 ? signed : data
      hasAgreement.value = true
      currentIndex.value = 0
      loadBills(0)
    }
  } catch (e) {
    console.error('获取房间信息失败', e)
  }
}

const billTypeMap: Record<number, string> = {
  1: '房租',
  2: '电费',
  3: '水费',
  4: '物业费'
}

function getBillTypeName(type: number) {
  return billTypeMap[type] || '其他'
}

const payDialogVisible = ref(false)
const currentPayType = ref(0)
const customAmount = ref('')

function showPayDialog(billType: number) {
  currentPayType.value = billType
  customAmount.value = ''
  payDialogVisible.value = true
}

async function handlePay(billId: number, idx: number) {
  try {
    await http.post(`/app/paymentRecord/pay/${billId}`)
    showSuccessToast('支付成功')
    const item = agreementList.value[idx]
    if (item) delete billsCache[item.id]
    await loadBills(idx)
  } catch {
    showFailToast('支付失败')
  }
}

async function onPayConfirm(action: string) {
  if (action === 'confirm') {
    const idx = currentIndex.value
    try {
      const item = agreementList.value[idx]
      if (!item) return true
      if (currentPayType.value === 1) {
        await http.post(`/app/paymentRecord/createBill?agreementId=${item.id}&billType=1&amount=${item.rent}`)
      } else {
        const amt = parseFloat(customAmount.value)
        if (!amt || amt <= 0) {
          showFailToast('请输入有效金额')
          return false
        }
        await http.post(`/app/paymentRecord/createBill?agreementId=${item.id}&billType=${currentPayType.value}&amount=${amt}`)
      }
      delete billsCache[item.id]
      await loadBills(idx)
      const bills = getBills(idx)
      if (bills.pending.length > 0) {
        await handlePay(bills.pending[0].id, idx)
      }
      showSuccessToast('支付成功')
    } catch {
      showFailToast('支付失败')
    }
  }
  return true
}

onMounted(() => {
  fetchMyRoom()
})
</script>

<style scoped lang="less">
.my-room-page {
  min-height: calc(100vh - 46px);
  background: #f5f7fa;
  padding-bottom: 20px;
  transition: background 0.3s ease;

  &.dark {
    background: #1a1a1a;
  }
}

.room-swipe {
  :deep(.van-swipe__indicators) {
    bottom: auto;
    top: 108px;
  }

  :deep(.van-swipe__indicator) {
    width: 6px;
    height: 6px;
    background: rgba(255, 255, 255, 0.5);
    border-radius: 3px;
    transition: all 0.3s;

    &--active {
      width: 18px;
      background: #fff;
    }
  }
}

.room-header {
  position: relative;
  height: 120px;
  overflow: hidden;
}

.room-header-bg {
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 100px;
  background: linear-gradient(135deg, #277941 0%, #34c759 100%);
  border-radius: 0 0 24px 24px;
  will-change: transform;
}

.room-header-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 28px;
}

.room-title {
  font-size: 20px;
  font-weight: 700;
  color: #fff;
}

.room-subtitle {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 4px;
}

.room-body {
  padding: 0 16px;
}

.info-card {
  background: #fff;
  border-radius: 14px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.03);

  .dark & {
    background: #2c2c2c;
  }
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;

  .dark & { color: #e0e0e0; }
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;

  .label {
    font-size: 13px;
    color: #999;
  }
  .value {
    font-size: 14px;
    color: #333;
    font-weight: 500;

    .dark & { color: #e0e0e0; }
    &.price { color: #277941; }
  }
}

.service-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}

.service-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;

  &:active { opacity: 0.7; }
}

.service-icon {
  width: 50px;
  height: 50px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;

  &.rent { background: linear-gradient(135deg, #277941, #34c759); }
  &.electric { background: linear-gradient(135deg, #f59e0b, #fbbf24); }
  &.water { background: linear-gradient(135deg, #3b82f6, #60a5fa); }
  &.repair { background: linear-gradient(135deg, #8b5cf6, #a78bfa); }
}

.service-name {
  font-size: 12px;
  color: #666;

  .dark & { color: #aaa; }
}

.bill-section {
  margin-bottom: 16px;
  content-visibility: auto;
  contain-intrinsic-size: 120px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 10px;

  .dark & { color: #e0e0e0; }
}

.empty-bills {
  text-align: center;
  color: #ccc;
  font-size: 13px;
  padding: 20px 0;

  .dark & { color: #555; }
}

.bill-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  will-change: transform;
}

.bill-item {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 12px;
  padding: 12px 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.02);
  transform: translateZ(0);

  .dark & {
    background: #2c2c2c;
  }

  &.pending {
    border-left: 3px solid #f59e0b;
  }

  .bill-info {
    flex: 1;
    .bill-type {
      font-size: 14px;
      font-weight: 500;
      color: #333;
      .dark & { color: #e0e0e0; }
    }
    .bill-date {
      font-size: 11px;
      color: #999;
      margin-top: 2px;
    }
  }

  .bill-amount {
    font-size: 16px;
    font-weight: 700;
    color: #e74c3c;
    margin-right: 12px;
  }
}

.pay-dialog-content {
  text-align: center;
  padding: 20px;
  .pay-type { font-size: 14px; color: #666; margin-bottom: 8px; }
  .pay-amount { font-size: 28px; font-weight: 700; color: #277941; }
  .pay-input-row {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    font-size: 14px;
    color: #333;
  }
  .pay-input {
    width: 160px;
    padding: 0;
    :deep(.van-field__control) {
      text-align: center;
      font-size: 18px;
      font-weight: 600;
      color: #277941;
    }
  }
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 46px - 50px);
}
</style>
