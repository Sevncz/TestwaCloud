package com.testwa.core.redis.config;

import lombok.Data;
import redis.clients.jedis.Protocol;

/**
 * Redis client 配置
 *
 * @author Wen
 */
@Data
public class RedisHAClientConfig {

    /**
     * redis连接池名称
     */
    private String cacheName = "default";

    /**
     * redis服务端地址
     */
    private String redisServerHost = "localhost";

    /**
     * redis服务端端口
     */
    private int redisServerPort = Protocol.DEFAULT_PORT;

    /**
     * redis密码
     */
    private String redisAuthKey;

    /**
     * redis连接超时
     */
    private int timeout = 20000;

}
