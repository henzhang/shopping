package com.changgou.oauth.config;

import com.changgou.oauth.util.UserJwt;
import com.changgou.user.feign.UserFeign;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/*****
 * 自定义授权认证类
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    ClientDetailsService clientDetailsService;

    @Autowired
    private UserFeign userFeign;

    /****
     * 自定义授权认证
     * @param username
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // ======================== 客户端信息认证 start =================================
        //取出身份，如果身份为空说明没有认证
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //没有认证统一采用httpbasic认证，httpbasic中存储了client_id和client_secret，开始认证client_id和client_secret
        if (authentication == null) {
            ClientDetails clientDetails = clientDetailsService.loadClientByClientId(username);
            if (clientDetails != null) {
                //秘钥
                String clientSecret = clientDetails.getClientSecret();

                //静态方式
//                return new User(
//                        username, // 客户端id
//                        new BCryptPasswordEncoder().encode(clientSecret),  // 客户端密钥
//                        AuthorityUtils.commaSeparatedStringToAuthorityList("")); // 权限

                //数据库查找方式
                return new User(
                        username, // 客户端id
                        clientSecret,  // 客户端密钥 (数据库取出来的已加密)
                        AuthorityUtils.commaSeparatedStringToAuthorityList(""));
            }
        }
        // ======================== 客户端信息认证 end =================================


        // ======================== 用户信息认证 start =================================

        if (StringUtils.isEmpty(username)) {
            return null;
        }

        /**
         * 从数据库加载用户信息
         * feign调用之前 :
         * 1. 没有令牌，需要生成令牌（admin）
         * 2. 令牌需要携带过去 , 且令牌需要存放入header中
         * 3. 请求-> feign调用 -> 拦截器 RequestInterceptor
         */
        Result<com.changgou.user.pojo.User> userResult = userFeign.findById(username);
        if (userResult == null || userResult.getData() == null) {
            return null;
        }
        String pwd = userResult.getData().getPassword();

        //创建User对象    (数据库没有指定role的信息，所以这里不需要设置)
        String permissions = "user,vip";     // 这里指定角色信息
        UserJwt userDetails = new UserJwt(username, pwd, AuthorityUtils.commaSeparatedStringToAuthorityList(permissions));
        // ======================== 用户信息认证 end =================================
        return userDetails;
    }
}
