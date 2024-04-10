package com.github.vitaliiev.t1aspect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class T1aspectApplication {

	public static void main(String[] args) {
		SpringApplication.run(T1aspectApplication.class, args);
	}

}
