package com.rfpe.estimator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "rfp_metadata")
public class RFP {

    @Id
    @Column(name = "rfp_id")
    private String rfpId;
    
    @Column(name = "original_filename", nullable = false)
    private String originalFilename;
    
    @Column(name = "client_name")
    private String clientName;
    
    @Column(name = "project_name")
    private String projectName;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "created_on", nullable = false)
    private LocalDateTime createDateTime;
    
    @Column(name = "updated_on", nullable = false)
    private LocalDateTime updateDateTime;
    
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private RFPStatus status;
    
    @Column(columnDefinition = "TEXT")
    private String content;

    @OneToOne(mappedBy = "rfp", fetch = FetchType.LAZY)
    private RFPSummary rfpSummary;
    
    @PrePersist
    protected void onCreate() {
        if (createDateTime == null) {
            createDateTime = LocalDateTime.now();
        }
        if (updateDateTime == null) {
            updateDateTime = LocalDateTime.now();
        }
        if (status == null) {
            status = RFPStatus.UPLOADED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateDateTime = LocalDateTime.now();
    }
}
