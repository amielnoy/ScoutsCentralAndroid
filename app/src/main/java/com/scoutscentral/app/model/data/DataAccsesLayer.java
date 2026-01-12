package com.scoutscentral.app.model.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.util.Log;

import com.scoutscentral.app.model.Activity;
import com.scoutscentral.app.model.Announcement;
import com.scoutscentral.app.model.AttendanceRecord;
import com.scoutscentral.app.model.Scout;
import com.scoutscentral.app.model.ScoutLevel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class DataAccsesLayer {
  private static DataAccsesLayer instance;

  private final MutableLiveData<List<Scout>> scouts = new MutableLiveData<>();
  private final MutableLiveData<List<Activity>> activities = new MutableLiveData<>();
  private final MutableLiveData<List<Announcement>> announcements = new MutableLiveData<>();
  private final MutableLiveData<List<AttendanceRecord>> attendanceRecords = new MutableLiveData<>();
  private final MutableLiveData<Long> syncCompletedAt = new MutableLiveData<>();
  private final SupabaseService supabaseService = new SupabaseService();

  private DataAccsesLayer() {
    seedData();
    attendanceRecords.setValue(new ArrayList<>());
    syncWithSupabase(false);
  }

  public static synchronized DataAccsesLayer getInstance() {
    if (instance == null) {
      instance = new DataAccsesLayer();
    }
    return instance;
  }

  private void seedData() {
    List<Scout> scoutList = new ArrayList<>();
    scoutList.add(new Scout("1", "ליאם גרין", "", ScoutLevel.KEFIR,
      "liam.parent@example.com", "טיולים, מחנאות וגילוף בעץ",
      "קשירת קשרים, עזרה ראשונה בסיסית", Arrays.asList("act-1", "act-3")));
    scoutList.add(new Scout("2", "אוליביה ميدו", "", ScoutLevel.OFER,
      "olivia.parent@example.com", "אפייה, אומנות ויצירה, שירות קהילתי",
      "מכירת עוגיות, הכנת צמידי חברות", Collections.singletonList("act-4")));
    scoutList.add(new Scout("3", "נוח ריבר", "", ScoutLevel.KEFIR,
      "noah.parent@example.com", "דיג, בניית מודלים, אסטרונומיה",
      "הטלת חכה, זיהוי קבוצות כוכבים", Arrays.asList("act-1", "act-2")));
    scoutList.add(new Scout("4", "אמה סטון", "", ScoutLevel.NACHSHON,
      "emma.parent@example.com", "תכנות, רובוטיקה, ניסויים מדעיים",
      "פייתון בסיסי, בניית מעגלים פשוטים", Collections.emptyList()));
    scoutList.add(new Scout("5", "אווה ווילו", "", ScoutLevel.A,
      "ava.parent@example.com", "מנהיגות, תכנון אירועים, חניכת צופים צעירים",
      "דיבור בפני קהל, ניהול פרויקטים", Arrays.asList("act-2", "act-4")));
    scouts.setValue(scoutList);

    List<Activity> activityList = new ArrayList<>();
    activityList.add(new Activity("act-1", "סדנת קשירת קשרים", "2024-07-15T10:00:00Z",
      "מתנ\"ס קהילתי", Arrays.asList("חבל (מטר 1 לכל חניך)", "חוברת הדרכה"),
      "למדו קשרים חיוניים למחנאות והישרדות.", getImageUrlForTitle("סדנת קשירת קשרים")));
    activityList.add(new Activity("act-2", "בישולי מדורה", "2024-07-20T18:00:00Z",
      "פארק עמק ירוק", Arrays.asList("נקניקיות", "לחמניות", "מרשמתלו", "שיפודים"),
      "התאספו סביב המדורה לשירים ואוכל טעים.", getImageUrlForTitle("בישולי מדורה")));
    activityList.add(new Activity("act-3", "טיול שימור יערות", "2024-08-01T09:00:00Z",
      "שביל היער הלוחש", Arrays.asList("כפפות", "שקיות אשפה", "בקבוקי מים"),
      "טיול המתמקד בלימוד על הצמחייה המקומית וניקוי השביל.", getImageUrlForTitle("טיול שימור יערות")));
    activityList.add(new Activity("act-4", "ביקור במרכז גיל הזהב", "2024-08-10T14:00:00Z",
      "מרכז גיל הזהב \"שדות مشמש\"", Arrays.asList("משחקי קופסה", "כרטיסי ברכה בעבודת יד"),
      "בלו אחר הצהריים עם קשישים מקומיים, שתפו סיפורים ומשחקים.", getImageUrlForTitle("ביקור במרכז גיל הזהב")));
    activities.setValue(activityList);

    List<Announcement> announcementList = new ArrayList<>();
    announcementList.add(new Announcement("ann-1", "הרשמה למחנה קיץ",
      "ההרשמה למחנה הקיץ השנתי פתוחה! אנא הירשמו עד ה-1 ביולי כדי להבטיח את מקומכם. תכננו עבורכם שורה של פעילויות מרגשות.",
      "2024-06-15T11:00:00Z"));
    announcementList.add(new Announcement("ann-2", "השגנו את יעד גיוס התרומות!",
      "תודה ענקית לכל מי שהשתתף במכירת העוגות האחרונה שלנו. הצלחנו לעמוד ביעד גיוס התרומות לתמיכה במקלט לבעלי חיים המקומי!",
      "2024-06-20T16:30:00Z"));
    announcements.setValue(announcementList);
  }

  private String getImageUrlForTitle(String title) {
    if (title == null || title.isEmpty()) {
      return "https://images.unsplash.com/photo-1526628953301-3e589a6a8b74?q=80&w=800&auto=format&fit=crop";
    }
    String query = title.toLowerCase();
    if (query.contains("קשרים") || query.contains("חבל")) {
      return "https://images.unsplash.com/photo-1517164850305-99a3e65bb47e?q=80&w=800&auto=format&fit=crop";
    } else if (query.contains("מדורה") || query.contains("בישול")) {
      return "https://images.unsplash.com/photo-1473221326025-9183b464bb7e?q=80&w=800&auto=format&fit=crop";
    } else if (query.contains("יער") || query.contains("טיול")) {
      return "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?q=80&w=800&auto=format&fit=crop";
    } else if (query.contains("גיל הזהב") || query.contains("קשישים")) {
      return "https://images.unsplash.com/photo-1516627145497-ae6968895b74?q=80&w=800&auto=format&fit=crop";
    } else if (query.contains("מחנה") || query.contains("קיץ")) {
      return "https://images.unsplash.com/photo-1523987355523-c7b5b0dd90a7?q=80&w=800&auto=format&fit=crop";
    } else if (query.contains("ניקוי") || query.contains("סביבה")) {
      return "https://images.unsplash.com/photo-1532996122724-e3c354a0b15b?q=80&w=800&auto=format&fit=crop";
    } else if (query.contains("תרומות") || query.contains("קהילה")) {
      return "https://images.unsplash.com/photo-1559027615-cd4428d63b5f?q=80&w=800&auto=format&fit=crop";
    }
    return "https://images.unsplash.com/photo-1526628953301-3e589a6a8b74?q=80&w=800&auto=format&fit=crop";
  }

  public LiveData<List<Scout>> getScouts() {
    return scouts;
  }

  public LiveData<List<Activity>> getActivities() {
    return activities;
  }

  public LiveData<List<Announcement>> getAnnouncements() {
    return announcements;
  }

  public LiveData<List<AttendanceRecord>> getAttendanceRecords() {
    return attendanceRecords;
  }

  public void refreshFromSupabase() {
    syncWithSupabase(true);
  }

  public LiveData<Long> getSyncCompletedAt() {
    return syncCompletedAt;
  }

  public void addScout(String name, ScoutLevel level, String contact, String avatarBase64) {
    List<Scout> current = new ArrayList<>(scouts.getValue());
    String id = "scout-" + System.currentTimeMillis();
    // In a real app, upload avatarBase64 to Supabase Storage and get URL
    // For now we store the base64 string directly or a placeholder if empty
    String avatarUrl = (avatarBase64 != null && !avatarBase64.isEmpty()) ? avatarBase64 : "";
    Scout newScout = new Scout(id, name, avatarUrl, level, contact, "", "", new ArrayList<>());
    current.add(0, newScout);
    scouts.setValue(current);
    runSupabaseTask(() -> supabaseService.upsertScout(newScout));
  }

  public void updateScout(Scout updated) {
    List<Scout> current = new ArrayList<>(scouts.getValue());
    for (int i = 0; i < current.size(); i++) {
      if (current.get(i).getId().equals(updated.getId())) {
        current.set(i, updated);
        break;
      }
    }
    scouts.setValue(current);
    runSupabaseTask(() -> supabaseService.upsertScout(updated));
  }

  public void removeScout(String id) {
    List<Scout> current = new ArrayList<>(scouts.getValue());
    current.removeIf(scout -> scout.getId().equals(id));
    scouts.setValue(current);
    runSupabaseTask(() -> supabaseService.deleteScout(id));
  }

  public void addActivity(String title, String date, String location, String description) {
    List<Activity> current = new ArrayList<>(activities.getValue());
    String id = "act-" + UUID.randomUUID().toString().substring(0, 8);
    String imageUrl = getImageUrlForTitle(title);
    Activity newActivity = new Activity(id, title, date, location, new ArrayList<>(), description, imageUrl);
    current.add(0, newActivity);
    activities.setValue(current);
    runSupabaseTask(() -> supabaseService.upsertActivity(newActivity));
  }

  public void deleteActivity(String id) {
      List<Activity> current = new ArrayList<>(activities.getValue());
      current.removeIf(activity -> activity.getId().equals(id));
      activities.setValue(current);
      runSupabaseTask(() -> supabaseService.deleteActivity(id));
  }

  public void addAnnouncement(String title, String message) {
    List<Announcement> current = new ArrayList<>(announcements.getValue());
    String id = "ann-" + System.currentTimeMillis();
    Announcement newAnnouncement = new Announcement(id, title, message, isoNow());
    current.add(0, newAnnouncement);
    announcements.setValue(current);
    runSupabaseTask(() -> supabaseService.upsertAnnouncement(newAnnouncement));
  }

  public void saveAttendance(String activityId, List<String> presentScoutIds) {
    runSupabaseTask(() -> {
      supabaseService.saveAttendance(activityId, presentScoutIds);
      List<AttendanceRecord> records = supabaseService.fetchAttendanceRecords();
      attendanceRecords.postValue(records);
    });
  }

  public void clearLocalData() {
    scouts.setValue(new ArrayList<>());
    activities.setValue(new ArrayList<>());
    announcements.setValue(new ArrayList<>());
    attendanceRecords.setValue(new ArrayList<>());
  }

  public List<String> fetchAttendanceForActivity(String activityId) {
    if (!supabaseService.isConfigured()) return new ArrayList<>();
    try {
      return supabaseService.fetchAttendanceForActivity(activityId);
    } catch (Exception ex) {
      Log.e("DataRepository", "Supabase attendance fetch failed", ex);
      return new ArrayList<>();
    }
  }

  public String fetchScoutActivityHistory(String scoutId, String from, String to) throws IOException {
      return supabaseService.getScoutAttendanceHistory(scoutId, from, to);
  }

  private String isoNow() {
    return java.time.Instant.now().toString();
  }

  private void syncWithSupabase(boolean notifyOnSuccess) {
    if (!supabaseService.isConfigured()) return;
    new Thread(() -> {
      try {
        List<Scout> remoteScouts = supabaseService.fetchScouts();
        if (remoteScouts != null && !remoteScouts.isEmpty()) scouts.postValue(remoteScouts);
        else supabaseService.syncScouts(scouts.getValue());

        List<Activity> remoteActivities = supabaseService.fetchActivities();
        if (remoteActivities != null && !remoteActivities.isEmpty()) activities.postValue(remoteActivities);
        else supabaseService.syncActivities(activities.getValue());

        List<Announcement> remoteAnnouncements = supabaseService.fetchAnnouncements();
        if (remoteAnnouncements != null && !remoteAnnouncements.isEmpty()) announcements.postValue(remoteAnnouncements);
        else supabaseService.syncAnnouncements(announcements.getValue());

        List<AttendanceRecord> remoteAttendance = supabaseService.fetchAttendanceRecords();
        if (remoteAttendance != null) attendanceRecords.postValue(remoteAttendance);
        if (notifyOnSuccess) syncCompletedAt.postValue(System.currentTimeMillis());
      } catch (Exception ex) {
        Log.e("DataRepository", "Supabase sync failed", ex);
      }
    }).start();
  }

  private void runSupabaseTask(SupabaseTask task) {
    if (!supabaseService.isConfigured()) return;
    new Thread(() -> {
      try { task.run(); } catch (Exception ex) { Log.e("DataRepository", "Supabase update failed", ex); }
    }).start();
  }

  private interface SupabaseTask { void run() throws Exception; }
}
