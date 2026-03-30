package com.fhsh.daitda.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseUserEntity extends BaseEntity {
    @CreatedBy
    @Column(length=45, updatable = false)
    protected String createdBy;

    @LastModifiedBy
    @Column(length=45)
    protected String modifiedBy;

    @Column(length=45)
    protected String deletedBy;

    // Soft Delete 구현
    protected void delete(String deletedBy) {
        // 중복 삭제로 인해 삭제 관련 필드가 업데이트되는 상황을 방지
        if (this.deletedAt != null) {
            return;
        }

        // 삭제자가 없으면 SYSTEM 삭제로 간주
        this.deletedBy = StringUtils.hasText(deletedBy) ? deletedBy : "SYSTEM";

        this.deletedAt = LocalDateTime.now();
    }
}
