package vn.viettel.quanlycongno.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.viettel.quanlycongno.dto.stats.StaffStatDTO;
import vn.viettel.quanlycongno.repository.StaffMetricRepository;
import vn.viettel.quanlycongno.service.MetricService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MetricServiceImpl implements MetricService {

    private final StaffMetricRepository staffMetricRepository;

    @Override
    public List<StaffStatDTO> getTopStaffForMonth(int limit) {
        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        
        // Get total counts for percentage calculations
        Long totalContracts = staffMetricRepository.getTotalContractsForPeriod(firstDayOfMonth);
        Long totalInvoices = staffMetricRepository.getTotalInvoicesForPeriod(firstDayOfMonth);
        
        if (totalContracts == null) totalContracts = 0L;
        if (totalInvoices == null) totalInvoices = 0L;
        
        // Get staff metrics
        List<Map<String, Object>> staffMetrics = staffMetricRepository.getTopStaffForPeriod(limit, firstDayOfMonth);
        
        return convertToStaffStatDTOs(staffMetrics, totalContracts, totalInvoices);
    }

    @Override
    public List<StaffStatDTO> getTopStaffAllTime(int limit) {
        // Get total counts for percentage calculations
        Long totalContracts = staffMetricRepository.getTotalContractsAllTime();
        Long totalInvoices = staffMetricRepository.getTotalInvoicesAllTime();
        
        if (totalContracts == null) totalContracts = 0L;
        if (totalInvoices == null) totalInvoices = 0L;
        
        // Get staff metrics
        List<Map<String, Object>> staffMetrics = staffMetricRepository.getTopStaffAllTime(limit);
        
        return convertToStaffStatDTOs(staffMetrics, totalContracts, totalInvoices);
    }
    
    private List<StaffStatDTO> convertToStaffStatDTOs(
            List<Map<String, Object>> staffMetrics, 
            Long totalContracts, 
            Long totalInvoices) {
        
        List<StaffStatDTO> result = new ArrayList<>();
        
        for (Map<String, Object> row : staffMetrics) {
            Long contractCount = ((Number) row.get("contract_count")).longValue();
            Long invoiceCount = ((Number) row.get("invoice_count")).longValue();
            
            StaffStatDTO dto = StaffStatDTO.builder()
                .staffId((String) row.get("staff_id"))
                .staffName((String) row.get("staff_name"))
                .contractCount(contractCount)
                .invoiceCount(invoiceCount)
                .contractPercentage(totalContracts > 0 ? (contractCount * 100.0 / totalContracts) : 0)
                .invoicePercentage(totalInvoices > 0 ? (invoiceCount * 100.0 / totalInvoices) : 0)
                .build();
            
            result.add(dto);
        }
        
        return result;
    }
}
