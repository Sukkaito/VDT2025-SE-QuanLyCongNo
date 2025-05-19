package vn.viettel.quanlycongno.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "invoice_id", nullable = false, unique = true)
    private String invoiceId;

    @Column(name = "invoice_symbol", nullable = false, unique = true)
    private String invoiceSymbol; // Mã/ký hiệu hóa đơn

    @Column(name = "invoice_number", nullable = false)
    private String invoiceNumber; // Số hóa đơn

    @Column(name = "original_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal originalAmount; // Tổng tiền nguyên tệ

    @Column(name = "currency_type", nullable = false)
    private String currencyType; // Loại tiền tệ

    @Column(name = "exchange_rate", nullable = false, precision = 19, scale = 2)
    private BigDecimal exchangeRate; // Tỷ giá quy đổi ra VNĐ

    @Column(name = "converted_amount_pre_vat",
            nullable = false, precision = 19, scale = 2,
            insertable = false, updatable = false
    )
    private BigDecimal convertedAmountPreVat; // Tổng tiền quy đổi ra VNĐ (chưa VAT)

    @Column(name = "vat", nullable = false, precision = 19, scale = 2)
    private BigDecimal vat; // VAT

    @Column(name = "total_amount_with_vat",
            nullable = false, precision = 19, scale = 2,
            insertable = false, updatable = false
    )
    private BigDecimal totalAmountWithVat; // Tổng tiền quy đổi (đã bao gồm VAT)

    @Column(name = "invoice_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date invoiceDate; // Ngày hóa đơn

    @Column(name = "due_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date dueDate; // Ngày tới hạn

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod; // Hình thức thanh toán

    @ManyToOne
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract; // Hợp đồng

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer; // Khách hàng

    @Column(name = "project_id", nullable = false)
    private String projectId; // Mã dự án

    @ManyToOne
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff; // Người phụ trách

    @Column(name = "department", nullable = false)
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
    private Staff createdBy; // Người tạo

    @ManyToOne
    @JoinColumn(name = "last_updated_by", nullable = false)
    private Staff lastUpdatedBy; // Người cập nhật cuối

    @PrePersist
    protected void onCreate() {
        if (createdDate == null) createdDate = new Date();
        lastUpdateDate = new Date();
        calculateDerivedAmounts();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdateDate = new Date();
        calculateDerivedAmounts();
    }

    private void calculateDerivedAmounts() {
        // Calculate convertedAmountPreVat = originalAmount * exchangeRate
        if (originalAmount != null && exchangeRate != null) {
            this.convertedAmountPreVat = originalAmount.multiply(exchangeRate);
        }

        // Calculate totalAmountWithVat = convertedAmountPreVat + vat
        if (convertedAmountPreVat != null && vat != null) {
            this.totalAmountWithVat = convertedAmountPreVat.add(vat);
        }
    }
}