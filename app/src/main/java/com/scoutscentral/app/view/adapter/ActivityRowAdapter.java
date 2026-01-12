package com.scoutscentral.app.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.scoutscentral.app.R;
import com.scoutscentral.app.model.Activity;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ActivityRowAdapter extends RecyclerView.Adapter<ActivityRowAdapter.ActivityViewHolder> {
  private final List<Activity> items = new ArrayList<>();

  public void submitList(List<Activity> activities) {
    items.clear();
    if (activities != null) {
      items.addAll(activities);
    }
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
      .inflate(R.layout.item_activity_row, parent, false);
    return new ActivityViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
    Activity activity = items.get(position);
    holder.title.setText(activity.getTitle());
    holder.location.setText(activity.getLocation());
    holder.date.setText(formatDate(activity.getDate()));
  }

  private String formatDate(String isoDate) {
      if (isoDate == null || isoDate.isEmpty()) {
          return "";
      }
      try {
          ZonedDateTime zonedDateTime = ZonedDateTime.parse(isoDate);
          DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm");
          return zonedDateTime.format(formatter);
      } catch (Exception e) {
          return isoDate; // Fallback to original string if parsing fails
      }
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

  static class ActivityViewHolder extends RecyclerView.ViewHolder {
    final TextView title;
    final TextView location;
    final TextView date;

    ActivityViewHolder(@NonNull View itemView) {
      super(itemView);
      title = itemView.findViewById(R.id.activity_title);
      location = itemView.findViewById(R.id.activity_location);
      date = itemView.findViewById(R.id.activity_date);
    }
  }
}
