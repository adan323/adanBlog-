import com.adan.blog.service.VisitLogService;

/** 独立验证 UA 解析与 ip2region 归属（临时测试用） */
public class VerifyParse {
    public static void main(String[] args) throws Exception {
        // 直接实例化（不依赖 Spring），用反射注入 ip2region 路径
        VisitLogService svc = new VisitLogService(null);
        java.lang.reflect.Field f = VisitLogService.class.getDeclaredField("ip2regionPath");
        f.setAccessible(true);
        f.set(svc, "/var/lib/adan-blog/ip2region.xdb");

        String[][] cases = {
            // 名称, UA, IP
            {"Chrome-Win", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36", "114.114.114.114"},
            {"Edge-Win", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36 Edg/126.0.0.0", "223.5.5.5"},
            {"Firefox-Linux", "Mozilla/5.0 (X11; Linux x86_64; rv:127.0) Gecko/20100101 Firefox/127.0", "8.8.8.8"},
            {"Safari-Mac", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.4 Safari/605.1.15", "119.29.29.29"},
            {"Safari-iOS", "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1", "180.101.50.242"},
            {"Chrome-Android", "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Mobile Safari/537.36", "101.226.103.106"},
            {"WeChat-iOS", "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148 MicroMessenger/8.0.49 NetType/WIFI", "183.232.120.11"},
            {"Opera-Win", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36 OPR/111.0.0.0", "202.96.209.133"},
            {"BaiduBot", "Mozilla/5.0 (compatible; Baiduspider/2.0; +http://www.baidu.com/search/spider.html)", "110.242.68.66"},
            {"GoogleBot", "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)", "66.249.65.1"},
            {"Unknown", "SomeWeirdBrowser/1.0 CustomAgent/2.0", "1.2.4.8"},
            {"BingBot", "Mozilla/5.0 (compatible; bingbot/2.0; +http://www.bing.com/bingbot.htm)", "40.77.167.20"},
        };

        System.out.printf("%-14s | %-12s | %-12s | %-14s%n", "用例", "浏览器", "平台", "IP归属");
        System.out.println("-".repeat(60));
        for (String[] c : cases) {
            java.lang.reflect.Method mb = VisitLogService.class.getDeclaredMethod("parseBrowser", String.class);
            java.lang.reflect.Method mp = VisitLogService.class.getDeclaredMethod("parsePlatform", String.class);
            java.lang.reflect.Method ml = VisitLogService.class.getDeclaredMethod("resolveLocation", String.class);
            mb.setAccessible(true); mp.setAccessible(true); ml.setAccessible(true);
            String browser = (String) mb.invoke(svc, c[1]);
            String platform = (String) mp.invoke(svc, c[1]);
            String loc = (String) ml.invoke(svc, c[2]);
            System.out.printf("%-14s | %-12s | %-12s | %s%n", c[0], browser, platform, loc);
        }
    }
}
