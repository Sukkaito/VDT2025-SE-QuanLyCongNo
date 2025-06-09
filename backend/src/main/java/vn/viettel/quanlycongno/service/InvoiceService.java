package vn.viettel.quanlycongno.service;

import jakarta.transaction.Transactional;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.viettel.quanlycongno.dto.InvoiceDto;
import vn.viettel.quanlycongno.dto.base.PagedResponse;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;
/**
 * InvoiceService interface provides methods for managing invoices in the application.
 * It includes methods for retrieving, saving, updating, deleting, searching, importing,
 * and exporting invoices.
 */
@Service
public interface InvoiceService {

    /**
     * Retrieves all invoices with pagination and sorting options.
     *
     * @param page the page number to retrieve
     * @param size the number of invoices per page
     * @param sortBy the field to sort by
     * @param sortAsc whether to sort in ascending order
     * @return a paginated list of InvoiceDto objects
     */
    PagedResponse<InvoiceDto> getAllInvoices(int page, int size, String sortBy, boolean sortAsc);

    /**
     * Retrieves an invoice by its ID.
     *
     * @param id the ID of the invoice to retrieve
     * @return the InvoiceDto object representing the invoice
     */
    InvoiceDto getInvoiceById(String id);

    /**
     * Saves a new invoice.
     *
     * @param invoiceDto the InvoiceDto object containing the invoice details
     * @return the saved InvoiceDto object
     */
    @Transactional
    InvoiceDto saveInvoice(InvoiceDto invoiceDto);

    /**
     * Updates an existing invoice.
     *
     * @param id the ID of the invoice to update
     * @param invoiceDto the InvoiceDto object containing the updated invoice details
     * @return the updated InvoiceDto object
     */
    @Transactional
    InvoiceDto updateInvoice(String id, InvoiceDto invoiceDto);

    /**
     * Deletes an invoice by its ID.
     *
     * @param invoiceId the ID of the invoice to delete
     */
    @Transactional
    void deleteInvoice(String invoiceId);

    /**
     * Searches for invoices based on various criteria.
     *
     * @param query the search query
     * @param staffUsername the username of the staff member associated with the invoice
     * @param contractId the ID of the contract associated with the invoice
     * @param customerId the ID of the customer associated with the invoice
     * @param createdByUsername the username of the user who created the invoice
     * @param invoiceDateStart the start date for filtering invoices by date
     * @param invoiceDateEnd the end date for filtering invoices by date
     * @param dueDateStart the start date for filtering invoices by due date
     * @param dueDateEnd the end date for filtering invoices by due date
     * @param minAmount the minimum amount for filtering invoices by amount
     * @param maxAmount the maximum amount for filtering invoices by amount
     * @param currencyType the currency type of the invoice
     * @param department the department associated with the invoice
     * @param page the page number to retrieve
     * @param size the number of invoices per page
     * @param sortBy the field to sort by
     * @param sortAsc whether to sort in ascending order
     * @return a paginated list of InvoiceDto objects matching the search criteria
     */
    PagedResponse<InvoiceDto> searchInvoices(
            String query,
            String staffUsername,
            String contractId,
            String customerId,
            String createdByUsername,
            Date invoiceDateStart,
            Date invoiceDateEnd,
            Date dueDateStart,
            Date dueDateEnd,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            String currencyType,
            String department,
            int page, int size, String sortBy, boolean sortAsc);

    /**
     * Imports invoices from a CSV file.
     *
     * @param file the MultipartFile containing the CSV data
     * @return a list of InvoiceDto objects representing the imported invoices
     * @throws IOException if an error occurs while reading the file
     */
    @Transactional
    List<InvoiceDto> importInvoicesFromCsv(MultipartFile file) throws IOException;

    /**
     * Exports invoices to a CSV file based on the provided search criteria.
     *
     * @param query the search query
     * @param staffUsername the username of the staff member associated with the invoice
     * @param contractId the ID of the contract associated with the invoice
     * @param customerId the ID of the customer associated with the invoice
     * @param createdByUsername the username of the user who created the invoice
     * @param invoiceDateStart the start date for filtering invoices by date
     * @param invoiceDateEnd the end date for filtering invoices by date
     * @param dueDateStart the start date for filtering invoices by due date
     * @param dueDateEnd the end date for filtering invoices by due date
     * @param minAmount the minimum amount for filtering invoices by amount
     * @param maxAmount the maximum amount for filtering invoices by amount
     * @param currencyType the currency type of the invoice
     * @param department the department associated with the invoice
     * @param page the page number to retrieve
     * @param size the number of invoices per page
     * @param sortBy the field to sort by
     * @param sortAsc whether to sort in ascending order
     * @return a Resource representing the exported CSV file
     */
    Resource exportInvoicesToCsv(
            String query,
            String staffUsername,
            String contractId,
            String customerId,
            String createdByUsername,
            Date invoiceDateStart,
            Date invoiceDateEnd,
            Date dueDateStart,
            Date dueDateEnd,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            String currencyType,
            String department,
            int page, int size, String sortBy, boolean sortAsc);

    /**
     * Retrieves invoices associated with a specific contract ID.
     *
     * @param contractId the ID of the contract to retrieve invoices for
     * @param page the page number to retrieve
     * @param size the number of invoices per page
     * @param sortBy the field to sort by
     * @param sortAsc whether to sort in ascending order
     * @return a paginated list of InvoiceDto objects associated with the specified contract ID
     */
    PagedResponse<InvoiceDto> getInvoiceByContractId(
            String contractId,
            int page, int size, String sortBy, boolean sortAsc);

    /**
     * Retrieves invoices associated with a specific customer ID.
     *
     * @param customerId the ID of the customer to retrieve invoices for
     * @param page the page number to retrieve
     * @param size the number of invoices per page
     * @param sortBy the field to sort by
     * @param sortAsc whether to sort in ascending order
     * @return a paginated list of InvoiceDto objects associated with the specified customer ID
     */
    PagedResponse<InvoiceDto> getInvoiceByCustomerId(
            String customerId,
            int page, int size, String sortBy, boolean sortAsc);
}
