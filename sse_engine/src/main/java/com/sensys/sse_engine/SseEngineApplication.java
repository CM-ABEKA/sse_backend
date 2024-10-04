package com.sensys.sse_engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class SseEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(SseEngineApplication.class, args);
	}

}
