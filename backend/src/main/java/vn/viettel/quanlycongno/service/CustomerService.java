package vn.viettel.quanlycongno.service;

import jakarta.transaction.Transactional;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.viettel.quanlycongno.dto.CustomerDto;

import java.io.IOException;
import java.util.*;

@Service
public interface CustomerService {

    Page<CustomerDto> getAllCustomers(int page, int size, String sortBy, boolean sortAsc);

    CustomerDto getCustomerById(String id);

    @Transactional
    CustomerDto saveCustomer(CustomerDto customerDto);

    @Transactional
    CustomerDto updateCustomer(String id, CustomerDto customerDto);

    @Transactional
    void deleteCustomer(String customerId);

    Page<CustomerDto> searchCustomers(String query,
                                      String createdByUsername,
                                      Date createDateStart,
                                      Date createDateEnd,
                                      Date lastUpdateStart,
                                      Date lastUpdateEnd,
                                      int page, int size, String sortBy, boolean sortAsc);

    @Transactional
    List<CustomerDto> importCustomersFromCsv(MultipartFile file) throws IOException;

    Resource exportCustomersToCsv(String query, String createdByUsername,
                                  Date createDateStart, Date createDateEnd,
                                  Date lastUpdateStart, Date lastUpdateEnd,
                                  int page, int size, String sortBy, boolean sortAsc);
}
