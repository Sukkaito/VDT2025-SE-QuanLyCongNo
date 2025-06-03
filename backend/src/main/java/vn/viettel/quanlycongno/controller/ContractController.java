package vn.viettel.quanlycongno.controller;

import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Date;
import java.util.Objects;

import vn.viettel.quanlycongno.dto.ContractDto;
import vn.viettel.quanlycongno.dto.base.ApiResponse;
import vn.viettel.quanlycongno.service.ContractService;

@RestController
@RequestMapping("/api/contracts")
@AllArgsConstructor
public class ContractController {
    private final ContractService contractService;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_STAFF')")
    public ApiResponse<?> getAllContracts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "contractName") String sortBy,
            @RequestParam(defaultValue = "true") boolean sortAsc
    ) {
        try {
            return ApiResponse.success(contractService.getAllContracts(page, size, sortBy, sortAsc));
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @GetMapping("/{id:[a-f0-9\\-]+}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_STAFF')")
    public ApiResponse<?> getContractById(@PathVariable String id) {
        try {
            return ApiResponse.success(contractService.getContractById(id));
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ApiResponse<?> createContract(@RequestBody ContractDto contractDto) {
        try {
            return ApiResponse.success(contractService.saveContract(contractDto), "Contract created successfully");
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_STAFF') and @authenticationService.isAuthorizedContract(#id)")
    public ApiResponse<?> updateContract(@PathVariable String id, @RequestBody ContractDto contractDto) {
        try {
            return ApiResponse.success(contractService.updateContract(id, contractDto), "Contract updated successfully");
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ApiResponse<?> deleteContract(@PathVariable String id) {
        try {
            contractService.deleteContract(id);
            return ApiResponse.success(null, "Contract deleted successfully");
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
    public ApiResponse<?> searchContracts(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String assignedStaffUsername,
            @RequestParam(required = false) String createdByUsername,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date createDateStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date createDateEnd,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date lastUpdateStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date lastUpdateEnd,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "contractName") String sortBy,
            @RequestParam(defaultValue = "true") boolean sortAsc) {
        try {
            return ApiResponse.success(contractService.searchContracts(
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
    public ApiResponse<?> importContracts(@RequestParam("file") MultipartFile file) {
        try {
            if (!Objects.equals(file.getContentType(), "text/csv") &&
                    !Objects.requireNonNull(file.getOriginalFilename()).endsWith(".csv")) {
                return ApiResponse.error(HttpStatus.BAD_REQUEST, "Only CSV files are supported");
            }

            List<ContractDto> importedContracts = contractService.importContractsFromCsv(file);
            return ApiResponse.success("Imported " + importedContracts.size() + " contract(s) successfully");
        } catch (IOException e) {
            return ApiResponse.error(HttpStatus.BAD_REQUEST, "Failed to read file: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_STAFF')")
    public ResponseEntity<?> exportContracts(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String assignedStaffUsername,
            @RequestParam(required = false) String createdByUsername,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date createDateStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date createDateEnd,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date lastUpdateStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date lastUpdateEnd,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "contractName") String sortBy,
            @RequestParam(defaultValue = "true") boolean sortAsc) {
        try {
            Resource file = contractService.exportContractsToCsv(
                    query, assignedStaffUsername, createdByUsername,
                    createDateStart, createDateEnd, lastUpdateStart, lastUpdateEnd,
                    page, size, sortBy, sortAsc);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"contracts.csv\"")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(file);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to export contracts: " + e.getMessage()));
        }
    }
}
