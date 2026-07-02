package org.sdb.aiban.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class AsyncSecurityConfig {

    @PostConstruct
    public void enableInheritableThreadLocal() {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

    @Bean("sseExecutor")
    public ExecutorService sseExecutor() {
        return new DelegatingSecurityContextExecutorService(
                Executors.newCachedThreadPool());
    }

    /**
     * 解决 SSE/Async 请求 SecurityContext 丢失问题：
     * 请求结束前将 SecurityContext 存入 request attribute，
     * 异步 dispatch 时从 attribute 恢复，使 AuthorizationFilter 有权限信息。
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public Filter asyncSecurityFilter() {
        return new Filter() {
            private static final String ATTR =
                    "org.springframework.security.web.context.RequestAttributeSecurityContextRepository.SPRING_SECURITY_CONTEXT";

            @Override
            public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
                    throws IOException, ServletException {
                HttpServletRequest request = (HttpServletRequest) req;

                // 异步 dispatch 时，从 attribute 恢复 SecurityContext
                Object saved = request.getAttribute(ATTR);
                if (saved instanceof SecurityContext) {
                    SecurityContextHolder.setContext((SecurityContext) saved);
                }

                chain.doFilter(req, resp);

                // 请求处理完毕后，将当前 SecurityContext 存入 request attribute
                request.setAttribute(ATTR, SecurityContextHolder.getContext());
            }
        };
    }
}
