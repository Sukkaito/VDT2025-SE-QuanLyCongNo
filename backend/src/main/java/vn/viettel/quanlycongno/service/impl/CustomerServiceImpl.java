package vn.viettel.quanlycongno.service.impl;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.viettel.quanlycongno.dto.ContractDto;
import vn.viettel.quanlycongno.dto.CustomerDto;
import vn.viettel.quanlycongno.dto.InvoiceDto;
import vn.viettel.quanlycongno.dto.base.PagedResponse;
import vn.viettel.quanlycongno.entity.Customer;
import vn.viettel.quanlycongno.entity.Staff;
import vn.viettel.quanlycongno.repository.CustomerRepository;
import vn.viettel.quanlycongno.repository.InvoiceRepository;
import vn.viettel.quanlycongno.repository.StaffRepository;
import vn.viettel.quanlycongno.service.AuthenticationService;
import vn.viettel.quanlycongno.service.ContractService;
import vn.viettel.quanlycongno.service.CustomerService;
import vn.viettel.quanlycongno.service.InvoiceService;
import vn.viettel.quanlycongno.util.CsvUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final StaffRepository staffRepository;
    private final AuthenticationService authenticationService;
    private final ModelMapper mapper;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceService invoiceService;
    private final ContractService contractService;

    public PagedResponse<CustomerDto> getAllCustomers(int page, int size, String sortBy, boolean sortAsc) {
        Pageable pageable = getPageableCustomer(page, size, sortBy, sortAsc);

        Page<CustomerDto> customerPage = customerRepository.findAll(pageable)
                .map(this::toContractDto);
        return PagedResponse.from(customerPage, customerPage.getContent());
    }

    public CustomerDto getCustomerById(String id) {
        return customerRepository.findById(id)
                .map(this::toContractDto)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
    }

    @Transactional
    public CustomerDto saveCustomer(CustomerDto customerDto) {

        Staff assignedStaff = null;
        if (customerDto.getAssignedStaffUsername() != null) {
            assignedStaff = staffRepository.findByUsername(customerDto.getAssignedStaffUsername())
                    .orElseThrow(() -> new RuntimeException("Assigned staff not found with username: " + customerDto.getAssignedStaffUsername()));
        }

        // Get required staff entities
        Staff createdBy = staffRepository.findByUsername(authenticationService.getCurrentUsername())
                .orElseThrow(() -> new RuntimeException("Created/updated by staff not found"));

        // Create new customer entity
        Customer customer = new Customer();

        // Map other properties from DTO to entity
        mapper.map(customerDto, customer);

        // Set staff relationships
        if (assignedStaff != null) {
            customer.setAssignedStaff(assignedStaff);
            customer.getUsedToBeHandledByStaffs().add(assignedStaff);
        }
        customer.setCreatedBy(createdBy);
        customer.setLastUpdatedBy(createdBy);

        // Save and return
        Customer savedCustomer = customerRepository.save(customer);
        return toContractDto(savedCustomer);
    }

    @Transactional
    public CustomerDto updateCustomer(String id, CustomerDto customerDto) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
        Staff assignedStaff = null;
        if (customerDto.getAssignedStaffUsername() != null) {
            assignedStaff = staffRepository.findByUsername(customerDto.getAssignedStaffUsername())
                    .orElseThrow(() -> new RuntimeException("Assigned staff not found"));
            if (!customer.getAssignedStaff().getUsername().equals(assignedStaff.getUsername()) &&
                    !authenticationService.isAdmin()) {
                throw new RuntimeException("Only admin can change assigned staff for customers");
            }
        }
        Staff lastUpdatedBy = staffRepository.findByUsername(authenticationService.getCurrentUsername())
                .orElseThrow(() -> new RuntimeException("Last updated by staff not found"));

