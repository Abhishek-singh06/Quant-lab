package com.quantlab.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "watchlists")
public class Watchlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "description")
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "watchlist_symbols", joinColumns = @JoinColumn(name = "watchlist_id"))
    @Column(name = "symbol", nullable = false)
    private List<String> symbols = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public Watchlist() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Watchlist(String name, String userId) {
        this.name = name;
        this.userId = userId;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Watchlist(String name, String userId, String description, List<String> symbols) {
        this.name = name;
        this.userId = userId;
        this.description = description;
        if (symbols != null) {
            this.symbols = new ArrayList<>(symbols);
        }
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getSymbols() { return symbols; }
    public void setSymbols(List<String> symbols) {
        this.symbols = symbols != null ? new ArrayList<>(symbols) : new ArrayList<>();
    }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
