package com.changgou.item.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/****
 * @Author:henzhang
 * @Description:
 *****/

@ControllerAdvice
@Configuration
public class EnableMvcConfig implements WebMvcConfigurer {
    /***
     * 静态资源放行
     * 生成的静态页放到resources/templates/items目录下,请求该目录下的静态页需要直接到该目录查找.
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/items/**").addResourceLocations("classpath:/templates/items/");
    }
}
