package com.hqy.YunBI.bizmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class MyMessageProducer {
    @Resource
    public RabbitTemplate rabbitTemplate;

    public void sentMessage(String exchange,String routingKey,String message){
        rabbitTemplate.convertAndSend(exchange,routingKey,message);
    }
}
