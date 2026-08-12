import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import katex from 'katex'

/** 行内公式渲染器 */
function renderInlineMath(md) {
  const defaultInline = md.renderer.rules.text || ((tokens, idx, options, env, self) => self.renderToken(tokens, idx, options))
  md.renderer.rules.text = (tokens, idx, options, env, self) => {
    const text = tokens[idx].content
    const pattern = /\$([^$]+)\$/g
    let result = ''
    let last = 0
    let match
    while ((match = pattern.exec(text)) !== null) {
      result += text.slice(last, match.index)
      try {
        result += katex.renderToString(match[1], { throwOnError: false })
      } catch {
        result += match[0]
      }
      last = match.index + match[0].length
    }
    result += text.slice(last)
    return result !== text || pattern.test(text) ? result : defaultInline(tokens, idx, options, env, self)
  }
}

/** 块级公式渲染器（$$...$$） */
function renderBlockMath(md) {
  const defaultFence = md.renderer.rules.fence
  md.renderer.rules.fence = (tokens, idx, options, env, self) => {
    const token = tokens[idx]
    if (token.info && token.info.trim() === 'math') {
      try {
        return `<div class="katex-display">${katex.renderToString(token.content, { displayMode: true, throwOnError: false })}</div>`
      } catch {
        return defaultFence(tokens, idx, options, env, self)
      }
    }
    return defaultFence(tokens, idx, options, env, self)
  }
}

export const md = new MarkdownIt({
  html: false,
  linkify: true,
  breaks: true,
  highlight(str, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return `<pre class="hljs"><code>${hljs.highlight(str, { language: lang, ignoreIllegals: true }).value}</code></pre>`
      } catch {}
    }
    return `<pre class="hljs"><code>${md.utils.escapeHtml(str)}</code></pre>`
  },
})

md.use(renderInlineMath).use(renderBlockMath)

/** 渲染 Markdown 为 HTML */
export function renderMarkdown(content) {
  return md.render(content || '')
}

/** 从 Markdown 提取目录（h2/h3） */
export function extractToc(content) {
  if (!content) return []
  const toc = []
  const lines = content.split('\n')
  for (const line of lines) {
    const h2 = line.match(/^##\s+(.+)/)
    if (h2) {
      toc.push({ level: 2, text: h2[1].trim(), id: slugify(h2[1]) })
      continue
    }
    const h3 = line.match(/^###\s+(.+)/)
    if (h3) {
      toc.push({ level: 3, text: h3[1].trim(), id: slugify(h3[1]) })
    }
  }
  return toc
}

function slugify(text) {
  return text
    .toLowerCase()
    .replace(/[^\u4e00-\u9fa5a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '')
    .slice(0, 80)
}

/** 阅读时长估算（中文按 ~300字/分） */
export function readingTime(content) {
  if (!content) return 1
  const chars = content.replace(/\s+/g, '').length
  return Math.max(1, Math.round(chars / 300))
}

/** 健壮地解析后端返回的日期（兼容无时区后缀的 LocalDateTime 格式） */
export function parseDate(iso) {
  if (!iso) return null
  const d = new Date(iso)
  if (!isNaN(d.getTime())) return d
  // 手动解析 "2026-08-12T23:45:17.617213" 这种无时区格式
  const m = String(iso).match(/^(\d{4})-(\d{2})-(\d{2})[T ](\d{2}):(\d{2}):(\d{2})/)
  if (m) return new Date(+m[1], +m[2] - 1, +m[3], +m[4], +m[5], +m[6])
  return null
}

/** 格式化日期 */
export function formatDate(iso) {
  const d = parseDate(iso)
  if (!d) return ''
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}
