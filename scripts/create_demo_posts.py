#!/usr/bin/env python3
"""批量创建测试博文：3条无封面 + 1条封面失效(演示骨架屏)"""
import json, urllib.request

BASE = "http://adan.ltd:8080/api"
TOKEN = ""

def api(path, method="GET", body=None, token=None):
    req = urllib.request.Request(f"{BASE}{path}", method=method)
    req.add_header("Content-Type", "application/json")
    if token:
        req.add_header("Authorization", f"Bearer {token}")
    data = json.dumps(body).encode() if body else None
    with urllib.request.urlopen(req, data=data, timeout=20) as r:
        return json.loads(r.read())

# 登录
login = api("/auth/login", "POST", {"username": "admin", "password": "Adan@Blog2026"})
TOKEN = login["token"]
print("登录成功")

posts = [
    {
        "title": "徒步记：在山顶看了一场日落",
        "slug": "sunset-hike",
        "summary": "爬了四个小时的山，只为山顶那十分钟的日落。",
        "content": "## 出发\n\n周六清晨六点，背上包出发。\n\n## 山顶\n\n日落只有十分钟，但那种橙红色的光，足够记一辈子。\n\n---\n\n下次还去。",
        "status": "published",
        "tags": ["随笔"]
    },
    {
        "title": "关于咖啡因与创作力的随想",
        "slug": "caffeine-creativity",
        "summary": "咖啡因是创作者的燃料还是安慰剂？",
        "content": "## 咖啡因的魔力\n\n一杯美式下肚，思路似乎真的清晰了一些。\n\n### 科学解释\n\n腺苷受体被阻断，多巴胺分泌增加……\n\n```js\nconst coffee = () => \"灵感来了\";\n```",
        "status": "published",
        "tags": ["技术", "随笔"]
    },
    {
        "title": "旧书店里淘到的一本绝版书",
        "slug": "old-bookstore",
        "summary": "在巷子深处的旧书店，遇到了十年前的自己。",
        "content": "## 偶遇\n\n下午三点，路过那家从没进去过的旧书店。\n\n## 发现\n\n角落的书架上，居然有那本找了很久的绝版书。\n\n> 有些相遇，需要一点运气。",
        "status": "published",
        "tags": ["生活"]
    },
    {
        "title": "封面加载失败演示文章",
        "slug": "broken-cover-demo",
        "summary": "这篇文章配置了一个不存在的封面 URL，用于演示骨架屏占位效果。",
        "content": "## 骨架屏演示\n\n这篇文章的封面指向一个不存在的图片地址，加载会失败。\n\n正常情况下，封面区域会显示**骨架屏**而不是消失或破图。",
        "status": "published",
        "tags": ["技术"],
        "coverUrl": "/uploads/demo/not-exist.jpg"
    },
]

for p in posts:
    r = api("/admin/articles", "POST", p, TOKEN)
    print(f"  创建: {r['title']} (id={r['id']}, cover={r.get('coverUrl') or '无'})")
