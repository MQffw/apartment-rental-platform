<template>
  <div class="my-room-detail" :class="{ dark: isDark }">
    <van-skeleton :row="12" :loading="!currentAgreement?.id">
      <!-- 头部：房间信息 -->
      <div class="room-header">
        <div class="room-header-bg"></div>
        <div class="room-header-content">
          <div class="room-title">{{ currentAgreement.apartmentName || '公寓' }} · {{ currentAgreement.roomNumber }}</div>
          <div class="room-subtitle">我的租住空间</div>
        </div>
      </div>

      <div class="room-body">
        <!-- 租约信息卡片 -->
        <div class="info-card">
          <div class="card-title">租约信息</div>
          <div class="info-row">
            <span class="label">租期</span>
            <span class="value">{{ currentAgreement.leaseStartDate }} ~ {{ currentAgreement.leaseEndDate }}</span>
          </div>
          <div class="info-row">
            <span class="label">月租</span>
            <span class="value price">¥{{ currentAgreement.rent }}</span>
          </div>
          <div class="info-row">
            <span class="label">支付方式</span>
            <span class="value">{{ currentAgreement.paymentTypeName || '月付' }}</span>
          </div>
          <div class="info-row">
            <span class="label">状态</span>
            <van-tag type="success" size="medium">{{ currentAgreement.statusText || '已签约' }}</van-tag>
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
          <div v-if="bills.pending.length === 0" class="empty-bills">暂无待缴账单</div>
          <div v-else class="bill-list">
            <div
              v-for="bill in bills.pending"
              :key="bill.id"
              class="bill-item pending"
            >
              <div class="bill-info">
                <div class="bill-type">{{ getBillTypeName(bill.billType) }}</div>
                <div class="bill-date">{{ bill.createTime }}</div>
              </div>
              <div class="bill-amount">¥{{ bill.amount }}</div>
              <van-button
                size="small"
                type="primary"
                plain
                round
                :loading="payingId === bill.id"
                @click="handlePay(bill.id)"
              >
                去支付
              </van-button>
            </div>
          </div>
        </div>

        <!-- 历史记录 -->
        <div class="bill-section">
          <div class="section-title">缴费历史</div>
          <div v-if="bills.paid.length === 0" class="empty-bills">暂无缴费记录</div>
          <div v-else class="bill-list">
            <div
              v-for="bill in bills.paid"
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
    </van-skeleton>

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

<script setup lang="ts" name="MyRoomDetail">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showSuccessToast, showFailToast } from 'vant'
import { useDarkMode } from '@/hooks/useToggleDarkMode'
import SvgIcon from '@/components/SvgIcon/index.vue'
import { getMyAgreementList } from '@/api/search'
import http from '@/utils/http'

const router = useRouter()
const route = useRoute()
const isDark = computed(() => useDarkMode())

const agreementId = route.query.id as string
const currentAgreement = ref<any>({})

// 账单数据
const bills = reactive<{ pending: any[], paid: any[] }>({ pending: [], paid: [] })

// 防重复支付：记录正在支付的账单 id
const payingId = ref<number | null>(null)

async function loadAgreement() {
  try {
    const { data } = await getMyAgreementList()
    const matched = (data || []).find((a: any) => String(a.id) === String(agreementId))
    if (matched) currentAgreement.value = matched
  } catch (e) {
    console.error('获取房间信息失败', e)
  }
}

async function loadBills() {
  if (!agreementId) return
  try {
    const { data } = await http.get(`/app/paymentRecord/bills?agreementId=${agreementId}`)
    if (data) {
      bills.pending = data.filter((b: any) => b.paymentStatus === 0)
      bills.paid = data.filter((b: any) => b.paymentStatus === 1)
    }
  } catch {
    bills.pending = []
    bills.paid = []
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

async function handlePay(billId: number) {
  if (payingId.value) return
  payingId.value = billId
  try {
    await http.post(`/app/paymentRecord/pay/${billId}`)
    showSuccessToast('支付成功')
    await loadBills()
  } catch {
    showFailToast('支付失败')
  } finally {
    payingId.value = null
  }
}

async function onPayConfirm(action: string) {
  if (action === 'confirm') {
    try {
      if (!currentAgreement.value?.id) return true
      // 期望金额：房租用月租，其余用输入金额
      const expected = currentPayType.value === 1
        ? Number(currentAgreement.value.rent)
        : parseFloat(customAmount.value)
      if (currentPayType.value !== 1 && (!expected || expected <= 0)) {
        showFailToast('请输入有效金额')
        return false
      }
      await http.post(
        `/app/paymentRecord/createBill?agreementId=${agreementId}&billType=${currentPayType.value}&amount=${expected}`
      )
      await loadBills()
      // 优先支付刚创建的那笔账单（同类型+同金额+时间最新），
      // 而不是 pending 列表的第一条（可能是历史旧账单）
      const target = bills.pending
        .filter((b: any) =>
          b.billType === currentPayType.value &&
          Math.abs(Number(b.amount) - expected) < 0.01
        )
        .sort((a: any, b: any) =>
          String(b.createTime || '').localeCompare(String(a.createTime || ''))
        )[0]
      const billToPay = target || bills.pending[0]
      if (billToPay) {
        await handlePay(billToPay.id)
      }
      showSuccessToast('支付成功')
    } catch {
      showFailToast('支付失败')
    }
  }
  return true
}

onMounted(async () => {
  await loadAgreement()
  await loadBills()
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

.my-room-detail {
  min-height: calc(100vh - 46px);
  background: #f5f7fa;
  padding-bottom: 20px;
  transition: background 0.3s ease;

  &.dark {
    background: #1a1a1a;
  }
}

// ========== 房间头部 ==========
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
  border-radius: 0 0 28px 28px;
}

.room-header-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 28px;
  animation: fade-up 0.45s cubic-bezier(0.22, 0.61, 0.36, 1) both;
}

.room-title {
  font-size: 20px;
  font-weight: 700;
  color: #fff;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.room-subtitle {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 4px;
}

// ========== 主体内容：卡片逐级入场 ==========
.room-body {
  padding: 0 16px;

  > * {
    animation: fade-up 0.45s cubic-bezier(0.22, 0.61, 0.36, 1) both;
  }

  > :nth-child(1) { animation-delay: 0.04s; }
  > :nth-child(2) { animation-delay: 0.1s; }
  > :nth-child(3) { animation-delay: 0.16s; }
  > :nth-child(4) { animation-delay: 0.22s; }
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

// ========== 生活服务 ==========
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
  transition: transform 0.15s ease;

  &:active {
    transform: scale(0.88);
  }
}

.service-icon {
  width: 50px;
  height: 50px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

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

// ========== 账单 ==========
.bill-section {
  margin-bottom: 16px;
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
}

.bill-item {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 12px;
  padding: 12px 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.02);
  transition: transform 0.15s ease;

  &:active {
    transform: scale(0.985);
  }

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

// ========== 支付弹窗 ==========
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
</style>
