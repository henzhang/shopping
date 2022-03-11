package com.changgou.token;

import org.junit.Test;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;

/*****
 * @Author: henzhang
 * @Description: com.changgou.token
 *  使用公钥解密令牌数据
 ****/
public class ParseJwtTest {

    /***
     * 校验令牌
     */
    @Test
    public void testParseToken() {
        //令牌
        String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlcyI6IlJPTEVfVklQLFJPTEVfVVNFUiIsIm5hbWUiOiJoZW5nIiwiaWQiOiIxIn0.kykNiwoh4KkALA6bnUaKr6-TSH62126l5KdsaDhazZdgWIbLkNw9XOU1ssSsSCYBoPbjhy3oXDVk63Od3FnLO9AQGTmP3J0pmmjeRtrqP1C_BZoXzR85XGV3sIh12-q8_L6x6v3S0nvEAauraqq17YAEY071BcAxQjhCFvjKSpjYhrm7TXgxqxd6vDpYdcoma_EPQvQYHg6lA7-lhZ67Twxmm3WxFzcrZh_ipJLtVH8v0WAr5VMJlIokCbi86ZYg6lIlSUh_GNEFTU1R9vmsGFZAazbnKqBt3ohicvCRj0A4S3JawbFEutDGD6EAa3pUkOiLvPyrZJjGzD3qIYSIOw";

        //公钥
        String publickey = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAk5H0ril+6pEwbtny7AiemJAVEiKVH/O9X4mDJoHj4CzAp699xa/IwILVorTkK5PHGWC3MtaS9ZGzV50BiwZxo8jzIXIFpGakv7fdR1iZlV8zNm3u2Vtoymjrefl33n146qrMyLz03rcPLeDyDcyQDpnS8SDK5hqrN6AYCabuUhZ+9D2aucU3asWarsiUpK850Xi899dr9lWgkhDv0VXNQeW9kVUJoYz/kv5qT3DMhQLdxrsrMe7cxL/lEp5VsXkvvG4IwfMbY6Eo6GtrIlfKOPx+Ve7Sexe6j6mZ1jdE0s6VtpU1T960XytFtZ1QylzimXk6uiA+JdfV4gM0VSozpwIDAQAB-----END PUBLIC KEY-----";

        //校验Jwt
        Jwt jwt = JwtHelper.decodeAndVerify(token, new RsaVerifier(publickey));

        //获取Jwt原始内容 载荷
        String claims = jwt.getClaims();
        System.out.println(claims);
        //jwt令牌
        String encoded = jwt.getEncoded();
        System.out.println(encoded);
    }
}
