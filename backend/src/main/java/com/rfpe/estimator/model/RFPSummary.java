package com.rfpe.estimator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "rfp_summary")
public class RFPSummary {
    @Id
    @Column(name = "summary_id", nullable = false)
    private String summaryId;

    @Column(name = "summary", nullable = false)
    private String summary;
    
    @Column(name = "functional_requirements", nullable = false, columnDefinition = "TEXT")
    private String functionalRequirements;
    
    @Column(name = "non_functional_requirements", nullable = false, columnDefinition = "TEXT")
    private String nonFunctionalRequirements;
    
    @OneToOne
    @JoinColumn(name = "rfp_id", nullable = false)
    @JsonIgnore
    private RFP rfp;

    @OneToMany(mappedBy = "rfpSummary", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Epic> epics;

    @Column(name = "create_date_time", nullable = false)
    private LocalDateTime createDateTime;

    @Column(name = "update_date_time", nullable = false)
    private LocalDateTime updateDateTime;
    
    @PrePersist
    protected void onCreate() {
        if (createDateTime == null) {
            createDateTime = LocalDateTime.now();
        }
        if (updateDateTime == null) {
            updateDateTime = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateDateTime = LocalDateTime.now();
    }
}
