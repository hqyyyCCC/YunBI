package com.hqy.YunBI.bizmq;

import com.hqy.YunBI.common.ErrorCode;
import com.hqy.YunBI.constant.BiMqConstant;
import com.hqy.YunBI.constant.CommonConstant;
import com.hqy.YunBI.exception.BusinessException;
import com.hqy.YunBI.manager.AiManager;
import com.hqy.YunBI.model.entity.Chart;
import com.hqy.YunBI.service.ChartService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
@Component
@Slf4j
public class BiMessageConsumer {
    @Resource
    private ChartService chartService;
    @Resource
    private AiManager aiManager;
    @RabbitListener(queues = {BiMqConstant.BI_QUEUE_NAME}, ackMode = "MANUAL")
    public void consumeMessage(String message,Channel channel,@Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("receiveMessage message chartID ={}",message);
        try {
            channel.basicAck(deliveryTag,false);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"channel确认信息失败");
        }
        long chartId = Long.parseLong(message);
        Chart chart = chartService.getById(chartId);
        // 根据ID取得预存图表信息为空
        if(chart == null){
            try {
                channel.basicNack(deliveryTag,false,true);
            } catch (IOException e) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"图表为空");
            }

        }
        Chart updateChar = new Chart();
        updateChar.setId(chartId);
        updateChar.setStatus("running");
        boolean saveResult = chartService.updateById(updateChar);
        if(!saveResult){
            chartService.handleChartUpdateError(chartId,"更新图表成功状态失败");
        }


        //调用AI分析
        String res = aiManager.doChat(CommonConstant.BI_MODEL_ID,buildUserInput(chart));
        //对分析结果进行括号拆分
        String[] split = res.split("【【【【【");

        if(split.length<3){
            chartService.handleChartUpdateError(chart.getId(),"AI生成参数错误");
        }
        // 数据持久化
        String genData = split[1].trim();
        //AI生成数据提取Json字符串
        String genChart = genData.substring(genData.indexOf("{"),genData.lastIndexOf("}")+1).replace("'","\"");
        String genResult = split[2].trim();

        Chart updateChartResult  = new Chart();
        updateChartResult.setId(chartId);
        updateChartResult.setGenChart(genChart);
        updateChartResult.setGenResult(genResult);
        updateChartResult.setStatus("succeed");
        boolean updateResult = chartService.updateById(updateChartResult);
        if(!updateResult){
            chartService.handleChartUpdateError(chart.getId(),"更新图表成功状态失败");
        }
        try {
            channel.basicAck(deliveryTag,false);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"channel确认信息失败");
        }
    }

    private String buildUserInput(Chart chart){
        String name = chart.getName();
        String goal = chart.getGoal();
        String chartType = chart.getChartType();
        String chartCsvData = chart.getChartData();
        // 用户输入
        StringBuilder  userInput = new StringBuilder();
        userInput.append("你是一个数据分析师和前端专家,接下来我会按照固定格式给你提供内容：" +
                "分析需求：" +
                "{数据分析的需求或者目标" +
                "原始数据：" +
                "【【【【【" +
                "{前端Echarts V5的option配置对象JSON代码，并严格使用双引号包裹JSON中的键,合理地将数据进行可视化,不要生成任何多余的内容比如注释}" +
                "【【【【【" +
                "{明确的数据分析结论、越详细越好，不要生成多余注释}").append("\n");
        String userGoal =goal;
        if(StringUtils.isNotBlank(chartType)){
            userGoal+= ",请使用"+chartType;
        }
        userInput.append("分析需求:").append(userGoal).append("\n");
        userInput.append("原始数据:").append(chartCsvData).append("\n");

        return userInput.toString();
    }


}
