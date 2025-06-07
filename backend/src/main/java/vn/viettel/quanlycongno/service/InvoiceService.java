package vn.viettel.quanlycongno.service;

import jakarta.transaction.Transactional;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.viettel.quanlycongno.dto.InvoiceDto;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;

@Service
public interface InvoiceService {
    Page<InvoiceDto> getAllInvoices(int page, int size, String sortBy, boolean sortAsc);

    InvoiceDto getInvoiceById(String id);

    @Transactional
    InvoiceDto saveInvoice(InvoiceDto invoiceDto);

    @Transactional
    InvoiceDto updateInvoice(String id, InvoiceDto invoiceDto);

    @Transactional
    void deleteInvoice(String invoiceId);

    Page<InvoiceDto> searchInvoices(
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

    @Transactional
    List<InvoiceDto> importInvoicesFromCsv(MultipartFile file) throws IOException;

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
}
