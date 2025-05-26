package vn.viettel.quanlycongno.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.viettel.quanlycongno.entity.Contract;

import java.io.Serializable;
import java.util.Date;

/**
 * DTO for {@link vn.viettel.quanlycongno.entity.Contract}
 */
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class ContractDto implements Serializable {
    String contractId;
    String contractName;
    Date createdDate;
    Date lastUpdateDate;
    String createdByUsername;
    String lastUpdatedByUsername;
    String assignedStaffUsername;
}