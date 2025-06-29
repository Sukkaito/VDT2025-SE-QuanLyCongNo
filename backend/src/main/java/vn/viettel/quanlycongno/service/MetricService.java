package vn.viettel.quanlycongno.service;

import vn.viettel.quanlycongno.dto.stats.StaffStatDTO;
import java.util.List;

/**
 * Service interface for retrieving metrics related to staff performance.
 * This service provides methods to get statistics about staff members based on their contracts and imports.
 */
public interface MetricService {
    /**
     * Get top staff with most contracts and imports for the past month
     * @param limit The number of staff to return
     * @return List of staff statistics
     */
    List<StaffStatDTO> getTopStaffForMonth(int limit);
    
    /**
     * Get top staff with most contracts and imports all time
     * @param limit The number of staff to return
     * @return List of staff statistics
     */
    List<StaffStatDTO> getTopStaffAllTime(int limit);

    /**
     * Get total count of contracts for a specific period
     * @param startDate The start date for filtering (inclusive)
     * @param endDate The end date for filtering (exclusive)
     * @return Total count of contracts
     */
    Long getTotalContractsForPeriod(String startDate, String endDate);

    /**
     * Get total count of invoices for a specific period
     * @param startDate The start date for filtering (inclusive)
     * @param endDate The end date for filtering (exclusive)
     * @return Total count of invoices
     */
    Long getTotalInvoicesForPeriod(String startDate, String endDate);
}
