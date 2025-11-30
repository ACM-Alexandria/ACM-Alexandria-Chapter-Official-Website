package com.acm.acmwebsite.feature.entity;

import com.acm.acmwebsite.feature.enums.subscriptionStatus;
import jakarta.persistence.*;

import java.time.LocalDate;


//this entity binds specific email the user entered to the Topic he subscribed to
@Entity
@Table(name = "subscription",
        uniqueConstraints = @UniqueConstraint(columnNames = {"email","topic"}))
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;


    @Column(nullable = false)
    private String email;




    private String topic;



    @Column(nullable = false)
    private subscriptionStatus status=subscriptionStatus.PENDING;  //PENDING or ACTIVE


    //forLogging
    private LocalDate confirmedAt;





    private final LocalDate createdAt = LocalDate.now();






    public Subscription() {}
    public Subscription(String email,String topic) {
        this.topic = topic;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public subscriptionStatus getStatus() {
        return status;
    }

    public void setStatus(subscriptionStatus status) {
        this.status = status;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }



    public LocalDate getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(LocalDate confirmedAt) {
        this.confirmedAt = confirmedAt;
    }
}
