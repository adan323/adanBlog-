// 博客前台 md-editor-v3 (MdPreview) 扩展资源本地化：默认从 unpkg CDN 动态加载，
// 改为自托管 public/vendor/ 下的静态资源（构建后位于 /vendor/）。
// 启用完整扩展：mermaid 图、katex 公式、echarts 图表、highlight 高亮。
// 通用动态数据占位符：文章里写 {{数据源名.字段}}，渲染时替换为该数据源的真实数据。
// 新增数据源 = 在 DATA_SOURCES 注册表加一个 load() 函数，引擎代码零改动。
import { config } from 'md-editor-v3'

const V = '/vendor/'

// ===== 数据源注册表（通用） =====
// 每个数据源：load() 返回 {字段: 值} 对象；ttl 为缓存时长(ms)，0 = 每次加载都刷新。
const DATA_SOURCES = {
  // 博客访问统计（自家 API）
  traffic: {
    ttl: 0,
    load: async () => {
      const r = await fetch('/api/public/stats/traffic?days=14')
      if (!r.ok) throw new Error(`traffic ${r.status}`)
      const d = await r.json()
      return {
        days: d.days || [],
        views: d.views || [],
        today: d.overview?.today ?? 0,
        week: d.overview?.week ?? 0,
        total: d.overview?.total ?? 0,
      }
    },
  },
  // 一周天气（uapis.cn 免费 API，不传 city 时按客户端 IP 自动定位，
  // 免 key 且 CORS 全开，浏览器可直接调用；ttl 30 分钟省调用额度）
  weather: {
    ttl: 30 * 60 * 1000,
    load: async () => {
      const r = await fetch('https://uapis.cn/api/v1/misc/weather?forecast=true&lang=zh')
      if (!r.ok) throw new Error(`weather ${r.status}`)
      const d = await r.json()
      const fc = d.forecast || []
      return {
        city: d.city || '未知',
        today: d.temperature ?? 0,
        days: fc.map((x) => String(x.date).slice(5)),      // "08-16"
        weeks: fc.map((x) => x.week || ''),                 // "星期日"
        tempMax: fc.map((x) => x.temp_max ?? 0),
        tempMin: fc.map((x) => x.temp_min ?? 0),
        weatherDay: fc.map((x) => x.weather_day || ''),
        weatherNight: fc.map((x) => x.weather_night || ''),
      }
    },
  },
}

// ===== 缓存与预加载 =====
// window.__dataSources = { traffic: { data: {...}, ts: 时间戳 }, weather: {...} }
const pending = {} // 进行中的数据源加载 promise

/** 确保指定数据源的数据就绪（TTL 内复用，超时或缺失则重新 load） */
export function ensureDataSources(namespaces) {
  const tasks = (namespaces || []).map((ns) => {
    const src = DATA_SOURCES[ns]
    if (!src) return Promise.resolve()
    const cached = window.__dataSources?.[ns]
    if (cached && Date.now() - cached.ts < src.ttl) return Promise.resolve()
    if (pending[ns]) return pending[ns]
    pending[ns] = Promise.resolve()
      .then(() => src.load())
      .then((data) => {
        window.__dataSources = window.__dataSources || {}
        window.__dataSources[ns] = { data, ts: Date.now() }
      })
      .catch(() => {
        // 失败给空对象兜底，不阻塞文章渲染（图表显示空数据）
        window.__dataSources = window.__dataSources || {}
        window.__dataSources[ns] = { data: {}, ts: Date.now() }
      })
      .finally(() => {
        delete pending[ns]
      })
    return pending[ns]
  })
  return Promise.all(tasks)
}

/** 扫描内容里的占位符，返回用到的数据源名列表（去重） */
export function collectDataSources(content) {
  const names = new Set()
  const re = /\{\{([a-zA-Z_][a-zA-Z0-9_]*)\./g
  let m
  while ((m = re.exec(content || ''))) names.add(m[1])
  return [...names]
}

// ===== 占位符替换（字符串级 + 对象级） =====

/** 按路径取数据：'days.0' → data.days[0]；取不到返回 undefined */
function getByPath(data, path) {
  return path.split('.').reduce((acc, seg) => (acc == null ? undefined : acc[seg]), data)
}

/**
 * 字符串级替换：{{traffic.days}} → ["2026-08-10",...]，
 * mode='text'（正文/表格，字符串裸输出避免多余引号）：
 *   {{weather.city}} → 深圳市
 * mode='json'（echarts 代码块）：
 *   引号内（"{{weather.city}} 未来一周气温"）→ 字符串裸替换，避免引号嵌套
 *   JSON 值位置（"data": {{weather.days}}）→ JSON.stringify
 */
export function replacePlaceholdersInCode(code, mode = 'json') {
  if (!code || !/\{\{/.test(code)) return code
  return code.replace(/\{\{([a-zA-Z_][a-zA-Z0-9_]*)\.([a-zA-Z0-9_.]+)\}\}/g, (full, ns, path, offset) => {
    const data = window.__dataSources?.[ns]?.data
    const val = getByPath(data, path)
    if (val === undefined) return full
    if (mode === 'text') {
      return typeof val === 'string' ? val : JSON.stringify(val)
    }
    // json 模式：看占位符前后字符，紧邻引号则裸替换
    const before = code.slice(Math.max(0, offset - 1), offset)
    const after = code.slice(offset + full.length, offset + full.length + 1)
    const inString = before === '"' || after === '"'
    if (inString && typeof val === 'string') return val
    return JSON.stringify(val)
  })
}

/** 单个值替换（对象级，echartsConfig 钩子用）：{{ns.key}} 或 {{ns.key.0}} → 数据源里的值 */
function resolvePlaceholder(value) {
  if (typeof value !== 'string') return value
  const m = value.match(/^\{\{([a-zA-Z_][a-zA-Z0-9_]*)\.([a-zA-Z0-9_.]+)\}\}$/)
  if (!m) return value
  const [, ns, path] = m
  const data = window.__dataSources?.[ns]?.data
  const val = getByPath(data, path)
  if (val !== undefined) return val
  // 数据未就绪/失败：数组字段回空数组，数值回 0
  const top = getByPath(data, path.split('.')[0])
  return Array.isArray(top) ? [] : 0
}

/** 对象级替换（echartsConfig 钩子用）：递归遍历 option，替换字符串值 */
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
  return resolvePlaceholder(node)
}

config({
  // echarts 渲染前钩子：把 {{源.字段}} 占位符替换为真实数据
  echartsConfig: (option) => resolveDynamicData(option),
  // 预览 HTML 渲染前替换正文里的 {{源.字段}} 文本占位符（说明文字等）
  markdownItPlugins: (plugins) => [
    ...plugins,
    {
      type: 'replace-data-placeholders',
      plugin: (md) => {
        md.core.ruler.push('replace-data-placeholders', (state) => {
          for (const token of state.tokens) {
            if (token.type === 'inline' && token.content) {
              token.content = replacePlaceholdersInCode(token.content, 'text')
              if (token.children) {
                for (const child of token.children) {
                  if (child.content) child.content = replacePlaceholdersInCode(child.content, 'text')
                }
              }
            }
          }
        })
      },
      options: {},
    },
  ],
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
      parseOption: (code) => new Function(`return ${replacePlaceholdersInCode(code)}`)(),
    },
  },
})
