package vn.viettel.quanlycongno.service.impl;

import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import vn.viettel.quanlycongno.dto.StaffDto;
import vn.viettel.quanlycongno.dto.base.PagedResponse;
import vn.viettel.quanlycongno.entity.Staff;
import vn.viettel.quanlycongno.repository.StaffRepository;
import vn.viettel.quanlycongno.service.StaffService;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class StaffServiceImpl implements StaffService {
    private final StaffRepository staffRepository;
    private final ModelMapper mapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return staffRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Override
    public PagedResponse<StaffDto> searchStaffs(String query, int page, int size, String sortBy, boolean sortAsc) {
        Pageable pageable = getPageableStaff(page, size, sortBy, sortAsc);

        Page<StaffDto> contractPage = staffRepository.filterByUsername(query, pageable)
                .map(staff -> mapper.map(staff, StaffDto.class));
        return PagedResponse.from(contractPage, contractPage.getContent());
    }

    private static Pageable getPageableStaff(int page, int size, String sortBy, boolean sortAsc) {
        Set<String> allowedSortFields = Arrays.stream(Staff.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());

//        allowedSortFields.addAll(Set.of("contractName, assignedStaff", "createdBy", "lastUpdatedBy"));

        // Validate and default if invalid
        String validatedSortField = allowedSortFields.contains(sortBy) ? sortBy : "username";

        return PageRequest.of(page, size, Sort.by(sortAsc ? Sort.Direction.ASC : Sort.Direction.DESC, validatedSortField));
    }


}
