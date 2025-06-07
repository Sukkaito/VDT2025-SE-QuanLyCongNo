package vn.viettel.quanlycongno.service;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.List;

import vn.viettel.quanlycongno.dto.ContractDto;

import java.util.Date;

@Service
public interface ContractService {

    Page<ContractDto> getAllContracts(int page, int size, String sortBy, boolean sortAsc);

    ContractDto getContractById(String id);

    @Transactional
    ContractDto saveContract(ContractDto contractDto);

    @Transactional
    ContractDto updateContract(String id, ContractDto contractDto);

    @Transactional
    void deleteContract(String contractId);

    Page<ContractDto> searchContracts(String query,
                                     String assignedStaffUsername,
                                     String createdByUsername,
                                     Date createDateStart,
                                     Date createDateEnd,
                                     Date lastUpdateStart,
                                     Date lastUpdateEnd,
                                     int page, int size, String sortBy, boolean sortAsc);

    @Transactional
    List<ContractDto> importContractsFromCsv(MultipartFile file) throws IOException;

    Resource exportContractsToCsv(String query, String assignedStaffUsername, String createdByUsername,
                                         Date createDateStart, Date createDateEnd, Date lastUpdateStart, Date lastUpdateEnd,
                                         int page, int size, String sortBy, boolean sortAsc);
}
