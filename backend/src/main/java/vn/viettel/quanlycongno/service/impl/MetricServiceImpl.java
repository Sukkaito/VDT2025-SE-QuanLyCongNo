package vn.viettel.quanlycongno.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import vn.viettel.quanlycongno.dto.stats.StaffStatDTO;
import vn.viettel.quanlycongno.repository.MetricRepository;
import vn.viettel.quanlycongno.service.MetricService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MetricServiceImpl implements MetricService {

    private final MetricRepository metricRepository;

    @Override
    @Cacheable(value = "topStaffForMonth", key = "#limit")
    @Scheduled(cron = "0 */10 * * * *")
    public List<StaffStatDTO> getTopStaffForMonth(int limit) {
        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        
        // Get total counts for percentage calculations
        Long totalContracts = metricRepository.getTotalContractsForPeriod(firstDayOfMonth, null);
        Long totalInvoices = metricRepository.getTotalInvoicesForPeriod(firstDayOfMonth, null);
        
        if (totalContracts == null) totalContracts = 0L;
        if (totalInvoices == null) totalInvoices = 0L;
        
        // Get staff metrics
        List<Map<String, Object>> staffMetrics = metricRepository.getTopStaffForPeriod(limit, firstDayOfMonth);
        
        return convertToStaffStatDTOs(staffMetrics, totalContracts, totalInvoices);
    }

    @Override
    @Cacheable(value = "topStaffAllTime", key = "#limit")
    @Scheduled(cron = "0 */10 * * * *")
    public List<StaffStatDTO> getTopStaffAllTime(int limit) {
        // Get total counts for percentage calculations
        Long totalContracts = metricRepository.getTotalContractsAllTime();
        Long totalInvoices = metricRepository.getTotalInvoicesAllTime();
        
        if (totalContracts == null) totalContracts = 0L;
        if (totalInvoices == null) totalInvoices = 0L;
        
        // Get staff metrics
        List<Map<String, Object>> staffMetrics = metricRepository.getTopStaffAllTime(limit);
        
        return convertToStaffStatDTOs(staffMetrics, totalContracts, totalInvoices);
    }

    @Override
    @Cacheable(value = "totalContractsForPeriod", key = "#startDate+#endDate")
    @Scheduled(cron = "0 */10 * * * *")
    public Long getTotalContractsForPeriod(String startDate, String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;
        return metricRepository.getTotalContractsForPeriod(start, end);
    }

    @Override
    @Cacheable(value = "totalContractsForPeriod", key = "#startDate+#endDate")
    @Scheduled(cron = "0 */10 * * * *")
    public Long getTotalInvoicesForPeriod(String startDate, String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;
        return metricRepository.getTotalInvoicesForPeriod(start, end);
    }

    private List<StaffStatDTO> convertToStaffStatDTOs(
            List<Map<String, Object>> staffMetrics, 
            Long totalContracts, 
            Long totalInvoices) {
        
        List<StaffStatDTO> result = new ArrayList<>();
        
        for (Map<String, Object> row : staffMetrics) {
            long contractCount = ((Number) row.get("contract_count")).longValue();
            long invoiceCount = ((Number) row.get("invoice_count")).longValue();
            
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
