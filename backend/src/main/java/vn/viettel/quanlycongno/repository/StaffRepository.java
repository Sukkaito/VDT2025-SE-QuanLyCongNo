package vn.viettel.quanlycongno.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import vn.viettel.quanlycongno.entity.Staff;

import java.util.Optional;

@Repository
public interface StaffRepository extends CrudRepository<Staff, String> {
    Optional<Staff> findByUsername(String userName);
}
