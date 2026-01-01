package com.scoutscentral.app.model;

public class Announcement {
  private final String id;
  private final String title;
  private final String message;
  private final String date;

  public Announcement(String id, String title, String message, String date) {
    this.id = id;
    this.title = title;
    this.message = message;
    this.date = date;
  }

  public String getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getMessage() {
    return message;
  }

  public String getDate() {
    return date;
  }
}
