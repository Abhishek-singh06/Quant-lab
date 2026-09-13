package com.quantlab.institutional.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "amc_master",
    schema = "market_data",
    indexes = {
        @Index(name = "idx_amc_name", columnList = "amc_name", unique = true),
        @Index(name = "idx_amc_code", columnList = "amc_code")
    }
)
public class AmcMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amc_name", nullable = false, unique = true, length = 128)
    private String amcName;

    @Column(name = "amc_code", length = 32)
    private String amcCode;

    @Column(name = "registration_number", length = 64)
    private String registrationNumber;

    @Column(length = 255)
    private String website;

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public AmcMaster() {}

    public AmcMaster(String amcName, String amcCode, String registrationNumber, String website) {
        this.amcName = amcName;
        this.amcCode = amcCode;
        this.registrationNumber = registrationNumber;
        this.website = website;
        this.status = "ACTIVE";
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAmcName() { return amcName; }
    public void setAmcName(String amcName) { this.amcName = amcName; }
    public String getAmcCode() { return amcCode; }
    public void setAmcCode(String amcCode) { this.amcCode = amcCode; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
