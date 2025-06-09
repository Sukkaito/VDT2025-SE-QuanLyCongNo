package vn.viettel.quanlycongno.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.viettel.quanlycongno.dto.base.ApiResponse;
import vn.viettel.quanlycongno.dto.stats.StaffStatDTO;
import vn.viettel.quanlycongno.service.MetricService;

import java.util.List;

@RestController
@RequestMapping("/api/stats")
@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_STAFF')")
@RequiredArgsConstructor
public class MetricController {
    
    private final MetricService metricService;
    
    @GetMapping("/staff/month")
    public ApiResponse<List<StaffStatDTO>> getTopStaffForMonth(
            @RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.success(
                metricService.getTopStaffForMonth(limit),
                "Retrieved top " + limit + " staff for the current month"
        );
    }
    
    @GetMapping("/staff/all-time")
    public ApiResponse<List<StaffStatDTO>> getTopStaffAllTime(
            @RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.success(
                metricService.getTopStaffAllTime(limit),
                "Retrieved top " + limit + " staff for all time"
        );
    }
}
