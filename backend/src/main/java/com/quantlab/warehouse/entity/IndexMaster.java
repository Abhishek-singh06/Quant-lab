package com.quantlab.warehouse.entity;

import com.quantlab.marketdata.model.Exchange;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "index_master",
    schema = "market_data",
    indexes = {
        @Index(name = "idx_index_symbol", columnList = "index_symbol", unique = true)
    }
)
public class IndexMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "index_symbol", nullable = false, unique = true, length = 32)
    private String indexSymbol;

    @Column(name = "index_name", nullable = false, length = 128)
    private String indexName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Exchange exchange = Exchange.NSE;

    @Column(length = 64)
    private String sector;

    @Column(length = 512)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public IndexMaster() {}

    public IndexMaster(String indexSymbol, String indexName, Exchange exchange, String sector, String description) {
        this.indexSymbol = indexSymbol;
        this.indexName = indexName;
        this.exchange = exchange;
        this.sector = sector;
        this.description = description;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getIndexSymbol() { return indexSymbol; }
    public void setIndexSymbol(String indexSymbol) { this.indexSymbol = indexSymbol; }
    public String getIndexName() { return indexName; }
    public void setIndexName(String indexName) { this.indexName = indexName; }
    public Exchange getExchange() { return exchange; }
    public void setExchange(Exchange exchange) { this.exchange = exchange; }
    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
