package com.scoutscentral.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.scoutscentral.app.R;
import com.scoutscentral.app.data.AttendanceRecord;

import java.util.ArrayList;
import java.util.List;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder> {
  private final List<AttendanceRecord> items = new ArrayList<>();

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
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

  static class AttendanceViewHolder extends RecyclerView.ViewHolder {
    final TextView activity;
    final TextView value;

    AttendanceViewHolder(@NonNull View itemView) {
      super(itemView);
      activity = itemView.findViewById(R.id.attendance_activity);
      value = itemView.findViewById(R.id.attendance_value);
    }
  }
}
