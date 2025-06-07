package vn.viettel.quanlycongno.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.viettel.quanlycongno.constant.RoleEnum;
import vn.viettel.quanlycongno.entity.Contract;
import vn.viettel.quanlycongno.entity.Customer;
import vn.viettel.quanlycongno.entity.Invoice;
import vn.viettel.quanlycongno.entity.Staff;
import vn.viettel.quanlycongno.repository.ContractRepository;
import vn.viettel.quanlycongno.repository.CustomerRepository;
import vn.viettel.quanlycongno.repository.InvoiceRepository;
import vn.viettel.quanlycongno.repository.StaffRepository;

import javax.swing.text.html.Option;
import java.util.Objects;
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
    private final CustomerRepository customerRepository;


    @Autowired
    public AuthenticationService(StaffRepository staffRepository,
                                 ContractRepository contractRepository,
                                 InvoiceRepository invoiceRepository, CustomerRepository customerRepository) {
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

        String username = getCurrentUsername();

        // Check if staff is the assigned staff for the contract
        Optional<Contract> contractOpt = contractRepository.findById(contractId);
        return contractOpt.filter(contract -> username.equals(contract.getAssignedStaff().getUsername())).isPresent();

    }

    /**
     * Checks if the current authenticated user is authorized to access the specified invoice.
     *
     * @param invoiceId The ID of the invoice to check authorization for.
     * @return true if the user is authorized, false otherwise.
     */
    public boolean isAuthorizedInvoice(String invoiceId) {

        if (isAdmin()) return true;

        String username = getCurrentUsername();

        // Check if staff is the assigned staff for the invoice
        Optional<Invoice> optInvoice = invoiceRepository.findById(invoiceId);
        return optInvoice.filter(invoice -> username.equals(invoice.getStaff().getUsername())).isPresent();
    }

    /**
     * Checks if the current authenticated user is authorized to access the specified customer.
     * @param customerId The ID of the customer to check authorization for.
     * @return true if the user is authorized, false otherwise.
     */
    public boolean isAuthorizedCustomer(String customerId) {

        if (isAdmin()) return true;

        String username = getCurrentUsername();

        // Check if staff is the assigned for the customer
        Optional<Customer> optCustomer = customerRepository.findById(customerId);
        return optCustomer.filter(contract -> username.equals(contract.getAssignedStaff().getUsername())).isPresent();
    }

    private boolean isAdmin() {
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
}
