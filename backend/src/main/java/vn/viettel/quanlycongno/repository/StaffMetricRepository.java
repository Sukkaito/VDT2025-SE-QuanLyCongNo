package vn.viettel.quanlycongno.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface StaffMetricRepository {
    /**
     * Get top staff with most contracts and invoices for a specific period
     * @param limit The number of staff to return
     * @param startDate The start date for filtering (inclusive)
     * @return List of staff statistics as maps
     */
    List<Map<String, Object>> getTopStaffForPeriod(int limit, LocalDate startDate);
    
    /**
     * Get top staff with most contracts and invoices for all time
     * @param limit The number of staff to return
     * @return List of staff statistics as maps
     */
    List<Map<String, Object>> getTopStaffAllTime(int limit);
    
    /**
     * Get total count of contracts after a specific date
     * @param startDate The start date for filtering (inclusive)
     * @return Total count of contracts
     */
    Long getTotalContractsForPeriod(LocalDate startDate);
    
    /**
     * Get total count of invoices after a specific date
     * @param startDate The start date for filtering (inclusive)
     * @return Total count of invoices
     */
    Long getTotalInvoicesForPeriod(LocalDate startDate);
    
    /**
     * Get total count of contracts for all time
     * @return Total count of contracts
     */
    Long getTotalContractsAllTime();
    
    /**
     * Get total count of invoices for all time
     * @return Total count of invoices
     */
    Long getTotalInvoicesAllTime();
}
