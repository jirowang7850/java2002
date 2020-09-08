package com.qf.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public boolean lock(String key, String value, int second){
        return stringRedisTemplate.opsForValue().setIfAbsent(key,value, second,TimeUnit.SECONDS);
    }

    public void unLock(String key) {
       stringRedisTemplate.delete(key);
    }
}
