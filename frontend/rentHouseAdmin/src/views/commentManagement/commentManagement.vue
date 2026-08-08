<template>
  <el-card>
    <ProTable
      ref="proTable"
      :columns="columns"
      :requestApi="getCommentPage"
      :dataCallback="dataCallback"
      :stripe="true"
      :searchCol="3"
    >
      <template #tableHeader>
        <span style="font-weight: 600; font-size: 16px;">评论管理</span>
      </template>
      <template #operation="scope">
        <el-popconfirm title="确定删除该评论？" @confirm="handleDelete(scope.row.id)">
          <template #reference>
            <el-button type="danger" icon="Delete">删除</el-button>
          </template>
        </el-popconfirm>
      </template>
    </ProTable>
  </el-card>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import ProTable from '@/components/ProTable/src/ProTable.vue'
import type { ColumnProps } from '@/components/ProTable/src/types'
import { getCommentPage, deleteComment } from '@/api/community'
import { ElMessage } from 'element-plus'

const proTable = ref()

const columns: ColumnProps[] = [
  { prop: 'id', label: 'ID', width: 80 },
  { prop: 'postId', label: '帖子ID', width: 80, search: { el: 'input' } },
  { prop: 'userId', label: '评论人ID', width: 90 },
  { prop: 'content', label: '评论内容' },
  { prop: 'parentId', label: '父评论ID', width: 90 },
  { prop: 'likeCount', label: '点赞', width: 70 },
  { prop: 'createTime', label: '评论时间', width: 170 },
  { prop: 'operation', label: '操作', width: 120, fixed: 'right' }
]

const dataCallback = (data: any) => ({
  list: data?.records || [],
  total: data?.total || 0
})

async function handleDelete(id: number) {
  try {
    await deleteComment(id)
    ElMessage.success('删除成功')
    proTable.value?.getTableList()
  } catch {
    ElMessage.error('删除失败')
  }
}
</script>
