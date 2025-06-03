package vn.viettel.quanlycongno.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import vn.viettel.quanlycongno.dto.ContractDto;
import vn.viettel.quanlycongno.entity.Contract;
import vn.viettel.quanlycongno.repository.ContractRepository;
import vn.viettel.quanlycongno.repository.InvoiceRepository;
import vn.viettel.quanlycongno.repository.StaffRepository;
import vn.viettel.quanlycongno.entity.Staff;

import java.util.Arrays;
import java.util.Date;
import java.lang.reflect.Field;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ContractService {
    private final ContractRepository contractRepository;
    private final StaffRepository staffRepository;
    private final AuthenticationService authenticationService;
    private final ModelMapper mapper;
    private final InvoiceRepository invoiceRepository;

    public Page<ContractDto> getAllContracts(int page, int size, String sortBy, boolean sortAsc) {

        Pageable pageable = getPageableContract(page, size, sortBy, sortAsc);

        return contractRepository.findAll(pageable)
                .map(contract -> mapper.map(contract, ContractDto.class));
    }

    public ContractDto getContractById(String id) {
        return contractRepository.findById(id).map(dto -> mapper.map(dto, ContractDto.class))
                .orElseThrow(() -> new RuntimeException("Contract not found with id: " + id));
    }

    @Transactional
    public ContractDto saveContract(ContractDto contractDto) {
        // Check if contract exists and use update method if it does
        if (contractDto.getContractId() != null && !contractDto.getContractId().isEmpty() &&
                contractRepository.existsById(contractDto.getContractId())) {
            return updateContract(contractDto.getContractId(), contractDto);
        }

        // Get required staff entities
        Staff assignedStaff = staffRepository.findByUsername(contractDto.getAssignedStaffUsername())
                .orElseThrow(() -> new RuntimeException("Assigned staff not found"));
        Staff createdBy = staffRepository.findByUsername(authenticationService.getCurrentUsername())
                .orElseThrow(() -> new RuntimeException("Created/updated by staff not found"));

        // Create new contract entity
        Contract contract = new Contract();

        // Set the ID first before mapping other properties
        if (contractDto.getContractId() != null && !contractDto.getContractId().isEmpty()) {
            contract.setContractId(contractDto.getContractId());
        }

        // Map other properties from DTO to entity
        mapper.map(contractDto, contract);

        // Set staff relationships
        contract.setCreatedBy(createdBy);
        contract.setAssignedStaff(assignedStaff);
        contract.setLastUpdatedBy(createdBy);

        System.out.println("Saving contract: " + contract);

        // Save and return
        Contract savedContract = contractRepository.save(contract);
        return mapper.map(savedContract, ContractDto.class);
    }

    @Transactional
    public ContractDto updateContract(String id, ContractDto contractDto) {
        contractDto.setContractId(id);
        if (contractDto.getContractId() == null || !contractRepository.existsById(contractDto.getContractId())) {
            throw new RuntimeException("Cannot update non-existent contract");
        }

        Contract contract = contractRepository.findById(contractDto.getContractId())
                .orElseThrow(() -> new RuntimeException("Contract not found with id: " + contractDto.getContractId()));
        Staff assignedStaff = staffRepository.findByUsername(contractDto.getAssignedStaffUsername())
                .orElseThrow(() -> new RuntimeException("Assigned staff not found"));
        Staff lastUpdatedBy = staffRepository.findByUsername(authenticationService.getCurrentUsername())
                .orElseThrow(() -> new RuntimeException("Last updated by staff not found"));

        mapper.map(contractDto, contract);
        contract.setAssignedStaff(assignedStaff);
        contract.setLastUpdatedBy(lastUpdatedBy);

        return mapper.map(contractRepository.save(contract), ContractDto.class);
    }

    @Transactional
    public void deleteContract(String contractId) {
        if (!contractRepository.existsById(contractId)) {
            throw new RuntimeException("Contract not found with id: " + contractId);
        }

        if (invoiceRepository.existsByContract_ContractId(contractId)) {
            throw new IllegalArgumentException("Cannot delete contract with existing invoices");
        }

        contractRepository.deleteById(contractId);
    }

    public Page<ContractDto> searchContracts(String query,
                                             String assignedStaffUsername,
                                             String createdByUsername,
                                             Date createDateStart,
                                             Date createDateEnd,
                                             Date lastUpdateStart,
                                             Date lastUpdateEnd,
                                             int page, int size, String sortBy, boolean sortAsc) {
        Pageable pageable = getPageableContract(page, size, sortBy, sortAsc);

        return contractRepository.searchByCriteria(query, assignedStaffUsername, createdByUsername,
                createDateStart, createDateEnd, lastUpdateStart, lastUpdateEnd, pageable)
                .map(contract -> mapper.map(contract, ContractDto.class));
    }

    private static Pageable getPageableContract(int page, int size, String sortBy, boolean sortAsc) {
        Set<String> allowedSortFields = Arrays.stream(Contract.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());

        allowedSortFields.addAll(Set.of("assignedStaff", "createdBy", "lastUpdatedBy"));

        // Validate and default if invalid
        String validatedSortField = allowedSortFields.contains(sortBy) ? sortBy : "contractName";

        return PageRequest.of(page, size, Sort.by(sortAsc ? Sort.Direction.ASC : Sort.Direction.DESC, validatedSortField));
    }

    @Transactional
    public List<ContractDto> importContractsFromCsv(MultipartFile file) throws IOException {
        List<ContractDto> importedContracts = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            // Read and parse header line
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("CSV file is empty");
            }

            String[] headers = parseContractCsvLine(headerLine);
            // Find column indexes for required fields
            int contractNameIndex = -1;
            int assignedStaffIndex = -1;

            for (int i = 0; i < headers.length; i++) {
                String header = headers[i].trim().toLowerCase();
                switch (header) {
                    case "contract name", "contractname" -> contractNameIndex = i;
                    case "assigned staff", "assignedstaff", "assigned staff username", "assignedstaffusername" ->
                            assignedStaffIndex = i;
                }
            }

            if (contractNameIndex == -1 || assignedStaffIndex == -1) {
                throw new IOException("CSV file must contain 'Contract Name' and 'Assigned Staff' columns");
            }

            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = parseContractCsvLine(line);
                if (values.length <= Math.max(contractNameIndex, assignedStaffIndex)) continue; // Skip invalid lines

                ContractDto contractDto = new ContractDto();
                contractDto.setContractName(values[contractNameIndex].trim());
                contractDto.setAssignedStaffUsername(values[assignedStaffIndex].trim());

                // Add the contract if essential fields are present and not empty
                if (!contractDto.getContractName().isEmpty() && !contractDto.getAssignedStaffUsername().isEmpty()) {
                    importedContracts.add(saveContract(contractDto));
                }
            }
        }
        return importedContracts;
    }

    private Double parseDouble(String value) {
        try {
            return value != null && !value.isEmpty() ? Double.parseDouble(value.trim()) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Helper method to properly parse CSV lines, handling quoted fields
    private String[] parseContractCsvLine(String line) {
        List<String> fields = Arrays.stream(line.split(","))
                .map(String::trim)
                .map(s -> s.replaceAll("^\"|\"$", "")) // Remove surrounding quotes
                .toList();

        return fields.toArray(new String[0]);
    }

    public Resource exportContractsToCsv(String query, String assignedStaffUsername, String createdByUsername,
                                         Date createDateStart, Date createDateEnd, Date lastUpdateStart, Date lastUpdateEnd,
                                         int page, int size, String sortBy, boolean sortAsc) {
        Pageable pageable = getPageableContract(page, size, sortBy, sortAsc);

        List<Contract> contracts = contractRepository.searchByCriteria(query, assignedStaffUsername, createdByUsername,
                createDateStart, createDateEnd, lastUpdateStart, lastUpdateEnd, pageable).toList();

        try (ByteArrayOutputStream ignored = new ByteArrayOutputStream()) {
            // Write CSV header with only fields available in DTO
            StringBuilder csvContent = new StringBuilder();
            csvContent.append("Contract ID,Contract Name,Assigned Staff,Created Date,Created By,Last Updated Date,Last Updated By\n");

            // Write data rows
            for (Contract contract : contracts) {
                csvContent.append(escapeCsvField(contract.getContractId())).append(",");
                csvContent.append(escapeCsvField(contract.getContractName())).append(",");
                csvContent.append(escapeCsvField(contract.getAssignedStaff().getUsername())).append(",");
                csvContent.append(contract.getCreatedDate() != null ? contract.getCreatedDate().toString() : "").append(",");
                csvContent.append(contract.getCreatedBy() != null ? escapeCsvField(contract.getCreatedBy().getUsername()) : "").append(",");
                csvContent.append(contract.getLastUpdateDate() != null ? contract.getLastUpdateDate().toString() : "").append(",");
                csvContent.append(contract.getLastUpdatedBy() != null ? escapeCsvField(contract.getLastUpdatedBy().getUsername()) : "").append("\n");
            }

            byte[] bytes = csvContent.toString().getBytes();
            return new ByteArrayResource(bytes);
        } catch (IOException e) {
            throw new RuntimeException("Failed to export contracts to CSV", e);
        }
    }

    // Helper method to escape CSV fields that might contain commas
    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }

        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
}
