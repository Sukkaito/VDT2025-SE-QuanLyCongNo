package vn.viettel.quanlycongno.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.viettel.quanlycongno.constant.RoleEnum;
import vn.viettel.quanlycongno.entity.Contract;
import vn.viettel.quanlycongno.entity.Staff;
import vn.viettel.quanlycongno.repository.ContractRepository;
import vn.viettel.quanlycongno.repository.InvoiceRepository;
import vn.viettel.quanlycongno.repository.StaffRepository;

import java.util.Optional;

/**
 * Service for handling authentication and authorization logic.
 * This service checks if the current authenticated user is authorized to access certain resources
 * such as contracts and invoices based on their role and assigned staff.
 */
@Service
public class AuthenticationService {
    private final StaffRepository staffRepository;
    private final ContractRepository contractRepository;
    private final InvoiceRepository invoiceRepository;


    @Autowired
    public AuthenticationService(StaffRepository staffRepository,
                                 ContractRepository contractRepository,
                                 InvoiceRepository invoiceRepository) {
        this.staffRepository = staffRepository;
        this.contractRepository = contractRepository;
        this.invoiceRepository = invoiceRepository;
    }

    /**
     * Checks if the current authenticated user is authorized to access the specified contract.
     *
     * @param contractId The ID of the contract to check authorization for.
     * @return true if the user is authorized, false otherwise.
     */
    public boolean isAuthorizedContract(String contractId) {

        // Get current authenticated user
        Authentication authentication = getAuthentication();
        if (authentication == null) return false;

        String username = authentication.getName();
        Optional<Staff> staffOpt = staffRepository.findByUsername(username);
        if (staffOpt.isEmpty()) return false;
        Staff staff = staffOpt.get();

        // Check if staff is admin
        if (staff.getRole().getRoleName().equals(RoleEnum.ADMIN)) {
            return true;
        }

        // Check if staff is the assigned staff for the contract
        Optional<Contract> contractOpt = contractRepository.findById(contractId);
        if (contractOpt.isEmpty()) return false;
        Contract contract = contractOpt.get();

        return contract.getAssignedStaff().getId().equals(staff.getId());
    }

    /**
     * Retrieves the current authenticated user from the security context.
     *
     * @return The Authentication object of the current user, or null if not authenticated.
     */
    private static Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return authentication;
    }

    /**
     * Checks if the current authenticated user is authorized to access the specified invoice.
     *
     * @param invoiceId The ID of the invoice to check authorization for.
     * @return true if the user is authorized, false otherwise.
     */
    public boolean isAuthorizedInvoice(String invoiceId) {

        String contractId = invoiceRepository.findById(invoiceId)
                .map(invoice -> invoice.getContract().getContractId())
                .orElse(null);
        return isAuthorizedContract(contractId);
    }
    /**
     * Retrieves the username of the currently authenticated user.
     * As a result, it only get invoked if authentication is guaranteed to be non-null.
     * @return The username of the current user.
     * @throws IllegalStateException if the authentication is null.
     */
    public String getCurrentUsername() {
        Authentication authentication = getAuthentication();
        assert authentication != null;
        return authentication.getName();
    }
}
