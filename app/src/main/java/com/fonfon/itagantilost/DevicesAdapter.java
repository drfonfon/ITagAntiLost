package com.fonfon.itagantilost;

import android.bluetooth.le.ScanResult;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fonfon.itagantilost.databinding.LayoutDeviceBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DevicesAdapter extends RecyclerView.Adapter<DevicesAdapter.Holder> {

    private HashMap<String, ScanResult> devices = new HashMap<>();

    private final Listener listener;

    public DevicesAdapter(Listener listener) {
        this.listener = listener;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(
                LayoutDeviceBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent, false
                )
        );
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        List<ScanResult> dev = new ArrayList<>(devices.values());
        ScanResult result = dev.get(position);
        String name = result.getScanRecord().getDeviceName() != null ? result.getScanRecord().getDeviceName() : result.getDevice().getAddress();
        holder.binding.setName(name);
        holder.binding.setMac(result.getDevice().getAddress());
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public void add(ScanResult result) {
        devices.put(result.getDevice().getAddress(), result);
        notifyDataSetChanged();
    }

    public void clear() {
        devices.clear();
        notifyDataSetChanged();
    }

    class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final LayoutDeviceBinding binding;

        public Holder(LayoutDeviceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.fabAction.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            List<ScanResult> dev = new ArrayList<>(devices.values());
            ScanResult result = dev.get(getAdapterPosition());
            listener.onDevice(result);
        }
    }

    public interface Listener {
        void onDevice(ScanResult result);
    }
}
