package vn.viettel.quanlycongno.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Stream;

/**
 * DTO for {@link vn.viettel.quanlycongno.entity.Customer}
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerDto implements Serializable {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    String customerId;

    @NotNull(message = "Customer name cannot be null")
    @NonNull
    String customerName;

    @NotNull(message = "Tax code cannot be null")
    @NonNull
    String taxCode;

    String abbreviationName;

    String assignedStaffUsername;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    Date createdDate;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    Date lastUpdateDate;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    String createdByUsername;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    String lastUpdatedByUsername;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @NonNull
    List<String> usedToBeHandledByStaffUsernames = new ArrayList<>();
}
