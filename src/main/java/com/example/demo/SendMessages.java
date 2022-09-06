package com.example.demo;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;

import java.time.Duration;

public class SendMessages {

    @Value("${spring.cloud.azure.servicebus.connection-string}")
    private String serviceBusConnectionString;

    @Value("${spring.cloud.stream.bindings.function1-in-0.destination}")
    private String queueName1;

    @Value("${spring.cloud.stream.bindings.function2-in-0.destination}")
    private String queueName2;


    public static void main(String[] args) {
        SpringApplication.run(SendMessages.class, args);
    }

    private static final int SEND_MESSAGE_NUMBER = 2;

    @Bean
    public void sendMessage() {
        AmqpRetryOptions options = new AmqpRetryOptions();
        options.setTryTimeout(Duration.ofMinutes(1L));

        ServiceBusSenderClient sender = new ServiceBusClientBuilder()
                .connectionString(serviceBusConnectionString)
                .retryOptions(options)
                .sender()
                .queueName(queueName1)
                .buildClient();


        for (int i = 0; i < SEND_MESSAGE_NUMBER; i++) {
            ServiceBusMessage message = new ServiceBusMessage("Message Content ID: " + i).setMessageId("" + i);

            sender.sendMessage(message);

            System.out.println("Send Message id: " + i);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        sender.close();
    }
}
