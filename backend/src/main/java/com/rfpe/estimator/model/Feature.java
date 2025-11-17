package com.rfpe.estimator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "feature")
public class Feature {
    @Id
    @Column(name = "feature_id")
    private String featureId;

    @Column(name = "feature_description", nullable = false, columnDefinition = "TEXT")
    private String featureDescription;

    @Column(name = "created_on", nullable = false)
    private LocalDateTime createDateTime;
    
    @Column(name = "updated_on", nullable = false)
    private LocalDateTime updateDateTime;

    @ManyToOne
    @JoinColumn(name = "epic_id", nullable = false)
    @JsonIgnore
    private Epic epic;

    @Column(name = "effort_size")
    @Enumerated(EnumType.STRING)
    private EffortSize effortSize;

    @Column(name = "complexity_buffer")
    private Integer complexityBuffer;

    @Column(name = "integration_buffer")
    private Integer integrationBuffer;

    @Column(name = "requirements_clarity_buffer")
    private Integer requirementsClarityBuffer;

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
