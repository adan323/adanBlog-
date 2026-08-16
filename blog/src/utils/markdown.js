// Markdown 渲染已交给 md-editor-v3 的 MdPreview（见 PostDetail.vue），
// 本文件仅保留文章元数据工具函数。

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

export function slugify(text) {
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
