package vn.viettel.quanlycongno.repository;

import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface MetricRepository {
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
     * @param endDate The end date for filtering (exclusive)
     * @return Total count of contracts
     */
    Long getTotalContractsForPeriod(LocalDate startDate, LocalDate endDate);
    
    /**
     * Get total count of invoices after a specific date
     * @param startDate The start date for filtering (inclusive)
     * @param endDate The end date for filtering (exclusive)
     * @return Total count of invoices
     */
    Long getTotalInvoicesForPeriod(LocalDate startDate, LocalDate endDate);
    
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
