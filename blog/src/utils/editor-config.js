// 博客前台 md-editor-v3 (MdPreview) 扩展资源本地化：默认从 unpkg CDN 动态加载，
// 改为自托管 public/vendor/ 下的静态资源（构建后位于 /vendor/）。
// 启用完整扩展：mermaid 图、katex 公式、echarts 图表、highlight 高亮。
// 支持 echarts 动态数据占位符：文章里写 {{traffic.xxx}}，渲染时替换为实时统计。
import { config } from 'md-editor-v3'

const V = '/vendor/'

// ===== echarts 动态数据（占位符替换） =====
// 约定：文章 echarts 代码块里可用 {{traffic.days}} / {{traffic.views}} /
// {{traffic.today}} / {{traffic.week}} / {{traffic.total}}。
// PostDetail.vue 检测到占位符后会先 fetch /api/public/stats/traffic 写入
// window.__trafficData 再渲染（同步替换，避免异步时序问题）。
const TRAFFIC_KEYS = ['days', 'views', 'today', 'week', 'total']

function resolveTrafficPlaceholder(value) {
  if (typeof value !== 'string') return value
  const m = value.match(/^\{\{traffic\.([a-zA-Z]+)\}\}$/)
  if (!m) return value
  const key = m[1]
  if (!TRAFFIC_KEYS.includes(key)) return value
  const data = window.__trafficData
  if (data && data[key] !== undefined) return data[key]
  // 数据未就绪：返回空占位（正常不会发生，PostDetail 会先预加载）
  if (key === 'days' || key === 'views') return []
  return 0
}

function resolveDynamicData(node) {
  if (Array.isArray(node)) {
    return node.map((n) => resolveDynamicData(n))
  }
  if (node && typeof node === 'object') {
    const out = {}
    for (const k of Object.keys(node)) {
      out[k] = resolveDynamicData(node[k])
    }
    return out
  }
  return resolveTrafficPlaceholder(node)
}

config({
  // echarts 渲染前钩子：把 {{traffic.*}} 占位符替换为实时数据
  echartsConfig: (option) => resolveDynamicData(option),
  editorExtensions: {
    highlight: {
      js: `${V}highlight.min.js`,
      css: {
        atom: { light: `${V}atom-one-light.min.css`, dark: `${V}atom-one-dark.min.css` },
        github: { light: `${V}atom-one-light.min.css`, dark: `${V}atom-one-dark.min.css` },
        a11y: { light: `${V}atom-one-light.min.css`, dark: `${V}atom-one-dark.min.css` },
        gradient: { light: `${V}atom-one-light.min.css`, dark: `${V}atom-one-dark.min.css` },
        kimbie: { light: `${V}atom-one-light.min.css`, dark: `${V}atom-one-dark.min.css` },
        paraiso: { light: `${V}atom-one-light.min.css`, dark: `${V}atom-one-dark.min.css` },
        qtcreator: { light: `${V}atom-one-light.min.css`, dark: `${V}atom-one-dark.min.css` },
        stackoverflow: { light: `${V}atom-one-light.min.css`, dark: `${V}atom-one-dark.min.css` },
      },
    },
    mermaid: { js: `${V}mermaid.min.js`, enableZoom: true },
    katex: { css: `${V}katex.min.css`, js: `${V}katex.min.js` },
    echarts: {
      js: `${V}echarts.min.js`,
      parseOption: (code) => new Function(`return ${code}`)(),
    },
  },
})

/** 预加载流量统计（含动态图表的文章在渲染前调用）：fetch 后写入 window.__trafficData */
let trafficPromise = null
export function ensureTrafficData(days = 14) {
  if (window.__trafficData) return Promise.resolve()
  if (trafficPromise) return trafficPromise
  trafficPromise = fetch(`/api/public/stats/traffic?days=${days}`)
    .then((r) => {
      if (!r.ok) throw new Error(`traffic ${r.status}`)
      return r.json()
    })
    .then((data) => {
      window.__trafficData = {
        days: data.days || [],
        views: data.views || [],
        today: data.overview?.today ?? 0,
        week: data.overview?.week ?? 0,
        total: data.overview?.total ?? 0,
      }
    })
    .catch(() => {
      // 失败给空数据兜底，不阻塞文章渲染
      window.__trafficData = { days: [], views: [], today: 0, week: 0, total: 0 }
    })
  return trafficPromise
}

/** 文章内容是否含 echarts 动态数据占位符 */
export function hasTrafficPlaceholder(content) {
  return /\{\{traffic\./.test(content || '')
}
