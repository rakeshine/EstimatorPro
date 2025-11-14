package com.rfpe.estimator.model;

import javax.persistence.Column;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Entity;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "resource_type")
public class ResourceType {
    @Id
    @Column(name = "resource_type_id")
    private String resourceTypeId;
    
    @Column(name = "resource_type_name", nullable = false)
    private String resourceTypeName;
    
    @Column(name = "created_on", nullable = false)
    private LocalDateTime createDateTime;
    
    @Column(name = "updated_on", nullable = false)
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
