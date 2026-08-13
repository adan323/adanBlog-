#!/usr/bin/env python3
"""批量验证 UA 解析 / IP 归属准确性：模拟各种真实浏览器访问，再核对统计"""
import json, subprocess, time, urllib.request

BASE = "http://adan.ltd:8080"

# (测试名, UA, X-Forwarded-For, 期望浏览器, 期望平台, 期望归属)
CASES = [
    # 浏览器系列
    ("Chrome-Win", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36",
     "114.114.114.114", "Chrome", "Windows", "南京市"),
    ("Edge-Win", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36 Edg/126.0.0.0",
     "223.5.5.5", "Edge", "Windows", "杭州市"),
    ("Firefox-Linux", "Mozilla/5.0 (X11; Linux x86_64; rv:127.0) Gecko/20100101 Firefox/127.0",
     "8.8.8.8", "Firefox", "Linux", "Google LLC"),
    ("Safari-Mac", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.4 Safari/605.1.15",
     "119.29.29.29", "Safari", "macOS", "广东"),
    ("Safari-iOS", "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1",
     "180.101.50.242", "Safari", "iOS", "江苏"),
    ("Chrome-Android", "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Mobile Safari/537.36",
     "101.226.103.106", "Chrome", "Android", "上海"),
    # 微信内置浏览器
    ("WeChat-iOS", "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148 MicroMessenger/8.0.49(0x18003123) NetType/WIFI Language/zh_CN",
     "183.232.120.11", "微信", "iOS", "广东"),
    # Opera
    ("Opera-Win", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36 OPR/111.0.0.0",
     "202.96.209.133", "Opera", "Windows", "上海"),
    # 爬虫
    ("BaiduBot", "Mozilla/5.0 (compatible; Baiduspider/2.0; +http://www.baidu.com/search/spider.html)",
     "110.242.68.66", "其他", "爬虫", "河北"),
    # 未知浏览器
    ("Unknown-UA", "SomeWeirdBrowser/1.0 CustomAgent/2.0",
     "1.2.4.8", "其他", "其他", "广东"),
]

def api(path, method="GET", body=None, token=None):
    req = urllib.request.Request(f"{BASE}{path}", method=method)
    req.add_header("Content-Type", "application/json")
    if token:
        req.add_header("Authorization", f"Bearer {token}")
    data = json.dumps(body).encode() if body else None
    with urllib.request.urlopen(req, data=data, timeout=20) as r:
        return json.loads(r.read())

# 登录
token = api("/api/auth/login", "POST", {"username": "admin", "password": "Adan@Blog2026"})["token"]

# 记录当前总访问数
before = api("/api/admin/stats/traffic?days=1", token=token)["overview"]["total"]

# 逐个模拟访问
print(f"{'用例':<18}{'浏览器→期望':<18}{'平台→期望':<16}{'归属→期望':<16}")
print("-" * 78)
for name, ua, ip, exp_browser, exp_platform, exp_loc in CASES:
    # 直接调用文章详情接口（会记录访问日志）
    req = urllib.request.Request(f"{BASE}/api/articles/welcome", method="GET")
    req.add_header("User-Agent", ua)
    req.add_header("X-Forwarded-For", ip)
    try:
        urllib.request.urlopen(req, timeout=15)
    except Exception as e:
        print(f"{name}: 访问失败 {e}")
    time.sleep(0.3)

time.sleep(1)

# 拉取统计数据核对
traffic = api(f"/api/admin/stats/traffic?days=1", token=token)
locs = {x["name"]: x["value"] for x in traffic["locations"]}
browsers = {x["name"]: x["value"] for x in traffic["browsers"]}
platforms = {x["name"]: x["value"] for x in traffic["platforms"]}
after = traffic["overview"]["total"]
print(f"\n新增访问记录: {after - before}")
print("\n=== 各维度统计结果 ===")
print("IP归属:", locs)
print("浏览器:", browsers)
print("平台:", platforms)
print("\n=== 期望 vs 实际核对 ===")
for name, ua, ip, exp_browser, exp_platform, exp_loc in CASES:
    print(f"{name}: 期望[{exp_browser}/{exp_platform}/{exp_loc}]")
