package com.changgou.oauth.interceptor;

import com.changgou.oauth.util.AdminToken;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;

/****
 * @Author:henzhang
 * @Description:
 *****/

@Configuration
public class TokenRequestInterceptor implements RequestInterceptor {

    /**
     * feign 执行之前进行拦截
     * <p>
     * feign调用之前 :
     * 1. 没有令牌，需要生成令牌（admin）
     * 2. 令牌需要携带过去 , 且令牌需要存放入header中
     *
     * @param requestTemplate
     */
    @Override
    public void apply(RequestTemplate requestTemplate) {
        String token = AdminToken.adminToken();
        requestTemplate.header("Authorization", "bearer " + token);
    }
}