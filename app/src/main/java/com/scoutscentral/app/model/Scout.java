package com.scoutscentral.app.model;

import java.util.List;

public class Scout {
  private final String id;
  private String name;
  private String avatarUrl;
  private ScoutLevel level;
  private String contact;
  private String interests;
  private String skills;
  private List<String> activityHistory;

  public Scout(String id, String name, String avatarUrl, ScoutLevel level, String contact,
               String interests, String skills, List<String> activityHistory) {
    this.id = id;
    this.name = name;
    this.avatarUrl = avatarUrl;
    this.level = level;
    this.contact = contact;
    this.interests = interests;
    this.skills = skills;
    this.activityHistory = activityHistory;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }

  public ScoutLevel getLevel() {
    return level;
  }

  public void setLevel(ScoutLevel level) {
    this.level = level;
  }

  public String getContact() {
    return contact;
  }

  public void setContact(String contact) {
    this.contact = contact;
  }

  public String getInterests() {
    return interests;
  }

  public void setInterests(String interests) {
    this.interests = interests;
  }

  public String getSkills() {
    return skills;
  }

  public void setSkills(String skills) {
    this.skills = skills;
  }

  public List<String> getActivityHistory() {
    return activityHistory;
  }
}
