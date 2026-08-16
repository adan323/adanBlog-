package com.adan.blog.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * HTTP(80) → HTTPS(443) 重定向。
 * 主端口 443 走 SSL（application.properties 配置，自动装配保留）；
 * 这里只附加一个 80 端口 connector + 强制跳转 Filter。
 */
@Configuration
public class HttpRedirectConfig {

    /** 附加 80 端口 connector（不覆盖自动配置的 443+SSL） */
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> httpConnectorCustomizer() {
        return factory -> {
            Connector httpConnector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
            httpConnector.setScheme("http");
            httpConnector.setPort(80);
            httpConnector.setSecure(false);
            httpConnector.setRedirectPort(443);
            factory.addAdditionalTomcatConnectors(httpConnector);
        };
    }

    /** 强制 HTTPS：http 请求 301 到 https 同路径 */
    @Bean
    public Filter httpsRedirectFilter() {
        return new Filter() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException {
                HttpServletRequest req = (HttpServletRequest) request;
                HttpServletResponse resp = (HttpServletResponse) response;
                if (!req.isSecure()) {
                    String host = req.getHeader("Host");
                    if (host != null && host.endsWith(":80")) {
                        host = host.substring(0, host.length() - 3);
                    }
                    String redirect = "https://" + host + req.getRequestURI()
                            + (req.getQueryString() != null ? "?" + req.getQueryString() : "");
                    resp.setStatus(301);
                    resp.setHeader("Location", redirect);
                    return;
                }
                chain.doFilter(request, response);
            }
        };
    }
}
