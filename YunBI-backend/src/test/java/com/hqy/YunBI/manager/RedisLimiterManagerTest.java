package com.hqy.YunBI.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


import javax.annotation.Resource;

@SpringBootTest
class RedisLimiterManagerTest {
    @Resource
    private RedisLimiterManager redisLimiterManager;
    @Test
    void doRateLimit() {
        String user_id = "2";
        for(int i = 0; i < 5; i++){
            redisLimiterManager.doRateLimit(user_id);
            System.out.println("成功");
        }
    }
}