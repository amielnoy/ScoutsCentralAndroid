package com.scoutscentral.app.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.scoutscentral.app.R;
import com.scoutscentral.app.model.Announcement;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.AnnouncementViewHolder> {
  private final List<Announcement> items = new ArrayList<>();

  public void submitList(List<Announcement> announcements) {
    items.clear();
    if (announcements != null) {
      items.addAll(announcements);
    }
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public AnnouncementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
      .inflate(R.layout.item_announcement, parent, false);
    return new AnnouncementViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull AnnouncementViewHolder holder, int position) {
    Announcement ann = items.get(position);
    holder.title.setText(ann.getTitle());
    holder.message.setText(ann.getMessage());
    holder.date.setText(formatDate(ann.getDate()));
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

  static class AnnouncementViewHolder extends RecyclerView.ViewHolder {
    final TextView title;
    final TextView message;
    final TextView date;

    AnnouncementViewHolder(@NonNull View itemView) {
      super(itemView);
      title = itemView.findViewById(R.id.announcement_title);
      message = itemView.findViewById(R.id.announcement_message);
      date = itemView.findViewById(R.id.announcement_date);
    }
  }
}
