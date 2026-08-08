<template>
  <div>
    <!-- 统计卡片 -->
    <el-row :gutter="16" style="margin-bottom: 16px;">
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="总帖子数" :value="stats.totalPosts || 0" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="正常帖子" :value="stats.normalPosts || 0" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="总评论数" :value="stats.totalComments || 0" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="今日新增" :value="stats.todayPosts || 0" />
        </el-card>
      </el-col>
    </el-row>

    <el-card>
      <ProTable
        ref="proTable"
        :columns="columns"
        :requestApi="getPostPage"
        :dataCallback="dataCallback"
        :stripe="true"
        :searchCol="3"
      >
        <template #tableHeader>
          <span style="font-weight: 600; font-size: 16px;">帖子管理</span>
        </template>
        <template #operation="scope">
          <el-popconfirm v-if="scope.row.status === 1" title="确定屏蔽该帖子？" @confirm="handleStatus(scope.row, 3)">
            <template #reference>
              <el-button type="warning" icon="View">屏蔽</el-button>
            </template>
          </el-popconfirm>
          <el-popconfirm v-if="scope.row.status === 3" title="确定恢复该帖子？" @confirm="handleStatus(scope.row, 1)">
            <template #reference>
              <el-button type="success" icon="RefreshRight">恢复</el-button>
            </template>
          </el-popconfirm>
          <el-popconfirm title="确定删除该帖子？" @confirm="handleDelete(scope.row.id)">
            <template #reference>
              <el-button type="danger" icon="Delete">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </ProTable>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import ProTable from '@/components/ProTable/src/ProTable.vue'
import type { ColumnProps } from '@/components/ProTable/src/types'
import { getPostPage, updatePostStatus, deletePost, getPostStatistics } from '@/api/community'
import { ElMessage, ElMessageBox, ElTag } from 'element-plus'

const proTable = ref()
const stats = ref<Record<string, number>>({})

const statusMap: Record<number, { label: string; type: string }> = {
  1: { label: '正常', type: 'success' },
  2: { label: '审核中', type: 'warning' },
  3: { label: '已屏蔽', type: 'danger' }
}

const columns: ColumnProps[] = [
  { prop: 'id', label: 'ID', width: 80 },
  { prop: 'userId', label: '用户ID', width: 80 },
  { prop: 'apartmentId', label: '关联公寓', width: 100 },
  { prop: 'title', label: '标题', width: 200 },
  { prop: 'rating', label: '评分', width: 70 },
  { prop: 'likeCount', label: '点赞', width: 70 },
  { prop: 'commentCount', label: '评论', width: 70 },
  {
    prop: 'status',
    label: '状态',
    width: 100,
    search: { el: 'select' },
    render: ({ row }: any) => {
      const s = statusMap[row.status] || { label: '未知', type: 'info' }
      return h(ElTag, { type: s.type as any }, () => s.label)
    }
  },
  { prop: 'createTime', label: '创建时间', width: 170 },
  { prop: 'operation', label: '操作', width: 260, fixed: 'right' }
]

const dataCallback = (data: any) => ({
  list: data?.records || [],
  total: data?.total || 0
})

async function handleStatus(row: any, status: number) {
  try {
    await updatePostStatus(row.id, status)
    ElMessage.success('操作成功')
    proTable.value?.getTableList()
  } catch {
    ElMessage.error('操作失败')
  }
}

async function handleDelete(id: number) {
  try {
    await deletePost(id)
    ElMessage.success('删除成功')
    proTable.value?.getTableList()
  } catch {
    ElMessage.error('删除失败')
  }
}

async function fetchStats() {
  try {
    const { data } = await getPostStatistics()
    stats.value = data || {}
  } catch {}
}

onMounted(fetchStats)
</script>
