package vn.viettel.quanlycongno;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class QuanlycongnoApplicationTests {

	@Test
	void shouldContextLoads() {
		assertDoesNotThrow(() -> {
			// This will throw an exception if the context fails to load
			ApplicationContext context = QuanlycongnoApplication.context;
		});

		assertNotNull(QuanlycongnoApplication.context, "Application context should not be null");
	}

}
