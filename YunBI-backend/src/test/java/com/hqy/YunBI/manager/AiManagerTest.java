package com.hqy.YunBI.manager;

import com.hqy.YunBI.constant.CommonConstant;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class AiManagerTest {
    @Resource private AiManager aiManager;
    private static final long biModel = CommonConstant.BI_MODEL_ID;
    @Test
    public void doChat(){
        String res = aiManager.doChat(1766393070611812354L,"分析需求：\n" +
                "分析网站用户的增长情况\n" +
                "原始数据：\n" +
                "日期，用户数\n" +
                "1号，10\n" +
                "2号，20\n" +
                "3号，30");
        System.out.println(res);
    }
}
