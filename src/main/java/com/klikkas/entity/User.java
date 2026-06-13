package com.klikkas.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    private UUID id;

    private String email;
    private String password;
    private String fullname;
    private String phone;
    private String address;
    private String remark;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;


    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getFullname() {
        return fullname;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getRemark() {
        return remark;
    }

    @Column(name = "created_at")
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Column(name = "updated_at")
    public LocalDateTime getUpdatedAt() {
        return createdAt;
    }

    @Column(name = "deleted_at")
    public LocalDateTime getDeletedAt() {
        return createdAt;
    }

}
