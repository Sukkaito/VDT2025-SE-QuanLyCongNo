package vn.viettel.quanlycongno.entity;

import jakarta.persistence.*;
import lombok.*;
import vn.viettel.quanlycongno.dto.ContractDto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Entity representing a contract in the system.
 */

@Entity
@Table(name = "contracts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "contract_id", nullable = false, unique = true)
    private String contractId; // Mã số hợp đồng

    @Column(name = "contract_name", nullable = false)
    private String contractName; // Tên hợp đồng

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

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Invoice> invoices = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "assigned_to", nullable = false)
    private Staff assignedStaff; // Nhân viên chính phụ trách hợp đồng

    // Constructor with required fields
    public Contract(String contractName, Staff createdBy, Staff lastUpdatedBy, Staff assignedStaff) {
        this.contractName = contractName;
        this.createdBy = createdBy;
        this.lastUpdatedBy = lastUpdatedBy;
        this.assignedStaff = assignedStaff;
    }

    public ContractDto toDTO() {
        return new ContractDto(
                contractId,
                contractName,
                createdDate,
                lastUpdateDate,
                createdBy.getUsername(),
                lastUpdatedBy.getUsername(),
                assignedStaff.getUsername()
        );
    }

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
