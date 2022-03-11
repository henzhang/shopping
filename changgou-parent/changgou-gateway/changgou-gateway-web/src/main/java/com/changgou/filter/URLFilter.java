package com.changgou.filter;

/****
 * @Author:henzhang
 * @Description: 不需要认证就能访问的路径校验
 *****/
public class URLFilter {


    /**
     * 要放行的路径
     */
    private static final String noAuthorizeUrls = "/api/user/add,/api/user/login";

    /**
     * 校验当前路径是否需要权限认证
     * 1. 不能直接放行，需要拦截 : false
     * 2. 放行 : true
     *
     * @param url
     * @return
     */
    public static boolean hasAuthorize(String url) {
        String[] split = noAuthorizeUrls.split(",");

        for (String s : split) {
            if (s.equals(url)) {
                return true;
            }
        }
        return false;
    }
}
