package com.localservicefinder.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "admin")
public class Admin {

    @Id
    @Column(name = "id")
    private UUID id;

    @OneToOne(fetch = FetchType.EAGER)
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    @Column(name = "is_super")
    private Boolean isSuper = false;

    // Default Constructor
    public Admin() {}

    public Admin(User user) {
        this.user = user;
        this.id = user.getId();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) {
        this.user = user;
        this.id = user != null ? user.getId() : null;
    }

    public Boolean getIsSuper() { return isSuper; }
    public void setIsSuper(Boolean isSuper) { this.isSuper = isSuper; }
}
