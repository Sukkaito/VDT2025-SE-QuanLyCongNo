package vn.viettel.quanlycongno.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.Date;

/**
 * DTO for {@link vn.viettel.quanlycongno.entity.Contract}
 */
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
@AllArgsConstructor
public class ContractDto implements Serializable {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    String contractId;

    @NonNull
    @NotNull(message = "Contract name cannot be null")
    String contractName;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    Date createdDate;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    Date lastUpdatedDate;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    String createdByUsername;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    String lastUpdatedByUsername;

    @NonNull
    @NotNull(message = "Assigned staff username cannot be null")
    String assignedStaffUsername;
}