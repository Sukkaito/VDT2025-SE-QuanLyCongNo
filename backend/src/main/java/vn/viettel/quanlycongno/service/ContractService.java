package vn.viettel.quanlycongno.service;

import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.viettel.quanlycongno.dto.ContractDto;
import vn.viettel.quanlycongno.entity.Contract;
import vn.viettel.quanlycongno.repository.ContractRepository;
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

    public Page<ContractDto> getAllContracts(int page, int size, String sortBy, boolean sortAsc) {

        Pageable pageable = getPageableContract(page, size, sortBy, sortAsc);

        return contractRepository.findAll(pageable)
                .map(contract -> mapper.map(contract, ContractDto.class));
    }

    public ContractDto getContractById(String id) {
        return contractRepository.findById(id).map(dto -> mapper.map(dto, ContractDto.class))
                .orElseThrow(() -> new RuntimeException("Contract not found with id: " + id));
    }

    public ContractDto saveContract(ContractDto contractDto) {
        Staff assignedStaff = staffRepository.findByUsername(contractDto.getAssignedStaffUsername())
                .orElseThrow(() -> new RuntimeException("Assigned staff not found"));

        Staff createdBy = staffRepository.findByUsername(authenticationService.getCurrentUsername())
                .orElseThrow(() -> new RuntimeException("Created/updated by staff not found"));

        Contract contract;
        contract = mapper.map(contractDto, Contract.class);
        contract.setCreatedBy(createdBy);
        contract.setAssignedStaff(assignedStaff);
        contract.setLastUpdatedBy(createdBy);

        return mapper.map(contractRepository.save(contract), ContractDto.class);
    }

    public ContractDto updateContract(ContractDto contractDto) {
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

    public void deleteContract(String contractId) {
        if (!contractRepository.existsById(contractId)) {
            throw new RuntimeException("Contract not found with id: " + contractId);
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
}