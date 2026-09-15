package com.acm.acmwebsite.feature.dto;

import java.time.LocalDateTime;

public class EventCardDto {
  private long id;
  private String name;
  private String imageUrl;
  private String description;
  private LocalDateTime eventTime;
  private String location;
  private boolean registrationOpen;

  public EventCardDto(long id, String name, String imageUrl, String description, LocalDateTime eventTime, String location, boolean registrationOpen) {
    this.id = id;
    this.name = name;
    this.imageUrl = imageUrl;
    this.description = description;
    this.eventTime = eventTime;
    this.location = location;
    this.registrationOpen = registrationOpen;
  }

  public EventCardDto(long id, String name, String imageUrl, String description, LocalDateTime eventTime, String location) {
    this(id, name, imageUrl, description, eventTime, location, false);
  }

  public EventCardDto() {}

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

  public LocalDateTime getEventTime() {
    return eventTime;
  }

  public void setEventTime(LocalDateTime eventTime) {
    this.eventTime = eventTime;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public boolean isRegistrationOpen() {
    return registrationOpen;
  }

  public boolean getRegistrationOpen() {
    return registrationOpen;
  }

  public void setRegistrationOpen(boolean registrationOpen) {
    this.registrationOpen = registrationOpen;
  }
}