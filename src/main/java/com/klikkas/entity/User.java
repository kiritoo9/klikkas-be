package com.klikkas.entity;

import jakarta.persistence.*;

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

}
