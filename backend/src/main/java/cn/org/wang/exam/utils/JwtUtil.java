package cn.org.wang.exam.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JWT 工具类
 * 使用 RS256 非对称加密算法，提升安全性
 *
 * @author Wang
 * @version 2.0
 */
@Slf4j
@Data
@Component
public class JwtUtil {

    /**
     * RSA 私钥资源路径
     */
    @Value("${jwt.private-key-path}")
    private Resource privateKeyResource;

    /**
     * RSA 公钥资源路径
     */
    @Value("${jwt.public-key-path}")
    private Resource publicKeyResource;

    /**
     * JWT 过期时间（毫秒）
     */
    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 续签阈值（毫秒）
     */
    @Value("${jwt.refresh-threshold}")
    private Long refreshThreshold;

    /**
     * RSA 私钥
     */
    private RSAPrivateKey privateKey;

    /**
     * RSA 公钥
     */
    private RSAPublicKey publicKey;

    /**
     * 初始化 RSA 密钥
     */
    @PostConstruct
    public void init() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        this.privateKey = loadPrivateKey(privateKeyResource);
        this.publicKey = loadPublicKey(publicKeyResource);
        log.info("RSA 密钥初始化完成，算法: RS256");
    }

    /**
     * 加载 RSA 私钥
     *
     * @param resource 私钥文件资源
     * @return RSA 私钥对象
     */
    private RSAPrivateKey loadPrivateKey(Resource resource) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String key = readKeyContent(resource);
        byte[] keyBytes = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) kf.generatePrivate(spec);
    }

    /**
     * 加载 RSA 公钥
     *
     * @param resource 公钥文件资源
     * @return RSA 公钥对象
     */
    private RSAPublicKey loadPublicKey(Resource resource) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String key = readKeyContent(resource);
        byte[] keyBytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) kf.generatePublic(spec);
    }

    /**
     * 读取密钥文件内容
     *
     * @param resource 密钥文件资源
     * @return 密钥内容（去除头尾标记和空白字符）
     */
    private String readKeyContent(Resource resource) throws IOException {
        try (InputStream is = resource.getInputStream()) {
            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return content
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
        }
    }

    /**
     * 创建 JWT Token
     *
     * @param userInfo 用户信息JSON
     * @param authList 权限列表
     * @return JWT Token字符串
     */
    public String createJwt(String userInfo, List<String> authList) {
        Date issDate = new Date();
        Date expireDate = new Date(issDate.getTime() + expiration);

        Map<String, Object> headerClaims = new HashMap<>();
        headerClaims.put("alg", "RS256");
        headerClaims.put("typ", "JWT");

        return JWT.create()
                .withHeader(headerClaims)
                .withIssuer("exam-system")
                .withIssuedAt(issDate)
                .withExpiresAt(expireDate)
                .withClaim("userInfo", userInfo)
                .withClaim("authList", authList)
                .sign(Algorithm.RSA256(publicKey, privateKey));
    }

    /**
     * 获取 JWT 验证器实例
     *
     * @return JWTVerifier 实例
     */
    private JWTVerifier getVerifier() {
        return JWT.require(Algorithm.RSA256(publicKey, privateKey)).build();
    }

    /**
     * 校验 Token 并尝试续签
     *
     * @param token Token令牌
     * @return 若不需要续签返回原 Token，若需要续签返回新 Token，若验证失败返回 null
     */
    public String verifyAndRefreshToken(String token) {
        try {
            JWTVerifier verifier = getVerifier();
            DecodedJWT jwt = verifier.verify(token);

            // 检查是否需要续签
            if (shouldRefresh(jwt)) {
                String userInfo = jwt.getClaim("userInfo").asString();
                List<String> authList = jwt.getClaim("authList").asList(String.class);
                return createJwt(userInfo, authList);
            }
            return token;
        } catch (Exception e) {
            log.warn("Token 验证失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 判断是否需要续签
     *
     * @param jwt 已解码的 JWT
     * @return 是否需要续签
     */
    private boolean shouldRefresh(DecodedJWT jwt) {
        Date expirationDate = jwt.getExpiresAt();
        long currentTime = System.currentTimeMillis();
        long remainingTime = expirationDate.getTime() - currentTime;
        return remainingTime < refreshThreshold;
    }

    /**
     * 校验 Token
     *
     * @param token Token令牌
     * @return 验证是否成功
     */
    public boolean verifyToken(String token) {
        try {
            JWTVerifier verifier = getVerifier();
            verifier.verify(token);
            return true;
        } catch (Exception e) {
            log.warn("Token 校验失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 根据 Token 获取用户信息
     *
     * @param token Token令牌
     * @return 用户信息JSON字符串
     */
    public String getUser(String token) {
        DecodedJWT jwt = JWT.decode(token);
        return jwt.getClaim("userInfo").asString();
    }

    /**
     * 根据 Token 获取权限列表
     *
     * @param token Token令牌
     * @return 权限列表
     */
    public List<String> getAuthList(String token) {
        DecodedJWT jwt = JWT.decode(token);
        return jwt.getClaim("authList").asList(String.class);
    }

    /**
     * 获取 Token 过期时间（秒）
     *
     * @return 过期时间（秒）
     */
    public long getExpirationSeconds() {
        return expiration / 1000;
    }
}
