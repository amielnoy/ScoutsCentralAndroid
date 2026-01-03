package com.scoutscentral.app.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.scoutscentral.app.R;
import com.scoutscentral.app.model.AttendanceRecord;

import java.util.ArrayList;
import java.util.List;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder> {
  private final List<AttendanceRecord> items = new ArrayList<>();
  private final int maxAttendance = 45; // Fixed scale matching your screenshot (0-15-30-45)

  public void submitList(List<AttendanceRecord> records) {
    items.clear();
    if (records != null) {
      items.addAll(records);
    }
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
      .inflate(R.layout.item_attendance, parent, false);
    return new AttendanceViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull AttendanceViewHolder holder, int position) {
    AttendanceRecord record = items.get(position);
    holder.activity.setText(record.getActivityName());
    holder.value.setText(String.valueOf(record.getAttendance()));

    int attendance = record.getAttendance();
    
    // Set the green bar's weight based on attendance
    LinearLayout.LayoutParams barParams = (LinearLayout.LayoutParams) holder.bar.getLayoutParams();
    barParams.weight = (float) Math.min(attendance, maxAttendance);
    holder.bar.setLayoutParams(barParams);

    // Set the empty space's weight to complete the total weightSum (45)
    LinearLayout.LayoutParams emptyParams = (LinearLayout.LayoutParams) holder.emptySpace.getLayoutParams();
    emptyParams.weight = (float) Math.max(0, maxAttendance - attendance);
    holder.emptySpace.setLayoutParams(emptyParams);
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

  static class AttendanceViewHolder extends RecyclerView.ViewHolder {
    final TextView activity;
    final TextView value;
    final View bar;
    final View emptySpace;

    AttendanceViewHolder(@NonNull View itemView) {
      super(itemView);
      activity = itemView.findViewById(R.id.attendance_activity);
      value = itemView.findViewById(R.id.attendance_value);
      bar = itemView.findViewById(R.id.attendance_bar);
      emptySpace = itemView.findViewById(R.id.attendance_empty_space);
    }
  }
}
