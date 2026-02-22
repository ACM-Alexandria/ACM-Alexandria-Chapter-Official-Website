package com.acm.acmwebsite.feature.entity;

import com.acm.acmwebsite.feature.enums.*;
import jakarta.persistence.*;

import java.time.LocalDateTime;


//this entity binds specific email the user entered to the Topic he subscribed to
@Entity
@Table(
        name = "subscription",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"emailId", "subscribeTo","subscribeToId"})
        })
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="emailId")
    private Email email;


    //could be eventId or committeid or programId (fk)

    private Long subscribeToId;


    private SubscripeTo subscribeTo;



    @Column(nullable = false)
    private SubscriptionStatus status= SubscriptionStatus.PENDING;  //PENDING or ACTIVE or unsubscribed


    //forLogging
    private LocalDateTime confirmedAt;




    //for logging
    private final LocalDateTime createdAt = LocalDateTime.now();


    public Subscription(Email email, SubscripeTo subscribeTo, Long subscribeToId) {
        this.subscribeTo = subscribeTo;
        this.email = email;
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

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        this.email = email;
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
