package com.qf.controller;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.qf.pojo.User;
import com.qf.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate redisTemplate;
    BloomFilter bloomFilter = BloomFilter.create(
            Funnels.stringFunnel(Charset.defaultCharset()), 10000, 0.03);

    @RequestMapping("findById/{id}")
    public User findById(@PathVariable("id") Integer id) {
        String userKey = "user#" + id;//当前用户的key值

        synchronized (UserController.class) {

            boolean isContain = bloomFilter.mightContain(userKey);
            //如果布隆过滤中没有，redis中肯定没有，如果有，可能出现误判

            if (isContain) {
                System.out.println("redis中取数据");
                //进一步判断
                User user = (User) redisTemplate.opsForValue().get(userKey);
                System.out.println(user);
                if (user != null) {
                    return user;
                } else {//误判
                    System.out.println("误判");
                    //考虑多线程
                    synchronized (userKey) {
                        user = userService.findById(id);
                        if (user != null) {
                            redisTemplate.opsForValue().set(userKey, user);
                        } else {
                            redisTemplate.opsForValue().set(userKey, "null", 10, TimeUnit.SECONDS);
                        }
                    }
                }
            } else {//缓存失效
                //考虑多线程
                System.out.println("查数据库");

                synchronized (userKey) {
                    User dbuser = userService.findById(id);

                    if (dbuser != null) {
                        redisTemplate.opsForValue().set(userKey, dbuser);
                        bloomFilter.put(userKey);
                    } else {
                        redisTemplate.opsForValue().set(userKey, "null", 10, TimeUnit.SECONDS);
                        bloomFilter.put(userKey);
                    }
                }
            }
        }
        return (User) redisTemplate.opsForValue().get(userKey);
    }
}