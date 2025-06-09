package vn.viettel.quanlycongno.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import vn.viettel.quanlycongno.repository.StaffMetricRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class StaffMetricRepositoryImpl implements StaffMetricRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Map<String, Object>> getTopStaffForPeriod(int limit, LocalDate startDate) {
        String sql = """
            SELECT 
                s.staff_id as staff_id, 
                s.username as staff_name,
                COUNT(DISTINCT c.contract_id) as contract_count,
                COUNT(DISTINCT i.invoice_id) as invoice_count
            FROM 
                staffs s
                LEFT JOIN contracts c ON s.staff_id = c.assigned_to AND c.created_date >= ?
                LEFT JOIN invoices i ON s.staff_id = i.staff_id AND i.created_date >= ?
            GROUP BY 
                s.staff_id, s.username
            ORDER BY 
                (COUNT(DISTINCT c.contract_id) + COUNT(DISTINCT i.invoice_id)) DESC
            LIMIT ?
        """;
        
        return jdbcTemplate.queryForList(sql, startDate, startDate, limit);
    }

    @Override
    public List<Map<String, Object>> getTopStaffAllTime(int limit) {
        String sql = """
            SELECT 
                s.staff_id as staff_id, 
                s.username as staff_name,
                COUNT(DISTINCT c.contract_id) as contract_count,
                COUNT(DISTINCT i.invoice_id) as invoice_count
            FROM 
                staffs s
                LEFT JOIN contracts c ON s.staff_id = c.assigned_to
                LEFT JOIN invoices i ON s.staff_id = i.staff_id
            GROUP BY 
                s.staff_id, s.username
            ORDER BY 
                (COUNT(DISTINCT c.contract_id) + COUNT(DISTINCT i.invoice_id)) DESC
            LIMIT ?
        """;
        
        return jdbcTemplate.queryForList(sql, limit);
    }

    @Override
    public Long getTotalContractsForPeriod(LocalDate startDate) {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM contracts WHERE created_date >= ?", 
            Long.class, 
            startDate
        );
    }

    @Override
    public Long getTotalInvoicesForPeriod(LocalDate startDate) {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM invoices WHERE created_date >= ?", 
            Long.class, 
            startDate
        );
    }

    @Override
    public Long getTotalContractsAllTime() {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM contracts", 
            Long.class
        );
    }

    @Override
    public Long getTotalInvoicesAllTime() {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM invoices", 
            Long.class
        );
    }
}
