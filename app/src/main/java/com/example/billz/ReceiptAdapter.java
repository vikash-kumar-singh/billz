package com.example.billz;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReceiptAdapter extends RecyclerView.Adapter<ReceiptAdapter.ViewHolder> {

    private final List<Receipt> receipts;
    private final List<Receipt> receiptsFull;
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd MMM yyyy - h:mm a", Locale.getDefault());

    public ReceiptAdapter(List<Receipt> receipts) {
        this.receipts = receipts;
        this.receiptsFull = new java.util.ArrayList<>(receipts);
    }

    public void filter(String text) {
        receipts.clear();
        if (text == null || text.isEmpty()) {
            receipts.addAll(receiptsFull);
        } else {
            text = text.toLowerCase();
            for (Receipt receipt : receiptsFull) {
                if ((receipt.getCustomerName() != null && receipt.getCustomerName().toLowerCase().contains(text)) || 
                    (receipt.getReceiptNo() != null && receipt.getReceiptNo().toLowerCase().contains(text)) ||
                    (receipt.getPaymentMode() != null && receipt.getPaymentMode().toLowerCase().contains(text))) {
                    receipts.add(receipt);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_receipt, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Receipt receipt = receipts.get(position);
        holder.textReceiptNo.setText(receipt.getReceiptNo());
        
        String customer = receipt.getCustomerName() != null && !receipt.getCustomerName().isEmpty() 
                ? receipt.getCustomerName() + " " : "";
        
        if (receipt.isPayment()) {
            holder.textReceiptDesc.setText("Payment via " + receipt.getPaymentMode());
            holder.textReceiptAmount.setText(String.format(Locale.getDefault(), "₹%,.0f", receipt.getTotalAmount()));
            holder.textReceiptAmount.setTextColor(0xFF10B981); // Green for payment received
            holder.imageReceiptIcon.setImageResource(R.drawable.ic_cash);
        } else if (receipt.isReturned()) {
            holder.textReceiptDesc.setText(customer + "(RETURNED)");
            holder.textReceiptAmount.setText(String.format(Locale.getDefault(), "-₹%,.0f", receipt.getTotalAmount()));
            holder.textReceiptAmount.setTextColor(0xFFEF4444); // Red
            holder.imageReceiptIcon.setImageResource(R.drawable.ic_refresh); 
        } else if ("Credit".equalsIgnoreCase(receipt.getPaymentMode())) {
            holder.textReceiptDesc.setText("Due");
            holder.textReceiptDesc.setTextColor(0xFFEF4444); // Red
            holder.textReceiptAmount.setText(String.format(Locale.getDefault(), "₹-%,.0f", receipt.getTotalAmount()));
            holder.textReceiptAmount.setTextColor(0xFFEF4444); // Red
            holder.imageReceiptIcon.setImageResource(R.drawable.ic_credit_calendar);
        } else {
            holder.textReceiptDesc.setText(customer + "by " + receipt.getPaymentMode());
            holder.textReceiptDesc.setTextColor(0xFF475569); // Default
            holder.textReceiptAmount.setText(String.format(Locale.getDefault(), "₹%,.0f", receipt.getTotalAmount()));
            holder.textReceiptAmount.setTextColor(0xFF2563EB); // Primary Blue
            holder.imageReceiptIcon.setImageResource(R.drawable.ic_credit_calendar);
        }
        
        String itemsCountText = receipt.getItemCount() > 0 ? receipt.getItemCount() + (receipt.getItemCount() == 1 ? " Item " : " Items ") : "";
        holder.textReceiptStats.setText(itemsCountText + getRelativeTime(receipt.getTimestamp()));

        View clickTarget = holder.layoutContent != null ? holder.layoutContent : holder.itemView;
        clickTarget.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ReceiptPreviewActivity.class);
            intent.putExtra("receipt_id", receipt.getId());
            v.getContext().startActivity(intent);
        });
    }

    private String getRelativeTime(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        if (diff < 60000) {
            return "just now";
        } else if (diff < 3600000) {
            long mins = diff / 60000;
            return mins + (mins == 1 ? " minute ago" : " minutes ago");
        } else if (diff < 86400000) {
            long hours = diff / 3600000;
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        } else {
            return dateTimeFormat.format(new Date(timestamp));
        }
    }

    @Override
    public int getItemCount() {
        return receipts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textReceiptNo, textReceiptDesc, textReceiptStats, textReceiptAmount;
        ImageView imageReceiptIcon;
        View layoutContent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textReceiptNo = itemView.findViewById(R.id.textReceiptNo);
            textReceiptDesc = itemView.findViewById(R.id.textReceiptDesc);
            textReceiptStats = itemView.findViewById(R.id.textReceiptStats);
            textReceiptAmount = itemView.findViewById(R.id.textReceiptAmount);
            imageReceiptIcon = itemView.findViewById(R.id.imageReceiptIcon);
            layoutContent = itemView.findViewById(R.id.layoutReceiptContent);
        }
    }
}
