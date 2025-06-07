package vn.viettel.quanlycongno.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.viettel.quanlycongno.dto.InvoiceDto;
import vn.viettel.quanlycongno.entity.Invoice;
import vn.viettel.quanlycongno.entity.Contract;
import vn.viettel.quanlycongno.entity.Customer;
import vn.viettel.quanlycongno.entity.Staff;
import vn.viettel.quanlycongno.repository.InvoiceRepository;
import vn.viettel.quanlycongno.repository.ContractRepository;
import vn.viettel.quanlycongno.repository.CustomerRepository;
import vn.viettel.quanlycongno.repository.StaffRepository;
import vn.viettel.quanlycongno.util.CsvUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final ContractRepository contractRepository;
    private final CustomerRepository customerRepository;
    private final StaffRepository staffRepository;
    private final AuthenticationService authenticationService;
    private final ModelMapper mapper;

    public Page<InvoiceDto> getAllInvoices(int page, int size, String sortBy, boolean sortAsc) {
        Pageable pageable = getPageableInvoice(page, size, sortBy, sortAsc);

        return invoiceRepository.findAll(pageable)
                .map(invoice -> mapper.map(invoice, InvoiceDto.class));
    }

    public InvoiceDto getInvoiceById(String id) {
        return invoiceRepository.findById(id)
                .map(dto -> mapper.map(dto, InvoiceDto.class))
                .orElseThrow(() -> new RuntimeException("Invoice not found with id: " + id));
    }

    @Transactional
    public InvoiceDto saveInvoice(InvoiceDto invoiceDto) {
        Contract contract = contractRepository.findById(invoiceDto.getContractId())
                .orElseThrow(() -> new RuntimeException("Contract not found with id: " + invoiceDto.getContractId()));
        Customer customer = customerRepository.findById(invoiceDto.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + invoiceDto.getCustomerId()));
        Staff currentStaff = staffRepository.findByUsername(authenticationService.getCurrentUsername())
                .orElseThrow(() -> new RuntimeException("Current staff not found"));
        Staff assignedStaff = staffRepository.findByUsername(invoiceDto.getStaffUsername())
                .orElseThrow(() -> new RuntimeException("Assigned staff not found with username: " + invoiceDto.getStaffUsername()));

        Invoice invoice;
        invoice = mapper.map(invoiceDto, Invoice.class);
        invoice.setContract(contract);
        invoice.setCustomer(customer);
        invoice.setStaff(assignedStaff);
        invoice.setCreatedBy(currentStaff);
        invoice.setLastUpdatedBy(currentStaff);

        return mapper.map(invoiceRepository.save(invoice), InvoiceDto.class);
    }

    @Transactional
    public InvoiceDto updateInvoice(String id, InvoiceDto invoiceDto) {
        if (id == null || !invoiceRepository.existsById(id)) {
            throw new RuntimeException("Cannot update non-existent invoice");
        }

        Invoice existingInvoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found with id: " + id));
        Staff assignedStaff = staffRepository.findByUsername(invoiceDto.getStaffUsername())
                .orElseThrow(() -> new RuntimeException("Assigned staff not found with username: " + invoiceDto.getStaffUsername()));
        // Set the last updated by
        Staff currentStaff = staffRepository.findByUsername(authenticationService.getCurrentUsername())
                .orElseThrow(() -> new RuntimeException("Current staff not found"));
        // Update the entity with the new values
        mapper.map(invoiceDto, existingInvoice);
        existingInvoice.setStaff(assignedStaff);
        existingInvoice.setLastUpdatedBy(currentStaff);

        return mapper.map(invoiceRepository.save(existingInvoice), InvoiceDto.class);
    }

    @Transactional
    public void deleteInvoice(String invoiceId) {
        if (!invoiceRepository.existsById(invoiceId)) {
            throw new RuntimeException("Invoice not found with id: " + invoiceId);
        }

        invoiceRepository.deleteById(invoiceId);
    }

    public Page<InvoiceDto> searchInvoices(
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
            int page, int size, String sortBy, boolean sortAsc) {

        Pageable pageable = getPageableInvoice(page, size, sortBy, sortAsc);

        return invoiceRepository.searchByCriteria(
                        query, staffUsername, contractId, customerId, createdByUsername,
                        invoiceDateStart, invoiceDateEnd, dueDateStart, dueDateEnd,
                        minAmount, maxAmount, currencyType, department, pageable)
                .map(invoice -> mapper.map(invoice, InvoiceDto.class));
    }

    private static Pageable getPageableInvoice(int page, int size, String sortBy, boolean sortAsc) {
        Set<String> allowedSortFields = Arrays.stream(Invoice.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());

        allowedSortFields.addAll(Set.of("contract", "customer", "staff", "createdBy", "lastUpdatedBy"));

        // Validate and default if invalid
        String validatedSortField = allowedSortFields.contains(sortBy) ? sortBy : "invoiceDate";

        return PageRequest.of(page, size, Sort.by(sortAsc ? Sort.Direction.ASC : Sort.Direction.DESC, validatedSortField));
    }

    @Transactional
    public List<InvoiceDto> importInvoicesFromCsv(MultipartFile file) throws IOException {
        List<InvoiceDto> importedInvoices = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            // Read header line
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("CSV file is empty");
            }

            String[] headers = CsvUtils.parseCsvLine(headerLine);

            // Find column indexes for required fields
            Map<String, Integer> columnIndexes = parseIndexes(headers);

            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = CsvUtils.parseCsvLine(line);
                if (values.length < headers.length) continue; // Skip invalid lines

                try {
                    InvoiceDto invoiceDto = new InvoiceDto();

                    // Set values from CSV
                    CsvUtils.setStringValue(columnIndexes, "invoice_symbol", values, invoiceDto::setInvoiceSymbol);
                    CsvUtils.setStringValue(columnIndexes, "invoice_number", values, invoiceDto::setInvoiceNumber);
                    CsvUtils.setStringValue(columnIndexes, "currency_type", values, invoiceDto::setCurrencyType);
                    CsvUtils.setStringValue(columnIndexes, "payment_method", values, invoiceDto::setPaymentMethod);
                    CsvUtils.setStringValue(columnIndexes, "contract_id", values, invoiceDto::setContractId);
                    CsvUtils.setStringValue(columnIndexes, "customer_id", values, invoiceDto::setCustomerId);
                    CsvUtils.setStringValue(columnIndexes, "project_id", values, invoiceDto::setProjectId);
                    CsvUtils.setStringValue(columnIndexes, "staff_username", values, invoiceDto::setStaffUsername);
                    CsvUtils.setStringValue(columnIndexes, "department", values, invoiceDto::setDepartment);
                    CsvUtils.setStringValue(columnIndexes, "notes", values, invoiceDto::setNotes);

                    // Set numeric values
                    CsvUtils.setNumericValue(columnIndexes, values, "original_amount", invoiceDto::setOriginalAmount);
                    CsvUtils.setNumericValue(columnIndexes, values, "exchange_rate", invoiceDto::setExchangeRate);
                    CsvUtils.setNumericValue(columnIndexes, values, "vat", invoiceDto::setVat);

                    // Set date values
                    CsvUtils.setDateValue(columnIndexes, values, dateFormat, "invoice_date", invoiceDto::setInvoiceDate);
                    CsvUtils.setDateValue(columnIndexes, values, dateFormat, "due_date", invoiceDto::setDueDate);

                    // Validate required fields before saving
                    if (isValidInvoiceDto(invoiceDto)) {
                        importedInvoices.add(saveInvoice(invoiceDto));
                    }

                } catch (NumberFormatException | ParseException e) {
                    // Log error and continue with next line
                    System.err.println("Error parsing line: " + line + " - " + e.getMessage());
                }
            }
        }
        return importedInvoices;
    }

    private static Map<String, Integer> parseIndexes(String[] headers) throws IOException {
        Map<String, Integer> columnIndexes = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            String header = headers[i].trim().toLowerCase().replace(" ", "_");
            columnIndexes.put(header, i);
        }

        // Check required columns
        List<String> requiredColumns = Arrays.asList(
                "invoice_symbol", "invoice_number", "original_amount", "currency_type",
                "exchange_rate", "vat", "invoice_date", "due_date", "payment_method",
                "contract_id", "customer_id", "project_id", "staff_username", "department"
        );

        for (String column : requiredColumns) {
            if (!columnIndexes.containsKey(column) &&
                    !columnIndexes.containsKey(column.replace("_", ""))) {
                throw new IOException("CSV file must contain column: " + column);
            }
        }
        return columnIndexes;
    }

    private boolean isValidInvoiceDto(InvoiceDto invoiceDto) {
        return !invoiceDto.getInvoiceSymbol().isEmpty() &&
                !invoiceDto.getInvoiceNumber().isEmpty() &&
                !invoiceDto.getCurrencyType().isEmpty() &&
                !invoiceDto.getPaymentMethod().isEmpty() &&
                !invoiceDto.getContractId().isEmpty() &&
                !invoiceDto.getCustomerId().isEmpty() &&
                !invoiceDto.getProjectId().isEmpty() &&
                !invoiceDto.getStaffUsername().isEmpty() &&
                !invoiceDto.getDepartment().isEmpty();
    }

    public Resource exportInvoicesToCsv(
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
            int page, int size, String sortBy, boolean sortAsc) {

        Pageable pageable = getPageableInvoice(page, size, sortBy, sortAsc);

        List<Invoice> invoices = invoiceRepository.searchByCriteria(
                query, staffUsername, contractId, customerId, createdByUsername,
                invoiceDateStart, invoiceDateEnd, dueDateStart, dueDateEnd,
                minAmount, maxAmount, currencyType, department, pageable).toList();

        try (ByteArrayOutputStream ignored = new ByteArrayOutputStream()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            StringBuilder csvContent = new StringBuilder();
            // Write CSV header
            csvContent.append("Invoice ID,Invoice Symbol,Invoice Number,Original Amount,Currency Type,")
                    .append("Exchange Rate,Converted Amount Pre VAT,VAT,Total Amount With VAT,")
                    .append("Invoice Date,Due Date,Payment Method,Contract ID,Customer ID,")
                    .append("Project ID,Staff Username,Department,Notes,")
                    .append("Created Date,Last Update Date,Created By,Last Updated By\n");

            // Write data rows
            for (Invoice invoice : invoices) {
                csvContent.append(CsvUtils.escapeCsvField(invoice.getInvoiceId())).append(",");
                csvContent.append(CsvUtils.escapeCsvField(invoice.getInvoiceSymbol())).append(",");
                csvContent.append(CsvUtils.escapeCsvField(invoice.getInvoiceNumber())).append(",");
                csvContent.append(invoice.getOriginalAmount()).append(",");
                csvContent.append(CsvUtils.escapeCsvField(invoice.getCurrencyType())).append(",");
                csvContent.append(invoice.getExchangeRate()).append(",");
                csvContent.append(invoice.getConvertedAmountPreVat() != null ? invoice.getConvertedAmountPreVat().toString() : "").append(",");
                csvContent.append(invoice.getVat()).append(",");
                csvContent.append(invoice.getTotalAmountWithVat() != null ? invoice.getTotalAmountWithVat().toString() : "").append(",");
                csvContent.append(dateFormat.format(invoice.getInvoiceDate())).append(",");
                csvContent.append(dateFormat.format(invoice.getDueDate())).append(",");
                csvContent.append(CsvUtils.escapeCsvField(invoice.getPaymentMethod())).append(",");
                csvContent.append(CsvUtils.escapeCsvField(invoice.getContract().getContractId())).append(",");
                csvContent.append(CsvUtils.escapeCsvField(invoice.getCustomer().getCustomerId())).append(",");
                csvContent.append(CsvUtils.escapeCsvField(invoice.getProjectId())).append(",");
                csvContent.append(CsvUtils.escapeCsvField(invoice.getStaff().getUsername())).append(",");
                csvContent.append(CsvUtils.escapeCsvField(invoice.getDepartment())).append(",");
                csvContent.append(CsvUtils.escapeCsvField(invoice.getNotes())).append(",");
                csvContent.append(invoice.getCreatedDate() != null ? dateFormat.format(invoice.getCreatedDate()) : "").append(",");
                csvContent.append(invoice.getLastUpdateDate() != null ? dateFormat.format(invoice.getLastUpdateDate()) : "").append(",");
                csvContent.append(CsvUtils.escapeCsvField(invoice.getCreatedBy().getUsername())).append(",");
                csvContent.append(CsvUtils.escapeCsvField(invoice.getLastUpdatedBy().getUsername())).append("\n");
            }

            byte[] bytes = csvContent.toString().getBytes();
            return new ByteArrayResource(bytes);
        } catch (IOException e) {
            throw new RuntimeException("Failed to export invoices to CSV", e);
        }
    }
}
