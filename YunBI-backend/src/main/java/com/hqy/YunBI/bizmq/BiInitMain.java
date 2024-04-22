package com.hqy.YunBI.bizmq;

import com.hqy.YunBI.constant.BiMqConstant;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class BiInitMain {
    // 创建BI交换机，队列，以及绑定关系
    public static void main(String[] args) {
        try {

            //创建BI 交换机，队列及绑定关系

            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost("localhost");
            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();
            // fanout 传递给所有绑定 direct 选择性转发
            channel.exchangeDeclare(BiMqConstant.BI_EXCHANGE_NAME,"direct");
            //声明队列
            channel.queueDeclare(BiMqConstant.BI_QUEUE_NAME,true,false,false,null);
            //绑定关系
            channel.queueBind(BiMqConstant.BI_QUEUE_NAME,BiMqConstant.BI_EXCHANGE_NAME,BiMqConstant.BI_ROUTING_KEY);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
