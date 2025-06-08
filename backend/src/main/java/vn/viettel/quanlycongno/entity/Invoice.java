package vn.viettel.quanlycongno.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

/**
 * Invoice entity representing an invoice in the system.
 */

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "invoice_id", nullable = false, unique = true)
    private String invoiceId;

    @Column(name = "invoice_symbol", nullable = false, unique = true)
    @NonNull
    private String invoiceSymbol; // Mã/ký hiệu hóa đơn

    @Column(name = "invoice_number", nullable = false)
    @NonNull
    private String invoiceNumber; // Số hóa đơn

    @Column(name = "original_amount", nullable = false, precision = 19, scale = 2)
    @NonNull
    private BigDecimal originalAmount; // Tổng tiền nguyên tệ

    @Column(name = "currency_type", nullable = false)
    @NonNull
    private String currencyType; // Loại tiền tệ

    @Column(name = "exchange_rate", nullable = false, precision = 19, scale = 2)
    @NonNull
    private BigDecimal exchangeRate; // Tỷ giá quy đổi ra VNĐ

    @Column(name = "converted_amount_pre_vat",
            nullable = false, precision = 19, scale = 2
    )
    private BigDecimal convertedAmountPreVat; // Tổng tiền quy đổi ra VNĐ (chưa VAT)

    @Column(name = "vat", nullable = false, precision = 19, scale = 2)
    @NonNull
    private BigDecimal vat; // VAT

    @Column(name = "total_amount_with_vat",
            nullable = false, precision = 19, scale = 2
    )
    private BigDecimal totalAmountWithVat; // Tổng tiền quy đổi (đã bao gồm VAT)

    @Column(name = "invoice_date", nullable = false)
    @Temporal(TemporalType.DATE)
    @NonNull
    private Date invoiceDate; // Ngày hóa đơn

    @Column(name = "due_date", nullable = false)
    @Temporal(TemporalType.DATE)
    @NonNull
    private Date dueDate; // Ngày tới hạn

    @Column(name = "payment_method", nullable = false)
    @NonNull
    private String paymentMethod; // Hình thức thanh toán

    @ManyToOne
    @JoinColumn(name = "contract_id", nullable = false)
    @NonNull
    private Contract contract; // Hợp đồng

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    @NonNull
    private Customer customer; // Khách hàng

    @Column(name = "project_id", nullable = false)
    @NonNull
    private String projectId; // Mã dự án

    @ManyToOne
    @JoinColumn(name = "staff_id", nullable = false)
    @NonNull
    private Staff staff; // Người phụ trách

    @Column(name = "department", nullable = false)
    @NonNull
    private String department; // Phòng ban

    @Column(name = "notes")
    private String notes; // Ghi chú

    @Column(name = "created_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date createdDate; // Ngày tạo

    @Column(name = "last_update_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date lastUpdateDate; // Ngày cập nhật cuối

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    @NonNull
    private Staff createdBy; // Người tạo

    @ManyToOne
    @JoinColumn(name = "last_updated_by", nullable = false)
    @NonNull
    private Staff lastUpdatedBy; // Người cập nhật cuối

    @PrePersist
    protected void onCreate() {
        createdDate = new Date();
        lastUpdateDate = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdateDate = new Date();
    }
}