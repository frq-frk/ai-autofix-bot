package com.saiyans.autofix.ai_review_bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAutoConfiguration
@EnableScheduling
public class AiReviewBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiReviewBotApplication.class, args);
	}

}
