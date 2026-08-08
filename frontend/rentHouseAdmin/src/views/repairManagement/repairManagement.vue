<template>
  <el-card>
    <ProTable
      ref="proTable"
      :columns="columns"
      :requestApi="getRepairPage"
      :dataCallback="dataCallback"
      :stripe="true"
      :searchCol="3"
    >
      <template #tableHeader>
        <span style="font-weight: 600; font-size: 16px;">报修工单管理</span>
      </template>
      <template #operation="scope">
        <el-button
          v-if="scope.row.status === 0"
          type="primary"
          icon="VideoPlay"
          @click="handleStatus(scope.row, 1)"
        >开始处理</el-button>
        <el-button
          v-if="scope.row.status === 1"
          type="success"
          icon="CircleCheck"
          @click="handleStatus(scope.row, 2)"
        >标记完成</el-button>
        <el-button
          v-if="scope.row.status < 3"
          type="info"
          icon="CircleClose"
          @click="handleStatus(scope.row, 3)"
        >关闭</el-button>
      </template>
    </ProTable>
  </el-card>
</template>

<script setup lang="ts">
import { ref, h } from 'vue'
import ProTable from '@/components/ProTable/src/ProTable.vue'
import type { ColumnProps } from '@/components/ProTable/src/types'
import { getRepairPage, updateRepairStatus } from '@/api/community'
import { ElMessage, ElMessageBox, ElTag } from 'element-plus'

const proTable = ref()

const statusMap: Record<number, { label: string; type: string }> = {
  0: { label: '待处理', type: 'danger' },
  1: { label: '处理中', type: 'warning' },
  2: { label: '已完成', type: 'success' },
  3: { label: '已关闭', type: 'info' }
}

const columns: ColumnProps[] = [
  { prop: 'id', label: 'ID', width: 80 },
  { prop: 'userId', label: '用户ID', width: 80 },
  { prop: 'apartmentId', label: '公寓ID', width: 80 },
  { prop: 'title', label: '标题', width: 200 },
  { prop: 'description', label: '描述', isShow: true },
  {
    prop: 'status',
    label: '状态',
    width: 100,
    search: { el: 'select' },
    fieldNames: { label: 'label', value: 'value' },
    render: ({ row }: any) => {
      const s = statusMap[row.status] || { label: '未知', type: 'info' }
      return h(ElTag, { type: s.type as any }, () => s.label)
    }
  },
  { prop: 'createTime', label: '创建时间', width: 170 },
  { prop: 'operation', label: '操作', width: 280, fixed: 'right' }
]

const dataCallback = (data: any) => ({
  list: data?.records || [],
  total: data?.total || 0
})

async function handleStatus(row: any, status: number) {
  const action = statusMap[status]?.label || '操作'
  await ElMessageBox.confirm(`确定要${action}该工单吗？`, '提示', { type: 'warning' })
  try {
    await updateRepairStatus(row.id, status)
    ElMessage.success('操作成功')
    proTable.value?.getTableList()
  } catch {
    ElMessage.error('操作失败')
  }
}
</script>
