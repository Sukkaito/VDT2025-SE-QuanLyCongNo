package vn.viettel.quanlycongno;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class QuanlycongnoApplication {
	public static ApplicationContext context;

	public static void main(String[] args) {
		context = SpringApplication.run(QuanlycongnoApplication.class, args);
	}

	@Bean
	CommandLineRunner commandLineRunner() {
		return args -> {
			// Code to run on application startup
			System.out.println("Application started successfully!");
		};
	}

}
