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

public class ServiceSelectionAdapter extends RecyclerView.Adapter<ServiceSelectionAdapter.ViewHolder> {
    private final List<ServiceFee> fees;
    private final OnClickListener listener;
    private int selectedId = -1;

    public interface OnClickListener { void onClick(ServiceFee fee); }

    public ServiceSelectionAdapter(List<ServiceFee> fees, OnClickListener listener) {
        this.fees = fees;
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
        ServiceFee fee = fees.get(position);
        holder.textName.setText(fee.getName());
        String symbol = fee.isPercentage() ? "%" : "₹";
        holder.textValue.setText(String.format(Locale.getDefault(), "%s(%s%.0f)", fee.getName(), symbol, fee.getValue()));

        holder.layoutContent.setBackgroundColor(fee.getId() == selectedId ? 0xFF2563EB : 0xFF3F51B5);
        holder.textName.setTextColor(Color.WHITE);
        holder.textValue.setTextColor(0xCCFFFFFF);
        holder.itemView.setOnClickListener(v -> listener.onClick(fee));
    }

    @Override
    public int getItemCount() { return fees.size(); }

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