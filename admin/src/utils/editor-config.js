// md-editor-v3 扩展资源本地化：默认从 unpkg CDN 动态加载（国内访问不稳），
// 全部改为自托管 public/vendor/ 下的静态资源（构建后位于 /admin/vendor/）。
// 启用完整扩展：mermaid 图、katex 公式、echarts 图表、highlight 高亮、prettier 格式化、图片裁剪、全屏。
// 支持 echarts 动态数据占位符：{{traffic.days}} / {{traffic.views}} 等（与博客前台一致）。
import { config } from 'md-editor-v3'

const V = '/admin/vendor/'

// ===== echarts 动态数据（占位符替换，与博客前台 editor-config.js 一致） =====
const TRAFFIC_KEYS = ['days', 'views', 'today', 'week', 'total']

function resolveTrafficPlaceholder(value) {
  if (typeof value !== 'string') return value
  const m = value.match(/^\{\{traffic\.([a-zA-Z]+)\}\}$/)
  if (!m) return value
  const key = m[1]
  if (!TRAFFIC_KEYS.includes(key)) return value
  const data = window.__trafficData
  if (data && data[key] !== undefined) return data[key]
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
  echartsConfig: (option) => resolveDynamicData(option),
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
      parseOption: (code) => new Function(`return ${code}`)(),
    },
  },
})
