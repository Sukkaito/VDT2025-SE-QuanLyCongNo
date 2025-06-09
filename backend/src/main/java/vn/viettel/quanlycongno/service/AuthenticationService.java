package vn.viettel.quanlycongno.service;

import org.springframework.stereotype.Service;

/**
 * Service for handling authentication and authorization logic.
 * This service checks if the current authenticated user is authorized to access certain resources
 * such as contracts and invoices based on their role and assigned staff.
 */
@Service
public interface AuthenticationService {

    /**
     * Checks if the current authenticated user is authorized to access the specified contract.
     *
     * @param contractId The ID of the contract to check authorization for.
     * @return true if the user is authorized, false otherwise.
     */
    boolean isAuthorizedContract(String contractId);

    /**
     * Checks if the current authenticated user is authorized to access the specified invoice.
     *
     * @param invoiceId The ID of the invoice to check authorization for.
     * @return true if the user is authorized, false otherwise.
     */
    boolean isAuthorizedInvoice(String invoiceId);

    /**
     * Checks if the current authenticated user is authorized to access the specified customer.
     * @param customerId The ID of the customer to check authorization for.
     * @return true if the user is authorized, false otherwise.
     */
    boolean isAuthorizedCustomer(String customerId);

    /**
     * Retrieves the username of the currently authenticated user.
     * As a result, it only gets invoked if authentication is guaranteed to be non-null.
     * @return The username of the current user.
     */
    String getCurrentUsername();
}
