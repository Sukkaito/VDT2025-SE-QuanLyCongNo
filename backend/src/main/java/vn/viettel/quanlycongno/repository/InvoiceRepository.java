package vn.viettel.quanlycongno.repository;

import org.springframework.data.repository.CrudRepository;
import vn.viettel.quanlycongno.entity.Invoice;

import java.util.List;

public interface InvoiceRepository extends CrudRepository<Invoice, String> {
    // Additional query methods can be defined here if needed
}
