package com.study.rabbitmq;

import com.study.rabbitmq.pojo.MessageReceiver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Slf4j
@Configuration
public class RabbitMQConfiguration {
    public static final String QUEUE_NAME = "testQueue";
    public static final String EXCHANGE_NAME = "testExchange";
    public static final String ROUTING_KEY = "testRoutingKey";
    public static final String DEAD_LETTER_QUEUE_NAME = "testDeadLetterQueue";
    public static final String DEAD_LETTER_EXCHANGE_NAME = "testDeadLetterExchange";
    public static final String DEAD_LETTER_ROUTING_KEY = "testDeadLetterRoutingKey";
    public static final String ANOTHER_QUEUE = "anotherQueue";
    private static final String ANOTHER_EXCHANGE = "anotherExchange";
    public static final String ANOTHER_ROUTING_KEY = "anotherRoutingKey";

    /**
     * Create a queue with dead letter exchange and routing key , with time to live as 10 seconds , that means after
     * 10 seconds the message will be moved to dead letter queue , if message is not consumed.
     *
     * @return Queue
     */
    @Bean
    public Queue testQueue() {
        log.info("Creating queue");
        return QueueBuilder.durable(QUEUE_NAME).deadLetterExchange(DEAD_LETTER_EXCHANGE_NAME).deadLetterRoutingKey(
                DEAD_LETTER_ROUTING_KEY).ttl(10000).build();
    }

    @Bean
    public Queue anotherQueue() {
        log.info("Another Queue");
        return QueueBuilder.durable(ANOTHER_QUEUE).deadLetterExchange(DEAD_LETTER_EXCHANGE_NAME).deadLetterRoutingKey(
                DEAD_LETTER_ROUTING_KEY).ttl(10000).build();
    }

    /**
     * Create an exchange.
     *
     * @return Exchange
     */
    @Bean
    public TopicExchange testExchange() {
        log.info("Creating {}", EXCHANGE_NAME);
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public TopicExchange anotherExchange() {
        log.info("Creating {}", ANOTHER_EXCHANGE);
        return new TopicExchange(ANOTHER_EXCHANGE);
    }

    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(DEAD_LETTER_EXCHANGE_NAME);
    }

    @Bean
    public Queue testDeadLetterQueue() {
        return new Queue(DEAD_LETTER_QUEUE_NAME, true, false, false);
    }
    @Bean
    public Binding binding(Queue testQueue, TopicExchange testExchange) {
        return BindingBuilder.bind(testQueue).to(testExchange).with(ROUTING_KEY);
    }

    @Bean
    public Binding anotherBinding(Queue anotherQueue, TopicExchange anotherExchange) {
        return BindingBuilder.bind(anotherQueue).to(anotherExchange).with(ANOTHER_ROUTING_KEY);
    }

    @Bean
    public Binding deadLetterBinding(Queue testDeadLetterQueue, TopicExchange deadLetterExchange) {
        return BindingBuilder.bind(testDeadLetterQueue).to(deadLetterExchange).with(DEAD_LETTER_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory =
                new SimpleRabbitListenerContainerFactory();
        simpleRabbitListenerContainerFactory.setConnectionFactory(connectionFactory);
        simpleRabbitListenerContainerFactory.setMessageConverter(messageConverter());
        simpleRabbitListenerContainerFactory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        simpleRabbitListenerContainerFactory.setAutoStartup(true);
        simpleRabbitListenerContainerFactory.setMissingQueuesFatal(false);

        ExponentialBackOffPolicy exponentialBackOffPolicy = new ExponentialBackOffPolicy();
        exponentialBackOffPolicy.setInitialInterval(1000);
        exponentialBackOffPolicy.setMultiplier(2);
        exponentialBackOffPolicy.setMaxInterval(10000);

        SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy();
        simpleRetryPolicy.setMaxAttempts(1);

        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(simpleRetryPolicy);
        retryTemplate.setBackOffPolicy(exponentialBackOffPolicy);
        retryTemplate.setThrowLastExceptionOnExhausted(true);

        simpleRabbitListenerContainerFactory.setRetryTemplate(retryTemplate);

        return simpleRabbitListenerContainerFactory;
    }

    @Bean
    public MessageListenerAdapter listenerAdapter(MessageReceiver messageReceiver) {
        return new MessageListenerAdapter(messageReceiver, "receiveMessage");
    }

    @Bean
    public ConnectionFactory connectionFactory(
            @Value("${spring.rabbitmq.host}") String host,
            @Value("${spring.rabbitmq.port}") int port,
            @Value("${spring.rabbitmq.username}") String username,
            @Value("${spring.rabbitmq.password}") String password) {

        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setChannelCacheSize(25);
        log.info("Creating connection factory {}", connectionFactory);
        return connectionFactory;
    }
}
