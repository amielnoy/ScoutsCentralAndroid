package com.scoutscentral.app.model;

import java.util.List;

public class Activity {
  private final String id;
  private final String title;
  private final String date;
  private final String location;
  private final List<String> materials;
  private final String description;
  private final String imageUrl;

  public Activity(String id, String title, String date, String location, List<String> materials,
                  String description, String imageUrl) {
    this.id = id;
    this.title = title;
    this.date = date;
    this.location = location;
    this.materials = materials;
    this.description = description;
    this.imageUrl = imageUrl;
  }

  public String getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getDate() {
    return date;
  }

  public String getLocation() {
    return location;
  }

  public List<String> getMaterials() {
    return materials;
  }

  public String getDescription() {
    return description;
  }

  public String getImageUrl() {
    return imageUrl;
  }
}
