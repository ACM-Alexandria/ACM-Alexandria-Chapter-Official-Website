package com.acm.acmwebsite.feature.dto.commiteedtos;

public class CommitteeDto {
    private long id;
    private String name;
    private String description;
    private String logoUrl;
    private boolean isOpen;
    private String callMessage;
    private String applicationFormLink;

    public CommitteeDto( long id,String name, String description, String logoUrl, String callMessage, boolean isOpen, String applicationFormLink) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.logoUrl = logoUrl;
        this.callMessage = callMessage;
        this.isOpen = isOpen;
        this.applicationFormLink = applicationFormLink;
    }

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

    public String getCallMessage() {
        return callMessage;
    }

    public void setCallMessage(String callMessage) {
        this.callMessage = callMessage;
    }

    public String getApplicationFormLink() {
        return applicationFormLink;
    }

    public void setApplicationFormLink(String applicationFormLink) {
        this.applicationFormLink = applicationFormLink;
    }
}
