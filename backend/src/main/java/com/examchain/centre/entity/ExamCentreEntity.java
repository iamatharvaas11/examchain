package com.examchain.centre.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "exam_centres")
public class ExamCentreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "centre_code", nullable = false, unique = true, length = 64)
    private String centreCode;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "city", nullable = false, length = 64)
    private String city;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public ExamCentreEntity() {
        this.isActive = true;
        this.createdAt = Instant.now();
    }

    public ExamCentreEntity(String centreCode, String name, String city) {
        this.centreCode = centreCode;
        this.name = name;
        this.city = city;
        this.isActive = true;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCentreCode() {
        return centreCode;
    }

    public void setCentreCode(String centreCode) {
        this.centreCode = centreCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

