package com.scoutscentral.app.model.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.scoutscentral.app.model.Activity as ActivityModel
import com.scoutscentral.app.model.Announcement
import com.scoutscentral.app.model.AttendanceRecord
import com.scoutscentral.app.model.Scout as ScoutModel
import com.scoutscentral.app.model.ScoutLevel
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZonedDateTime
import java.util.*

class DataAccessLayer private constructor() {
    private val _scouts = MutableLiveData<List<ScoutModel>>()
    val scouts: LiveData<List<ScoutModel>> = _scouts

    private val _activities = MutableLiveData<List<ActivityModel>>()
    val activities: LiveData<List<ActivityModel>> = _activities

    private val _announcements = MutableLiveData<List<Announcement>>()
    val announcements: LiveData<List<Announcement>> = _announcements

    private val _attendanceRecords = MutableLiveData<List<AttendanceRecord>>()
    val attendanceRecords: LiveData<List<AttendanceRecord>> = _attendanceRecords

    private val _syncCompletedAt = MutableLiveData<Long>()
    val syncCompletedAt: LiveData<Long> = _syncCompletedAt

    private val supabaseService = SupabaseService()

    init {
        _attendanceRecords.value = emptyList()
        seedData()
        syncWithSupabase(false)
    }

    fun seedData() {
        val scoutList = mutableListOf<ScoutModel>()
        scoutList.add(ScoutModel("1", "ליאם גרין", "", ScoutLevel.KEFIR, "liam.parent@example.com"))
        scoutList.add(ScoutModel("2", "אוליביה", "", ScoutLevel.OFER, "olivia.parent@example.com"))
        _scouts.value = scoutList

        val activityList = mutableListOf<ActivityModel>()
        activityList.add(ActivityModel("act-1", "סדנת קשרים", "2024-07-16T10:00:00Z", "Base", emptyList(), "Description", null))
        _activities.value = activityList
    }

    private fun getImageUrlForActivity(title: String?, description: String?): String {
        val fullText = "${title ?: ""} ${description ?: ""}".lowercase()
        return when {
            fullText.contains("קשרים") || fullText.contains("חבל") -> "https://images.unsplash.com/photo-1517164850305-99a3e65bb47e?q=80&w=800&auto=format&fit=crop"
            fullText.contains("מדורה") || fullText.contains("בישול") -> "https://images.unsplash.com/photo-1473221326025-9183b464bb7e?q=80&w=800&auto=format&fit=crop"
            fullText.contains("טיול") || fullText.contains("טבע") -> "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?q=80&w=800&auto=format&fit=crop"
            else -> "https://images.unsplash.com/photo-1526628953301-3e589a6a8b74?q=80&w=800&auto=format&fit=crop"
        }
    }

    private fun isTuesdayOrFriday(isoDate: String?): Boolean {
        if (isoDate.isNullOrEmpty()) return false
        return try {
            val zdt = ZonedDateTime.parse(isoDate)
            zdt.dayOfWeek == DayOfWeek.TUESDAY || zdt.dayOfWeek == DayOfWeek.FRIDAY
        } catch (e: Exception) { false }
    }

    fun syncWithSupabase(notifyOnSuccess: Boolean) {
        if (!supabaseService.isConfigured) return
        Thread {
            try {
                // Sync Scouts
                val remoteScouts = supabaseService.fetchScouts()
                if (!remoteScouts.isNullOrEmpty()) {
                    _scouts.postValue(remoteScouts as List<ScoutModel>)
                }

                // Sync Activities
                val remoteActivities = supabaseService.fetchActivities()
                if (!remoteActivities.isNullOrEmpty()) {
                    val processed = (remoteActivities as List<ActivityModel>)
                        .filter { activity: ActivityModel -> isTuesdayOrFriday(activity.date) }
                        .onEach { activity: ActivityModel -> 
                            activity.imageUrl = getImageUrlForActivity(activity.title, activity.description) 
                        }
                        .sortedBy { activity: ActivityModel -> activity.date }
                    _activities.postValue(processed)
                }

                // Sync Announcements
                val remoteAnnouncements = supabaseService.fetchAnnouncements()
                if (!remoteAnnouncements.isNullOrEmpty()) _announcements.postValue(remoteAnnouncements)

                // Sync Attendance
                val remoteAttendance = supabaseService.fetchAttendanceRecords()
                if (remoteAttendance != null) _attendanceRecords.postValue(remoteAttendance)

                if (notifyOnSuccess) _syncCompletedAt.postValue(System.currentTimeMillis())
            } catch (ex: Exception) {
                Log.e("DataRepository", "Supabase sync failed", ex)
            }
        }.start()
    }

    fun addScout(name: String, level: ScoutLevel, contact: String, avatarBase64: String?) {
        val id = "scout-" + System.currentTimeMillis()
        val newScout = ScoutModel(id, name, avatarBase64, level, contact)
        val current = _scouts.value?.toMutableList() ?: mutableListOf()
        current.add(0, newScout)
        _scouts.value = current
        Thread { 
            try { 
                supabaseService.upsertScout(newScout as com.scoutscentral.app.model.Scout) 
            } catch (e: Exception) { Log.e("DataRepo", "Upsert scout failed", e) }
        }.start()
    }

