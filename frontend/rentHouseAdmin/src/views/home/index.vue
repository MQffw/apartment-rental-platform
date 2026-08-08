<template>
  <div class="home container">
    <el-card shadow="hover">
      <div class="page-header">
        <el-avatar :size="60" :src="userInfo.avatarUrl || logoPng" />
        <div class="page-header-tip">
          <p class="page-header-tip-title">
            {{ timeFix() }}{{ userInfo?.name }}，{{ welcome() }}
          </p>
          <p class="page-header-tip-desc">
            {{ getEnvByName('VITE_APP_TITLE') }}
          </p>
        </div>
      </div>
    </el-card>

    <!-- 数据卡片 -->
    <el-row class="mt-[16px] stat-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <el-statistic title="公寓总数" :value="stats.apartmentCount || 0">
            <template #prefix><el-icon><OfficeBuilding /></el-icon></template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <el-statistic title="房间总数" :value="stats.roomCount || 0">
            <template #prefix><el-icon><House /></el-icon></template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <el-statistic title="注册用户" :value="stats.userCount || 0">
            <template #prefix><el-icon><User /></el-icon></template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <el-statistic title="空闲房间" :value="stats.freeRoomCount || 0">
            <template #prefix><el-icon><Key /></el-icon></template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域第一行：仪表盘 + 房间状态饼图 + 预约状态饼图 -->
    <el-row class="mt-[16px] chart-row" :gutter="16">
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header><span>入住率</span></template>
          <div ref="gaugeChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="9">
        <el-card shadow="hover">
          <template #header><span>房间状态分布</span></template>
          <div ref="pieChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="9">
        <el-card shadow="hover">
          <template #header><span>预约状态分布</span></template>
          <div ref="appointmentChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域第二行：月度趋势折线图 -->
    <el-row class="mt-[16px]" :gutter="16">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header><span>近12个月租约趋势</span></template>
          <div ref="trendChartRef" class="chart-container-wide"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域第三行：租约状态详情 -->
    <el-row class="mt-[16px]" :gutter="16">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header><span>租约状态详情</span></template>
          <div ref="leaseBarChartRef" class="chart-container-wide"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域第四行：各公寓房间数量（横向柱状图+滚动） -->
    <el-row class="mt-[16px]" :gutter="16">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header><span>各公寓房间数量</span></template>
          <div ref="barChartRef" class="chart-container-tall"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { useUserStore } from '@/store/modules/user'
import { timeFix, welcome } from '@/utils/index'
import { getEnvByName } from '@/utils/getEnv'
import logoPng from '@/assets/images/logo.png'
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import http from '@/utils/http'
import * as echarts from 'echarts'
import { OfficeBuilding, House, User, Key } from '@element-plus/icons-vue'

const userStore = useUserStore()
const userInfo = userStore.userInfo

const stats = ref<Record<string, any>>({})

// 图表 refs
const gaugeChartRef = ref<HTMLElement>()
const pieChartRef = ref<HTMLElement>()
const appointmentChartRef = ref<HTMLElement>()
const trendChartRef = ref<HTMLElement>()
const barChartRef = ref<HTMLElement>()
const leaseBarChartRef = ref<HTMLElement>()

// 图表实例
let gaugeChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null
let appointmentChart: echarts.ECharts | null = null
let trendChart: echarts.ECharts | null = null
let barChart: echarts.ECharts | null = null
let leaseBarChart: echarts.ECharts | null = null

async function fetchStats() {
  try {
    const { data } = await http.get('/admin/dashboard/statistics')
    stats.value = data
    await nextTick()
    renderCharts(data)
  } catch (e) {
    console.error('获取看板数据失败', e)
  }
}

