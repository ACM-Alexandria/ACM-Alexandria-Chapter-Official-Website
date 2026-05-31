package com.acm.acmwebsite.feature.entity;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.feature.enums.*;
import jakarta.persistence.*;

import java.time.LocalDateTime;


//this entity binds specific user to the Topic they subscribed to
@Entity
@Table(
        name = "subscription",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "subscribeTo","subscribeToId"})
        })
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable = false)
    private User user;


    //could be  committeid or news/0L (fk)
    private Long subscribeToId;


    @Enumerated(EnumType.STRING)
    private SubscripeTo subscribeTo;



    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status= SubscriptionStatus.PENDING;  //PENDING or ACTIVE or unsubscribed


    //forLogging
    private LocalDateTime confirmedAt;




    //for logging
    private final LocalDateTime createdAt = LocalDateTime.now();


    public Subscription() {}

    public Subscription(User user, SubscripeTo subscribeTo, Long subscribeToId) {
        this.subscribeTo = subscribeTo;
        this.user = user;
        this.subscribeToId = subscribeToId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }



    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(LocalDateTime confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public Long getSubscribeToId() {
        return subscribeToId;
    }

    public void setSubscribeToId(Long subscribeToId) {
        this.subscribeToId = subscribeToId;
    }

    public SubscripeTo getSubscribeTo() {
        return subscribeTo;
    }

    public void setSubscribeTo(SubscripeTo subscribeTo) {
        this.subscribeTo = subscribeTo;
    }
}
