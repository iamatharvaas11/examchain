package com.examchain.release.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "paper_approvals", uniqueConstraints = {
        @UniqueConstraint(name = "uq_paper_authority", columnNames = {"paper_id", "authority_id"})
})
public class PaperApprovalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "paper_id", nullable = false, length = 64)
    private String paperId;

    @Column(name = "authority_id", nullable = false, length = 64)
    private String authorityId;

    @Column(name = "authority_role", nullable = false, length = 64)
    private String authorityRole;

    @Column(name = "authority_name", length = 128)
    private String authorityName;

    @Column(name = "signature_hash", nullable = false, length = 64)
    private String signatureHash;

    @Column(name = "comments", length = 255)
    private String comments;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public PaperApprovalEntity() {
        this.createdAt = Instant.now();
    }

    public PaperApprovalEntity(
            String paperId,
            String authorityId,
            String authorityRole,
            String authorityName,
            String signatureHash,
            String comments
    ) {
        this.paperId = paperId;
        this.authorityId = authorityId;
        this.authorityRole = authorityRole;
        this.authorityName = authorityName;
        this.signatureHash = signatureHash;
        this.comments = comments;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getPaperId() {
        return paperId;
    }

    public void setPaperId(String paperId) {
        this.paperId = paperId;
    }

    public String getAuthorityId() {
        return authorityId;
    }

    public void setAuthorityId(String authorityId) {
        this.authorityId = authorityId;
    }

    public String getAuthorityRole() {
        return authorityRole;
    }

    public void setAuthorityRole(String authorityRole) {
        this.authorityRole = authorityRole;
    }

    public String getAuthorityName() {
        return authorityName;
    }

    public void setAuthorityName(String authorityName) {
        this.authorityName = authorityName;
    }

    public String getSignatureHash() {
        return signatureHash;
    }

    public void setSignatureHash(String signatureHash) {
        this.signatureHash = signatureHash;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

