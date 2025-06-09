package vn.viettel.quanlycongno.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import vn.viettel.quanlycongno.configuration.SecurityConfig;
import vn.viettel.quanlycongno.dto.base.ApiResponse;
import vn.viettel.quanlycongno.entity.Staff;
import vn.viettel.quanlycongno.util.JwtUtils;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthenticationController {

    private final AuthenticationManager authManager;

    private final JwtUtils tokenProvider;

    @PostMapping("/login")
    public ApiResponse<?> login(@RequestBody LoginRequest request) {
        try {
            var authToken = new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());
            Authentication authentication = authManager.authenticate(authToken);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            Staff verifiedStaff = (Staff) authentication.getPrincipal();
            String jwt = tokenProvider.generateJwtToken(verifiedStaff);
            return ApiResponse.success(new LoginResponse(
                    jwt,
                    new LoginResponse.UserDetailsDTO(
                            verifiedStaff.getId(), verifiedStaff.getUsername(), verifiedStaff.getRole().getRoleName().name()
                    )
            ));
        } catch (AuthenticationException e) {
            return ApiResponse.error(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @AllArgsConstructor
    @Setter
    @Getter
    public static class LoginResponse {

        @AllArgsConstructor
        @Setter
        @Getter
        public static class UserDetailsDTO {
            private String id;
            private String username;
            private String role;
        }

        private String accessToken;
        private UserDetailsDTO user;
    }
}
