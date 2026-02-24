package com.acm.acmwebsite.feature.entity;


import jakarta.persistence.*;

@Entity

public class Committee {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false, unique = true,length = 100)
    private String name;


    @Column(columnDefinition = "TEXT")
    private String description;



    private String logoUrl;

    private boolean isOpen=false;

    @OneToOne()
    private Message messageForCalls ;

    private String applicationFormLink;

    public Committee() {}
    public Committee(  String name, String description, String logoUrl, boolean isOpen, Message messageForCalls,String applicationFormLink) {

        this.name = name;
        this.description = description;
        this.logoUrl = logoUrl;
        this.isOpen = isOpen;
        this.applicationFormLink = applicationFormLink;
        this.messageForCalls = messageForCalls;
    }



    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getApplicationFormLink() {
        return applicationFormLink;
    }

    public void setApplicationFormLink(String applicationFormLink) {
        this.applicationFormLink = applicationFormLink;
    }

    public Message getCallMessage() {
        return messageForCalls;
    }

    public void setCallMessage(Message callMessage) {
        this.messageForCalls = callMessage;
    }



    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }





}
