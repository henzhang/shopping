package com.changgou.filter;

import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/****
 * @Author:henzhang
 * @Description: 全局过滤器
 *
 * 实现用户权限校验
 *****/

@Component
public class AuthorizeFilter implements GlobalFilter, Ordered {
    //令牌头名字
    private static final String AUTHORIZE_TOKEN = "Authorization";

    /**
     * 全局拦截
     *
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // 用户如果是登录或一些不需要做权限认证的请求，直接放行
        String path = request.getPath().toString();
        if (URLFilter.hasAuthorize(path)) {
            return chain.filter(exchange);
        }


        boolean hasToken = true; // true 表示令牌在头文件中，false 表示令牌不在头文件中（将令牌封装到头文件中，再传递给其他微服务）

        // 获取用户令牌信息
        // 1.header中
        String token = request.getHeaders().getFirst(AUTHORIZE_TOKEN);
        // 2.参数中
        if (StringUtils.isEmpty(token)) {
            token = request.getQueryParams().getFirst(AUTHORIZE_TOKEN);
            hasToken = false;
        }
        // 3.cookie中
        if (StringUtils.isEmpty(token)) {
            HttpCookie cookie = request.getCookies().getFirst(AUTHORIZE_TOKEN);
            if (cookie != null) {
                token = cookie.getValue();
            }
        }

        // 没有权限
        if (StringUtils.isEmpty(token)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);  // 设置没有权限的字节码 401
            return response.setComplete(); // 响应空数据
        }

        // 令牌判断是否为空，不为空，则放入头部 放行
        if (StringUtils.isEmpty(token)) {
            // 无效则拦截
            response.setStatusCode(HttpStatus.UNAUTHORIZED);  // 设置没有权限的字节码 401
            return response.setComplete(); // 响应空数据
        } else {
            // 将令牌封装到头文件中
            if (!hasToken) {
                if (!token.startsWith("bearer ") && !token.startsWith("Bearer ")) {
                    token = "bearer " + token;
                }
                request.mutate().header(AUTHORIZE_TOKEN, token);
            }

        }

//        try {
////            JwtUtil.parseJWT(token);
//        } catch (Exception e) {
//        }


        return chain.filter(exchange);
    }

    /**
     * 过滤器执行顺序
     *
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
