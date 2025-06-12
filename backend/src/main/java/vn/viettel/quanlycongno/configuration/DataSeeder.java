//package vn.viettel.quanlycongno.configuration;
//
//import jakarta.transaction.Transactional;
//import lombok.AllArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Profile;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//import vn.viettel.quanlycongno.entity.*;
//import vn.viettel.quanlycongno.constant.RoleEnum;
//import vn.viettel.quanlycongno.repository.*;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.UUID;
//
//@Component
//@AllArgsConstructor
//@Slf4j
//public class DataSeeder {
//    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);
//
//    private final StaffRepository staffRepository;
//    private final CustomerRepository customerRepository;
//    private final RoleRepository roleRepository;
//    private final ContractRepository contractRepository;
//    private final InvoiceRepository invoiceRepository;
//    private final PasswordEncoder encoder;
//    private final JdbcTemplate jdbcTemplate = new JdbcTemplate();
//
//    @Bean
//    @Profile("prod")
//    @Transactional
//    public CommandLineRunner dataLoaderProd() {
//        return args -> {
//            logger.atInfo().log("Seeding initial data...");
//            Role adminRole = roleRepository.findByRoleName(RoleEnum.ADMIN)
//                    .orElseThrow(() -> new RuntimeException("Admin role not found"));
//            Role staffRole = roleRepository.findByRoleName(RoleEnum.STAFF)
//                    .orElseThrow(() -> new RuntimeException("Staff role not found"));
//
//            Staff adminStaff = new Staff("admin",
//                    encoder.encode("admin123"),
//                    adminRole); // Create admin staff with default credentials
//            staffRepository.save(adminStaff);
//            logger.atInfo().log("Default admin account created with username 'admin' and password 'admin123'.");
//
//            Staff staffMember1 = new Staff("staff1",
//                    encoder.encode("staff123"),
//                    staffRole); // Create a staff member with default credentials
//            staffRepository.save(staffMember1);
//            logger.atInfo().log("Default staff account created with username 'staff1' and password 'staff123'.");
//
//            Staff staffMember2 = new Staff("staff2",
//                    encoder.encode("staff123"),
//                    staffRole); // Create another staff member with default credentials
//            staffRepository.save(staffMember2);
//            logger.atInfo().log("Default staff account created with username 'staff2' and password 'staff123'.");
//
//            Customer defaultCustomer = new Customer("Default Customer", // Customer name
//                    "123456789", // Tax code,
//                    adminStaff, // Created by admin
//                    adminStaff); // Last updated by admin
//            customerRepository.save(defaultCustomer);
//            logger.atInfo().log("Default customer created with name 'Default Customer' and tax code '123456789'.");
//
//            Contract defaultContract = new Contract(
//                    "Test Contract", // Contract name
//                    adminStaff, // Created by admin
//                    adminStaff,
//                    staffMember1); // Last updated by admin
//            contractRepository.save(defaultContract);
//            logger.atInfo().log("Default contract created with name 'Test Contract'.");
//
//            // Create a default invoice
//            Invoice invoice1 = new Invoice(
//                    "INV1", // Invoice symbol
//                    "INV-0001", // Invoice number
//                    new BigDecimal("1000.00"), // Original amount
//                    "VND", // Currency type
//                    new BigDecimal("1.0"), // Exchange rate
//                    new BigDecimal("100.00"), // VAT
//                    new Date(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000), // Created date 30 days ago
//                    new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000), // Due date 30 days from now
//                    "Bank Transfer", // Payment method
//                    defaultContract, // Associated contract
//                    defaultCustomer, // Associated customer
//                    "PRJ-001", // Project ID
//                    staffMember1, // Staff in charge
//                    "IT Department", // Department
//                    adminStaff, // Created by
//                    adminStaff); // Last updated by
//
//            Invoice invoice2 = new Invoice(
//                    "INV2", // Invoice symbol
//                    "INV-0002", // Invoice number
//                    new BigDecimal("2000.00"), // Original amount
//                    "USD", // Currency type
//                    new BigDecimal("23000.00"), // Exchange rate
//                    new BigDecimal("200.00"), // VAT
//                    new Date(System.currentTimeMillis() - 15L * 24 * 60 * 60 * 1000), // Created date 15 days ago
//                    new Date(System.currentTimeMillis() + 45L * 24 * 60 * 60 * 1000), // Due date 45 days from now
//                    "Credit Card", // Payment method
//                    defaultContract, // Associated contract
//                    defaultCustomer, // Associated customer
//                    "PRJ-002", // Project ID
//                    staffMember2, // Staff in charge
//                    "Finance Department", // Department
//                    adminStaff, // Created by
//                    adminStaff); // Last updated by
//
//            invoiceRepository.save(invoice1);
//            invoiceRepository.save(invoice2);
//
//            logger.atInfo().log("Default invoice created with symbol 'INV1' and number 'INV-0001'.");
//            logger.atInfo().log("Default invoice created with symbol 'INV2' and number 'INV-0002'.");
//
//            logger.atInfo().log("Initial data seeded successfully.");
//        };
//    }
//
//    @Bean
//    @Profile("first-run") // Do not run in production profile
//    @Transactional
//    public CommandLineRunner dataLoaderDev() {
//        return args -> {
//            logger.atInfo().log("Seeding initial data...");
//            Role adminRole = roleRepository.findByRoleName(RoleEnum.ADMIN)
//                    .orElseThrow(() -> new RuntimeException("Admin role not found"));
//            Role staffRole = roleRepository.findByRoleName(RoleEnum.STAFF)
//                    .orElseThrow(() -> new RuntimeException("Staff role not found"));
//
//            Staff adminStaff;
//            Staff staffMember1;
//
//            try {
//                adminStaff = new Staff("admin",
//                        encoder.encode("admin123"),
//                        adminRole); // Create admin staff with default credentials
//                staffRepository.save(adminStaff);
//                logger.atInfo().log("Default admin account created with username 'admin' and password 'admin123'.");
//
////                staffMember1 = new Staff("staff1",
////                        encoder.encode("staff123"),
////                        staffRole); // Create a staff member with default credentials
////                staffRepository.save(staffMember1);
//                logger.atInfo().log("Default staff account created with username 'staff1' and password 'staff123'.");
//            } catch (Exception e) {
//                adminStaff = staffRepository.findByUsername("admin")
//                        .orElseThrow(() -> new RuntimeException("Admin staff not found"));
////                staffMember1 = staffRepository.findByUsername("staff1")
////                        .orElseThrow(() -> new RuntimeException("Staff member 'staff1' not found"));
//            }
////
////            for (int i = 1; i <= 100000; i++) {
////                Contract contract = new Contract(
////                        "Contract " + i, // Contract name
////                        adminStaff, // Created by admin
////                        adminStaff,
////                        staffMember1); // Last updated by admin
////                contractRepository.save(contract);
////            }
////
////            // Create indexes after data is loaded
////            final JdbcTemplate jdbcTemplate = new JdbcTemplate();
////
////            logger.atInfo().log("Creating database indexes...");
////            try {
////                // Drop existing indexes first
////                jdbcTemplate.execute("DROP INDEX IF EXISTS idx_contract_name ON contracts");
////                jdbcTemplate.execute("DROP INDEX IF EXISTS idx_assigned_to ON contracts");
////                jdbcTemplate.execute("DROP INDEX IF EXISTS idx_created_by ON contracts");
////                jdbcTemplate.execute("DROP INDEX IF EXISTS idx_last_updated_by ON contracts");
////
////
////                jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_contract_name ON contracts(contract_name)");
////                jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_assigned_to ON contracts(assigned_to)");
////                jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_created_by ON contracts(created_by)");
////                jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_last_updated_by ON contracts(last_updated_by)");
////
////                logger.atInfo().log("Database indexes created successfully.");
////            } catch (Exception e) {
////                logger.atError().log("Error creating indexes: " + e.getMessage());
////            }
//        };
//    }
//
//    @Bean
//    @Profile("dev")
//    public CommandLineRunner dataFeeder() {
//        return args -> {
//            final boolean DISABLED = true; // Change this to perform data feeding
//            if (DISABLED) return;
//            logger.atInfo().log("Data feeder is running...");
//            // This method can be used to run any additional data seeding logic
//            // For example, you can call dataLoaderProd() or dataLoaderDev() here
//            // based on your requirements.
//            Role staffRole = roleRepository.findByRoleName(RoleEnum.STAFF)
//                    .orElseThrow(() -> new RuntimeException("Staff role not found"));
//
//            Staff admin = staffRepository.findByUsername("admin")
//                    .orElseThrow(() -> new RuntimeException("Admin staff not found"));
//
//            List<Staff> importedStaff = new ArrayList<>();
//            for (int i = 1; i <= 100; ++i) {
//                Staff staff = new Staff("staff" + i,
//                        encoder.encode("staff123"),
//                        staffRole); // Create a staff member with default credentials
//                importedStaff.add(staffRepository.save(staff));
////                logger.atInfo().log("Default staff account created with username 'staff" + i + "' and password 'staff123'.");
//            }
//
//            importedStaff = (List<Staff>) staffRepository.findAll();
//            for (Staff staff : importedStaff) {
//                int customerCount = (int) (Math.random() * 1000) + 1; // Random number of customers per staff
//                int contractCount = (int) (Math.random() * 5000) + 1; // Random number of contracts per staff
//                int invoiceCount = (int) (Math.random() * 10000) + 1; // Random number of invoices per staff
//                Date createdDate = new Date(LocalDate.now().minusDays((int) (Math.random() * 365)).toEpochDay() * 24 * 60 * 60 * 1000);
//                List<Customer> customers = new ArrayList<>();
//                List<Contract> contracts = new ArrayList<>();
//                for (int j = 0; j < customerCount; j++) {
//                    Customer customer = new Customer("Customer " + (j + 1) + " of " + staff.getUsername(),
//                            "123456789-" + (j + 1), // Unique tax code
//                            staff, // Created by this staff member
//                            staff); // Last updated by this staff member
//                    customer.setAssignedStaff(staff); // Assign to this staff member
//                    Customer c = customerRepository.save(customer);
//                    c.getUsedToBeHandledByStaffs().add(staff); // Add staff member to the history
//                    customers.add(customerRepository.save(c));
//                }
//
//                for (int j = 0; j < contractCount; j++) {
//                    Contract contract = new Contract("Contract " + (j + 1) + " of " + staff.getUsername(),
//                            admin, // Created by admin
//                            admin, // Last updated by admin
//                            staff); // Assigned to this staff member
//                    contract.setCreatedDate(createdDate); // Set created date
//                    contracts.add(contractRepository.save(contract));
//                }
//
//                for (int j = 0; j < invoiceCount; j++) {
////                    Date createdDate = new Date(LocalDate.now().minusDays((int) (Math.random() * 365)).toEpochDay() * 24 * 60 * 60 * 1000);
//                    Date dueDate = new Date(createdDate.getTime() + (long) (Math.random() * 30 + 1) * 24 * 60 * 60 * 1000); // Due date within 1 month of created date
//                    Invoice invoice = new Invoice(
//                            "INV-" + UUID.randomUUID().toString().substring(0,3) + (j + 1) + " of " + staff.getUsername(), // Invoice symbol
//                            "INV-000" + (j + 1) + "-" + staff.getUsername(), // Invoice number
//                            BigDecimal.valueOf(1000 + j), // Original amount
//                            List.of("VND", "USD").get((int) Math.round(Math.random())), // Currency type
//                            BigDecimal.valueOf(Math.random() * 25000), // Exchange rate
//                            BigDecimal.valueOf(j % 100), // VAT
//                            createdDate, // Created date
//                            dueDate, // Due date
//                            "Bank Transfer", // Payment method
//                            contracts.get((int) (Math.random() * (contracts.size() - 1))), // Associated contract
//                            customers.get((int) (Math.random() * (customers.size() - 1))), // Associated customer
//                            "PRJ-00" + (j % 10), // Project ID
//                            staff, // Staff in charge
//                            "IT Department", // Department
//                            admin, // Created by admin
//                            admin); // Last updated by admin
//                    invoice.setCreatedDate(createdDate); // Set created date
//                    invoiceRepository.save(invoice);
//                }
//            }
//
//            jdbcTemplate.execute("DROP INDEX IF EXISTS idx_staff_id ON invoices");
//            jdbcTemplate.execute("DROP INDEX IF EXISTS idx_contract_id ON invoices");
//            jdbcTemplate.execute("DROP INDEX IF EXISTS idx_customer_id ON invoices");
//
//
//            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_staff_id ON invoices(staff_id)");
//            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_contract_id ON invoices(contract_id)");
//            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_customer_id ON invoices(customer_id)");
//
//            logger.atInfo().log("Data feeder completed successfully.");
//
//        };
//    }
//
//    @Bean
//    public CommandLineRunner onetimeRunner() {
//        return args -> {
//            System.out.println("Running one-time data seeder...");
//            logger.atInfo().log("Running one-time data seeder...");
//            // This method can be used to run any one-time data seeding logic
//            // For example, you can call dataLoaderProd() or dataLoaderDev() here
//            // based on your requirements.
//            List<Contract> contracts = contractRepository.findAll();
////            Date createdDate = new Date(LocalDate.now().minusDays((int) (Math.random() * 365)).toEpochDay() * 24 * 60 * 60 * 1000);
////            Date dueDate = new Date(createdDate.getTime() + (long) (Math.random() * 30 + 1) * 24 * 60 * 60 * 1000); // Due date within 1 month of created date
//
//            for (Contract contract : contracts) {
//                Date createdDate = new Date(LocalDate.now().minusDays((int) (Math.random() * 365)).toEpochDay() * 24 * 60 * 60 * 1000);
////                Date dueDate = new Date(createdDate.getTime() + (long) (Math.random() * 30 + 1) * 24 * 60 * 60 * 1000); // Due date within 1 month of created date
//                contract.setCreatedDate(createdDate);
//                contractRepository.save(contract);
//            }
//
//            List<Invoice> invoices = invoiceRepository.findAll();
//            for (Invoice invoice : invoices) {
//                Date createdDate = invoice.getContract().getCreatedDate();
//                Date dueDate = new Date(createdDate.getTime() + (long) (Math.random() * 30 + 1) * 24 * 60 * 60 * 1000); // Due date within 1 month of created date
//                invoice.setCreatedDate(createdDate);
//                invoice.setDueDate(dueDate);
//                invoiceRepository.save(invoice);
//            }
//            logger.atInfo().log("One-time data seeder completed successfully.");
//        };
//    }
//}
