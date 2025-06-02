package vn.viettel.quanlycongno.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.viettel.quanlycongno.entity.Invoice;

import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, String> {

    boolean existsByContract_ContractId(String contractId);
}
