package com.example.billz;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PrinterAdapter extends RecyclerView.Adapter<PrinterAdapter.ViewHolder> {

    private List<Printer> printers;

    public PrinterAdapter(List<Printer> printers) {
        this.printers = printers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_printer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Printer printer = printers.get(position);
        holder.textName.setText(printer.getName());
        
        StringBuilder details = new StringBuilder();
        if (printer.getConnectionType() != null) details.append(printer.getConnectionType().toLowerCase()).append(" • ");
        if (printer.getModel() != null) details.append(printer.getModel()).append(" • ");
        if (printer.getPaperSize() != null) details.append(printer.getPaperSize()).append(" • ");
        if (printer.getPrintType() != null) details.append(printer.getPrintType());
        
        String detailsStr = details.toString();
        if (detailsStr.endsWith(" • ")) detailsStr = detailsStr.substring(0, detailsStr.length() - 3);
        holder.textDetails.setText(detailsStr);

        if (printer.getIconResId() != 0) {
            holder.imgIcon.setImageResource(printer.getIconResId());
        }

        holder.textAuto.setVisibility(printer.isAuto() ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return printers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgIcon;
        TextView textName, textDetails, textAuto;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.imgPrinterIcon);
            textName = itemView.findViewById(R.id.textPrinterName);
            textDetails = itemView.findViewById(R.id.textPrinterDetails);
            textAuto = itemView.findViewById(R.id.textAutoLabel);
        }
    }
}
