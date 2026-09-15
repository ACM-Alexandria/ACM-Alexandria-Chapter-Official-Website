package com.acm.acmwebsite.feature.dto;

import com.acm.acmwebsite.feature.dto.commiteedtos.CommitteeBoardMemberDto;
import java.util.List;

public class ClubCardDto {
    private long id;
    private String name;
    private String imageUrl;
    private String description;
    private boolean isExternal;
    private List<CommitteeBoardMemberDto> boardRoles;

    public ClubCardDto() {
    }

    public ClubCardDto(long id, String name, String imageUrl, String description) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.description = description;
    }

    public ClubCardDto(long id, String name, String imageUrl, String description, boolean isExternal) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.description = description;
        this.isExternal = isExternal;
    }

    public ClubCardDto(long id, String name, String imageUrl, String description, List<CommitteeBoardMemberDto> boardRoles) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.description = description;
        this.boardRoles = boardRoles;
    }

    public ClubCardDto(long id, String name, String imageUrl, String description, boolean isExternal, List<CommitteeBoardMemberDto> boardRoles) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.description = description;
        this.isExternal = isExternal;
        this.boardRoles = boardRoles;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean getIsExternal() {
        return isExternal;
    }

    public boolean isExternal() {
        return isExternal;
    }

    public void setIsExternal(boolean isExternal) {
        this.isExternal = isExternal;
    }

    public List<CommitteeBoardMemberDto> getBoardRoles() {
        return boardRoles;
    }

    public void setBoardRoles(List<CommitteeBoardMemberDto> boardRoles) {
        this.boardRoles = boardRoles;
    }
}
