package com.acm.acmwebsite.feature.dto;

public class EventCardDto {
  private long id;
  private String name;
  private String imageUrl;
  private String description;

  public EventCardDto() {
  }

  public EventCardDto(long id, String name, String imageUrl, String description) {
    this.id = id;
    this.name = name;
    this.imageUrl = imageUrl;
    this.description = description;
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
}