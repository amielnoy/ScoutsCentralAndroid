package com.scoutscentral.app.data;

public class AttendanceRecord {
  private final String activityName;
  private final int attendance;

  public AttendanceRecord(String activityName, int attendance) {
    this.activityName = activityName;
    this.attendance = attendance;
  }

  public String getActivityName() {
    return activityName;
  }

  public int getAttendance() {
    return attendance;
  }
}
