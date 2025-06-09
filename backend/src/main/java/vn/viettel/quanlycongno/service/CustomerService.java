package vn.viettel.quanlycongno.service;

import jakarta.transaction.Transactional;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.viettel.quanlycongno.dto.ContractDto;
import vn.viettel.quanlycongno.dto.CustomerDto;
import vn.viettel.quanlycongno.dto.InvoiceDto;
import vn.viettel.quanlycongno.dto.base.PagedResponse;

import java.io.IOException;
import java.util.*;

/**
 * Service interface for managing customers in the application.
 * Provides methods for CRUD operations, searching, importing, and exporting customer data.
 */
@Service
public interface CustomerService {

    /**
     * Retrieves a paginated list of all customers.
     *
     * @param page the page number to retrieve (0-indexed)
     * @param size the number of customers per page
     * @param sortBy the field to sort by
     * @param sortAsc true for ascending order, false for descending
     * @return a Page containing CustomerDto objects
     */
    PagedResponse<CustomerDto> getAllCustomers(int page, int size, String sortBy, boolean sortAsc);

    /**
     * Retrieves a customer by their ID.
     *
     * @param id the ID of the customer
     * @return the CustomerDto object if found, null otherwise
     */
    CustomerDto getCustomerById(String id);

    /**
     * Saves a new customer.
     *
     * @param customerDto the CustomerDto object to save
     * @return the saved CustomerDto object
     */
    @Transactional
    CustomerDto saveCustomer(CustomerDto customerDto);

    /**
     * Updates an existing customer.
     *
     * @param id the ID of the customer to update
     * @param customerDto the CustomerDto object with updated information
     * @return the updated CustomerDto object
     */
    @Transactional
    CustomerDto updateCustomer(String id, CustomerDto customerDto);

    /**
     * Deletes a customer by their ID.
     *
     * @param customerId the ID of the customer to delete
     */
    @Transactional
    void deleteCustomer(String customerId);

    /**
     * Searches for customers based on various criteria.
     *
     * @param query the search query
     * @param assignedStaffUsername the username of the assigned staff
     * @param createdByUsername the username of the creator
     * @param createDateStart the start date for creation date filter
     * @param createDateEnd the end date for creation date filter
     * @param lastUpdateStart the start date for last update filter
     * @param lastUpdateEnd the end date for last update filter
     * @param page the page number to retrieve (0-indexed)
     * @param size the number of customers per page
     * @param sortBy the field to sort by
     * @param sortAsc true for ascending order, false for descending
     * @return a Page containing CustomerDto objects matching the search criteria
     */
    PagedResponse<CustomerDto> searchCustomers(String query,
                                      String assignedStaffUsername,
                                      String createdByUsername,
                                      Date createDateStart,
                                      Date createDateEnd,
                                      Date lastUpdateStart,
                                      Date lastUpdateEnd,
                                      int page, int size, String sortBy, boolean sortAsc);

    /**
     * Imports customers from a CSV file.
     *
     * @param file the MultipartFile containing the CSV data
     * @return a List of CustomerDto objects created from the CSV data
     * @throws IOException if an error occurs while reading the file
     */
    @Transactional
    List<CustomerDto> importCustomersFromCsv(MultipartFile file) throws IOException;

    /**
     * Exports customers to a CSV file based on search criteria.
     *
     * @param query the search query
     * @param assignedStaffUsername the username of the assigned staff
     * @param createdByUsername the username of the creator
     * @param createDateStart the start date for creation date filter
     * @param createDateEnd the end date for creation date filter
     * @param lastUpdateStart the start date for last update filter
     * @param lastUpdateEnd the end date for last update filter
     * @param page the page number to retrieve (0-indexed)
     * @param size the number of customers per page
     * @param sortBy the field to sort by
     * @param sortAsc true for ascending order, false for descending
     * @return a Resource containing the CSV data
     */
    Resource exportCustomersToCsv(String query, String assignedStaffUsername, String createdByUsername,
                                  Date createDateStart, Date createDateEnd,
                                  Date lastUpdateStart, Date lastUpdateEnd,
                                  int page, int size, String sortBy, boolean sortAsc);

    PagedResponse<InvoiceDto> getInvoicesByCustomerId(
            String customerId,
            int page,
            int size,
            String sortBy,
            boolean sortAsc
    );

    PagedResponse<ContractDto> getContractsByCustomerId(
            String customerId,
            int page,
            int size,
            String sortBy,
            boolean sortAsc
    );
}
