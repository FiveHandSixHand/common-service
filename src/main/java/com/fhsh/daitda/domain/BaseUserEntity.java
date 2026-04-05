package com.fhsh.daitda.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseUserEntity extends BaseEntity {

    @CreatedBy
    @Column(updatable = false)
    protected UUID createdBy;

    @LastModifiedBy
    protected UUID updatedBy;

    protected UUID deletedBy;

    // Soft Delete 구현
    protected void delete(UUID deletedBy) {
        // 중복 삭제로 인해 삭제 관련 필드가 업데이트되는 상황을 방지
        if (isDeleted()) {
            return;
        }
        
        this.updatedBy = deletedBy;
        this.deletedBy = deletedBy;

        this.updatedAt = LocalDateTime.now();
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    // 삭제 복구
    public void restore(UUID restoredBy) {
        if (!isDeleted()) {
            return;
        }

        this.updatedBy = restoredBy;
        this.deletedBy = null;

        this.updatedAt = LocalDateTime.now();
        this.deletedAt = null;
    }
}
