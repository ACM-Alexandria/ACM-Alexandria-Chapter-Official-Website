package com.acm.acmwebsite.feature.entity;

import jakarta.persistence.*;

import java.util.concurrent.Flow;

@Entity
public class Email {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long emailId;

    private String email;

    public Email(String email) {
        this.email = email;
    }
    public Email() {}

    public String getEmail() {
        return email;
    }


    public void setEmail(String email) {
        this.email = email;
    }

    public long getEmailId() {
        return emailId;
    }

    public void setEmailId(long emailId) {
        this.emailId = emailId;
    }
}
