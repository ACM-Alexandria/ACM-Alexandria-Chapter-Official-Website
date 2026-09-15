package com.acm.acmwebsite.feature.dto.commiteedtos;

import java.util.UUID;

public class CommitteeBoardMemberDto {
  private Long id;
  private String role;
  private Integer order;

  private UUID userId;

  public CommitteeBoardMemberDto() {
  }

  public CommitteeBoardMemberDto(Long id, String role, Integer order, UUID userId) {
    this.id = id;
    this.role = role;
    this.order = order;
    this.userId = userId;
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
}
