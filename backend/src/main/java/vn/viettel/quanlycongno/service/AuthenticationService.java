package vn.viettel.quanlycongno.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.viettel.quanlycongno.constant.RoleEnum;
import vn.viettel.quanlycongno.entity.Contract;
import vn.viettel.quanlycongno.entity.Staff;
import vn.viettel.quanlycongno.repository.ContractRepository;
import vn.viettel.quanlycongno.repository.StaffRepository;

import java.util.Optional;

@Service
public class AuthenticationService {
    private final StaffRepository staffRepository;
    private final ContractRepository contractRepository;

    @Autowired
    public AuthenticationService(StaffRepository staffRepository, ContractRepository contractRepository) {
        this.staffRepository = staffRepository;
        this.contractRepository = contractRepository;
    }

    public boolean isAuthorized(String contractId) {

        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String username = authentication.getName();
        Optional<Staff> staffOpt = staffRepository.findByUsername(username);
        if (staffOpt.isEmpty()) return false;
        Staff staff = staffOpt.get();

        // Check if staff is admin
        System.out.println(staff.getRole().getRoleName());
        if (staff.getRole().getRoleName().equals(RoleEnum.ADMIN)) {
            return true;
        }

        // Check if staff is the assigned staff for the contract
        Optional<Contract> contractOpt = contractRepository.findById(contractId);
        if (contractOpt.isEmpty()) return false;
        Contract contract = contractOpt.get();

        return contract.getAssignedStaff().getId().equals(staff.getId());
    }

    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String username = authentication.getName();
        Optional<Staff> staffOpt = staffRepository.findByUsername(username);
        return staffOpt.map(staff -> staff.getRole().getRoleName() == RoleEnum.ADMIN).orElse(false);
    }

    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return authentication.getName();
    }
}
