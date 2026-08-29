package com.acm.acmwebsite.feature.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity

public class Committee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String logoUrl;

    private boolean isOpen = false;

    @OneToOne(cascade = CascadeType.ALL)
    private Message messageForCalls;

    // used to map the topic to the email in subscription like
    // (user@example.com,id_Committeename =>2_oc)
    // this pair is unique for each description
    // private String topicToken;

    @OneToMany(mappedBy = "committee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommitteeBoard> boardRoles = new ArrayList<>();

    public Committee() {
    }

    public Committee(String name, String description, String logoUrl, boolean isOpen, Message messageForCalls) {

        this.name = name;
        this.description = description;
        this.logoUrl = logoUrl;
        this.isOpen = isOpen;
        // this.topicToken="committee"+"-"+this.name;
        this.messageForCalls = messageForCalls;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public List<CommitteeBoard> getBoardRoles() {
        return boardRoles;
    }

    public void setBoardRoles(List<CommitteeBoard> boardRoles) {
        this.boardRoles.clear();
        if (boardRoles == null) {
            return;
        }
        for (CommitteeBoard boardRole : boardRoles) {
            boardRole.setCommittee(this);
            this.boardRoles.add(boardRole);
        }
    }

}
