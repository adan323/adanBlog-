<template>
  <div>
    <h2 class="page-title">数据看板</h2>

    <!-- 文章统计 -->
    <div class="stat-grid">
      <div class="stat-card">
        <div class="stat-icon blue"><el-icon :size="22"><Document /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.total || 0 }}</div>
          <div class="stat-label">文章总数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon green"><el-icon :size="22"><CircleCheck /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.published || 0 }}</div>
          <div class="stat-label">已发布</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon orange"><el-icon :size="22"><View /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.views || 0 }}</div>
          <div class="stat-label">总阅读量</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon purple"><el-icon :size="22"><CollectionTag /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.tags || 0 }}</div>
          <div class="stat-label">标签数</div>
        </div>
      </div>
    </div>

    <!-- 访问概览 -->
    <div class="stat-grid traffic-grid">
      <div class="stat-card">
        <div class="stat-icon red"><el-icon :size="22"><TrendCharts /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ traffic.overview?.today || 0 }}</div>
          <div class="stat-label">今日访问</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon cyan"><el-icon :size="22"><Calendar /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ traffic.overview?.week || 0 }}</div>
          <div class="stat-label">近7日访问</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon indigo"><el-icon :size="22"><DataAnalysis /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ traffic.overview?.total || 0 }}</div>
          <div class="stat-label">累计访问</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon teal"><el-icon :size="22"><Timer /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ trafficDays }}天</div>
          <div class="stat-label">统计周期</div>
        </div>
      </div>
    </div>

    <!-- 日浏览量折线图 -->
    <el-card class="chart-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>日浏览量趋势</span>
          <el-radio-group v-model="trafficDays" size="small" @change="loadTraffic">
            <el-radio-button :value="7">7天</el-radio-button>
            <el-radio-button :value="14">14天</el-radio-button>
            <el-radio-button :value="30">30天</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <div ref="dailyChartRef" class="chart chart-lg"></div>
    </el-card>

    <div class="chart-row">
      <!-- IP 归属分布 -->
      <el-card class="chart-card" shadow="never">
        <template #header><span class="card-header-title">IP 归属地分布</span></template>
        <div ref="locationChartRef" class="chart"></div>
      </el-card>

      <!-- 浏览器分布 -->
      <el-card class="chart-card" shadow="never">
        <template #header><span class="card-header-title">浏览器分布</span></template>
        <div ref="browserChartRef" class="chart"></div>
      </el-card>
    </div>

    <div class="chart-row">
      <!-- 客户端平台 -->
      <el-card class="chart-card" shadow="never">
        <template #header><span class="card-header-title">客户端平台</span></template>
        <div ref="platformChartRef" class="chart"></div>
      </el-card>

      <!-- 快捷操作 -->
      <el-card class="chart-card" shadow="never">
        <template #header><span class="card-header-title">快捷操作</span></template>
        <div class="quick-actions">
          <el-button type="primary" @click="$router.push('/articles/new')">
            <el-icon style="margin-right:4px"><EditPen /></el-icon>写新文章
          </el-button>
          <el-button @click="$router.push('/articles')">
            <el-icon style="margin-right:4px"><Document /></el-icon>管理文章
          </el-button>
          <el-button @click="$router.push('/settings')">
            <el-icon style="margin-right:4px"><Setting /></el-icon>站点设置
          </el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
