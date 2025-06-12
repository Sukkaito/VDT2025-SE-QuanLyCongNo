package vn.viettel.quanlycongno.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.viettel.quanlycongno.entity.Invoice;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, String> {
    @Query("SELECT i FROM Invoice i " +
            "LEFT JOIN i.staff s WITH (:staffUsername IS NOT NULL) " +
            "LEFT JOIN i.contract c WITH (:contractId IS NOT NULL) " +
            "LEFT JOIN i.customer cust WITH (:customerId IS NOT NULL) " +
            "LEFT JOIN i.createdBy cb WITH (:createdByUsername IS NOT NULL) " +
            "WHERE (:query IS NULL OR LOWER(i.invoiceSymbol) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "    OR LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "AND (:staffUsername IS NULL OR s.username = :staffUsername) " +
            "AND (:contractId IS NULL OR c.contractId = :contractId) " +
            "AND (:customerId IS NULL OR cust.customerId = :customerId) " +
            "AND (:createdByUsername IS NULL OR cb.username = :createdByUsername) " +
            "AND (:invoiceDateStart IS NULL OR i.invoiceDate >= :invoiceDateStart) " +
            "AND (:invoiceDateEnd IS NULL OR i.invoiceDate <= :invoiceDateEnd) " +
            "AND (:dueDateStart IS NULL OR i.dueDate >= :dueDateStart) " +
            "AND (:dueDateEnd IS NULL OR i.dueDate <= :dueDateEnd) " +
            "AND (:minAmount IS NULL OR i.totalAmountWithVat >= :minAmount) " +
            "AND (:maxAmount IS NULL OR i.totalAmountWithVat <= :maxAmount) " +
            "AND (:currencyType IS NULL OR i.currencyType = :currencyType) " +
            "AND (:department IS NULL OR LOWER(i.department) LIKE LOWER(CONCAT('%', :department, '%')))")
    Page<Invoice> searchByCriteria(
            @Param("query") String query,
            @Param("staffUsername") String staffUsername,
            @Param("contractId") String contractId,
            @Param("customerId") String customerId,
            @Param("createdByUsername") String createdByUsername,
            @Param("invoiceDateStart") Date invoiceDateStart,
            @Param("invoiceDateEnd") Date invoiceDateEnd,
            @Param("dueDateStart") Date dueDateStart,
            @Param("dueDateEnd") Date dueDateEnd,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            @Param("currencyType") String currencyType,
            @Param("department") String department,
            Pageable pageable);

    boolean existsByContract_ContractId(String contractId);

    boolean existsByCustomer_CustomerId(String customerId);

    Page<Invoice> findByContract_ContractId(String contractId, Pageable pageable);

    Page<Invoice> findByCustomer_CustomerId(String customerId, Pageable pageable);

    boolean existsByInvoiceIdAndStaffId(String invoiceId, String username);

}
