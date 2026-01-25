package com.scoutscentral.app.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.scoutscentral.app.R;
import com.scoutscentral.app.model.Activity;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    
    // Set date badge
    if (activity.getDate() != null && !activity.getDate().isEmpty()) {
        try {
            ZonedDateTime zdt = ZonedDateTime.parse(activity.getDate());
            holder.dateDay.setText(zdt.format(DateTimeFormatter.ofPattern("dd")));
            holder.dateMonth.setText(zdt.format(DateTimeFormatter.ofPattern("MMM", Locale.US)));
        } catch (Exception e) {
            holder.dateDay.setText("??");
            holder.dateMonth.setText("---");
        }
    }

    // Set description
    if (activity.getDescription() != null && !activity.getDescription().isEmpty()) {
        holder.description.setText(activity.getDescription());
        holder.description.setVisibility(View.VISIBLE);
    } else {
        holder.description.setText("אין תיאור זמין");
        holder.description.setVisibility(View.VISIBLE);
    }

    // Load image
    Glide.with(holder.itemView.getContext())
        .load(activity.getImageUrl())
        .placeholder(R.drawable.avatar_placeholder)
        .centerCrop()
        .into(holder.image);
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

  static class ActivityViewHolder extends RecyclerView.ViewHolder {
    final TextView title;
    final TextView description;
    final TextView location;
    final TextView dateDay;
    final TextView dateMonth;
    final ImageView image;

    ActivityViewHolder(@NonNull View itemView) {
      super(itemView);
      title = itemView.findViewById(R.id.activity_title);
      description = itemView.findViewById(R.id.activity_description);
      location = itemView.findViewById(R.id.activity_location);
      dateDay = itemView.findViewById(R.id.activity_date_day);
      dateMonth = itemView.findViewById(R.id.activity_date_month);
      image = itemView.findViewById(R.id.activity_image);
    }
  }
}
