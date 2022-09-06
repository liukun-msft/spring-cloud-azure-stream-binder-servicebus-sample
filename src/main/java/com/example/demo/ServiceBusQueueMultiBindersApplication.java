package com.example.demo;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;


@SpringBootApplication
public class ServiceBusQueueMultiBindersApplication {

	private static final Scheduler scheduler = Schedulers.newBoundedElastic(100, Integer.MAX_VALUE, "provision-thread");

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusQueueMultiBindersApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(ServiceBusQueueMultiBindersApplication.class, args);
	}

	@Bean
	public Function<Message<String>, Message<String>> function1() {
		return message -> {
			System.out.println("function1 received: " + message);
			Mono.when(
					Mono.fromRunnable(() -> {
						LOGGER.info("function1 Update Azure start " + message);
						sleep(2);
						LOGGER.info("function1 Update Azure finished " + message);
					}).publishOn(scheduler),
					Mono.fromRunnable(() -> {
						LOGGER.info("function1 Update K8s start " + message);
						sleep(1);
						LOGGER.info("function1 Update K8s finished " + message);
					}).publishOn(scheduler)
			).block();

			System.out.println("function1 finished " + message);
			return message;
		};
	}

	@Bean
	public Function<Message<String>, Message<String>> function2() {
		return message -> {
			System.out.println("function2 received: " + message);

			Mono.when(
					Mono.fromRunnable(() -> {
						LOGGER.info("function2 Update Azure start " + message);
						sleep(2);
						LOGGER.info("function2 Update Azure finished " + message);
					}).publishOn(scheduler),
					Mono.fromRunnable(() -> {
						LOGGER.info("function2 Update K8s start " + message);
						sleep(1);
						LOGGER.info("function2 Update K8s finished " + message);
					}).publishOn(scheduler)).block();

			System.out.println("function2 finished " + message);
			return message;
		};
	}

	private void sleep(int minutes){
		try {
			TimeUnit.MINUTES.sleep(minutes);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}