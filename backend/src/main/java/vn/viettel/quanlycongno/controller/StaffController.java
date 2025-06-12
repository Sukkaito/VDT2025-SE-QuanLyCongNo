package vn.viettel.quanlycongno.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.viettel.quanlycongno.dto.base.ApiResponse;
import vn.viettel.quanlycongno.service.StaffService;

@RestController
@RequestMapping("/api/staff")
@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_STAFF')")
public class StaffController {

    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @GetMapping("/search")
    public ApiResponse<?> searchStaffs(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "username") String sortBy,
            @RequestParam(defaultValue = "true") boolean sortAsc) {
        try {
            return ApiResponse.success(staffService.searchStaffs(
                    query, page, size, sortBy, sortAsc
            ));
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
