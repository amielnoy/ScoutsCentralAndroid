package com.scoutscentral.app.model;

public class AttendanceRecord {
  private final String activityName;
  private final String date;
  private final int attendance;

  public AttendanceRecord(String activityName, String date, int attendance) {
    this.activityName = activityName;
    this.date = date;
    this.attendance = attendance;
  }

  public String getActivityName() {
    return activityName;
  }

  public String getDate() {
    return date;
  }

  public int getAttendance() {
    return attendance;
  }
}
