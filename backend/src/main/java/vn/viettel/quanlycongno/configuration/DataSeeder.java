package vn.viettel.quanlycongno.configuration;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import vn.viettel.quanlycongno.entity.Role;
import vn.viettel.quanlycongno.entity.Staff;
import vn.viettel.quanlycongno.constant.RoleEnum;
import vn.viettel.quanlycongno.repository.RoleRepository;
import vn.viettel.quanlycongno.repository.StaffRepository;

@Component
@AllArgsConstructor
@Slf4j
public class DataSeeder {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(DataSeeder.class);

    private final StaffRepository staffRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;

    @Bean
    public CommandLineRunner dataLoader() {
        return args -> {
            logger.atInfo().log("Seeding initial data...");
            Role adminRole = roleRepository.findByRoleName(RoleEnum.ADMIN)
                    .orElseThrow(() -> new RuntimeException("Admin role not found"));

            staffRepository.save(
                    new Staff("admin",
                            encoder.encode("admin123"),
                            adminRole)
            );
        };
    }
}
