package com.acm.acmwebsite.feature.dto.commiteedtos;

import java.util.UUID;

public class CommitteeBoardMemberDto {
  private Long id;
  private String role;
  private Integer order;

  private UUID userId;
  private String userName;
  private String profileImageUrl;
  private String linkedinUrl;

  public CommitteeBoardMemberDto() {
  }

  public CommitteeBoardMemberDto(Long id, String role, Integer order, UUID userId) {
    this.id = id;
    this.role = role;
    this.order = order;
    this.userId = userId;
  }

  public CommitteeBoardMemberDto(Long id, String role, Integer order, UUID userId, String userName, String profileImageUrl, String linkedinUrl) {
    this.id = id;
    this.role = role;
    this.order = order;
    this.userId = userId;
    this.userName = userName;
    this.profileImageUrl = profileImageUrl;
    this.linkedinUrl = linkedinUrl;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public Integer getOrder() {
    return order;
  }

  public void setOrder(Integer order) {
    this.order = order;
  }

  public UUID getUserId() {
    return userId;
  }

  public void setUserId(UUID userId) {
    this.userId = userId;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getProfileImageUrl() {
    return profileImageUrl;
  }

  public void setProfileImageUrl(String profileImageUrl) {
    this.profileImageUrl = profileImageUrl;
  }

  public String getLinkedinUrl() {
    return linkedinUrl;
  }

  public void setLinkedinUrl(String linkedinUrl) {
    this.linkedinUrl = linkedinUrl;
  }
}

