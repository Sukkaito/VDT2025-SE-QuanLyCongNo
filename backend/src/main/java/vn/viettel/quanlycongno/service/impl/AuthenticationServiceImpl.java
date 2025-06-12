package vn.viettel.quanlycongno.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.viettel.quanlycongno.constant.RoleEnum;
import vn.viettel.quanlycongno.entity.Staff;
import vn.viettel.quanlycongno.repository.ContractRepository;
import vn.viettel.quanlycongno.repository.CustomerRepository;
import vn.viettel.quanlycongno.repository.InvoiceRepository;
import vn.viettel.quanlycongno.repository.StaffRepository;
import vn.viettel.quanlycongno.service.AuthenticationService;

import java.util.Objects;
import java.util.Optional;

@Service("authenticationService")
public class AuthenticationServiceImpl implements AuthenticationService {

    private final StaffRepository staffRepository;
    private final ContractRepository contractRepository;
    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;

    @Autowired
    public AuthenticationServiceImpl(StaffRepository staffRepository, ContractRepository contractRepository, InvoiceRepository invoiceRepository, CustomerRepository customerRepository) {
        this.staffRepository = staffRepository;
        this.contractRepository = contractRepository;
        this.invoiceRepository = invoiceRepository;
        this.customerRepository = customerRepository;
    }

    /**
     * Checks if the current authenticated user is authorized to access the specified contract.
     *
     * @param contractId The ID of the contract to check authorization for.
     * @return true if the user is authorized, false otherwise.
     */
    public boolean isAuthorizedContract(String contractId) {

        if (isAdmin()) return true;

        String userid = ((Staff) Objects.requireNonNull(getAuthentication()).getPrincipal()).getId();

        return contractRepository.existsByContractIdAndAssignedStaffId(contractId, userid);
    }

    /**
     * Checks if the current authenticated user is authorized to access the specified invoice.
     *
     * @param invoiceId The ID of the invoice to check authorization for.
     * @return true if the user is authorized, false otherwise.
     */
    public boolean isAuthorizedInvoice(String invoiceId) {

        if (isAdmin()) return true;

        String userid = ((Staff) Objects.requireNonNull(getAuthentication()).getPrincipal()).getId();

        // Check if staff is the assigned staff for the invoice
        return invoiceRepository.existsByInvoiceIdAndStaffId(invoiceId, userid);
    }

    /**
     * Checks if the current authenticated user is authorized to access the specified customer.
     * @param customerId The ID of the customer to check authorization for.
     * @return true if the user is authorized, false otherwise.
     */
    public boolean isAuthorizedCustomer(String customerId) {

        if (isAdmin()) return true;

        String userId = ((Staff) Objects.requireNonNull(getAuthentication()).getPrincipal()).getId();

        // Check if staff is the assigned for the customer
        return customerRepository.existsByCustomerIdAndAssignedStaffId(customerId, userId);
    }

    /**
     * Retrieves the username of the currently authenticated user.
     * As a result, it only gets invoked if authentication is guaranteed to be non-null.
     * @return The username of the current user.
     * @throws IllegalStateException if the authentication is null.
     */
    public String getCurrentUsername() {
        Authentication authentication = getAuthentication();
        assert authentication != null;
        return authentication.getName();
    }

    /**
     * Checks if the current authenticated user has admin privileges.
     * @return true if the user is an admin, false otherwise.
     */
    public boolean isAdmin() {
        // Get current authenticated user
        Authentication authentication = getAuthentication();
        if (authentication == null) return false;

        String username = authentication.getName();
        Optional<Staff> staffOpt = staffRepository.findByUsername(username);
        if (staffOpt.isEmpty()) return false;
        Staff staff = staffOpt.get();

        // Check if staff is admin
        return staff.getRole().getRoleName().equals(RoleEnum.ADMIN);
    }

    private static Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return authentication;
    }
}
