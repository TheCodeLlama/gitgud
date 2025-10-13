package com.syntaxllama.gitgud.backend.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for code execution queue.
 */
@Configuration
public class RabbitMQConfig {

    public static final String CODE_EXECUTION_QUEUE = "code.execution.queue";
    public static final String CODE_EXECUTION_EXCHANGE = "code.execution.exchange";
    public static final String CODE_EXECUTION_ROUTING_KEY = "code.execution";

    public static final String DEAD_LETTER_QUEUE = "code.execution.dlq";
    public static final String DEAD_LETTER_EXCHANGE = "code.execution.dlx";
    public static final String DEAD_LETTER_ROUTING_KEY = "code.execution.dlq";

    @Value("${spring.rabbitmq.listener.simple.concurrency:5}")
    private int concurrency;

    @Value("${spring.rabbitmq.listener.simple.max-concurrency:10}")
    private int maxConcurrency;

    /**
     * Main code execution queue.
     * Messages that fail processing will be sent to dead letter queue.
     */
    @Bean
    public Queue codeExecutionQueue() {
        return QueueBuilder.durable(CODE_EXECUTION_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    /**
     * Dead letter queue for failed executions.
     */
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
    }

    /**
     * Main exchange for code execution.
     */
    @Bean
    public DirectExchange codeExecutionExchange() {
        return new DirectExchange(CODE_EXECUTION_EXCHANGE);
    }

    /**
     * Dead letter exchange.
     */
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE);
    }

    /**
     * Binding between execution queue and exchange.
     */
    @Bean
    public Binding codeExecutionBinding() {
        return BindingBuilder
                .bind(codeExecutionQueue())
                .to(codeExecutionExchange())
                .with(CODE_EXECUTION_ROUTING_KEY);
    }

    /**
     * Binding between dead letter queue and exchange.
     */
    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(DEAD_LETTER_ROUTING_KEY);
    }

    /**
     * JSON message converter for serializing/deserializing job objects.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate with JSON converter.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    /**
     * Listener container factory with JSON converter and concurrency settings.
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setConcurrentConsumers(concurrency);
        factory.setMaxConcurrentConsumers(maxConcurrency);
        return factory;
    }
}
