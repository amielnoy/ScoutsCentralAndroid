package com.scoutscentral.app.data;

public enum ScoutLevel {
  KEFIR("כפיר"),
  OFER("עופר"),
  NACHSHON("נחשון"),
  A("a");

  private final String label;

  ScoutLevel(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
