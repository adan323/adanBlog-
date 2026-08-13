const BASE = '/api'

async function request(path, options = {}) {
  const res = await fetch(`${BASE}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  })
  if (!res.ok) {
    let msg = `HTTP ${res.status}`
    try {
      const body = await res.json()
      if (body.error) msg = body.error
    } catch {}
    throw new Error(msg)
  }
  return res.json()
}

export const api = {
  // 公开
  listArticles: (page = 1, size = 6, tag = '') =>
    request(`/articles?page=${page}&size=${size}${tag ? `&tag=${encodeURIComponent(tag)}` : ''}`),
  getArticle: (slug) => request(`/articles/${slug}`),
  getArchive: () => request('/archive'),
  getTags: () => request('/tags'),
  getTag: (slug) => request(`/tags/${encodeURIComponent(slug)}`),
  getSettings: () => request('/settings'),
  getStats: () => request('/public/stats'),
  search: (q) => request(`/search?q=${encodeURIComponent(q)}`),
}
