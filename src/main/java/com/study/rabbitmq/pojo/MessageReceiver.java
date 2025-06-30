package com.study.rabbitmq.pojo;

import com.rabbitmq.client.Channel;
import com.study.rabbitmq.RabbitMQConfiguration;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MessageReceiver {

    @RabbitListener(queues = {RabbitMQConfiguration.QUEUE_NAME, RabbitMQConfiguration.ANOTHER_QUEUE},
            containerFactory = "simpleRabbitListenerContainerFactory", ackMode = "MANUAL")
    public void receiveMessage(
            @Payload CustomMessage message,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag, Channel channel) throws IOException {
        try {
            System.out.println("Received message Text: " + message.getText());
            System.out.println("Received message Priority: " + message.getPriority());
            System.out.println("Delivery Tag: " + deliveryTag);

            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            System.out.println("Exception message Text: " + e.getMessage());
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
