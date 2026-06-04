package com.example.billz;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class OtherSelectionAdapter extends RecyclerView.Adapter<OtherSelectionAdapter.ViewHolder> {

    private final List<OtherFee> others;
    private final OnOtherClickListener listener;
    private int selectedId = -1;

    public interface OnOtherClickListener {
        void onOtherClick(OtherFee other);
    }

    public OtherSelectionAdapter(List<OtherFee> others, OnOtherClickListener listener) {
        this.others = others;
        this.listener = listener;
    }

    public void setSelectedId(int id) {
        this.selectedId = id;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_other_selection_tile, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OtherFee other = others.get(position);
        holder.textName.setText(other.getName());
        String symbol = other.isPercentage() ? "%" : "₹";
        holder.textValue.setText(String.format(Locale.getDefault(), "%s(%s%.0f)", other.getName(), symbol, other.getValue()));

        // All blue as per reference
        holder.layoutContent.setBackgroundColor(0xFF3F51B5);
        holder.textName.setTextColor(Color.WHITE);
        holder.textValue.setTextColor(0xCCFFFFFF);

        if (other.getId() == selectedId) {
            holder.layoutContent.setBackgroundColor(0xFF2563EB); // Brighter blue for selected
        }

        holder.itemView.setOnClickListener(v -> listener.onOtherClick(other));
    }

    @Override
    public int getItemCount() {
        return others.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textValue;
        LinearLayout layoutContent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textOtherName);
            textValue = itemView.findViewById(R.id.textOtherValue);
            layoutContent = itemView.findViewById(R.id.layoutOtherContent);
        }
    }
}