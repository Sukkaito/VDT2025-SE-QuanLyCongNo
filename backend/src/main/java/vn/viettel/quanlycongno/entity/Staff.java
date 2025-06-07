package vn.viettel.quanlycongno.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
@Entity
@Table(name = "staffs")
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
public class Staff implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "staff_id", nullable = false, unique = true)
    private String id;

    @Column(name = "username", nullable = false, unique = true)
    @NonNull
    private String username;

    @Column(name = "password", nullable = false)
    @NonNull
    private String password;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    @NonNull
    private Role role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.getRoleName().name()));
    }
}