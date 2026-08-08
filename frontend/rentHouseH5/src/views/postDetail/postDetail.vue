<template>
  <div class="post-detail-page" :class="{ dark: isDark }">

    <div v-if="post" class="detail-body">
      <!-- 作者信息 -->
      <div class="author-row">
        <van-image
          round width="40px" height="40px"
          :src="post.avatarUrl || defaultAvatar"
          class="author-avatar"
        />
        <div class="author-info">
          <div class="author-name">{{ post.nickname || '匿名用户' }}</div>
          <div class="author-time">{{ formatTime(post.createTime) }}</div>
        </div>
      </div>

      <!-- 标题和内容 -->
      <h2 class="detail-title">{{ post.title }}</h2>

      <div v-if="post.rating" class="detail-rating">
        <van-rate v-model="post.rating" readonly size="16" color="#f7ba2a" />
        <span class="rating-text">{{ post.rating }}分</span>
      </div>

      <div class="detail-content">{{ post.content }}</div>

      <!-- 图片 -->
      <div v-if="imageList.length" class="detail-images">
        <van-image
          v-for="(img, idx) in imageList"
          :key="idx"
          :src="img"
          fit="cover"
          class="detail-img"
          @click="previewImage(idx)"
        />
      </div>

      <!-- 关联公寓 -->
      <div v-if="post.apartmentId" class="apartment-tag">
        <van-icon name="location-o" />
        <span>关联公寓</span>
      </div>

      <!-- 操作栏 -->
      <div class="action-bar">
        <div class="action-item" @click="handleLike">
          <van-icon :name="isLiked ? 'good-job' : 'good-job-o'" size="20"
            :color="isLiked ? '#e74c3c' : undefined" />
          <span>{{ likeCount }}</span>
        </div>
        <div class="action-item">
          <van-icon name="comment-o" size="20" />
          <span>{{ commentList.length }}</span>
        </div>
      </div>

      <!-- 评论区 -->
      <div class="comment-section">
        <div class="comment-title">评论 ({{ commentList.length }})</div>

        <div v-if="commentList.length === 0" class="empty-comment">
          暂无评论，快来抢沙发吧
        </div>

        <div v-for="comment in commentList" :key="comment.id" class="comment-item">
          <van-image
            round width="32px" height="32px"
            :src="comment.avatarUrl || defaultAvatar"
            class="comment-avatar"
          />
          <div class="comment-body">
            <div class="comment-author">
              {{ comment.nickname || '匿名' }}
              <span v-if="comment.replyToNickname" class="reply-to">
                回复 @{{ comment.replyToNickname }}
              </span>
            </div>
            <div class="comment-text">{{ comment.content }}</div>
            <div class="comment-meta">
              <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
              <span class="comment-reply" @click="setReply(comment)">回复</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部评论输入 -->
    <div class="comment-input-bar">
      <van-field
        v-model="commentText"
        :placeholder="replyTo ? `回复 @${replyTo.nickname}` : '写评论...'"
        class="comment-input"
        @focus="showInput = true"
      />
      <van-button
        type="primary"
        size="small"
        round
        :disabled="!commentText.trim()"
        @click="submitComment"
      >
        发送
      </van-button>
    </div>
  </div>
</template>

<script setup lang="ts" name="PostDetail">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPostDetail, getCommentList, createComment, toggleLike } from '@/api/post'
import { useDarkMode } from '@/hooks/useToggleDarkMode'
import { showToast } from 'vant'
import { showImagePreview } from 'vant'
import defaultAvatar from '@/assets/logo_melomini.png'

const route = useRoute()
const router = useRouter()
const isDark = computed(() => useDarkMode())

const post = ref<any>(null)
const commentList = ref<any[]>([])
const isLiked = ref(false)
const likeCount = ref(0)
const commentText = ref('')
const showInput = ref(false)
const replyTo = ref<any>(null)

const imageList = computed(() => {
  if (!post.value?.images) return []
  try { return JSON.parse(post.value.images) } catch { return [] }
})

function formatTime(t: string) {
  if (!t) return ''
  const d = new Date(t)
  const now = Date.now()
  const diff = now - d.getTime()
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  return d.toLocaleDateString()
}

function previewImage(idx: number) {
  showImagePreview({ images: imageList.value, startPosition: idx })
}

function setReply(comment: any) {
  replyTo.value = comment
}

