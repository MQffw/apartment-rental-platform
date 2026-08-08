<template>
  <div class="create-post-page" :class="{ dark: isDark }">

    <div class="form-body">
      <!-- 评分 -->
      <div class="form-section">
        <div class="form-label">评分</div>
        <van-rate v-model="form.rating" size="28" color="#f7ba2a" />
      </div>

      <!-- 关联公寓 -->
      <div class="form-section">
        <div class="form-label">关联公寓（可选）</div>
        <van-field
          v-model="apartmentSearch"
          placeholder="搜索公寓名称"
          clearable
          @search="searchApartment"
        >
          <template #button>
            <van-button size="small" type="primary" @click="searchApartment">搜索</van-button>
          </template>
        </van-field>
        <div v-if="selectedApartment" class="selected-apartment">
          <van-tag type="success" size="large" closeable @close="selectedApartment = null">
            {{ selectedApartment.name }}
          </van-tag>
        </div>
        <div v-if="apartmentList.length > 0 && !selectedApartment" class="apartment-results">
          <div
            v-for="apt in apartmentList"
            :key="apt.id"
            class="apartment-option"
            @click="selectApartment(apt)"
          >
            {{ apt.name }}
          </div>
        </div>
      </div>

      <!-- 标题 -->
      <div class="form-section">
        <div class="form-label">标题</div>
        <van-field
          v-model="form.title"
          placeholder="给评价起个标题"
          maxlength="50"
          show-word-limit
        />
      </div>

      <!-- 正文 -->
      <div class="form-section">
        <div class="form-label">评价内容</div>
        <van-field
          v-model="form.content"
          type="textarea"
          placeholder="分享你的居住体验..."
          rows="5"
          maxlength="500"
          show-word-limit
          autosize
        />
      </div>

      <!-- 图片上传 -->
      <div class="form-section">
        <div class="form-label">图片（可选，最多{{ maxImages }}张）</div>
        <van-uploader
          v-model="imageList"
          :max-count="maxImages"
          :max-size="5 * 1024 * 1024"
          @oversize="onOversize"
        />
      </div>
    </div>

    <!-- 发布按钮 -->
    <div class="submit-wrapper">
      <van-button
        type="primary"
        block
        round
        :loading="submitting"
        :disabled="!canSubmit || submitting"
        @click="handleSubmit"
        class="submit-btn"
      >
        发布评价
      </van-button>
    </div>
  </div>
</template>

<script setup lang="ts" name="CreatePost">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { createPost } from '@/api/post'
import { useDarkMode } from '@/hooks/useToggleDarkMode'
import { showSuccessToast, showFailToast, showToast } from 'vant'
import http from '@/utils/http'

const router = useRouter()
const isDark = computed(() => useDarkMode())
const submitting = ref(false)
const maxImages = 9

const form = ref({
  title: '',
  content: '',
  rating: 0
})

const imageList = ref<any[]>([])
const apartmentSearch = ref('')
const apartmentList = ref<any[]>([])
const selectedApartment = ref<any>(null)

const canSubmit = computed(() => {
  return form.value.title.trim() && form.value.content.trim() && form.value.rating > 0
})

async function searchApartment() {
  if (!apartmentSearch.value.trim()) return
  try {
    const { data } = await http.get('/app/apartment/pageItem', {
      current: 1, size: 5, keyword: apartmentSearch.value
    })
    apartmentList.value = data?.records || []
  } catch {
    apartmentList.value = []
  }
}

function selectApartment(apt: any) {
  selectedApartment.value = apt
  apartmentList.value = []
  apartmentSearch.value = ''
}

function onOversize() {
  showToast('图片大小不能超过5MB')
}

async function handleSubmit() {
  if (submitting.value) return
  if (!canSubmit.value) return
  submitting.value = true
  try {
    const images = imageList.value
      .filter((f: any) => f.url || f.content)
      .map((f: any) => f.url || f.content)

    await createPost({
      title: form.value.title,
      content: form.value.content,
      rating: form.value.rating,
      apartmentId: selectedApartment.value?.id,
      images: images.length ? JSON.stringify(images) : undefined
    })

    showSuccessToast('发布成功')
    router.back()
  } catch {
    showFailToast('发布失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="less">
.create-post-page {
  min-height: 100vh;
  background: #f5f7fa;

  &.dark { background: #1a1a1a; }
}

.form-body {
  padding: 16px;
}

.form-section {
  margin-bottom: 20px;
}

.form-label {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;

  .dark & { color: #e0e0e0; }
}

.selected-apartment {
  margin-top: 8px;
}

.apartment-results {
  margin-top: 8px;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;

  .dark & { background: #2c2c2c; }
}

.apartment-option {
  padding: 10px 12px;
  font-size: 14px;
  color: #333;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;

  .dark & {
    color: #e0e0e0;
    border-bottom-color: #3a3a3a;
  }

  &:active { background: #f5f7fa; }
  &:last-child { border-bottom: none; }
}

.submit-wrapper {
  padding: 16px;
}

.submit-btn {
  height: 44px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #277941, #34c759) !important;
  border: none !important;
}
</style>
