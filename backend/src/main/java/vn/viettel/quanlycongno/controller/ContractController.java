package vn.viettel.quanlycongno.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.viettel.quanlycongno.dto.ContractDto;
import vn.viettel.quanlycongno.entity.Contract;
import vn.viettel.quanlycongno.service.ContractService;

@RestController
@RequestMapping("/api/contracts")
@AllArgsConstructor
public class ContractController {
    private final ContractService contractService;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_STAFF')")
    public ResponseEntity<?> getAllContracts() {
        try {
            return new ResponseEntity<>(contractService.getAllContracts(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_STAFF')")
    public ResponseEntity<?> getContractById(@PathVariable String id) {
        try {
            return new ResponseEntity<>(contractService.getContractById(id), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> createContract(@RequestBody ContractDto contractDto) {
        try {
            return new ResponseEntity<>(contractService.saveContract(contractDto), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_STAFF') and @authenticationService.isAuthorized(#id)")
    public ResponseEntity<?> updateContract(@PathVariable String id, @RequestBody ContractDto contractDto) {
        try {
            contractDto.setContractId(id);
            return new ResponseEntity<>(contractService.updateContract(contractDto), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> deleteContract(@PathVariable String id) {
        try {
            contractService.deleteContract(id);
            return new ResponseEntity<>("Contract deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}