async function fetchDetail() {
  const id = Number(route.query.id)
  if (!id) return
  try {
    const { data } = await getPostDetail(id)
    post.value = data.post
    isLiked.value = data.isLiked || false
    likeCount.value = data.post?.likeCount || 0
  } catch {}
}

async function fetchComments() {
  const id = Number(route.query.id)
  if (!id) return
  try {
    const { data } = await getCommentList(id)
    commentList.value = data || []
  } catch {}
}

async function handleLike() {
  try {
    const { data } = await toggleLike(post.value.id)
    isLiked.value = data.isLiked
    likeCount.value = data.likeCount
    post.value.likeCount = data.likeCount
  } catch {
    showToast('操作失败')
  }
}

async function submitComment() {
  if (!commentText.value.trim()) return
  try {
    await createComment({
      postId: post.value.id,
      content: commentText.value,
      parentId: replyTo.value?.parentId || replyTo.value?.id || 0,
      replyToId: replyTo.value?.userId || 0
    })
    commentText.value = ''
    replyTo.value = null
    showToast('评论成功')
    await fetchComments()
  } catch {
    showToast('评论失败')
  }
}

onMounted(() => {
  fetchDetail()
  fetchComments()
})
</script>

<style scoped lang="less">
.post-detail-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 60px;

  &.dark { background: #1a1a1a; }
}

.detail-body {
  padding: 16px;
}

.author-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.author-avatar {
  border: 2px solid #f0f0f0;
  .dark & { border-color: #444; }
}

.author-info {
  .author-name {
    font-size: 15px;
    font-weight: 600;
    color: #333;
    .dark & { color: #e0e0e0; }
  }
  .author-time {
    font-size: 12px;
    color: #999;
  }
}

.detail-title {
  font-size: 20px;
  font-weight: 700;
  color: #222;
  margin: 0 0 8px;

  .dark & { color: #f0f0f0; }
}

.detail-rating {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;

  .rating-text {
    font-size: 13px;
    color: #f7ba2a;
    font-weight: 600;
  }
}

.detail-content {
  font-size: 15px;
  color: #444;
  line-height: 1.8;
  margin-bottom: 16px;
  white-space: pre-wrap;

  .dark & { color: #ccc; }
}

.detail-images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.detail-img {
  width: calc(33.33% - 6px);
  height: 120px;
  border-radius: 8px;
}

.apartment-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  background: rgba(39,121,65,0.08);
  color: #277941;
  border-radius: 20px;
  font-size: 12px;
  margin-bottom: 16px;

  .dark & {
    background: rgba(52,199,89,0.12);
    color: #34c759;
  }
}

.action-bar {
  display: flex;
  gap: 24px;
  padding: 12px 0;
  border-top: 1px solid #f0f0f0;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 16px;

  .dark & { border-color: #3a3a3a; }
}

.action-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #666;
  cursor: pointer;

  .dark & { color: #aaa; }
  &:active { opacity: 0.7; }
}

.comment-section {
  .comment-title {
    font-size: 15px;
    font-weight: 600;
    color: #333;
    margin-bottom: 12px;
    .dark & { color: #e0e0e0; }
  }
}

.empty-comment {
  text-align: center;
  color: #ccc;
  padding: 30px 0;
  font-size: 13px;
  .dark & { color: #555; }
}

.comment-item {
  display: flex;
  gap: 10px;
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;

  .dark & { border-bottom-color: #333; }
}

.comment-avatar {
  flex-shrink: 0;
}

.comment-body {
  flex: 1;
}

.comment-author {
  font-size: 13px;
  font-weight: 600;
  color: #333;
  .dark & { color: #e0e0e0; }

  .reply-to {
    font-weight: 400;
    color: #999;
    margin-left: 4px;
  }
}

.comment-text {
  font-size: 14px;
  color: #555;
  line-height: 1.5;
  margin: 4px 0;
  .dark & { color: #bbb; }
}

.comment-meta {
  display: flex;
  gap: 16px;
  font-size: 11px;
  color: #999;

  .comment-reply {
    color: #277941;
    cursor: pointer;
    .dark & { color: #34c759; }
  }
}

.comment-input-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #fff;
  border-top: 1px solid #f0f0f0;
  z-index: 100;

  .dark & {
    background: #2c2c2c;
    border-top-color: #3a3a3a;
  }
}

.comment-input {
  flex: 1;
  padding: 0;

  :deep(.van-field__body) {
    background: #f5f7fa;
    border-radius: 20px;
    padding: 0 12px;
    .dark & { background: #3a3a3a; }
  }
}
</style>
