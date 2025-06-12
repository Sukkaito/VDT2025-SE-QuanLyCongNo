package vn.viettel.quanlycongno.repository.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import vn.viettel.quanlycongno.repository.MetricRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public class MetricRepositoryImpl implements MetricRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
                (contract_count + invoice_count) DESC
            LIMIT ?
            """;

        String sql2 = """
                SELECT
                    s.staff_id as staff_id,
                    s.username as staff_name,
                    COALESCE(ic.contract_count, 0) AS contract_count,
                    COALESCE(iv.invoice_count, 0) AS invoice_count
                FROM staffs s
                LEFT JOIN (
                    SELECT assigned_to AS staff_id, COUNT(DISTINCT contract_id) AS contract_count
                    FROM contracts c
                    WHERE c.created_date >= ?
                    GROUP BY assigned_to
                ) ic ON ic.staff_id = s.staff_id
                LEFT JOIN (
                    SELECT staff_id, COUNT(DISTINCT invoice_id) AS invoice_count
                    FROM invoices i
                    WHERE i.created_date >= ?
                    GROUP BY staff_id
                ) iv ON iv.staff_id = s.staff_id
                ORDER BY contract_count + invoice_count DESC
                LIMIT ?
                """;
        return jdbcTemplate.queryForList(sql2, startDate, startDate, limit);
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
                (contract_count + invoice_count) DESC
            LIMIT ?
            """;

        String sql2 = """
        SELECT 
            agg.staff_id as staff_id,
            agg.contract_count,
            agg.invoice_count,
            s.username as staff_name
        FROM (
            SELECT 
                i.staff_id,
                COUNT(DISTINCT i.contract_id) AS contract_count,
                COUNT(DISTINCT i.invoice_id) AS invoice_count
            FROM invoices i
            GROUP BY i.staff_id
        ) agg
        LEFT JOIN staffs s ON agg.staff_id = s.staff_id
        ORDER BY (agg.contract_count + agg.invoice_count) DESC
        LIMIT ?
                """;
        return jdbcTemplate.queryForList(sql2, limit);
    }

    @Override
    public Long getTotalContractsForPeriod(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT COUNT(*) FROM contracts WHERE created_date >= ? AND " +
                "(? IS NULL OR created_date < ?)";
        return jdbcTemplate.queryForObject(sql, Long.class, startDate, endDate, endDate);
    }

    @Override
    public Long getTotalInvoicesForPeriod(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT COUNT(*) FROM invoices WHERE created_date >= ? AND" +
                "(? IS NULL OR created_date < ?)";
        return jdbcTemplate.queryForObject(sql, Long.class, startDate, endDate, endDate);
    }

    @Override
    public Long getTotalContractsAllTime() {
        String sql = "SELECT COUNT(*) FROM contracts";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    @Override
    public Long getTotalInvoicesAllTime() {
        String sql = "SELECT COUNT(*) FROM invoices";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }
}
