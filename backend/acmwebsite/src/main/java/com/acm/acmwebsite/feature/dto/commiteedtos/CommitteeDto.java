package com.acm.acmwebsite.feature.dto.commiteedtos;

import com.acm.acmwebsite.feature.entity.Message;

import java.util.List;

public class CommitteeDto {
    private long id;
    private String name;
    private String description;
    private String logoUrl;
    private boolean isOpen;
    private Message callMessage;

    private String topicToken;
    private List<CommitteeBoardMemberDto> boardRoles;

    public CommitteeDto(long id, String name, String description, String logoUrl, Message callMessage, boolean isOpen,
            List<CommitteeBoardMemberDto> boardRoles) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.logoUrl = logoUrl;
        this.callMessage = callMessage;
        this.isOpen = isOpen;
        this.boardRoles = boardRoles;
    }
    public CommitteeDto(){}

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public Message getCallMessage() {
        return callMessage;
    }

    public void setCallMessage(Message callMessage) {
        this.callMessage = callMessage;
    }

    public String getTopicToken() {
        return topicToken;
    }

    public void setTopicToken(String topicToken) {
        this.topicToken = topicToken;
    }

    public List<CommitteeBoardMemberDto> getBoardRoles() {
        return boardRoles;
    }

    public void setBoardRoles(List<CommitteeBoardMemberDto> boardRoles) {
        this.boardRoles = boardRoles;
    }
}