function renderCharts(data: Record<string, any>) {
  // 1. 入住率仪表盘
  if (gaugeChartRef.value) {
    gaugeChart = echarts.init(gaugeChartRef.value)
    gaugeChart.setOption({
      series: [{
        type: 'gauge',
        startAngle: 200,
        endAngle: -20,
        min: 0,
        max: 100,
        progress: { show: true, width: 18 },
        axisLine: { lineStyle: { width: 18 } },
        axisTick: { show: false },
        splitLine: { show: false },
        axisLabel: { show: false },
        pointer: { show: false },
        title: { show: true, offsetCenter: [0, '70%'], fontSize: 14 },
        detail: {
          valueAnimation: true,
          fontSize: 32,
          offsetCenter: [0, '40%'],
          formatter: '{value}%',
          color: 'inherit'
        },
        data: [{ value: data.occupancyRate || 0, name: '入住率' }]
      }]
    })
  }

  // 2. 房间状态分布饼图
  if (pieChartRef.value && data.roomStatusList) {
    pieChart = echarts.init(pieChartRef.value)
    pieChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { bottom: 0 },
      color: ['#67C23A', '#E6A23C', '#909399'],
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
        label: { show: false },
        emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
        labelLine: { show: false },
        data: data.roomStatusList
      }]
    })
  }

  // 3. 预约状态分布饼图
  if (appointmentChartRef.value && data.appointmentStatusList) {
    appointmentChart = echarts.init(appointmentChartRef.value)
    appointmentChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { bottom: 0 },
      color: ['#409EFF', '#F56C6C', '#67C23A'],
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
        label: { show: false },
        emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
        labelLine: { show: false },
        data: data.appointmentStatusList
      }]
    })
  }

  // 4. 近12个月租约趋势折线图
  if (trendChartRef.value && data.monthlyTrendList) {
    trendChart = echarts.init(trendChartRef.value)
    const months = data.monthlyTrendList.map((item: any) => item.month)
    const newCounts = data.monthlyTrendList.map((item: any) => item.newCount)
    const endedCounts = data.monthlyTrendList.map((item: any) => item.endedCount)
    trendChart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['新增租约', '到期/退租'], bottom: 0 },
      grid: { left: '3%', right: '4%', bottom: '15%', top: '10%', containLabel: true },
      xAxis: { type: 'category', data: months, boundaryGap: false },
      yAxis: { type: 'value', minInterval: 1 },
      series: [
        {
          name: '新增租约',
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 6,
          data: newCounts,
          itemStyle: { color: '#67C23A' },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(103,194,58,0.3)' },
              { offset: 1, color: 'rgba(103,194,58,0.05)' }
            ])
          }
        },
        {
          name: '到期/退租',
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 6,
          data: endedCounts,
          itemStyle: { color: '#F56C6C' },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(245,108,108,0.3)' },
              { offset: 1, color: 'rgba(245,108,108,0.05)' }
            ])
          }
        }
      ]
    })
  }

  // 5. 租约状态详情柱状图
  if (leaseBarChartRef.value && data.leaseStatusMap) {
    leaseBarChart = echarts.init(leaseBarChartRef.value)
    const statusNames = Object.keys(data.leaseStatusMap)
    const statusValues = Object.values(data.leaseStatusMap) as number[]
    const colors = ['#409EFF', '#67C23A', '#F56C6C', '#E6A23C', '#909399', '#b37feb', '#36cfc9']
    leaseBarChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: statusNames },
      yAxis: { type: 'value', minInterval: 1 },
      series: [{
        type: 'bar',
        data: statusValues.map((val, idx) => ({
          value: val,
          itemStyle: { color: colors[idx % colors.length] }
        })),
        barWidth: '50%',
        itemStyle: { borderRadius: [4, 4, 0, 0] },
        label: { show: true, position: 'top', formatter: '{c}' }
      }]
    })
  }

  // 6. 各公寓房间数量横向柱状图（带 dataZoom 滚动）
  if (barChartRef.value && data.apartmentRoomList) {
    barChart = echarts.init(barChartRef.value)
    const names = data.apartmentRoomList.map((item: any) => item.name).reverse()
    const values = data.apartmentRoomList.map((item: any) => item.totalRooms).reverse()
    const totalItems = names.length
    const showCount = Math.min(15, totalItems)
    barChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: '3%', right: '12%', bottom: '3%', top: '3%', containLabel: true },
      xAxis: { type: 'value', minInterval: 1 },
      yAxis: {
        type: 'category',
        data: names,
        axisLabel: {
          width: 120,
          overflow: 'truncate',
          ellipsis: '...'
        }
      },
      dataZoom: totalItems > showCount ? [
        {
          type: 'slider',
          yAxisIndex: 0,
          startValue: 0,
          endValue: showCount - 1,
          width: 20,
          right: 10,
          showDataShadow: true,
          borderColor: '#ddd',
          fillerColor: 'rgba(64,158,255,0.15)',
          handleStyle: { color: '#409EFF' }
        },
        {
          type: 'inside',
          yAxisIndex: 0,
          startValue: 0,
          endValue: showCount - 1
        }
      ] : [],
      series: [{
        type: 'bar',
        data: values,
        barWidth: '60%',
        itemStyle: {
          borderRadius: [0, 4, 4, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#79bbff' },
            { offset: 1, color: '#409EFF' }
          ])
        },
        label: { show: true, position: 'right', formatter: '{c}' }
      }]
    })
  }
}

function handleResize() {
  gaugeChart?.resize()
  pieChart?.resize()
  appointmentChart?.resize()
  trendChart?.resize()
  barChart?.resize()
  leaseBarChart?.resize()
}

onMounted(() => {
  fetchStats()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  gaugeChart?.dispose()
  pieChart?.dispose()
  appointmentChart?.dispose()
  trendChart?.dispose()
  barChart?.dispose()
  leaseBarChart?.dispose()
})
</script>

<style scoped lang="scss">
.home {
  height: 100%;

  .page-header {
    display: flex;
    align-items: center;

    .page-header-tip {
      flex: 1;
      margin-left: 20px;
    }

    .page-header-tip-title {
      margin-bottom: 12px;
      font-size: 20px;
      font-weight: 700;
      color: #3c4a54;
    }

    .page-header-tip-desc {
      min-height: 20px;
      font-size: 14px;
      color: #808695;
    }
  }

  // 去掉行内列之间的间距（仅 stat-row 紧密拼接）
  .stat-row {
    .el-col {
      padding: 0 !important;
    }

    .el-card {
      margin: 0;
      border-radius: 0;
    }

    // 第一列左边圆角
    .el-col:first-child .el-card {
      border-radius: 4px 0 0 4px;
    }

    // 最后一列右边圆角
    .el-col:last-child .el-card {
      border-radius: 0 4px 4px 0;
    }
  }

  .stat-card {
    text-align: center;
  }

  .chart-container {
    height: 280px;
    width: 100%;
  }

  .chart-container-wide {
    height: 320px;
    width: 100%;
  }

  .chart-container-tall {
    height: 500px;
    width: 100%;
  }
}
</style>
