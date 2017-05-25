package com.fonfon.noloss.ui.newdevice;

import android.bluetooth.le.ScanResult;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fonfon.noloss.databinding.ItemNewDeviceBinding;

import java.util.ArrayList;
import java.util.HashMap;

final class NewDevicesAdapter extends RecyclerView.Adapter<NewDevicesAdapter.Holder> {

    private HashMap<String, ScanResult> devices = new HashMap<>();

    private final NewDevicesAdapter.Listener listener;

    NewDevicesAdapter(
            NewDevicesAdapter.Listener listener
    ) {
        this.listener = listener;
    }

    @Override
    public NewDevicesAdapter.Holder onCreateViewHolder(
            ViewGroup parent,
            int viewType
    ) {
        return new NewDevicesAdapter.Holder(
                ItemNewDeviceBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent, false
                )
        );
    }

    @Override
    public void onBindViewHolder(
            Holder holder,
            int position
    ) {
        ScanResult result = new ArrayList<>(devices.values()).get(position);
        holder.binding.setName(result.getScanRecord().getDeviceName());
        holder.binding.setAddress(result.getDevice().getAddress());
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    void add(ScanResult scanResult) {
        devices.put(scanResult.getDevice().getAddress(), scanResult);
        notifyDataSetChanged();
    }

    void clear() {
        devices.clear();
        notifyDataSetChanged();
    }

    class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ItemNewDeviceBinding binding;

        Holder(ItemNewDeviceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onDevice(new ArrayList<>(devices.values()).get(getAdapterPosition()));
        }
    }

    interface Listener {
        void onDevice(ScanResult result);
    }
}

