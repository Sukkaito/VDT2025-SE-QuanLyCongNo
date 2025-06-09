package vn.viettel.quanlycongno.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

/**
 * DTO for {@link vn.viettel.quanlycongno.entity.Invoice}
 */
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
public class InvoiceDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String invoiceId;

    @NonNull
    @NotBlank(message = "Invoice symbol cannot be null")
    private String invoiceSymbol;

    @NonNull
    @NotBlank(message = "Invoice number cannot be null")
    private String invoiceNumber;

    @NonNull
    @NotNull(message = "Original amount cannot be null")
    private BigDecimal originalAmount;

    @NonNull
    @NotBlank(message = "Currency type cannot be null")
    private String currencyType;

    @NonNull
    @NotNull(message = "Exchange rate cannot be null")
    private BigDecimal exchangeRate;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BigDecimal convertedAmountPreVat;

    @NonNull
    @NotNull(message = "VAT cannot be null")
    private BigDecimal vat;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BigDecimal totalAmountWithVat;

    @NonNull
    @NotNull(message = "Invoice date cannot be null")
    private Date invoiceDate;

    @NonNull
    @NotNull(message = "Due date cannot be null")
    private Date dueDate;

    @NonNull
    @NotBlank(message = "Payment method cannot be null")
    private String paymentMethod;

    @NonNull
    @NotBlank(message = "Contract ID cannot be null")
    private String contractId;

    @NonNull
    @NotBlank(message = "Customer ID cannot be null")
    private String customerId;

    @NonNull
    @NotBlank(message = "Project ID cannot be null")
    private String projectId;

    @NonNull
    @NotBlank(message = "Staff Username cannot be null")
    private String staffUsername;

    @NonNull
    @NotBlank(message = "Department cannot be null")
    private String department;

    private String notes;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Date createdDate;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Date lastUpdateDate;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String createdByUsername;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String lastUpdatedByUsername;
}