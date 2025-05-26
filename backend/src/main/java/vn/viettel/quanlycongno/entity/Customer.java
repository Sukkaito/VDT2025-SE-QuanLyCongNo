package vn.viettel.quanlycongno.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

/**
 * Entity representing a customer in the system.
 */

@Entity
@Table(name = "customers")
@Setter
@Getter
@NoArgsConstructor
@RequiredArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "customer_id", nullable = false, unique = true)
    private String customerId; // Mã khách hàng

    @Column(name = "customer_name", nullable = false)
    @NonNull
    private String customerName; // Tên khách hàng

    @Column(name = "tax_code", nullable = false)
    @NonNull
    private String taxCode; // Mã số thuế

    @Column(name = "abbreviation_name")
    private String abbreviationName; // Tên viết tắt

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