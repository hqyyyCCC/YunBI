package com.hqy.YunBI.controller;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@RestController
@RequestMapping("/thread")
@Slf4j
@Profile({"dev","local"})
public class ThreadExecutorController {
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @GetMapping("/add")
    public void add(String name) {
        //  启动一个异步任务一步执行Runnable任务
        CompletableFuture.runAsync(()->{
                log.info("任务执行:" + name + "线程名称:" + Thread.currentThread().getName());

                try {
                    Thread.sleep(100000L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        , threadPoolExecutor);
    }

    @GetMapping("/get")
    public String get() {
        // 创建一个map存储线程池的信息
        Map<String, Object> map = new HashMap();
        map.put("活跃进程:", threadPoolExecutor.getActiveCount());
        map.put("接受任务总数", threadPoolExecutor.getTaskCount());
        map.put("等待队列进程数:", threadPoolExecutor.getQueue().size());
        map.put("完成进程数：", threadPoolExecutor.getCompletedTaskCount());

        return JSONUtil.toJsonStr(map);
    }
}