//        System.out.println(customerDto.getUsedToBeHandledByStaffUsernames());

        mapper.map(customerDto, customer);
        if (assignedStaff != null) {
            customer.getUsedToBeHandledByStaffs().add(assignedStaff);
        }
        customer.setAssignedStaff(assignedStaff);
        customer.setLastUpdatedBy(lastUpdatedBy);
        return toContractDto(customerRepository.save(customer));
    }

    @Transactional
    public void deleteCustomer(String customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new RuntimeException("Customer not found with id: " + customerId);
        }

        if (invoiceRepository.existsByCustomer_CustomerId(customerId)) {
            throw new RuntimeException("Cannot delete customer with existing invoices");
        }

        customerRepository.deleteById(customerId);
    }

    public PagedResponse<CustomerDto> searchCustomers(String query,
                                             String assignedStaffUsername,
                                             String createdByUsername,
                                             Date createDateStart,
                                             Date createDateEnd,
                                             Date lastUpdateStart,
                                             Date lastUpdateEnd,
                                             int page, int size, String sortBy, boolean sortAsc) {
        Pageable pageable = getPageableCustomer(page, size, sortBy, sortAsc);

        Page<CustomerDto> customerPage =  customerRepository.searchByCriteria(query, assignedStaffUsername, createdByUsername,
                        createDateStart, createDateEnd, lastUpdateStart, lastUpdateEnd, pageable)
                .map(customer -> mapper.map(customer, CustomerDto.class));
        return PagedResponse.from(customerPage, customerPage.getContent());
    }

    private static Pageable getPageableCustomer(int page, int size, String sortBy, boolean sortAsc) {
        Set<String> allowedSortFields = Arrays.stream(Customer.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());

        allowedSortFields.addAll(
                Set.of("createdBy", "lastUpdatedBy", "createdDate", "lastUpdatedDate", "customerName", "abbreviationName")
        );

        // Validate and default if invalid
        String validatedSortField = allowedSortFields.contains(sortBy) ? sortBy : "customerName";

        return PageRequest.of(page, size, Sort.by(sortAsc ? Sort.Direction.ASC : Sort.Direction.DESC, validatedSortField));
    }

    @Transactional
    public List<CustomerDto> importCustomersFromCsv(MultipartFile file) throws IOException {
        List<CustomerDto> importedCustomers = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            // Read and parse header line
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("CSV file is empty");
            }

            String[] headers = parseCustomerCsvLine(headerLine);
            // Find column indexes for required fields
            int customerNameIndex = -1;
            int taxCodeIndex = -1;
            int abbreviationNameIndex = -1;
            int assignedStaffUsernameIndex = -1;

            for (int i = 0; i < headers.length; i++) {
                String header = headers[i].trim().toLowerCase();
                System.out.println(header);
                switch (header) {
                case "customer name", "customername" -> customerNameIndex = i;
                case "tax code", "taxcode" -> taxCodeIndex = i;
                case "abbreviation name", "abbreviationname" -> abbreviationNameIndex = i;
                case "assigned staff username", "assignedstaffusername" -> assignedStaffUsernameIndex = i;
                }
            }

            if (customerNameIndex == -1 || taxCodeIndex == -1) {
                throw new IOException("CSV file must contain 'Customer Name' and 'Tax Code' columns");
            }

            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = parseCustomerCsvLine(line);
                if (values.length <= Math.max(customerNameIndex, taxCodeIndex)) continue; // Skip invalid lines

                CustomerDto customerDto = new CustomerDto(
                        null,
                        values[customerNameIndex].trim(),
                        values[taxCodeIndex].trim(),
                        abbreviationNameIndex >= 0 && values.length > abbreviationNameIndex ? values[abbreviationNameIndex].trim() : null,
                        assignedStaffUsernameIndex >= 0 && values.length > assignedStaffUsernameIndex ? values[assignedStaffUsernameIndex].trim() : null,
                        null, null, null, null, new ArrayList<>()
                );

                // Add the customer if essential fields are present and not empty
                if (!customerDto.getCustomerName().isEmpty() && !customerDto.getTaxCode().isEmpty()) {
                    importedCustomers.add(saveCustomer(customerDto));
                }
            }
        }
        return importedCustomers;
    }

    // Helper method to properly parse CSV lines, handling quoted fields
    private String[] parseCustomerCsvLine(String line) {
        return CsvUtils.parseCsvLine(line);
    }

    public Resource exportCustomersToCsv(String query, String assignedStaffUsername, String createdByUsername,
                                         Date createDateStart, Date createDateEnd,
                                         Date lastUpdateStart, Date lastUpdateEnd,
                                         int page, int size, String sortBy, boolean sortAsc) {
        Pageable pageable = getPageableCustomer(page, size, sortBy, sortAsc);

        List<Customer> customers = customerRepository.searchByCriteria(query, assignedStaffUsername, createdByUsername,
                createDateStart, createDateEnd, lastUpdateStart, lastUpdateEnd, pageable).toList();

        try (ByteArrayOutputStream ignored = new ByteArrayOutputStream()) {
            // Write CSV header with only fields available in DTO
            StringBuilder csvContent = new StringBuilder();
            csvContent.append("Customer ID,Customer Name,Tax Code,Abbreviation Name,Assigned Staff,Created Date,Created By,Last Updated Date,Last Updated By\n");

            // Write data rows
            for (Customer customer : customers) {
                csvContent.append(escapeCsvField(customer.getCustomerId())).append(",");
                csvContent.append(escapeCsvField(customer.getCustomerName())).append(",");
                csvContent.append(escapeCsvField(customer.getTaxCode())).append(",");
                csvContent.append(escapeCsvField(customer.getAbbreviationName())).append(",");
                csvContent.append(escapeCsvField(customer.getAssignedStaff() != null? customer.getAssignedStaff().getUsername() : null)).append(",");
                csvContent.append(customer.getCreatedDate() != null ? customer.getCreatedDate().toString() : "").append(",");
                csvContent.append(escapeCsvField(customer.getCreatedBy().getUsername())).append(",");
                csvContent.append(customer.getLastUpdateDate() != null ? customer.getLastUpdateDate().toString() : "").append(",");
                csvContent.append(escapeCsvField(customer.getLastUpdatedBy().getUsername())).append("\n");
            }

            byte[] bytes = csvContent.toString().getBytes();
            return new ByteArrayResource(bytes);
        } catch (IOException e) {
            throw new RuntimeException("Failed to export customers to CSV", e);
        }
    }

    // Helper method to escape CSV fields that might contain commas
    private String escapeCsvField(String field) {
        return CsvUtils.escapeCsvField(field);
    }

    public CustomerDto toContractDto(Customer customer) {
        CustomerDto dto = mapper.map(customer, CustomerDto.class);
        dto.setUsedToBeHandledByStaffUsernames(
                customer.getUsedToBeHandledByStaffs().stream().map(Staff::getUsername).toList()
        );
        return dto;
    }

    @Override
    public PagedResponse<InvoiceDto> getInvoicesByCustomerId(
            String customerId,
            int page,
            int size,
            String sortBy,
            boolean sortAsc
    ) {
        return invoiceService.getInvoiceByCustomerId(customerId, page, size, sortBy,sortAsc);
    }

    @Override
    public PagedResponse<ContractDto> getContractsByCustomerId(
            String customerId,
            int page,
            int size,
            String sortBy,
            boolean sortAsc
    ) {
        return contractService.getContractsByCustomerId(customerId, page, size, sortBy,sortAsc);
    }
}
