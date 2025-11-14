package com.rfpe.estimator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "epic")
public class Epic {

    @Id
    @Column(name = "epic_id")
    private String epicId;

    @Column(name = "epic_description", nullable = false, columnDefinition = "TEXT")
    private String epicDescription;

    @Column(name = "created_on", nullable = false)
    private LocalDateTime createDateTime;
    
    @Column(name = "updated_on", nullable = false)
    private LocalDateTime updateDateTime;

    @ManyToOne
    @JoinColumn(name = "rfp_summary_id", nullable = false)
    @JsonIgnore
    private RFPSummary rfpSummary;

    @OneToMany(mappedBy = "epic", fetch = FetchType.LAZY)
    private List<Feature> features;

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
