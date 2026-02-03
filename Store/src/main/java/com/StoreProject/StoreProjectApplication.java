package com.StoreProject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class StoreProjectApplication {
	public static void main(String[] args) {
		SpringApplication.run(StoreProjectApplication.class, args);
	}
}