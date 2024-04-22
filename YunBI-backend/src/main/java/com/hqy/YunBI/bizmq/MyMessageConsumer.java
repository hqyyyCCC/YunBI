package com.hqy.YunBI.bizmq;


import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class MyMessageConsumer {
    @RabbitListener(queues = {}, ackMode = "MANUAL")
    public void consumeMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("receiveMessage message ={}",message);
        try {
            channel.basicAck(deliveryTag,false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
