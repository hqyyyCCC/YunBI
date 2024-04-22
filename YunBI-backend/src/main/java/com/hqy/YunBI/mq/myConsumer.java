package com.hqy.YunBI.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class myConsumer {
    private static final String EXCHANGE_NAME = "Test";
    public static void main(String[] argv) throws Exception {
        // 创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        // 创建连接
        Connection connection = factory.newConnection();

        Channel channel1 = connection.createChannel();
        Channel channel2 =connection.createChannel();
        channel1.exchangeDeclare(EXCHANGE_NAME, "fanout");

        String queueName1 = "zhangsan_queue";
        channel1.queueDeclare(queueName1, true, false, false, null);
        channel1.queueBind(queueName1,EXCHANGE_NAME,"");


        String queueName2 = "lisi_queue";
        channel1.queueDeclare(queueName2, true, false, false, null);
        channel2.queueBind(queueName2,EXCHANGE_NAME,"");



        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback1 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [zhangsan] Received '" + message + "'");
        };
        DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [lisi] Received '" + message + "'");
        };
        channel1.basicConsume(queueName1, true, deliverCallback1, consumerTag -> { });
        channel1.basicConsume(queueName2, true, deliverCallback2, consumerTag -> { });
    }

}
