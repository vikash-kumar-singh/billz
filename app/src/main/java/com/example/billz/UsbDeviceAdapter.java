package com.example.billz;

import android.hardware.usb.UsbDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class UsbDeviceAdapter extends RecyclerView.Adapter<UsbDeviceAdapter.ViewHolder> {

    private List<UsbDevice> devices;
    private OnDeviceClickListener listener;

    public interface OnDeviceClickListener {
        void onDeviceClick(UsbDevice device);
    }

    public UsbDeviceAdapter(List<UsbDevice> devices, OnDeviceClickListener listener) {
        this.devices = devices;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_usb_device, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UsbDevice device = devices.get(position);
        
        String manufacturer = "";
        String product = "";
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            manufacturer = device.getManufacturerName();
            product = device.getProductName();
        }

        String displayName = (manufacturer != null ? manufacturer + " " : "") + (product != null ? product : "USB Device");
        if (displayName.trim().isEmpty()) displayName = "USB Device " + device.getDeviceId();
        
        holder.textName.setText(displayName);
        holder.textDetails.setText("VID: " + device.getVendorId() + " PID: " + device.getProductId());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onDeviceClick(device);
        });
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textDetails;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textUsbDeviceName);
            textDetails = itemView.findViewById(R.id.textUsbDeviceDetails);
        }
    }
}
