package vn.viettel.quanlycongno.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import vn.viettel.quanlycongno.dto.StaffDto;
import vn.viettel.quanlycongno.dto.base.PagedResponse;

/**
 * Service interface for managing staff users.
 * This interface extends UserDetailsService to provide user details for authentication.
 */
@Service
public interface StaffService extends UserDetailsService {

    /**
     * Loads user details by username.
     *
     * @param username the username of the user to load
     * @return UserDetails object containing user information
     * @throws UsernameNotFoundException if the user is not found
     */
    @Override
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;

    PagedResponse<StaffDto> searchStaffs(
            String query,
            int page,
            int size,
            String sortBy,
            boolean sortAsc
    );
}
