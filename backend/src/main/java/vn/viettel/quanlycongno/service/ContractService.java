package vn.viettel.quanlycongno.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.List;

import vn.viettel.quanlycongno.dto.ContractDto;
import vn.viettel.quanlycongno.dto.InvoiceDto;
import vn.viettel.quanlycongno.dto.base.PagedResponse;

import java.util.Date;

/**
 * Service interface for managing contracts.
 * Provides methods for CRUD operations, searching, importing, and exporting contracts.
 */
@Service
public interface ContractService {

    /** * Retrieves all contracts with pagination and sorting options.
     *
     * @param page the page number to retrieve
     * @param size the number of contracts per page
     * @param sortBy the field to sort by
     * @param sortAsc true for ascending order, false for descending order
     * @return a paginated list of ContractDto objects
     */
    PagedResponse<ContractDto> getAllContracts(int page, int size, String sortBy, boolean sortAsc);

    /**
     * Retrieves a contract by its ID.
     *
     * @param id the ID of the contract to retrieve
     * @return the ContractDto object representing the contract, or null if not found
     */
    ContractDto getContractById(String id);

    /**
     * Saves a new contract or updates an existing one.
     *
     * @param contractDto the ContractDto object containing contract details
     * @return the saved or updated ContractDto object
     */
    @Transactional
    ContractDto saveContract(ContractDto contractDto);

    /**
     * Updates an existing contract by its ID.
     *
     * @param id the ID of the contract to update
     * @param contractDto the ContractDto object containing updated contract details
     * @return the updated ContractDto object
     */
    @Transactional
    ContractDto updateContract(String id, ContractDto contractDto);

    /**
     * Deletes a contract by its ID.
     *
     * @param contractId the ID of the contract to delete
     */
    @Transactional
    void deleteContract(String contractId);

    /**
     * Searches for contracts based on various criteria.
     *
     * @param query the search query
     * @param assignedStaffUsername the username of the assigned staff member
     * @param createdByUsername the username of the creator
     * @param createDateStart the start date for contract creation
     * @param createDateEnd the end date for contract creation
     * @param lastUpdateStart the start date for last update
     * @param lastUpdateEnd the end date for last update
     * @param page the page number to retrieve
     * @param size the number of contracts per page
     * @param sortBy the field to sort by
     * @param sortAsc true for ascending order, false for descending order
     * @return a paginated list of ContractDto objects matching the search criteria
     */
    PagedResponse<ContractDto> searchContracts(String query,
                                     String assignedStaffUsername,
                                     String createdByUsername,
                                     Date createDateStart,
                                     Date createDateEnd,
                                     Date lastUpdateStart,
                                     Date lastUpdateEnd,
                                     int page, int size, String sortBy, boolean sortAsc);

    /**
     * Imports contracts from a CSV file.
     *
     * @param file the MultipartFile containing the CSV data
     * @return a list of ContractDto objects created from the CSV data
     * @throws IOException if an error occurs while reading the file
     */
    @Transactional
    List<ContractDto> importContractsFromCsv(MultipartFile file) throws IOException;

    /**
     * Exports contracts to a CSV file based on search criteria.
     *
     * @param query the search query
     * @param assignedStaffUsername the username of the assigned staff member
     * @param createdByUsername the username of the creator
     * @param createDateStart the start date for contract creation
     * @param createDateEnd the end date for contract creation
     * @param lastUpdateStart the start date for last update
     * @param lastUpdateEnd the end date for last update
     * @param page the page number to retrieve
     * @param size the number of contracts per page
     * @param sortBy the field to sort by
     * @param sortAsc true for ascending order, false for descending order
     * @return a Resource representing the exported CSV file
     */
    Resource exportContractsToCsv(String query, String assignedStaffUsername, String createdByUsername,
                                         Date createDateStart, Date createDateEnd, Date lastUpdateStart, Date lastUpdateEnd,
                                         int page, int size, String sortBy, boolean sortAsc);

    /**
     * Retrieves all invoices associated with a specific contract ID.
     *
     * @param contractId the ID of the contract
     * @param page the page number to retrieve
     * @param size the number of invoices per page
     * @param sortBy the field to sort by
     * @param sortAsc true for ascending order, false for descending order
     * @return a paginated list of InvoiceDto objects associated with the contract
     */
    PagedResponse<InvoiceDto> getInvoicesByContractId(String contractId, int page, int size, String sortBy, boolean sortAsc);

    PagedResponse<ContractDto> getContractsByCustomerId(String customerId, int page, int size, String sortBy, boolean sortAsc);
}
