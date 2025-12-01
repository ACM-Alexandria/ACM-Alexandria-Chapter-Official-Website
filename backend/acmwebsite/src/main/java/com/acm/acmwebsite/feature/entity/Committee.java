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

    @OneToOne(fetch = FetchType.LAZY)
    private Message messageForCalls ;



    //used to map the topic to the email in subscription like (moatef123@gmail.com,id_Committeename =>2_oc)
    //this pair is unique for each description
    private String topicToken;





    private String applicationFormLink;

    public Committee() {}
    public Committee(  String name, String description, String logoUrl, boolean isOpen, Message messageForCalls,String applicationFormLink) {

        this.name = name;
        this.description = description;
        this.logoUrl = logoUrl;
        this.isOpen = isOpen;
        this.topicToken="committee"+"-"+this.name;
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
    public String getTopicToken() {
        return topicToken;
    }
    public void setTopicToken(String topicToken) {
        this.topicToken = topicToken;
    }




}
