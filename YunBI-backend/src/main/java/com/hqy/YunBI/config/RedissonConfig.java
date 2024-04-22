package com.hqy.YunBI.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix="spring.redis")
@Data
public class RedissonConfig {
    private Integer database;
    private String host ;
    private Integer port;
    private String password;
    @Bean
    public RedissonClient getRedissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setDatabase(database)
                //8.149.129.154:6379
                .setAddress("redis://"+"8.149.129.154:6379")
                .setPassword(password);

//                .setAddress("redis://"+host+":"+port);

//        config.useClusterServers()
//                // use "rediss://" for SSL connection
//                .addNodeAddress("redis://127.0.0.1:7181");

        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }

}
