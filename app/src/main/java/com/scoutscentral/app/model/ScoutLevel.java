package com.scoutscentral.app.model;

public enum ScoutLevel {
  KEFIR("כפיר"),
  OFER("עופר"),
  NACHSHON("נחשון"),
  A("יהודה");

  private final String label;

  ScoutLevel(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
