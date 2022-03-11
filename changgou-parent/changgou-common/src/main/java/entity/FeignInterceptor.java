package entity;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Enumeration;

/****
 * @Author:henzhang
 * @Description:
 *****/

public class FeignInterceptor implements RequestInterceptor {

    /**
     * feign 执行之前进行拦截
     *
     * @param requestTemplate
     */
    @Override
    public void apply(RequestTemplate requestTemplate) {
        /**
         * 获取用户的令牌
         * 将令牌再次封装到头中
         */

        // 记录了用户请求的所有数据： 请求头+请求参数
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            Enumeration<String> headerNames = requestAttributes.getRequest().getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerKey = headerNames.nextElement();
                String headerValue = requestAttributes.getRequest().getHeader(headerKey);

                // 将请求头中的信息，封装进header中，使用feign调用的时候，传递给下一个微服务
                requestTemplate.header(headerKey, headerValue);
            }
        }


    }
}