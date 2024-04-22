package com.hqy.YunBI.manager;

import com.hqy.YunBI.common.ErrorCode;
import com.hqy.YunBI.exception.BusinessException;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class RedisLimiterManager {
    @Resource
    private RedissonClient redissonClient;

    /**
     * 限流操作，用于控制访问频率，以保护系统免受过多请求的影响。
     * @param key 用于区分不同的限流器，比如根据不同的用户ID进行限流。
     */
    public void doRateLimit(String key) {
        // 获取指定key的限流器，并设置其速率限制为每秒最多2个请求
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        rateLimiter.trySetRate(RateType.OVERALL,2,1, RateIntervalUnit.MINUTES);

        // 尝试获取一个令牌，如果获取失败，则表示请求过多，抛出业务异常
        boolean canOpt = rateLimiter.tryAcquire(1);
        if(!canOpt){
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST);
        }
    }
}
