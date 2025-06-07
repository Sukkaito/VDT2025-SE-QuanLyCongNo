package vn.viettel.quanlycongno.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import vn.viettel.quanlycongno.repository.StaffRepository;
import vn.viettel.quanlycongno.service.StaffService;

@Service
@AllArgsConstructor
public class StaffServiceImpl implements StaffService {
    private final StaffRepository staffRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return staffRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
