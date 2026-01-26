package com.scoutscentral.app.view

import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.*
import com.google.android.material.snackbar.Snackbar
import com.scoutscentral.app.R
import com.scoutscentral.app.model.Scout as ScoutModel
import com.scoutscentral.app.model.data.DataAccsesLayer
import com.scoutscentral.app.view.adapter.ActivityRowAdapter
import com.scoutscentral.app.view_model.ActivitiesViewModel
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

import com.scoutscentral.app.model.Activity as ScoutActivity

class ActivitiesFragment : Fragment(), ActivityRowAdapter.ActivityActionListener {
    private lateinit var viewModel: ActivitiesViewModel
    private var allActivities: List<ScoutActivity> = emptyList()
    private lateinit var listAdapter: ActivityRowAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_activities, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[ActivitiesViewModel::class.java]

        listAdapter = ActivityRowAdapter()
        listAdapter.setListener(this)
        val recyclerView = view.findViewById<RecyclerView>(R.id.activities_list_full)
        recyclerView?.layoutManager = LinearLayoutManager(requireContext())
        recyclerView?.adapter = listAdapter

        val btnTuesday = view.findViewById<View>(R.id.btn_attendance_tuesday)
        val btnFriday = view.findViewById<View>(R.id.btn_attendance_friday)
        val deleteAllButton = view.findViewById<View>(R.id.delete_all_activities)

        btnTuesday?.setOnClickListener { showMeetingDatePicker("יום שלישי", Calendar.TUESDAY) }
        btnFriday?.setOnClickListener { showMeetingDatePicker("יום שישי", Calendar.FRIDAY) }

        deleteAllButton?.setOnClickListener {
            if (!isAdded) return@setOnClickListener
            AlertDialog.Builder(requireContext())
                .setTitle("מחיקת כל הפעילויות")
                .setMessage("האם למחוק את כל הפעילויות? פעולה זו תמחק גם את הנתונים מהענן.")
                .setPositiveButton("מחק הכל") { _, _ ->
                    viewModel.deleteAllActivities()
                    Snackbar.make(requireView(), "כל הפעילויות נמחקו", Snackbar.LENGTH_SHORT).show()
                }
                .setNegativeButton("ביטול", null)
                .show()
        }

