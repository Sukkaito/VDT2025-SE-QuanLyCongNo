package vn.viettel.quanlycongno.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.viettel.quanlycongno.constant.RoleEnum;

import java.io.Serializable;

/**
 * DTO for {@link vn.viettel.quanlycongno.entity.Staff}
 */
@AllArgsConstructor
@Getter
@NoArgsConstructor(force = true)
@Setter
public class StaffDto implements Serializable {
    @NotNull(message = "Staff id cannot be null")
    private String id;
    @NotNull(message = "Staff username cannot be null")
    private String username;
    @Setter
    private String roleName;
}