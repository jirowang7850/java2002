package com.qf.controller;


import com.qf.util.RedisUtil;
import com.qf.util.RedissonUtil;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
public class SecondKillController {
    private static Map<String, Integer> stockMap = new HashMap<>();
    private static Map<String, Integer> orderMap = new HashMap<>();

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private RedissonUtil redissonUtil;

    static {
        stockMap.put("商品",1000);
        orderMap.put("商品",0);
    }

    @RequestMapping("secondkill")
    public String secondkill(String item) throws InterruptedException {
        RedissonClient redissonClient = redissonUtil.createRedissonClient();
        redissonClient.getLock(item).lock(10,TimeUnit.SECONDS);

        try {
            //获取库存数量
            Integer stockNum = (Integer) stockMap.get(item);
            if (stockNum <= 0) {
                return "库存不足";
            }

            //模拟数据库操作，查询库存
            Thread.sleep(100);
            stockMap.put(item, stockNum - 1);//库存减一

            //获取订单数量
            Integer orderNum = (Integer) orderMap.get(item);
            //模拟数据库操作，创建订单
            orderMap.put(item, orderNum + 1);
        } finally {
            redissonClient.getLock(item).unlock();
        }


        return "秒杀成功：" + item + "，当前库存数量为：" + stockMap.get(item)
                + "，订单数量为：" + orderMap.get(item);
    }

//    @RequestMapping("secondKill")
//    public String secondKill(String item) throws InterruptedException {
//        boolean lock = redisUtil.lock(item, System.currentTimeMillis() + "", 10);
//
//        if(lock){
//            //获取库存数量
//            Integer stockNum = (Integer)stockMap.get(item);
//            if(stockNum<=0){
//                return "库存不足";
//            }
//
//            //模拟数据库操作，查询库存
//            Thread.sleep(100);
//            stockMap.put(item,stockNum-1);//库存减一
//
//            //获取订单数量
//            Integer orderNum  = (Integer) orderMap.get(item);
//            //模拟数据库操作，创建订单
//            orderMap.put(item,orderNum+1);
//            redisUtil.unLock(item);
//        }
//
//        return "秒杀成功："+item+"，当前库存数量为："+stockMap.get(item)+"，订单数量为："+orderMap.get(item);
//    }
}

