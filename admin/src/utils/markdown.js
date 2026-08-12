import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import katex from 'katex'
import 'highlight.js/styles/atom-one-dark.css'
import 'katex/dist/katex.min.css'

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

// 行内公式
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
  return result || text
}

// 块级公式 ```math
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

export function renderMarkdown(content) {
  return md.render(content || '')
}
