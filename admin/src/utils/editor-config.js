// md-editor-v3 扩展资源本地化：默认从 unpkg CDN 动态加载（国内访问不稳），
// 全部改为自托管 public/vendor/ 下的静态资源（构建后位于 /admin/vendor/）。
// 启用完整扩展：mermaid 图、katex 公式、echarts 图表、highlight 高亮、prettier 格式化、图片裁剪、全屏。
// 通用动态数据占位符：{{数据源名.字段}}，与博客前台 editor-config.js 同一套注册表逻辑。
import { config } from 'md-editor-v3'

const V = '/admin/vendor/'

// ===== 数据源注册表（与博客前台保持一致） =====
const DATA_SOURCES = {
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
        days: fc.map((x) => String(x.date).slice(5)),
        weeks: fc.map((x) => x.week || ''),
        tempMax: fc.map((x) => x.temp_max ?? 0),
        tempMin: fc.map((x) => x.temp_min ?? 0),
        weatherDay: fc.map((x) => x.weather_day || ''),
        weatherNight: fc.map((x) => x.weather_night || ''),
      }
    },
  },
}

const pending = {}

/** 确保指定数据源的数据就绪（TTL 内复用） */
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

/** 按路径取数据：'days.0' → data.days[0]；取不到返回 undefined */
function getByPath(data, path) {
  return path.split('.').reduce((acc, seg) => (acc == null ? undefined : acc[seg]), data)
}

/** 字符串级替换：{{源.字段}} 或 {{源.字段.索引}} → JSON 值，在 parseOption / 正文渲染前调用。
 * mode='text'（正文/表格，字符串裸输出避免多余引号）：{{weather.city}} → 深圳市
 * mode='json'（echarts 代码块）：引号内裸替换避免嵌套，JSON 值位置 stringify */
function replacePlaceholdersInCode(code, mode = 'json') {
  if (!code || !/\{\{/.test(code)) return code
  return code.replace(/\{\{([a-zA-Z_][a-zA-Z0-9_]*)\.([a-zA-Z0-9_.]+)\}\}/g, (full, ns, path, offset) => {
    const data = window.__dataSources?.[ns]?.data
    const val = getByPath(data, path)
    if (val === undefined) return full
    if (mode === 'text') {
      return typeof val === 'string' ? val : JSON.stringify(val)
    }
    const before = code.slice(Math.max(0, offset - 1), offset)
    const after = code.slice(offset + full.length, offset + full.length + 1)
    const inString = before === '"' || after === '"'
    if (inString && typeof val === 'string') return val
    return JSON.stringify(val)
  })
}

/** 对象级替换（echartsConfig 钩子用） */
function resolvePlaceholder(value) {
  if (typeof value !== 'string') return value
  const m = value.match(/^\{\{([a-zA-Z_][a-zA-Z0-9_]*)\.([a-zA-Z0-9_.]+)\}\}$/)
  if (!m) return value
  const [, ns, path] = m
  const data = window.__dataSources?.[ns]?.data
  const val = getByPath(data, path)
  if (val !== undefined) return val
  return Array.isArray(getByPath(data, path.split('.')[0])) ? [] : 0
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
  return resolvePlaceholder(node)
}

config({
  echartsConfig: (option) => resolveDynamicData(option),
  // 预览 HTML 渲染前替换正文里的 {{源.字段}} 文本占位符
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
        a11y: { light: `${V}a11y-light.min.css`, dark: `${V}a11y-dark.min.css` },
        atom: { light: `${V}atom-one-light.min.css`, dark: `${V}atom-one-dark.min.css` },
        github: { light: `${V}github.min.css`, dark: `${V}github-dark.min.css` },
        gradient: { light: `${V}gradient-light.min.css`, dark: `${V}gradient-dark.min.css` },
        kimbie: { light: `${V}kimbie-light.min.css`, dark: `${V}kimbie-dark.min.css` },
        paraiso: { light: `${V}paraiso-light.min.css`, dark: `${V}paraiso-dark.min.css` },
        qtcreator: { light: `${V}qtcreator-light.min.css`, dark: `${V}qtcreator-dark.min.css` },
        stackoverflow: { light: `${V}stackoverflow-light.min.css`, dark: `${V}stackoverflow-dark.min.css` },
      },
    },
    prettier: {
      standaloneJs: `${V}prettier.standalone.js`,
      parserMarkdownJs: `${V}prettier.markdown.js`,
    },
    screenfull: { js: `${V}screenfull.js` },
    cropper: { css: `${V}cropper.min.css`, js: `${V}cropper.min.js` },
    mermaid: { js: `${V}mermaid.min.js`, enableZoom: false },
    katex: { css: `${V}katex.min.css`, js: `${V}katex.min.js` },
    echarts: {
      js: `${V}echarts.min.js`,
      parseOption: (code) => new Function(`return ${replacePlaceholdersInCode(code)}`)(),
    },
  },
})
