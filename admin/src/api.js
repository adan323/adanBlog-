const BASE = '/api'

async function request(path, options = {}) {
  const headers = { 'Content-Type': 'application/json' }
  const token = localStorage.getItem('admin_token')
  if (token) headers['Authorization'] = `Bearer ${token}`
  const res = await fetch(`${BASE}${path}`, { ...options, headers })
  if (res.status === 401) {
    localStorage.removeItem('admin_token')
    window.location.href = '/admin/login'
    throw new Error('登录已过期')
  }
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

export const adminApi = {
  login: (username, password) =>
    request('/auth/login', { method: 'POST', body: JSON.stringify({ username, password }) }),
  changePassword: (oldPassword, newPassword) =>
    request('/auth/change-password', { method: 'POST', body: JSON.stringify({ oldPassword, newPassword }) }),
  listArticles: (page = 1, size = 10, status = '') =>
    request(`/admin/articles?page=${page}&size=${size}${status ? `&status=${status}` : ''}`),
  getArticle: (id) => request(`/admin/articles/${id}`),
  createArticle: (data) => request('/admin/articles', { method: 'POST', body: JSON.stringify(data) }),
  updateArticle: (id, data) => request(`/admin/articles/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteArticle: (id) => request(`/admin/articles/${id}`, { method: 'DELETE' }),
  getStats: () => request('/admin/stats'),
  getTraffic: (days = 7) => request(`/admin/stats/traffic?days=${days}`),
  getTags: () => request('/tags'),
  getSettings: () => request('/admin/settings'),
  updateSettings: (data) => request('/admin/settings', { method: 'PUT', body: JSON.stringify(data) }),
  upload: (file) => {
    const fd = new FormData()
    fd.append('file', file)
    const token = localStorage.getItem('admin_token')
    return fetch('/api/admin/upload', {
      method: 'POST',
      headers: token ? { Authorization: `Bearer ${token}` } : {},
      body: fd,
    }).then(async (res) => {
      if (!res.ok) throw new Error('上传失败')
      return res.json()
    })
  },
}
