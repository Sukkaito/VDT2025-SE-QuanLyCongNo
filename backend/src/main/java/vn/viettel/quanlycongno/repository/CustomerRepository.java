package vn.viettel.quanlycongno.repository;

import org.springframework.data.repository.CrudRepository;
import vn.viettel.quanlycongno.entity.Customer;

public interface CustomerRepository extends CrudRepository<Customer, String> {
    // This interface extends BaseRepository to inherit common CRUD operations.
    // Additional custom query methods can be defined here if needed.
}
