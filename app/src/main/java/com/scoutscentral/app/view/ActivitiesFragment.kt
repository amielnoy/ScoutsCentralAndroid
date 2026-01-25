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
import androidx.annotation.ColorInt
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.datepicker.*
import com.google.android.material.snackbar.Snackbar
import com.scoutscentral.app.R
import com.scoutscentral.app.model.Activity
import com.scoutscentral.app.model.data.DataAccsesLayer
import com.scoutscentral.app.view_model.ActivitiesViewModel
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class ActivitiesFragment : Fragment() {
    private lateinit var viewModel: ActivitiesViewModel
    private val repository = DataAccsesLayer.getInstance()
    private var allActivities: List<Activity> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_activities, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[ActivitiesViewModel::class.java]

        val btnTuesday = view.findViewById<View>(R.id.btn_attendance_tuesday)
        val btnFriday = view.findViewById<View>(R.id.btn_attendance_friday)
        val deleteAllButton = view.findViewById<View>(R.id.delete_all_activities)

        btnTuesday?.setOnClickListener { showMeetingDatePicker("יום שלישי", Calendar.TUESDAY) }
        btnFriday?.setOnClickListener { showMeetingDatePicker("יום שישי", Calendar.FRIDAY) }

        deleteAllButton?.setOnClickListener {
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

        viewModel.activities.observe(viewLifecycleOwner) { activities ->
            if (activities != null) {
                allActivities = activities
            }
        }
    }

    private fun showMeetingDatePicker(dayName: String, dayOfWeek: Int) {
        if (!isAdded) return

        val tag = "meeting_picker_$dayOfWeek"
        if (childFragmentManager.findFragmentByTag(tag) != null) return

        try {
            val constraintsBuilder = CalendarConstraints.Builder()
            //constraintsBuilder.setValidator(DateValidatorPointBackward.before(MaterialDatePicker.todayInUtcMilliseconds()))

            val builder = MaterialDatePicker.Builder.datePicker()
            builder.setTitleText("בחר תאריך לפעולת $dayName")
            builder.setCalendarConstraints(constraintsBuilder.build())
            
            // Extract dates for highlighting
            val dates = allActivities.mapNotNull { it.date?.split("T")?.get(0) }
            val highlightColor = requireContext().getColor(R.color.primary)
            
            builder.setDayViewDecorator(ActivityDayDecorator(ArrayList(dates), highlightColor))

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

            picker.show(childFragmentManager, tag)
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

        val existing = allActivities.find { it.date?.startsWith(dateStr) == true }

        if (existing != null) {
            onAttendance(existing)
        } else {
            showNewActivityAttendanceDialog(meetingType, "${dateStr}T16:00:00Z")
        }
    }

    private fun showNewActivityAttendanceDialog(meetingType: String, isoDate: String) {
        if (!isAdded) return

        val scouts = repository.scouts.value ?: return
        if (scouts.isEmpty()) {
            Snackbar.make(requireView(), "לא נמצאו חניכים במערכת", Snackbar.LENGTH_SHORT).show()
            return
        }

        val names = scouts.map { it.name }.toTypedArray()
        val checked = BooleanArray(scouts.size) { false }

        AlertDialog.Builder(requireContext())
            .setTitle("פעולה חדשה וסימון נוכחות - $meetingType")
            .setMultiChoiceItems(names, checked) { _, which, isChecked ->
                checked[which] = isChecked
            }
            .setPositiveButton("שמור פעולה ונוכחות") { _, _ ->
                val activityId = viewModel.addActivity(meetingType, isoDate, "שבט מוצקין", "פעולה קבועה")
                val presentIds = scouts.filterIndexed { index, _ -> checked[index] }.map { it.id }
                if (presentIds.isNotEmpty()) {
                    repository.saveAttendance(activityId, presentIds)
                }
                Snackbar.make(requireView(), "פעולה ונוכחות נשמרו בהצלחה", Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("ביטול", null)
            .show()
    }

    private fun onAttendance(activity: Activity) {
        if (!isAdded) return
        val scouts = repository.scouts.value ?: return
        val loading = Snackbar.make(requireView(), "טוען נוכחות...", Snackbar.LENGTH_INDEFINITE)
        loading.show()

        Thread {
            val presentIds = repository.fetchAttendanceForActivity(activity.id)
            val names = scouts.map { it.name }.toTypedArray()
            val checked = BooleanArray(scouts.size) { presentIds.contains(scouts[it].id) }

            if (isAdded) {
                requireActivity().runOnUiThread {
                    loading.dismiss()
                    AlertDialog.Builder(requireContext())
                        .setTitle("נוכחות - ${activity.title} (${formatDate(activity.date)})")
                        .setMultiChoiceItems(names, checked) { _, which, isChecked ->
                            checked[which] = isChecked
                        }
                        .setPositiveButton("שמור") { _, _ ->
                            val updatedIds = scouts.filterIndexed { index, _ -> checked[index] }.map { it.id }
                            repository.saveAttendance(activity.id, updatedIds)
                            Snackbar.make(requireView(), "נוכחות נשמרה", Snackbar.LENGTH_SHORT).show()
                        }
                        .setNegativeButton("ביטול", null)
                        .show()
                }
            }
        }.start()
    }

    private fun formatDate(isoDate: String?): String {
        if (isoDate.isNullOrEmpty()) return ""
        return try {
            LocalDate.parse(isoDate.split("T")[0]).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        } catch (e: Exception) {
            isoDate ?: ""
        }
    }
}

/**
 * Robust DayViewDecorator with manual Parcelable implementation to prevent crashes.
 */
class ActivityDayDecorator(
    private val activityDates: ArrayList<String>,
    @ColorInt private val highlightColor: Int
) : DayViewDecorator() {

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
