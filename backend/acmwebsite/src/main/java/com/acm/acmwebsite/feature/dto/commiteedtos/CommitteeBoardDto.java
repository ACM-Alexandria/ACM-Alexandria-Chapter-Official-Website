package com.acm.acmwebsite.feature.dto.commiteedtos;

public class CommitteeBoardDto {
  private Long id;
  private String name;
  private String imageUrl;
  private String role;
  private Integer order;
  private String linkedinUrl;

  public CommitteeBoardDto(Long id, String name, String imageUrl, String role, Integer order, String linkedinUrl) {
    this.id = id;
    this.name = name;
    this.imageUrl = imageUrl;
    this.role = role;
    this.order = order;
    this.linkedinUrl = linkedinUrl;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
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

  public String getLinkedinUrl() {
    return linkedinUrl;
  }

  public void setLinkedinUrl(String linkedinUrl) {
    this.linkedinUrl = linkedinUrl;
  }
}