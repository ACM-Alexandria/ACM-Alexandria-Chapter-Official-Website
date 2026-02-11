package com.acm.acmwebsite.feature.dto.commiteedtos;


public class SubscriptionDto {
    private String email;

    public SubscriptionDto() {
    }
    public SubscriptionDto(String email) {
        this.email = email;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
}
