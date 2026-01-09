package com.scoutscentral.app.view.adapter;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.scoutscentral.app.R;
import com.scoutscentral.app.model.Activity;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ActivityCardAdapter extends RecyclerView.Adapter<ActivityCardAdapter.ActivityViewHolder> {
  public interface ActivityActionListener {
    void onAttendance(Activity activity);
    void onDelete(Activity activity);
  }

  private final List<Activity> items = new ArrayList<>();
  private final ActivityActionListener listener;

  public ActivityCardAdapter(ActivityActionListener listener) {
    this.listener = listener;
  }

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
      .inflate(R.layout.item_activity_card, parent, false);
    return new ActivityViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
    Activity activity = items.get(position);
    holder.title.setText(activity.getTitle());
    holder.date.setText(formatDate(activity.getDate()));
    holder.description.setText(activity.getDescription());
    Glide.with(holder.itemView)
      .load(activity.getImageUrl())
      .placeholder(R.drawable.activity_placeholder)
      .error(R.drawable.activity_placeholder)
      .into(holder.image);
    holder.attendanceButton.setOnClickListener(v -> listener.onAttendance(activity));
    holder.menuButton.setOnClickListener(v -> showMenu(v, activity));
  }

  private void showMenu(View anchor, Activity activity) {
      PopupMenu popup = new PopupMenu(anchor.getContext(), anchor);
      popup.getMenuInflater().inflate(R.menu.activity_item_menu, popup.getMenu());
      popup.setOnMenuItemClickListener(item -> {
          if (item.getItemId() == R.id.action_delete_activity) {
              listener.onDelete(activity);
              return true;
          }
          return false;
      });
      popup.show();
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
    final TextView date;
    final TextView description;
    final MaterialButton attendanceButton;
    final ImageView image;
    final ImageButton menuButton;

    ActivityViewHolder(@NonNull View itemView) {
      super(itemView);
      image = itemView.findViewById(R.id.activity_image);
      title = itemView.findViewById(R.id.activity_card_title);
      date = itemView.findViewById(R.id.activity_card_date);
      description = itemView.findViewById(R.id.activity_card_description);
      attendanceButton = itemView.findViewById(R.id.activity_attendance);
      menuButton = itemView.findViewById(R.id.activity_menu);
    }
  }
}
