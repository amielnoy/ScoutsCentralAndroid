package com.scoutscentral.app.data;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SupabaseService {
  private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

  private final OkHttpClient client = new OkHttpClient();
  private final Gson gson = new Gson();
  private final String restUrl;
  private final String apiKey;

  public SupabaseService() {
    this.restUrl = SupabaseConfig.SUPABASE_URL;
    this.apiKey = SupabaseConfig.SUPABASE_ANON_KEY;
  }

  public boolean isConfigured() {
    return restUrl != null
      && restUrl.startsWith("https://")
      && apiKey != null
      && !apiKey.isEmpty();
  }

  public List<Scout> fetchScouts() throws IOException {
    JsonArray scoutsJson = getJsonArray("/scouts?select=*");
    JsonArray historyJson = getJsonArray("/scout_activity_history?select=scout_id,activity_id");
    Map<String, List<String>> historyMap = new HashMap<>();
    for (JsonElement element : historyJson) {
      JsonObject row = element.getAsJsonObject();
      String scoutId = getString(row, "scout_id");
      String activityId = getString(row, "activity_id");
      if (scoutId == null || activityId == null) {
        continue;
      }
      historyMap.computeIfAbsent(scoutId, key -> new ArrayList<>()).add(activityId);
    }

    List<Scout> result = new ArrayList<>();
    for (JsonElement element : scoutsJson) {
      JsonObject row = element.getAsJsonObject();
      String id = getString(row, "id");
      String name = getString(row, "name");
      String avatarUrl = getString(row, "avatar_url");
      String levelRaw = getString(row, "level");
      String contact = getString(row, "contact");
      String interests = getString(row, "interests");
      String skills = getString(row, "skills");
      List<String> history = historyMap.getOrDefault(id, new ArrayList<>());
      ScoutLevel level = parseLevel(levelRaw);
      result.add(new Scout(id, name, avatarUrl, level, contact, interests, skills, history));
    }
    return result;
  }

  public List<Activity> fetchActivities() throws IOException {
    JsonArray activitiesJson = getJsonArray("/activities?select=*");
    List<Activity> result = new ArrayList<>();
    for (JsonElement element : activitiesJson) {
      JsonObject row = element.getAsJsonObject();
      String id = getString(row, "id");
      String title = getString(row, "title");
      String date = getString(row, "date");
      String location = getString(row, "location");
      String description = getString(row, "description");
      String imageUrl = getString(row, "image_url");
      List<String> materials = getStringList(row, "materials");
      result.add(new Activity(id, title, date, location, materials, description, imageUrl));
    }
    return result;
  }

  public List<Announcement> fetchAnnouncements() throws IOException {
    JsonArray announcementsJson = getJsonArray("/announcements?select=*");
    List<Announcement> result = new ArrayList<>();
    for (JsonElement element : announcementsJson) {
      JsonObject row = element.getAsJsonObject();
      String id = getString(row, "id");
      String title = getString(row, "title");
      String message = getString(row, "message");
      String date = getString(row, "date");
      result.add(new Announcement(id, title, message, date));
    }
    return result;
  }

  public void syncScouts(List<Scout> scouts) throws IOException {
    if (scouts == null || scouts.isEmpty()) {
      return;
    }
    JsonArray payload = new JsonArray();
    JsonArray historyPayload = new JsonArray();
    for (Scout scout : scouts) {
      payload.add(toScoutJson(scout));
      if (scout.getActivityHistory() != null) {
        for (String activityId : scout.getActivityHistory()) {
          JsonObject join = new JsonObject();
          join.addProperty("scout_id", scout.getId());
          join.addProperty("activity_id", activityId);
          historyPayload.add(join);
        }
      }
    }
    postJson("/scouts?on_conflict=id", payload, true);
    if (historyPayload.size() > 0) {
      postJson("/scout_activity_history?on_conflict=scout_id,activity_id", historyPayload, true);
    }
  }

  public void syncActivities(List<Activity> activities) throws IOException {
    if (activities == null || activities.isEmpty()) {
      return;
    }
    JsonArray payload = new JsonArray();
    for (Activity activity : activities) {
      payload.add(toActivityJson(activity));
    }
    postJson("/activities?on_conflict=id", payload, true);
  }

  public void syncAnnouncements(List<Announcement> announcements) throws IOException {
    if (announcements == null || announcements.isEmpty()) {
      return;
    }
    JsonArray payload = new JsonArray();
    for (Announcement announcement : announcements) {
      payload.add(toAnnouncementJson(announcement));
    }
    postJson("/announcements?on_conflict=id", payload, true);
  }

  public void upsertScout(Scout scout) throws IOException {
    postJson("/scouts?on_conflict=id", toScoutJson(scout), true);
  }

  public void deleteScout(String id) throws IOException {
    Request request = requestBuilder("/scouts?id=eq." + id)
      .delete()
      .build();
    executeRequest(request);
  }

  public void upsertActivity(Activity activity) throws IOException {
    postJson("/activities?on_conflict=id", toActivityJson(activity), true);
  }

  public void upsertAnnouncement(Announcement announcement) throws IOException {
    postJson("/announcements?on_conflict=id", toAnnouncementJson(announcement), true);
  }

  public void saveAttendance(String activityId, List<String> presentScoutIds) throws IOException {
    Request deleteRequest = requestBuilder("/activity_attendance?activity_id=eq." + activityId)
      .delete()
      .build();
    executeRequest(deleteRequest);

    if (presentScoutIds == null || presentScoutIds.isEmpty()) {
      return;
    }
    JsonArray payload = new JsonArray();
    for (String scoutId : presentScoutIds) {
      JsonObject row = new JsonObject();
      row.addProperty("activity_id", activityId);
      row.addProperty("scout_id", scoutId);
      row.addProperty("present", true);
      payload.add(row);
    }
    postJson("/activity_attendance?on_conflict=activity_id,scout_id", payload, true);
  }

  public List<AttendanceRecord> fetchAttendanceRecords() throws IOException {
    JsonArray activitiesJson = getJsonArray("/activities?select=id,title,date");
    JsonArray attendanceJson = getJsonArray("/activity_attendance?select=activity_id,scout_id");

    List<ActivityMeta> activities = new ArrayList<>();
    for (JsonElement element : activitiesJson) {
      JsonObject row = element.getAsJsonObject();
      String id = getString(row, "id");
      String title = getString(row, "title");
      String dateRaw = getString(row, "date");
      Instant date = null;
      if (dateRaw != null && !dateRaw.isEmpty()) {
        try {
          date = Instant.parse(dateRaw);
        } catch (Exception ignored) {
          date = null;
        }
      }
      if (id != null) {
        activities.add(new ActivityMeta(id, title != null ? title : id, date));
      }
    }

    Map<String, Integer> counts = new HashMap<>();
    for (JsonElement element : attendanceJson) {
      JsonObject row = element.getAsJsonObject();
      String activityId = getString(row, "activity_id");
      if (activityId == null) {
        continue;
      }
      counts.put(activityId, counts.getOrDefault(activityId, 0) + 1);
    }

    activities.sort((a, b) -> {
      if (a.date == null && b.date == null) {
        return 0;
      }
      if (a.date == null) {
        return 1;
      }
      if (b.date == null) {
        return -1;
      }
      return b.date.compareTo(a.date);
    });

    List<AttendanceRecord> records = new ArrayList<>();
    int limit = Math.min(5, activities.size());
    for (int i = 0; i < limit; i++) {
      ActivityMeta meta = activities.get(i);
      int count = counts.getOrDefault(meta.id, 0);
      records.add(new AttendanceRecord(meta.title, count));
    }
    return records;
  }

  private static class ActivityMeta {
    final String id;
    final String title;
    final Instant date;

    ActivityMeta(String id, String title, Instant date) {
      this.id = id;
      this.title = title;
      this.date = date;
    }
  }

  public List<String> fetchAttendanceForActivity(String activityId) throws IOException {
    String path = "/activity_attendance?select=scout_id&activity_id=eq." + activityId + "&present=eq.true";
    JsonArray attendanceJson = getJsonArray(path);
    List<String> presentIds = new ArrayList<>();
    for (JsonElement element : attendanceJson) {
      JsonObject row = element.getAsJsonObject();
      String scoutId = getString(row, "scout_id");
      if (scoutId != null) {
        presentIds.add(scoutId);
      }
    }
    return presentIds;
  }

  public Instructor authenticateInstructor(String email, String password) throws IOException {
    String path = "/instructors?select=id,name,email,password&email=eq." + email;
    JsonArray instructors = getJsonArray(path);
    if (instructors.size() == 0) {
      return null;
    }
    JsonObject row = instructors.get(0).getAsJsonObject();
    String stored = getString(row, "password");
    if (stored == null || !stored.equals(password)) {
      return null;
    }
    String id = getString(row, "id");
    String name = getString(row, "name");
    return new Instructor(id != null ? id : "", name != null ? name : email);
  }

  public static class Instructor {
    public final String id;
    public final String name;

    public Instructor(String id, String name) {
      this.id = id;
      this.name = name;
    }
  }

  public String generateProgressPlan(Scout scout, String interests, String skills) throws IOException {
    if (!isConfigured()) {
      throw new IOException("Supabase not configured");
    }
    JsonObject payload = new JsonObject();
    payload.addProperty("scout_id", scout.getId());
    payload.addProperty("scout_name", scout.getName());
    payload.addProperty("level", scout.getLevel() != null ? scout.getLevel().name() : "");
    payload.addProperty("interests", interests);
    payload.addProperty("skills", skills);
    RequestBody requestBody = RequestBody.create(gson.toJson(payload), JSON);
    Request request = functionRequestBuilder("/progress-plan")
      .post(requestBody)
      .build();
    try (Response response = client.newCall(request).execute()) {
      if (!response.isSuccessful()) {
        throw new IOException("Supabase function failed: " + response.code());
      }
      String body = response.body() != null ? response.body().string() : "{}";
      JsonObject json = JsonParser.parseString(body).getAsJsonObject();
      String plan = getString(json, "plan");
      return plan != null ? plan : "";
    }
  }

  private JsonArray getJsonArray(String path) throws IOException {
    Request request = requestBuilder(path).get().build();
    try (Response response = client.newCall(request).execute()) {
      if (!response.isSuccessful()) {
        throw new IOException("Supabase GET failed: " + response.code());
      }
      String body = response.body() != null ? response.body().string() : "[]";
      return JsonParser.parseString(body).getAsJsonArray();
    }
  }

  private void postJson(String path, JsonElement body, boolean mergeDuplicates) throws IOException {
    RequestBody requestBody = RequestBody.create(gson.toJson(body), JSON);
    Request.Builder builder = requestBuilder(path)
      .post(requestBody);
    if (mergeDuplicates) {
      builder.addHeader("Prefer", "resolution=merge-duplicates");
    }
    executeRequest(builder.build());
  }

  private Request.Builder requestBuilder(String path) {
    return new Request.Builder()
      .url(restUrl + "/rest/v1" + path)
      .addHeader("apikey", apiKey)
      .addHeader("Authorization", "Bearer " + apiKey)
      .addHeader("Content-Type", "application/json");
  }

  private Request.Builder functionRequestBuilder(String path) {
    return new Request.Builder()
      .url(restUrl + "/functions/v1" + path)
      .addHeader("apikey", apiKey)
      .addHeader("Authorization", "Bearer " + apiKey)
      .addHeader("Content-Type", "application/json");
  }

  private void executeRequest(Request request) throws IOException {
    try (Response response = client.newCall(request).execute()) {
      if (!response.isSuccessful()) {
        throw new IOException("Supabase request failed: " + response.code());
      }
    }
  }

  private JsonObject toScoutJson(Scout scout) {
    JsonObject obj = new JsonObject();
    obj.addProperty("id", scout.getId());
    obj.addProperty("name", scout.getName());
    obj.addProperty("avatar_url", scout.getAvatarUrl());
    obj.addProperty("level", scout.getLevel() != null ? scout.getLevel().name() : null);
    obj.addProperty("contact", scout.getContact());
    obj.addProperty("interests", scout.getInterests());
    obj.addProperty("skills", scout.getSkills());
    return obj;
  }

  private JsonObject toActivityJson(Activity activity) {
    JsonObject obj = new JsonObject();
    obj.addProperty("id", activity.getId());
    obj.addProperty("title", activity.getTitle());
    obj.addProperty("date", activity.getDate());
    obj.addProperty("location", activity.getLocation());
    obj.addProperty("description", activity.getDescription());
    obj.addProperty("image_url", activity.getImageUrl());
    JsonArray materials = new JsonArray();
    if (activity.getMaterials() != null) {
      for (String material : activity.getMaterials()) {
        materials.add(material);
      }
    }
    obj.add("materials", materials);
    return obj;
  }

  private JsonObject toAnnouncementJson(Announcement announcement) {
    JsonObject obj = new JsonObject();
    obj.addProperty("id", announcement.getId());
    obj.addProperty("title", announcement.getTitle());
    obj.addProperty("message", announcement.getMessage());
    obj.addProperty("date", announcement.getDate());
    return obj;
  }

  private ScoutLevel parseLevel(String levelRaw) {
    if (levelRaw == null || levelRaw.isEmpty()) {
      return ScoutLevel.KEFIR;
    }
    try {
      return ScoutLevel.valueOf(levelRaw);
    } catch (IllegalArgumentException ex) {
      return ScoutLevel.KEFIR;
    }
  }

  private String getString(JsonObject obj, String key) {
    JsonElement element = obj.get(key);
    return element != null && !element.isJsonNull() ? element.getAsString() : null;
  }

  private List<String> getStringList(JsonObject obj, String key) {
    JsonElement element = obj.get(key);
    List<String> result = new ArrayList<>();
    if (element == null || element.isJsonNull()) {
      return result;
    }
    for (JsonElement item : element.getAsJsonArray()) {
      result.add(item.getAsString());
    }
    return result;
  }
}
