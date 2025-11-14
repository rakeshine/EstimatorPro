package com.rfpe.estimator.model;

import javax.persistence.Column;
import javax.persistence.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime; 
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "resource")
public class Resource {
    @Id
    private String resourceId;

    @ManyToOne
    @JoinColumn(name = "resource_type_id", nullable = false)
    private ResourceType resourceType;

    @Column(name = "count", nullable = false)
    private float count;

    @Column(name = "created_on", nullable = false)
    private LocalDateTime createDateTime;
    
    @Column(name = "updated_on", nullable = false)
    private LocalDateTime updateDateTime;

    @ManyToOne
    @JoinColumn(name = "feature_id", nullable = false)
    private Feature feature;

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