// echarts 按需引入，减小打包体积（1GB 服务器构建会 OOM）
import * as echarts from 'echarts/core'
import { LineChart, BarChart, PieChart } from 'echarts/charts'
import {
  TitleComponent, TooltipComponent, GridComponent, LegendComponent,
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { LabelLayout } from 'echarts/features'
echarts.use([
  LineChart, BarChart, PieChart,
  TitleComponent, TooltipComponent, GridComponent, LegendComponent,
  CanvasRenderer, LabelLayout,
])
import { adminApi } from '../api'

const stats = ref({})
const traffic = ref({})
const trafficDays = ref(7)

const dailyChartRef = ref(null)
const locationChartRef = ref(null)
const browserChartRef = ref(null)
const platformChartRef = ref(null)

let charts = []

/** 图表通用配色 */
const COLORS = ['#3b82f6', '#10b981', '#f59e0b', '#8b5cf6', '#ef4444', '#06b6d4', '#ec4899', '#84cc16', '#f97316', '#64748b']

function initChart(el, option) {
  if (!el) return null
  const chart = echarts.init(el)
  chart.setOption(option)
  charts.push(chart)
  return chart
}

function renderCharts() {
  // 销毁旧图表
  charts.forEach((c) => c.dispose())
  charts = []
  nextTick(() => {
    // 日浏览量折线
    const daily = traffic.value.daily || []
    initChart(dailyChartRef.value, {
      tooltip: { trigger: 'axis' },
      grid: { left: 40, right: 20, top: 30, bottom: 30 },
      xAxis: { type: 'category', data: daily.map((d) => d.date.slice(5)), boundaryGap: false },
      yAxis: { type: 'value', minInterval: 1 },
      series: [{
        name: '访问量',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 7,
        data: daily.map((d) => d.pv),
        lineStyle: { width: 3, color: '#3b82f6' },
        itemStyle: { color: '#3b82f6' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(59,130,246,.35)' },
            { offset: 1, color: 'rgba(59,130,246,.02)' },
          ]),
        },
      }],
    })

    // IP 归属横向条形图
    const locs = traffic.value.locations || []
    const locNames = locs.map((l) => l.name).reverse()
    const locVals = locs.map((l) => l.value).reverse()
    initChart(locationChartRef.value, {
      tooltip: { trigger: 'axis' },
      grid: { left: 10, right: 30, top: 10, bottom: 10, containLabel: true },
      xAxis: { type: 'value', minInterval: 1 },
      yAxis: { type: 'category', data: locNames },
      series: [{
        type: 'bar',
        data: locVals,
        barWidth: 16,
        itemStyle: {
          borderRadius: [0, 8, 8, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#6366f1' },
            { offset: 1, color: '#8b5cf6' },
          ]),
        },
        label: { show: true, position: 'right' },
      }],
    })

    // 浏览器饼图
    initChart(browserChartRef.value, {
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { bottom: 0, type: 'scroll' },
      series: [{
        type: 'pie',
        radius: ['38%', '65%'],
        center: ['50%', '45%'],
        data: (traffic.value.browsers || []).map((b, i) => ({ name: b.name, value: b.value, itemStyle: { color: COLORS[i % COLORS.length] } })),
        label: { formatter: '{b}\n{d}%' },
      }],
    })

    // 平台饼图
    initChart(platformChartRef.value, {
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { bottom: 0, type: 'scroll' },
      series: [{
        type: 'pie',
        radius: ['38%', '65%'],
        center: ['50%', '45%'],
        data: (traffic.value.platforms || []).map((p, i) => ({ name: p.name, value: p.value, itemStyle: { color: COLORS[(i + 3) % COLORS.length] } })),
        label: { formatter: '{b}\n{d}%' },
      }],
    })
  })
}

async function loadTraffic() {
  try {
    traffic.value = await adminApi.getTraffic(trafficDays.value)
    renderCharts()
  } catch (e) {
    console.error(e)
  }
}

function handleResize() {
  charts.forEach((c) => c.resize())
}

onMounted(async () => {
  try {
    stats.value = await adminApi.getStats()
  } catch (e) {
    console.error(e)
  }
  await loadTraffic()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  charts.forEach((c) => c.dispose())
  charts = []
})
</script>

<style scoped>
.page-title { font-size: 20px; font-weight: 600; color: #0f172a; margin: 0 0 20px; }
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}
.traffic-grid { margin-top: 0; }
.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  border: 1px solid #eef2f7;
  transition: box-shadow .2s, transform .2s;
}
.stat-card:hover {
  box-shadow: 0 8px 24px rgba(15,23,42,.08);
  transform: translateY(-2px);
}
.stat-icon {
  width: 46px;
  height: 46px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}
.stat-icon.blue { background: linear-gradient(135deg, #3b82f6, #2563eb); }
.stat-icon.green { background: linear-gradient(135deg, #10b981, #059669); }
.stat-icon.orange { background: linear-gradient(135deg, #f59e0b, #d97706); }
.stat-icon.purple { background: linear-gradient(135deg, #8b5cf6, #6d28d9); }
.stat-icon.red { background: linear-gradient(135deg, #ef4444, #dc2626); }
.stat-icon.cyan { background: linear-gradient(135deg, #06b6d4, #0891b2); }
.stat-icon.indigo { background: linear-gradient(135deg, #6366f1, #4f46e5); }
.stat-icon.teal { background: linear-gradient(135deg, #14b8a6, #0d9488); }
.stat-value { font-size: 24px; font-weight: 700; color: #0f172a; line-height: 1.2; }
.stat-label { font-size: 13px; color: #94a3b8; margin-top: 2px; }
.chart-card { border-radius: 12px; margin-bottom: 16px; }
.chart-card .card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
  color: #0f172a;
}
.card-header-title { font-weight: 600; color: #0f172a; }
.chart { width: 100%; height: 300px; }
.chart-lg { height: 320px; }
.chart-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.chart-row .chart-card { margin-bottom: 0; }
.quick-actions { display: flex; flex-direction: column; gap: 12px; align-items: flex-start; padding-top: 8px; }
@media (max-width: 1200px) {
  .chart-row { grid-template-columns: 1fr; }
  .chart-row .chart-card { margin-bottom: 16px; }
}
@media (max-width: 900px) {
  .stat-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
