package com.example.billz;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReceiptAdapter extends RecyclerView.Adapter<ReceiptAdapter.ViewHolder> {

    private final List<Receipt> receipts;
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd MMM yyyy - h:mm a", Locale.getDefault());

    public ReceiptAdapter(List<Receipt> receipts) {
        this.receipts = receipts;
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
        holder.textReceiptDesc.setText(customer + "by " + receipt.getPaymentMode());
        
        String items = receipt.getItemCount() + (receipt.getItemCount() == 1 ? " Item " : " Items ");
        holder.textReceiptStats.setText(items + dateTimeFormat.format(new Date(receipt.getTimestamp())));
        
        holder.textReceiptAmount.setText(String.format(Locale.getDefault(), "₹%,.0f", receipt.getTotalAmount()));
    }

    @Override
    public int getItemCount() {
        return receipts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textReceiptNo, textReceiptDesc, textReceiptStats, textReceiptAmount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textReceiptNo = itemView.findViewById(R.id.textReceiptNo);
            textReceiptDesc = itemView.findViewById(R.id.textReceiptDesc);
            textReceiptStats = itemView.findViewById(R.id.textReceiptStats);
            textReceiptAmount = itemView.findViewById(R.id.textReceiptAmount);
        }
    }
}
