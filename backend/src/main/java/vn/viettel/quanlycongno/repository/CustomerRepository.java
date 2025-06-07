package vn.viettel.quanlycongno.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.viettel.quanlycongno.entity.Customer;

import java.util.Date;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {
    @Query("SELECT c FROM Customer c " +
            "LEFT JOIN c.createdBy u WITH (:createdByUsername IS NOT NULL) " +
            "WHERE (:query IS NULL OR LOWER(c.customerName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "       OR LOWER(c.taxCode) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "       OR LOWER(c.abbreviationName) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "AND (:createdByUsername IS NULL OR u.username = :createdByUsername) " +
            "AND (:createDateStart IS NULL OR c.createdDate >= :createDateStart) " +
            "AND (:createDateEnd IS NULL OR c.createdDate <= :createDateEnd) " +
            "AND (:lastUpdateStart IS NULL OR c.lastUpdateDate >= :lastUpdateStart) " +
            "AND (:lastUpdateEnd IS NULL OR c.lastUpdateDate <= :lastUpdateEnd) ")
    Page<Customer> searchByCriteria(
            @Param("query") String query,
            @Param("createdByUsername") String createdByUsername,
            @Param("createDateStart") Date createDateStart,
            @Param("createDateEnd") Date createDateEnd,
            @Param("lastUpdateStart") Date lastUpdateStart,
            @Param("lastUpdateEnd") Date lastUpdateEnd,
            Pageable pageable);
}
