package com.changgou.oauth.util;

import com.alibaba.fastjson.JSON;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;

/****
 * @Author:henzhang
 * @Description:
 *****/

@Component
public class AdminToken {

//    private static String key_location; //证书文件路径
//    private static String key_password;  //秘钥库密码
//    private static String keypwd; //秘钥密码
//    private static String alias; //秘钥别名
//
//    @Value("${encrypt.key-store.location}")
//    public void setKey_location(String key_location) {
//        AdminToken.key_location = key_location;
//    }
//
//    @Value("${encrypt.key-store.secret}")
//    public void setKey_password(String key_password) {
//        AdminToken.key_password = key_password;
//    }
//
//    @Value("${encrypt.key-store.password}")
//    public void setKeypwd(String keypwd) {
//        AdminToken.keypwd = keypwd;
//    }
//
//    @Value("${encrypt.key-store.alias}")
//    public void setAlias(String alias) {
//        AdminToken.alias = alias;
//    }

    /**
     * 管理员令牌发放
     *
     * @return
     */
    public static String adminToken() {
        //证书文件路径
        String key_location = "changgou.jks";
        //秘钥库密码
        String key_password = "changgou";
        //秘钥密码
        String keypwd = "changgou";
        //秘钥别名
        String alias = "changgou";

        /**
         * 获取私钥
         */
        ClassPathResource resource = new ClassPathResource(key_location);  //访问证书路径 读取jks的文件
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(resource, key_password.toCharArray()); //创建秘钥工厂
        KeyPair keyPair = keyStoreKeyFactory.getKeyPair(alias, keypwd.toCharArray()); //读取秘钥对(公钥、私钥)
        RSAPrivateKey rsaPrivate = (RSAPrivateKey) keyPair.getPrivate(); //获取私钥

        /**
         * 创建令牌，需要私钥加盐
         */

        //自定义Payload
        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put("id", "1");
        tokenMap.put("name", "heng");
        tokenMap.put("authorities", new String[]{"admin", "oauth"});

        //生成Jwt令牌
        Jwt jwt = JwtHelper.encode(JSON.toJSONString(tokenMap), new RsaSigner(rsaPrivate));

        //取出令牌
        String encoded = jwt.getEncoded();
        return encoded;
    }
}