    fun updateScout(updated: ScoutModel) {
        val current = _scouts.value?.toMutableList() ?: return
        val index = current.indexOfFirst { it.id == updated.id }
        if (index != -1) {
            current[index] = updated
            _scouts.value = current
            Thread { 
                try { 
                    supabaseService.upsertScout(updated as com.scoutscentral.app.model.Scout) 
                } catch (e: Exception) { Log.e("DataRepo", "Update scout failed", e) }
            }.start()
        }
    }

    fun removeScout(id: String) {
        val current = _scouts.value?.toMutableList() ?: return
        if (current.removeIf { it.id == id }) {
            _scouts.value = current
            Thread { 
                try { supabaseService.deleteScout(id) } catch (e: Exception) { Log.e("DataRepo", "Delete scout failed", e) }
            }.start()
        }
    }

    fun addActivity(title: String, date: String, location: String, description: String): String {
        val id = "act-" + UUID.randomUUID().toString().substring(0, 8)
        val imageUrl = getImageUrlForActivity(title, description)
        val newActivity = ActivityModel(id, title, date, location, emptyList(), description, imageUrl)
        
        val current = _activities.value?.toMutableList() ?: mutableListOf()
        if (isTuesdayOrFriday(date)) {
            current.add(newActivity)
            current.sortBy { it.date }
            _activities.value = current
        }
        Thread { 
            try { 
                supabaseService.upsertActivity(newActivity as com.scoutscentral.app.model.Activity) 
            } catch (e: Exception) { Log.e("DataRepo", "Add activity failed", e) }
        }.start()
        return id
    }

    fun updateActivity(updated: ActivityModel) {
        val current = _activities.value?.toMutableList() ?: return
        val index = current.indexOfFirst { it.id == updated.id }
        if (index != -1) {
            current[index] = updated
            current.sortBy { it.date }
            _activities.value = current
            Thread { 
                try { 
                    supabaseService.upsertActivity(updated as ActivityModel)
                } catch (e: Exception) { Log.e("DataRepo", "Update activity failed", e) }
            }.start()
        }
    }

    fun deleteActivity(id: String) {
        val current = _activities.value?.toMutableList() ?: return
        if (current.removeIf { it.id == id }) {
            _activities.value = current
            Thread { 
                try { supabaseService.deleteActivity(id) } catch (e: Exception) { Log.e("DataRepo", "Delete activity failed", e) }
            }.start()
        }
    }

    fun deleteAllActivities() {
        _activities.value = emptyList()
        Thread { 
            try { supabaseService.deleteAllActivities() } catch (e: Exception) { Log.e("DataRepo", "Delete all activities failed", e) }
        }.start()
    }

    fun addAnnouncement(title: String, message: String) {
        val id = "ann-" + System.currentTimeMillis()
        val newAnnouncement = Announcement(id, title, message, Instant.now().toString())
        val current = _announcements.value?.toMutableList() ?: mutableListOf()
        current.add(0, newAnnouncement)
        _announcements.value = current
        Thread { 
            try { supabaseService.upsertAnnouncement(newAnnouncement) } catch (e: Exception) { Log.e("DataRepo", "Add announcement failed", e) }
        }.start()
    }

    fun deleteAllAnnouncements() {
        _announcements.value = emptyList()
        Thread { 
            try { supabaseService.deleteAllAnnouncements() } catch (e: Exception) { Log.e("DataRepo", "Delete all announcements failed", e) }
        }.start()
    }

    fun saveAttendance(activityId: String, presentScoutIds: List<String>) {
        Thread {
            try {
                supabaseService.saveAttendance(activityId, presentScoutIds)
                val records = supabaseService.fetchAttendanceRecords()
                if (records != null) _attendanceRecords.postValue(records)
            } catch (e: Exception) { Log.e("DataRepo", "Save attendance failed", e) }
        }.start()
    }

    fun fetchAttendanceForActivity(activityId: String): List<String> {
        return try { supabaseService.fetchAttendanceForActivity(activityId) } catch (e: Exception) { emptyList() }
    }

    fun fetchScoutActivityHistory(scoutId: String, from: String, to: String): String {
        return try {
            supabaseService.getScoutAttendanceHistory(scoutId, from, to)
        } catch (e: Exception) {
            ""
        }
    }

    fun refreshFromSupabase() { syncWithSupabase(true) }

    fun clearLocalData() {
        _scouts.value = emptyList()
        _activities.value = emptyList()
        _announcements.value = emptyList()
        _attendanceRecords.value = emptyList()
    }

    companion object {
        @Volatile
        private var instanceInternal: DataAccessLayer? = null

        @JvmStatic
        fun getInstance(): DataAccessLayer {
            return instanceInternal ?: synchronized(this) {
                instanceInternal ?: DataAccessLayer().also { instanceInternal = it }
            }
        }
    }
}
