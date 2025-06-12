package vn.viettel.quanlycongno.controller;

import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import vn.viettel.quanlycongno.dto.CustomerDto;
import vn.viettel.quanlycongno.dto.base.ApiResponse;
import vn.viettel.quanlycongno.service.AuthenticationService;
import vn.viettel.quanlycongno.service.CustomerService;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/customers")
@AllArgsConstructor
public class CustomerController {
    private final CustomerService customerService;
    private final AuthenticationService authenticationService;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_STAFF')")
    public ApiResponse<?> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "customerName") String sortBy,
            @RequestParam(defaultValue = "true") boolean sortAsc
    ) {
        try {
            return ApiResponse.success(customerService.getAllCustomers(page, size, sortBy, sortAsc));
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @GetMapping("/{id:[a-f0-9\\-]+}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_STAFF')")
    public ApiResponse<?> getCustomerById(@PathVariable String id) {
        try {
            return ApiResponse.success(customerService.getCustomerById(id));
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ApiResponse<?> createCustomer(@RequestBody CustomerDto customerDto) {
        try {
            return ApiResponse.success(customerService.saveCustomer(customerDto), "Customer created successfully");
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_STAFF') and @authenticationService.isAuthorizedCustomer(#id))")
    public ApiResponse<?> updateCustomer(@PathVariable String id, @RequestBody CustomerDto customerDto) {
        try {
//            System.out.println(customerDto.getUsedToBeHandledByStaffUsernames());
            return ApiResponse.success(customerService.updateCustomer(id, customerDto), "Customer updated successfully");
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ApiResponse<?> deleteCustomer(@PathVariable String id) {
        try {
            customerService.deleteCustomer(id);
            return ApiResponse.success(null, "Customer deleted successfully");
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            return ApiResponse.error(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_STAFF')")
    public ApiResponse<?> searchCustomers(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String assignedStaffUsername,
            @RequestParam(required = false) String createdByUsername,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date createDateStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date createDateEnd,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date lastUpdateStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date lastUpdateEnd,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "customerName") String sortBy,
            @RequestParam(defaultValue = "true") boolean sortAsc) {
        try {
            return ApiResponse.success(customerService.searchCustomers(
                    query, assignedStaffUsername, createdByUsername,
                    createDateStart, createDateEnd,
                    lastUpdateStart, lastUpdateEnd,
                    page, size, sortBy, sortAsc
            ));
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PostMapping("/import")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ApiResponse<?> importCustomers(@RequestParam("file") MultipartFile file) {
        try {
            if (!Objects.equals(file.getContentType(), "text/csv") &&
                    !Objects.requireNonNull(file.getOriginalFilename()).endsWith(".csv")) {
                return ApiResponse.error(HttpStatus.BAD_REQUEST, "Only CSV files are supported");
            }

            List<CustomerDto> importedCustomers = customerService.importCustomersFromCsv(file);
            return ApiResponse.success("Imported " + importedCustomers.size() + " customer(s) successfully");
        } catch (IOException e) {
            return ApiResponse.error(HttpStatus.BAD_REQUEST, "Failed to read file: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_STAFF')")
    public ResponseEntity<?> exportCustomers(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String assignedStaffUsername,
            @RequestParam(required = false) String createdByUsername,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date createDateStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date createDateEnd,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date lastUpdateStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date lastUpdateEnd,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "customerName") String sortBy,
            @RequestParam(defaultValue = "true") boolean sortAsc) {
        try {
            Resource file = customerService.exportCustomersToCsv(
                    query, assignedStaffUsername, createdByUsername,
                    createDateStart, createDateEnd, lastUpdateStart, lastUpdateEnd,
                    page, size, sortBy, sortAsc);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"customers.csv\"")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(file);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to export customers: " + e.getMessage()));
        }
    }

    @GetMapping("/{customerId:[a-f0-9\\-]+}/invoices")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_STAFF')")
    public ApiResponse<?> getInvoicesByCustomerId(
            @PathVariable String customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "contractName") String sortBy,
            @RequestParam(defaultValue = "true") boolean sortAsc) {
        try {
            return ApiResponse.success(customerService.getInvoicesByCustomerId(
                    customerId, page, size, sortBy, sortAsc));
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/{customerId:[a-f0-9\\-]+}/contracts")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_STAFF')")
    public ApiResponse<?> getContractsByCustomerId(
            @PathVariable String customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "contractName") String sortBy,
            @RequestParam(defaultValue = "true") boolean sortAsc) {
        try {
            return ApiResponse.success(customerService.getContractsByCustomerId(
                    customerId, page, size, sortBy, sortAsc));
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
