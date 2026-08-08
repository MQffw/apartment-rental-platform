<template>
  <el-upload
    v-bind="$attrs"
    :action="UPLOAD_IMG_URL"
    :on-preview="handlePictureCardPreview"
    :on-error="handleError"
    :headers="{ 'access-token': useUserStore().token }"
    :before-upload="beforeUpload"
  >
    <el-icon>
      <Plus />
    </el-icon>
  </el-upload>

  <el-dialog v-model="dialogVisible">
    <el-image :src="dialogImageUrl" fit="fill" />
  </el-dialog>
</template>
<script setup lang="ts">
import { UPLOAD_IMG_URL } from '@/api/upload'
import { ref } from 'vue'
import { UploadFile, UploadRawFile } from 'element-plus/es/components/upload/src/upload'
import { useUserStore } from '@/store/modules/user'
import { ElMessage } from 'element-plus'

const dialogImageUrl = ref('')
const dialogVisible = ref(false)

function beforeUpload(file: UploadRawFile) {
  const allowed = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp']
  if (!allowed.includes(file.type)) {
    ElMessage.error('仅支持 jpg/png/gif/webp 格式的图片')
    return false
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过 5MB')
    return false
  }
  return true
}

function handleError() {
  ElMessage.error('上传失败，请检查网络或服务端')
}

function handlePictureCardPreview(uploadFile: UploadFile) {
  dialogImageUrl.value = uploadFile.url!
  dialogVisible.value = true
}
</script>

<style scoped lang="scss"></style>
