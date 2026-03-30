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

    private static final int AUDITOR_MAX_LENGTH = 45;

    @CreatedBy
    @Column(length = AUDITOR_MAX_LENGTH, updatable = false)
    protected String createdBy;

    @LastModifiedBy
    @Column(length = AUDITOR_MAX_LENGTH)
    protected String updatedBy;

    @Column(length = AUDITOR_MAX_LENGTH)
    protected String deletedBy;

    // Soft Delete 구현
    protected void delete(String deletedBy) {
        // 중복 삭제로 인해 삭제 관련 필드가 업데이트되는 상황을 방지
        if (this.deletedAt != null) {
            return;
        }

        if (deletedBy != null && deletedBy.length() > AUDITOR_MAX_LENGTH) {
            throw new IllegalArgumentException("삭제자 정보는 %d자를 초과할 수 없습니다.".formatted(AUDITOR_MAX_LENGTH));
        }

        // 삭제자가 없으면 SYSTEM 삭제로 간주
        this.updatedBy = StringUtils.hasText(deletedBy) ? deletedBy : "SYSTEM";
        this.deletedBy = StringUtils.hasText(deletedBy) ? deletedBy : "SYSTEM";

        this.updatedAt = LocalDateTime.now();
        this.deletedAt = LocalDateTime.now();
    }
}
