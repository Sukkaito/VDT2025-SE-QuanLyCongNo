package vn.viettel.quanlycongno.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import vn.viettel.quanlycongno.entity.Staff;

import java.util.Optional;

@Repository
public interface StaffRepository extends CrudRepository<Staff, String> {
    Optional<Staff> findByUsername(String userName);

    @Query("SELECT s FROM Staff s " +
            "WHERE (:query IS NULL OR LOWER(s.username) LIKE LOWER(CONCAT('%', :query, '%'))) ")
    Page<Staff> filterByUsername(String query, Pageable pageable);
}
