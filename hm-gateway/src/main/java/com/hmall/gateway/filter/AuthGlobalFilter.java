package com.hmall.gateway.filter;

import com.hmall.gateway.config.AuthProperties;
import com.hmall.gateway.utils.JwtTool;
import lombok.RequiredArgsConstructor;
import org.apache.el.parser.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final JwtTool jwtTool;
    private final AuthProperties authProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 获取request
        ServerHttpRequest request = exchange.getRequest();
        // 判断是否需要拦截
        String path = request.getURI().getPath();
        if (isExclude(path)) {
            return chain.filter(exchange);
        }

        // 获取token
        List<String> authorization = request.getHeaders().get("Authorization");
        if (authorization == null || authorization.isEmpty()) {
            // 没有token
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        String token = authorization.get(0);
        // 解析校验token
        Long userId;
        try {
            userId = jwtTool.parseToken(token);
        }catch (Exception e) {
            // token不合法
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        // 传递用户信息
        ServerWebExchange serverWebExchange = exchange.mutate()
                .request(
                        builder ->
                        builder.header("user-info", userId.toString())
                )
                .build();
        return chain.filter(serverWebExchange);
    }
    private boolean isExclude(String path) {
        for (String pathPattern : authProperties.getExcludePaths()) {
            if (pathMatcher.match(pathPattern, path)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
