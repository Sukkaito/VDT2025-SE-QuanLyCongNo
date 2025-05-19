package vn.viettel.quanlycongno.repository;

import org.springframework.data.repository.CrudRepository;
import vn.viettel.quanlycongno.entity.Contract;

public interface ContractRepository extends CrudRepository<Contract, String> {
    // Define any custom query methods if needed
}
