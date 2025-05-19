package vn.viettel.quanlycongno.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import vn.viettel.quanlycongno.entity.Contract;
import vn.viettel.quanlycongno.repository.ContractRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class ContractService {
    private final ContractRepository contractRepository;


    public List<Contract> getAllContracts() {
        return (List<Contract>) contractRepository.findAll();
    }

    public Contract getContractById(String id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contract not found with id: " + id));
    }

    public Contract saveContract(Contract contract) {
        return contractRepository.save(contract);
    }

    public Contract updateContract(Contract contract) {
        if (contract.getContractId() == null || !contractRepository.existsById(contract.getContractId())) {
            throw new RuntimeException("Cannot update non-existent contract");
        }
        return contractRepository.save(contract);
    }

    public void deleteContract(String id) {
        if (!contractRepository.existsById(id)) {
            throw new RuntimeException("Contract not found with id: " + id);
        }
        contractRepository.deleteById(id);
    }
}