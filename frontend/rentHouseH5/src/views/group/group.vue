<template>
  <div class="group-page" :class="{ dark: isDark }">
    <!-- 顶部Tab -->
    <van-tabs v-model:active="activeTab" sticky swipeable @change="onTabChange">
      <van-tab title="最新" name="latest" />
      <van-tab title="热门" name="hot" />
      <van-tab title="好评" name="rating" />
      <van-tab title="差评" name="bad" />
    </van-tabs>

    <!-- 帖子列表 -->
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="loadMore"
      >
        <div class="post-list">
          <div
            v-for="post in postList"
            :key="post.id"
            class="post-card"
            @click="router.push(`/postDetail?id=${post.id}`)"
          >
            <div class="post-header">
              <van-image
                round
                width="36px"
                height="36px"
                :src="post.avatarUrl || defaultAvatar"
                class="post-avatar"
              />
              <div class="post-meta">
                <div class="post-author">{{ post.nickname || '匿名用户' }}</div>
                <div class="post-time">{{ formatTime(post.createTime) }}</div>
              </div>
            </div>

            <div class="post-title">{{ post.title }}</div>

            <!-- 评分 -->
            <div v-if="post.rating" class="post-rating">
              <van-rate v-model="post.rating" readonly size="14" color="#f7ba2a" />
            </div>

            <!-- 正文摘要 -->
            <div class="post-content">{{ truncate(post.content, 80) }}</div>

            <!-- 图片 -->
            <div v-if="post.images && parseImages(post.images).length" class="post-images">
              <van-image
                v-for="(img, idx) in parseImages(post.images).slice(0, 3)"
                :key="idx"
                :src="img"
                fit="cover"
                class="post-thumb"
                :class="{ 'single': parseImages(post.images).length === 1 }"
              />
              <div v-if="parseImages(post.images).length > 3" class="img-more">
                +{{ parseImages(post.images).length - 3 }}
              </div>
            </div>

            <!-- 底部操作 -->
            <div class="post-footer">
              <div class="post-stat" @click.stop="handleLike(post)">
                <van-icon :name="post.isLiked ? 'good-job' : 'good-job-o'" size="16"
                  :color="post.isLiked ? '#e74c3c' : undefined" />
                <span>{{ post.likeCount || 0 }}</span>
              </div>
              <div class="post-stat">
                <van-icon name="comment-o" size="16" />
                <span>{{ post.commentCount || 0 }}</span>
              </div>
            </div>
          </div>
        </div>
      </van-list>
    </van-pull-refresh>

    <!-- 悬浮发帖按钮 -->
    <Transition name="fab">
      <div v-show="!scrolled" class="fab-btn" @click="router.push('/createPost')">
        <van-icon name="plus" size="24" color="#fff" />
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts" name="Group">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPostList, toggleLike } from '@/api/post'
import { useDarkMode } from '@/hooks/useToggleDarkMode'
import defaultAvatar from '@/assets/logo_melomini.png'
import { showToast } from 'vant'

const router = useRouter()
const isDark = computed(() => useDarkMode())

const activeTab = ref('latest')
const postList = ref<any[]>([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const currentPage = ref(1)
const scrolled = ref(false)

function onScroll() {
  scrolled.value = window.scrollY > 300
}

onMounted(() => {
  window.addEventListener('scroll', onScroll, { passive: true })
  onScroll()
})

onUnmounted(() => {
  window.removeEventListener('scroll', onScroll)
})

function formatTime(t: string) {
  if (!t) return ''
  const d = new Date(t)
  const now = Date.now()
  const diff = now - d.getTime()
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
  return d.toLocaleDateString()
}

function truncate(text: string, len: number) {
  if (!text) return ''
  return text.length > len ? text.slice(0, len) + '...' : text
}

function parseImages(images: string): string[] {
  if (!images) return []
  try { return JSON.parse(images) } catch { return [] }
}

async function loadMore() {
  try {
    const { data } = await getPostList({
      current: currentPage.value,
      size: 10,
      sort: activeTab.value === 'latest' ? undefined : activeTab.value
    })
    const records = data?.records || []
    if (currentPage.value === 1) {
      postList.value = records
    } else {
      postList.value.push(...records)
    }
    if (records.length < 10) {
      finished.value = true
    }
    currentPage.value++
  } catch {
    finished.value = true
  } finally {
    loading.value = false
  }
}

function onRefresh() {
  currentPage.value = 1
  finished.value = false
  refreshing.value = false
  loadMore()
}

function onTabChange() {
  currentPage.value = 1
  postList.value = []
  finished.value = false
  loadMore()
}

async function handleLike(post: any) {
  try {
    const res = await toggleLike(post.id)
    post.isLiked = res.data.isLiked
    post.likeCount = res.data.likeCount
  } catch (e: any) {
    console.error('点赞失败:', e)
    showToast(e?.message || '操作失败')
  }
}
</script>

<style scoped lang="less">
.group-page {
  min-height: calc(100vh - 46px);
  background: #f5f7fa;
  padding-bottom: 80px;

  &.dark {
    background: #1a1a1a;
  }
}

.post-list {
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.post-card {
  background: #fff;
  border-radius: 14px;
  padding: 16px;
  box-shadow: 0 1px 8px rgba(0,0,0,0.04);
  cursor: pointer;

  .dark & {
    background: #2c2c2c;
    box-shadow: none;
  }

  &:active { opacity: 0.95; }
}

.post-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.post-avatar {
  border: 2px solid #f0f0f0;
  .dark & { border-color: #444; }
}

.post-meta {
  .post-author {
    font-size: 14px;
    font-weight: 600;
    color: #333;
    .dark & { color: #e0e0e0; }
  }
  .post-time {
    font-size: 11px;
    color: #999;
  }
}

.post-title {
  font-size: 16px;
  font-weight: 700;
  color: #222;
  margin-bottom: 6px;

  .dark & { color: #f0f0f0; }
}

.post-rating {
  margin-bottom: 6px;
}

.post-content {
  font-size: 14px;
  color: #555;
  line-height: 1.6;
  margin-bottom: 10px;

  .dark & { color: #bbb; }
}

.post-images {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
  position: relative;
}

.post-thumb {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  object-fit: cover;

  &.single {
    width: 200px;
    height: 140px;
  }
}

.img-more {
  position: absolute;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  color: #fff;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 8px 0 8px 0;
}

.post-footer {
  display: flex;
  gap: 20px;
  padding-top: 8px;
  border-top: 1px solid #f5f5f5;

  .dark & { border-top-color: #3a3a3a; }
}

.post-stat {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #999;
  cursor: pointer;

  span { color: inherit; }
}

.fab-btn {
  position: fixed;
  right: 16px;
  bottom: 96px;
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: linear-gradient(135deg, #277941, #34c759);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 16px rgba(39,121,65,0.4);
  cursor: pointer;
  z-index: 998;

  &:active { transform: scale(0.9); }
}

.fab-enter-active,
.fab-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.fab-enter-from,
.fab-leave-to {
  opacity: 0;
  transform: scale(0.6) translateY(12px);
}
</style>
