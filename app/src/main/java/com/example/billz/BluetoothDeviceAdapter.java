package com.example.billz;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceAdapter.ViewHolder> {

    private List<BluetoothDevice> devices;
    private OnDeviceClickListener listener;

    public interface OnDeviceClickListener {
        void onDeviceClick(BluetoothDevice device);
    }

    public BluetoothDeviceAdapter(List<BluetoothDevice> devices, OnDeviceClickListener listener) {
        this.devices = devices;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bluetooth_device, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BluetoothDevice device = devices.get(position);
        try {
            String name = device.getName();
            holder.textName.setText(name != null ? name : "Unknown Device");
            holder.textAddress.setText(device.getAddress());
            
            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                holder.textStatus.setVisibility(View.VISIBLE);
                holder.textStatus.setText("Paired");
            } else {
                holder.textStatus.setVisibility(View.GONE);
            }
        } catch (SecurityException e) {
            holder.textName.setText("Permission required");
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onDeviceClick(device);
        });
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textAddress, textStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textDeviceName);
            textAddress = itemView.findViewById(R.id.textDeviceAddress);
            textStatus = itemView.findViewById(R.id.textPairStatus);
        }
    }
}
