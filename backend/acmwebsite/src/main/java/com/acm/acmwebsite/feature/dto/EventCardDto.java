package com.acm.acmwebsite.feature.dto;

public class EventCardDto {
  private long id;
  private String name;
  private String imageUrl;

  public EventCardDto() {
  }

  public EventCardDto(long id, String name, String imageUrl) {
    this.id = id;
    this.name = name;
    this.imageUrl = imageUrl;
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
}