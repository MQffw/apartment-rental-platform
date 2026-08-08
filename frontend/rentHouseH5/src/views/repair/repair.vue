<template>
  <div class="repair-page" :class="{ dark: isDark }">

    <!-- 报修表单 -->
    <div class="form-body">
      <div class="form-section">
        <div class="form-label">报修标题</div>
        <van-field v-model="form.title" placeholder="请简要描述问题" maxlength="50" />
      </div>

      <div class="form-section">
        <div class="form-label">问题描述</div>
        <van-field
          v-model="form.description"
          type="textarea"
          placeholder="详细描述您遇到的问题..."
          rows="4"
          maxlength="300"
          show-word-limit
          autosize
        />
      </div>

      <div class="form-section">
        <div class="form-label">上传图片（可选）</div>
        <van-uploader v-model="imageList" :max-count="3" :max-size="5 * 1024 * 1024" />
      </div>
    </div>

    <div class="submit-wrapper">
      <van-button
        type="primary"
        block
        round
        :loading="submitting"
        :disabled="!form.title.trim()"
        @click="handleSubmit"
        class="submit-btn"
      >
        提交报修
      </van-button>
    </div>

    <!-- 历史工单 -->
    <div class="history-section">
      <div class="section-title">报修记录</div>
      <div v-if="repairList.length === 0" class="empty-text">暂无报修记录</div>
      <div v-for="item in repairList" :key="item.id" class="repair-item">
        <div class="repair-header">
          <span class="repair-title">{{ item.title }}</span>
          <van-tag :type="statusTag(item.status)" size="medium">{{ statusText(item.status) }}</van-tag>
        </div>
        <div class="repair-desc">{{ item.description }}</div>
        <div class="repair-time">{{ item.createTime }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts" name="Repair">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useDarkMode } from '@/hooks/useToggleDarkMode'
import { submitRepair, getRepairList } from '@/api/repair'
import { showSuccessToast, showFailToast } from 'vant'

const router = useRouter()
const isDark = computed(() => useDarkMode())
const submitting = ref(false)

const form = ref({ title: '', description: '' })
const imageList = ref<any[]>([])
const repairList = ref<any[]>([])

function statusText(s: number) {
  return ['待处理', '处理中', '已完成', '已关闭'][s] || '未知'
}

function statusTag(s: number): string {
  return ['warning', 'primary', 'success', 'default'][s] || 'default'
}

async function handleSubmit() {
  if (!form.value.title.trim()) return
  submitting.value = true
  try {
    const images = imageList.value.filter((f: any) => f.url).map((f: any) => f.url).join(',')
    await submitRepair({
      title: form.value.title,
      description: form.value.description,
      images: images || undefined
    })
    showSuccessToast('提交成功')
    form.value = { title: '', description: '' }
    imageList.value = []
    await fetchList()
  } catch {
    showFailToast('提交失败')
  } finally {
    submitting.value = false
  }
}

async function fetchList() {
  try {
    const { data } = await getRepairList()
    repairList.value = data || []
  } catch {}
}

onMounted(fetchList)
</script>

<style scoped lang="less">
.repair-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 20px;

  &.dark { background: #1a1a1a; }
}

.form-body { padding: 16px; }
.form-section { margin-bottom: 16px; }

.form-label {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
  .dark & { color: #e0e0e0; }
}

.submit-wrapper { padding: 0 16px 20px; }

.submit-btn {
  height: 44px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #277941, #34c759) !important;
  border: none !important;
}

.history-section {
  padding: 0 16px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
  .dark & { color: #e0e0e0; }
}

.empty-text {
  text-align: center;
  color: #ccc;
  padding: 30px 0;
  .dark & { color: #555; }
}

.repair-item {
  background: #fff;
  border-radius: 12px;
  padding: 14px;
  margin-bottom: 10px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.03);

  .dark & { background: #2c2c2c; }
}

.repair-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.repair-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  .dark & { color: #e0e0e0; }
}

.repair-desc {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  .dark & { color: #aaa; }
}

.repair-time {
  font-size: 11px;
  color: #999;
  margin-top: 6px;
}
</style>
