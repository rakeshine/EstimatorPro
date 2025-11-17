package com.rfpe.estimator.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "resource")
public class Resource {
    @Id
    private String resourceId;

    @Column(name = "resource_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ResourceType resourceType;

    @OneToMany(mappedBy = "resource", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Allocation> allocations = new ArrayList<Allocation>();

    @Column(name = "created_on", nullable = false)
    private LocalDateTime createDateTime;
    
    @Column(name = "updated_on", nullable = false)
    private LocalDateTime updateDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rfp_id", nullable = false)
    @JsonIgnore
    private RFP rfp;

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

    public void clearAllocations() {
        allocations.clear();
    }

    public void setAllocations(List<Allocation> allocations) {
        if (this.allocations == null) {
            this.allocations = new ArrayList<>();
        } else {
            this.allocations.clear();
        }
        if (allocations != null) {
            this.allocations.addAll(allocations);
        }
    }

    public void addAllocation(Allocation allocation) {
        if (allocations == null) {
            allocations = new ArrayList<>();
        }
        allocations.add(allocation);
    }

    public void removeAllocation(Allocation allocation) {
        if (allocations == null) {
            return;
        }
        allocations.remove(allocation);
    }

    public void updateAllocation(Allocation allocation) {
        if (allocations == null) {
            return;
        }
        boolean isUpdated = false;
        for (Allocation a : allocations) {
            if (a.getAllocationId().equals(allocation.getAllocationId())) {
                a.setAllocationPercent(allocation.getAllocationPercent());
                isUpdated = true;
                break; // No need to continue once found and updated
            }
        }
        if (!isUpdated) {
        	allocation.setAllocationId(UUID.randomUUID().toString());
            addAllocation(allocation);
        }
    }
}
