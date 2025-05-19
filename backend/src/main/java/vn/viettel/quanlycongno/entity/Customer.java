package vn.viettel.quanlycongno.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "customers")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "customer_id", nullable = false, unique = true)
    private String customerId; // Mã khách hàng

    @Column(name = "customer_name", nullable = false)
    private String customerName; // Tên khách hàng

    @Column(name = "tax_code", nullable = false)
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
    private Staff createdBy; // Người tạo

    @ManyToOne
    @JoinColumn(name = "last_updated_by", nullable = false)
    private Staff lastUpdatedBy; // Người cập nhật cuối

    @PrePersist
    protected void onCreate() {
        if (createdDate == null) createdDate = new Date();
        lastUpdateDate = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdateDate = new Date();
    }
}