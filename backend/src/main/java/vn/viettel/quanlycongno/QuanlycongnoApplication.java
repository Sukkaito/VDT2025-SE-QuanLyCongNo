package vn.viettel.quanlycongno;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication(scanBasePackages = "vn.viettel.quanlycongno")
@EnableCaching
@EnableScheduling
public class QuanlycongnoApplication {
	public static ApplicationContext context;

	public static void main(String[] args) {
		System.setProperty("spring.profiles.active", "dev");
		context = SpringApplication.run(QuanlycongnoApplication.class, args);
	}

	@Bean
	CommandLineRunner commandLineRunner() {
		return args -> {
			// Code to run on application startup
			System.out.println("Application started successfully!");
		};
	}

	@Scheduled(cron = "0 */10 * * * *")
	private void cron() {
		for (int i = 5; i <= 20; i = i + 5 ) {
			context.getBean("MetricServiceImpl", vn.viettel.quanlycongno.service.impl.MetricServiceImpl.class)
					.getTopStaffForMonth(i);
			context.getBean("MetricServiceImpl", vn.viettel.quanlycongno.service.impl.MetricServiceImpl.class)
					.getTopStaffAllTime(i);
		}
	}
}
