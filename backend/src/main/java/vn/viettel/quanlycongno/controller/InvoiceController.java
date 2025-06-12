package vn.viettel.quanlycongno.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.viettel.quanlycongno.dto.InvoiceDto;
import vn.viettel.quanlycongno.dto.base.ApiResponse;
import vn.viettel.quanlycongno.service.InvoiceService;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

@RestController
@RequestMapping("/api/invoices")
@AllArgsConstructor
public class InvoiceController {
    private final InvoiceService invoiceService;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_STAFF')")
    public ApiResponse<?> getAllInvoices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "invoiceDate") String sortBy,
            @RequestParam(defaultValue = "false") boolean sortAsc
    ) {
        try {
            return ApiResponse.success(invoiceService.getAllInvoices(page, size, sortBy, sortAsc));
        } catch (RuntimeException e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @GetMapping("/{id:[a-f0-9\\-]+}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_STAFF')")
    public ApiResponse<?> getInvoiceById(@PathVariable String id) {
        try {
            return ApiResponse.success(invoiceService.getInvoiceById(id));
        } catch (RuntimeException e) {
            return ApiResponse.error(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping
//    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ApiResponse<?> createInvoice(@Valid @RequestBody InvoiceDto invoiceDto) {
        try {
            return ApiResponse.success(invoiceService.saveInvoice(invoiceDto), "Invoice created successfully");
        } catch (RuntimeException e) {
            return ApiResponse.error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_STAFF') and @authenticationService.isAuthorizedInvoice(#id)")
    public ApiResponse<?> updateInvoice(@PathVariable String id, @Valid @RequestBody InvoiceDto invoiceDto) {
        try {
            return ApiResponse.success(invoiceService.updateInvoice(id, invoiceDto), "Invoice updated successfully");
        } catch (RuntimeException e) {
            return ApiResponse.error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ApiResponse<?> deleteInvoice(@PathVariable String id) {
        try {
            invoiceService.deleteInvoice(id);
            return ApiResponse.success(null, "Invoice deleted successfully");
        } catch (RuntimeException e) {
            return ApiResponse.error(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_STAFF')")
    public ApiResponse<?> searchInvoices(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String staffUsername,
            @RequestParam(required = false) String contractId,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String createdByUsername,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date invoiceDateStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date invoiceDateEnd,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dueDateStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dueDateEnd,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) String currencyType,
            @RequestParam(required = false) String department,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "invoiceDate") String sortBy,
            @RequestParam(defaultValue = "false") boolean sortAsc) {
        try {
            return ApiResponse.success(invoiceService.searchInvoices(
                    query, staffUsername, contractId, customerId, createdByUsername,
                    invoiceDateStart, invoiceDateEnd, dueDateStart, dueDateEnd,
                    minAmount, maxAmount, currencyType, department,
                    page, size, sortBy, sortAsc
            ));
        } catch (RuntimeException e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ApiResponse<?> importInvoices(@RequestParam("file") MultipartFile file) {
        try {
            if (!Objects.equals(file.getContentType(), "text/csv") &&
                    !Objects.requireNonNull(file.getOriginalFilename()).endsWith(".csv")) {
                return ApiResponse.error(HttpStatus.BAD_REQUEST, "Only CSV files are supported");
            }

            var importedInvoices = invoiceService.importInvoicesFromCsv(file);
            return ApiResponse.success("Imported " + importedInvoices.size() + " invoice(s) successfully");
        } catch (IOException e) {
            return ApiResponse.error(HttpStatus.BAD_REQUEST, "Failed to read file: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_STAFF')")
    public ResponseEntity<?> exportInvoices(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String staffUsername,
            @RequestParam(required = false) String contractId,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String createdByUsername,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date invoiceDateStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date invoiceDateEnd,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dueDateStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dueDateEnd,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) String currencyType,
            @RequestParam(required = false) String department,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "invoiceDate") String sortBy,
            @RequestParam(defaultValue = "false") boolean sortAsc) {
        try {
            Resource file = invoiceService.exportInvoicesToCsv(
                    query, staffUsername, contractId, customerId, createdByUsername,
                    invoiceDateStart, invoiceDateEnd, dueDateStart, dueDateEnd,
                    minAmount, maxAmount, currencyType, department,
                    page, size, sortBy, sortAsc);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"invoices.csv\"")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(file);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to export invoices: " + e.getMessage()));
        }
    }
}
