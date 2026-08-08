<template>
  <el-card>
    <template #header>
      <span style="font-weight: 600; font-size: 16px;">发送系统公告</span>
    </template>

    <el-form :model="form" label-width="100px" style="max-width: 600px;">
      <el-form-item label="发送对象">
        <el-radio-group v-model="form.targetType">
          <el-radio label="all">全部用户</el-radio>
          <el-radio label="user">指定用户</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item v-if="form.targetType === 'user'" label="用户ID">
        <el-input v-model="form.userIdStr" placeholder="请输入用户ID" style="width: 220px;" />
      </el-form-item>

      <el-form-item label="标题">
        <el-input v-model="form.title" placeholder="请输入公告标题" maxlength="50" show-word-limit />
      </el-form-item>

      <el-form-item label="内容">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="5"
          placeholder="请输入公告内容"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" :loading="sending" @click="handleSend">
          发送公告
        </el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { sendAnnouncement } from '@/api/community'
import { ElMessage, ElMessageBox } from 'element-plus'

const sending = ref(false)
const form = ref({
  targetType: 'all',
  userIdStr: '',
  title: '',
  content: ''
})

async function handleSend() {
  if (!form.value.title.trim()) {
    ElMessage.warning('请输入标题')
    return
  }
  if (!form.value.content.trim()) {
    ElMessage.warning('请输入内容')
    return
  }

  if (form.value.targetType === 'user' && !form.value.userIdStr.trim()) {
    ElMessage.warning('请输入用户ID')
    return
  }

  const target = form.value.targetType === 'all' ? 'all' : form.value.userIdStr.trim()

  try {
    await ElMessageBox.confirm(
      form.value.targetType === 'all'
        ? '确定要向所有用户发送此公告吗？'
        : `确定要向用户 ${form.value.userIdStr} 发送此公告吗？`,
      '确认发送',
      { type: 'warning' }
    )
  } catch {
    return
  }

  sending.value = true
  try {
    await sendAnnouncement({
      title: form.value.title,
      content: form.value.content,
      target
    })
    ElMessage.success('发送成功')
    form.value.title = ''
    form.value.content = ''
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || e?.message || '发送失败')
  } finally {
    sending.value = false
  }
}
</script>
