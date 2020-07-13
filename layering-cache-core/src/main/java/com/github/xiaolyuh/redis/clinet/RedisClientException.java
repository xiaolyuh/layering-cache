package com.github.xiaolyuh.redis.clinet;

/**
 * redis客户端异常
 */
public class RedisClientException extends RuntimeException {
    public RedisClientException() {
        super();
    }

    public RedisClientException(String message) {
        super(message);
    }

    public RedisClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public RedisClientException(Throwable cause) {
        super(cause);
    }

    protected RedisClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
