package com.example.billz;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StaffAdapter extends RecyclerView.Adapter<StaffAdapter.ViewHolder> {

    private final List<Staff> staffList;
    private OnStaffClickListener listener;

    public interface OnStaffClickListener {
        void onStaffClick(Staff staff);
        void onPermissionsClick(Staff staff);
        void onAttendanceClick(Staff staff);
        void onPaySlipClick(Staff staff);
    }

    public StaffAdapter(List<Staff> staffList, OnStaffClickListener listener) {
        this.staffList = staffList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_staff, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Staff staff = staffList.get(position);
        holder.textName.setText(staff.name);
        holder.textEmail.setText(staff.email != null && !staff.email.isEmpty() ? staff.email : "-");
        holder.textRoleBadge.setText(staff.role + " Permissions");
        
        if (staff.name != null && !staff.name.isEmpty()) {
            holder.textInitials.setText(String.valueOf(staff.name.charAt(0)).toUpperCase());
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onStaffClick(staff);
        });

        holder.textRoleBadge.setOnClickListener(v -> {
            if (listener != null) listener.onPermissionsClick(staff);
        });

        holder.btnAttendance.setOnClickListener(v -> {
            if (listener != null) listener.onAttendanceClick(staff);
        });

        holder.btnPaySlip.setOnClickListener(v -> {
            if (listener != null) listener.onPaySlipClick(staff);
        });

        // Set entry animation
        setAnimation(holder.itemView, position);
    }

    private int lastPosition = -1;
    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            android.view.animation.Animation animation = android.view.animation.AnimationUtils.loadAnimation(viewToAnimate.getContext(), android.R.anim.fade_in);
            animation.setDuration(400);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return staffList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textEmail, textRoleBadge, textInitials;
        View btnAttendance, btnPaySlip;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textStaffName);
            textEmail = itemView.findViewById(R.id.textStaffEmail);
            textRoleBadge = itemView.findViewById(R.id.textStaffRoleBadge);
            textInitials = itemView.findViewById(R.id.textStaffInitials);
            btnAttendance = itemView.findViewById(R.id.btnAttendance);
            btnPaySlip = itemView.findViewById(R.id.btnPaySlip);
        }
    }
}
