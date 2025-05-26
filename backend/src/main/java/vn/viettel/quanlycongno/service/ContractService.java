package vn.viettel.quanlycongno.service;

import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.viettel.quanlycongno.dto.ContractDto;
import vn.viettel.quanlycongno.entity.Contract;
import vn.viettel.quanlycongno.repository.ContractRepository;
import vn.viettel.quanlycongno.repository.StaffRepository;
import vn.viettel.quanlycongno.entity.Staff;

import java.util.List;
import java.util.stream.StreamSupport;

@Service
@AllArgsConstructor
public class ContractService {
    private final ContractRepository contractRepository;
    private final StaffRepository staffRepository; // Uncomment if you need to access staff information
    private final AuthenticationService authenticationService;


    public List<ContractDto> getAllContracts() {
        return StreamSupport.stream(contractRepository.findAll().spliterator(), true)
                .map(Contract::toDTO)
                .toList();
    }

    public ContractDto getContractById(String id) {
        return contractRepository.findById(id).map(Contract::toDTO)
                .orElseThrow(() -> new RuntimeException("Contract not found with id: " + id));
    }

    public ContractDto saveContract(ContractDto contractDto) {
        Contract contract = new Contract();
        Staff assignedStaff = staffRepository.findByUsername(contractDto.getAssignedStaffUsername())
                .orElseThrow(() -> new RuntimeException("Assigned staff not found"));


        Staff createdBy = staffRepository.findByUsername(authenticationService.getCurrentUsername())
                .orElseThrow(() -> new RuntimeException("Created/updated by staff not found"));

        contract.setContractName(contractDto.getContractName());
        contract.setCreatedBy(createdBy);
        contract.setAssignedStaff(assignedStaff);
        contract.setLastUpdatedBy(createdBy);

        return contractRepository.save(contract).toDTO();
    }

    public ContractDto updateContract(ContractDto contractDto) {
        if (contractDto.getContractId() == null || !contractRepository.existsById(contractDto.getContractId())) {
            throw new RuntimeException("Cannot update non-existent contract");
        }

        Contract contract = contractRepository.findById(contractDto.getContractId())
                .orElseThrow(() -> new RuntimeException("Contract not found with id: " + contractDto.getContractId()));

        if (contractDto.getContractName() != null) contract.setContractName(contractDto.getContractName());
        if (contractDto.getAssignedStaffUsername() != null) contract.setAssignedStaff(staffRepository.findByUsername(contractDto.getAssignedStaffUsername())
                .orElseThrow(() -> new RuntimeException("Assigned staff not found")));

        contract.setLastUpdatedBy(staffRepository.findByUsername(authenticationService.getCurrentUsername())
                .orElseThrow(() -> new RuntimeException("Last updated by staff not found")));

        return contractRepository.save(contract).toDTO();
    }

    public void deleteContract(String contractId) {
        if (!contractRepository.existsById(contractId)) {
            throw new RuntimeException("Contract not found with id: " + contractId);
        }

        contractRepository.deleteById(contractId);
    }
}