package vn.viettel.quanlycongno.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffStatDTO {
    private String staffId;
    private String staffName;
    private Long contractCount;
    private Long invoiceCount;
    private Double contractPercentage;
    private Double invoicePercentage;
}
