package vn.viettel.quanlycongno.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.viettel.quanlycongno.entity.Contract;

import java.util.Date;
import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<Contract, String> {
    @Query("SELECT c FROM Contract c " +
            "LEFT JOIN c.assignedStaff s WITH (:assignedStaffUsername IS NOT NULL) " +
            "LEFT JOIN c.createdBy u WITH (:createdByUsername IS NOT NULL) " +
            "WHERE (:query IS NULL OR LOWER(c.contractName) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "AND (:assignedStaffUsername IS NULL OR s.username = :assignedStaffUsername) " +
            "AND (:createdByUsername IS NULL OR u.username = :createdByUsername) " +
            "AND (:createDateStart IS NULL OR c.createdDate >= :createDateStart) " +
            "AND (:createDateEnd IS NULL OR c.createdDate <= :createDateEnd) " +
            "AND (:lastUpdateStart IS NULL OR c.lastUpdatedDate >= :lastUpdateStart) " +
            "AND (:lastUpdateEnd IS NULL OR c.lastUpdatedDate <= :lastUpdateEnd) ")
    Page<Contract> searchByCriteria(
            @Param("query") String query,
            @Param("assignedStaffUsername") String assignedStaffUsername,
            @Param("createdByUsername") String createdByUsername,
            @Param("createDateStart") Date createDateStart,
            @Param("createDateEnd") Date createDateEnd,
            @Param("lastUpdateStart") Date lastUpdateStart,
            @Param("lastUpdateEnd") Date lastUpdateEnd,
            Pageable pageable);

    @Query("SELECT DISTINCT c FROM Contract c " +
            "LEFT JOIN Invoice i ON i.contract.contractId = c.contractId " +
            "WHERE i.customer.customerId = :customerId")
    Page<Contract> getContractsByCustomerIdAndInvoiceID(
            @Param("customerId") String customerId,
            Pageable pageable);

    boolean existsByContractIdAndAssignedStaffId(String contractId, String userid);

    List<Contract> findAllByOrderByCreatedDate();
}
