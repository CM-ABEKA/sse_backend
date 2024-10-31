package com.sensys.sse_engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
@ConfigurationPropertiesScan("com.sensys.sse_engine.config")
public class SseEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(SseEngineApplication.class, args);
	}

}
