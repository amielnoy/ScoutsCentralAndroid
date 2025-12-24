package com.scoutscentral.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.scoutscentral.app.R;
import com.scoutscentral.app.data.Scout;

import java.util.ArrayList;
import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {
  public interface MemberActionListener {
    void onEdit(Scout scout);
    void onDelete(Scout scout);
  }

  private final List<Scout> items = new ArrayList<>();
  private final MemberActionListener listener;

  public MemberAdapter(MemberActionListener listener) {
    this.listener = listener;
  }

  public void submitList(List<Scout> scouts) {
    items.clear();
    if (scouts != null) {
      items.addAll(scouts);
    }
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
      .inflate(R.layout.item_member, parent, false);
    return new MemberViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
    Scout scout = items.get(position);
    holder.name.setText(scout.getName());
    holder.level.setText(scout.getLevel().getLabel());
    holder.contact.setText(scout.getContact());
    Glide.with(holder.itemView)
      .load(scout.getAvatarUrl())
      .placeholder(R.drawable.avatar_placeholder)
      .error(R.drawable.avatar_placeholder)
      .into(holder.avatar);
    holder.menu.setOnClickListener(v -> showMenu(v, scout));
  }

  private void showMenu(View anchor, Scout scout) {
    PopupMenu menu = new PopupMenu(anchor.getContext(), anchor);
    menu.getMenuInflater().inflate(R.menu.member_item_menu, menu.getMenu());
    menu.setOnMenuItemClickListener(item -> handleMenu(item, scout));
    menu.show();
  }

  private boolean handleMenu(MenuItem item, Scout scout) {
    int id = item.getItemId();
    if (id == R.id.action_edit) {
      listener.onEdit(scout);
      return true;
    }
    if (id == R.id.action_delete) {
      listener.onDelete(scout);
      return true;
    }
    return false;
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

  static class MemberViewHolder extends RecyclerView.ViewHolder {
    final TextView name;
    final TextView level;
    final TextView contact;
    final ImageButton menu;
    final ImageView avatar;

    MemberViewHolder(@NonNull View itemView) {
      super(itemView);
      avatar = itemView.findViewById(R.id.member_avatar);
      name = itemView.findViewById(R.id.member_name);
      level = itemView.findViewById(R.id.member_level);
      contact = itemView.findViewById(R.id.member_contact);
      menu = itemView.findViewById(R.id.member_menu);
    }
  }
}
