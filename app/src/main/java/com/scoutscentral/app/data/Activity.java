package com.scoutscentral.app.data;

import java.util.List;

public class Activity {
  private final String id;
  private String title;
  private String date;
  private String location;
  private List<String> materials;
  private String description;
  private String imageUrl;

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