        viewModel.getActivities().observe(viewLifecycleOwner) { activities ->
            val list = (activities as? List<ScoutActivity>) ?: emptyList()
            allActivities = list
            listAdapter.submitList(list)
        }
    }

    // Explicitly using the full model path to avoid clash with android.app.Activity
    override fun onDelete(activity: ScoutActivity) {
        if (!isAdded) return
        AlertDialog.Builder(requireContext())
            .setTitle("מחיקת פעילות")
            .setMessage("האם למחוק את הפעילות: ${activity.title}?")
            .setPositiveButton("מחק") { _, _ ->
                viewModel.deleteActivity(activity.id)
                Snackbar.make(requireView(), "הפעילות נמחקה", Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("ביטול", null)
            .show()
    }

    override fun onEdit(activity: ScoutActivity) {
        if (!isAdded) return
        showNewActivityDetailsDialog(activity.title, activity.date, activity)
    }

    private fun showMeetingDatePicker(dayName: String, dayOfWeek: Int) {
        if (!isAdded || childFragmentManager.isStateSaved) return

        val tag = "meeting_picker_$dayOfWeek"
        if (childFragmentManager.findFragmentByTag(tag) != null) return

        try {
            val builder = MaterialDatePicker.Builder.datePicker()
            builder.setTitleText("בחר תאריך לפעולת $dayName")
            builder.setCalendarConstraints(CalendarConstraints.Builder().build())
            
            try {
                builder.setTheme(R.style.Theme_ScoutsCentral_DatePicker)
            } catch (ignored: Exception) {}

            val dates = allActivities.mapNotNull { it.date?.split("T")?.getOrNull(0) }
            val highlightColor = ContextCompat.getColor(requireContext(), R.color.primary)
            if (dates.isNotEmpty()) {
                try {
                    builder.setDayViewDecorator(ActivityDayDecorator(ArrayList(dates), highlightColor))
                } catch (ignored: Throwable) {
                    // Ignore decorator issues to avoid crashing the picker.
                }
            }

            val picker = builder.build()

            picker.addOnPositiveButtonClickListener { selection ->
                if (!isAdded || selection == null) return@addOnPositiveButtonClickListener

                val selectedDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                selectedDate.setTimeInMillis(selection)

                if (selectedDate.get(Calendar.DAY_OF_WEEK) != dayOfWeek) {
                    Snackbar.make(requireView(), "נא לבחור תאריך שחל ב$dayName", Snackbar.LENGTH_LONG).show()
                    return@addOnPositiveButtonClickListener
                }

                handleDateSelection(selectedDate, dayName)
            }

            try {
                picker.show(childFragmentManager, tag)
            } catch (e: IllegalStateException) {
                Snackbar.make(requireView(), "שגיאה בפתיחת לוח השנה", Snackbar.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Snackbar.make(requireView(), "שגיאה בפתיחת לוח השנה", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun handleDateSelection(calendar: Calendar, meetingType: String) {
        if (!isAdded) return

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val dateStr = sdf.format(calendar.time)

        val existing = allActivities.firstOrNull { it.date?.startsWith(dateStr) == true }

        if (existing != null) {
            onAttendance(existing)
        } else {
            showNewActivityDetailsDialog(meetingType, "${dateStr}T16:00:00Z", null)
        }
    }

    private fun showNewActivityDetailsDialog(defaultTitle: String, isoDate: String, existingActivity: ScoutActivity?) {
        val inflater = LayoutInflater.from(requireContext())
        val dialogView = inflater.inflate(R.layout.dialog_new_activity, null)
        val titleInput = dialogView.findViewById<EditText>(R.id.activity_title_input)
        val descInput = dialogView.findViewById<EditText>(R.id.activity_desc_input)

        titleInput.setText(existingActivity?.title ?: defaultTitle)
        descInput.setText(existingActivity?.description ?: "")

        AlertDialog.Builder(requireContext())
            .setTitle(if (existingActivity == null) "פרטי פעולה חדשה" else "עריכת פעולה")
            .setView(dialogView)
            .setPositiveButton("שמור") { _, _ ->
                val title = titleInput.text.toString().trim()
                val description = descInput.text.toString().trim()
                
                if (title.isEmpty()) {
                    Snackbar.make(requireView(), "נא להזין שם לפעולה", Snackbar.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                if (existingActivity == null) {
                    showNewActivityAttendanceDialog(title, description, isoDate)
                } else {
                    val updated = existingActivity.copy(title = title, description = description)
                    viewModel.updateActivity(updated)
                    Snackbar.make(requireView(), "הפעילות עודכנה", Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("ביטול", null)
            .show()
    }

    private fun showNewActivityAttendanceDialog(title: String, description: String, isoDate: String) {
        if (!isAdded) return

        val repository = DataAccsesLayer.getInstance()
        val scouts = (repository?.scouts?.value as? List<ScoutModel>) ?: return
        if (scouts.isEmpty()) {
            Snackbar.make(requireView(), "לא נמצאו חניכים במערכת", Snackbar.LENGTH_SHORT).show()
            return
        }

        val names = scouts.map { it.name }.toTypedArray()
        val checked = BooleanArray(scouts.size) { false }

        AlertDialog.Builder(requireContext())
            .setTitle("סימון נוכחות - $title")
            .setMultiChoiceItems(names, checked) { _, which, isChecked ->
                checked[which] = isChecked
            }
            .setPositiveButton("שמור") { _, _ ->
                val activityId = viewModel.addActivity(title, isoDate, "שבט מוצקין", description)
                val presentIds = scouts.filterIndexed { index, _ -> checked[index] }.map { it.id }
                if (presentIds.isNotEmpty()) {
                    repository?.saveAttendance(activityId, presentIds)
                }
                Snackbar.make(requireView(), "פעולה ונוכחות נשמרו בהצלחה", Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("ביטול", null)
            .show()
    }

    private fun onAttendance(activityModel: ScoutActivity) {
        if (!isAdded) return
        val repository = DataAccsesLayer.getInstance()
        val scouts = (repository?.scouts?.value as? List<ScoutModel>) ?: return
        val loading = Snackbar.make(requireView(), "טוען נוכחות...", Snackbar.LENGTH_INDEFINITE)
        loading.show()

        Thread {
            val presentIds = repository?.fetchAttendanceForActivity(activityModel.id) ?: emptyList()
            val names = scouts.map { it.name }.toTypedArray()
            val checked = BooleanArray(scouts.size) { presentIds.contains(scouts[it].id) }

            activity?.runOnUiThread {
                if (!isAdded) return@runOnUiThread
                loading.dismiss()
                AlertDialog.Builder(requireContext())
                    .setTitle("נוכחות - ${activityModel.title} (${formatDate(activityModel.date)})")
                    .setMultiChoiceItems(names, checked) { _, which, isChecked ->
                        checked[which] = isChecked
                    }
                    .setPositiveButton("שמור") { _, _ ->
                        val updatedIds = scouts.filterIndexed { index, _ -> checked[index] }.map { it.id }
                        repository.saveAttendance(activityModel.id, updatedIds)
                        Snackbar.make(requireView(), "נוכחות נשמרה", Snackbar.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("ביטול", null)
                    .show()
            }
        }.start()
    }

    private fun formatDate(isoDate: String?): String {
        if (isoDate.isNullOrEmpty()) return ""
        return try {
            LocalDate.parse(isoDate.split("T")[0]).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        } catch (e: Exception) {
            isoDate
        }
    }
}

/**
 * Robust manual Parcelable DayViewDecorator matching Material 1.13.0 API.
 */
class ActivityDayDecorator(
    private val activityDates: ArrayList<String>,
    @ColorInt private val highlightColor: Int
) : DayViewDecorator(), Parcelable {

    override fun getBackgroundColor(context: Context, year: Int, month: Int, day: Int, valid: Boolean, selected: Boolean): ColorStateList? {
        val dateKey = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)
        return if (activityDates.contains(dateKey)) ColorStateList.valueOf(highlightColor) else null
    }

    override fun getTextColor(context: Context, year: Int, month: Int, day: Int, valid: Boolean, selected: Boolean): ColorStateList? {
        val dateKey = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)
        return if (activityDates.contains(dateKey)) ColorStateList.valueOf(Color.WHITE) else null
    }

    constructor(parcel: Parcel) : this(
        parcel.createStringArrayList() ?: arrayListOf(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeStringList(activityDates)
        parcel.writeInt(highlightColor)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ActivityDayDecorator> {
        override fun createFromParcel(parcel: Parcel): ActivityDayDecorator = ActivityDayDecorator(parcel)
        override fun newArray(size: Int): Array<ActivityDayDecorator?> = arrayOfNulls(size)
    }
}
