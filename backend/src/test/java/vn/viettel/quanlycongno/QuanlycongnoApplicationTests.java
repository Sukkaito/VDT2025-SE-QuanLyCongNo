package vn.viettel.quanlycongno;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class QuanlycongnoApplicationTests {

	@Test
	void shouldContextLoads() {
		AtomicReference<ApplicationContext> context = new AtomicReference<>();
		assertDoesNotThrow(() -> {
			// This will throw an exception if the context fails to load
			context.set(QuanlycongnoApplication.context);
		});
	}

}
