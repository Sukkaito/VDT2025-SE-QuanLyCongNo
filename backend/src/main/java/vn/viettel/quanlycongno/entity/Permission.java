package vn.viettel.quanlycongno.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "permissions")
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permission_id", nullable = false, unique = true)
    private Long id;

    @Column(name = "permission_name", nullable = false, unique = true)
    private String permissionName;
}
