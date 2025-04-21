package com.example.leave_management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LeaveManagementSystemApplication {

	public static void main(String[] args) {
		System.setProperty("jwt.scret", "scret@124");
		System.setProperty("jwt.expiration", "86400000");
		SpringApplication.run(LeaveManagementSystemApplication.class, args);
	}

}
