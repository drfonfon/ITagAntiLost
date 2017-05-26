package com.fonfon.noloss.ui.main;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fonfon.noloss.databinding.ItemDeviceBinding;
import com.fonfon.noloss.lib.Device;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

final class DevicesAdapter extends RecyclerView.Adapter<DevicesAdapter.Holder> {

    private final Listener listener;
    private final List<Device> devices = new ArrayList<>();

    DevicesAdapter(Listener listener) {
        this.listener = listener;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(
                ItemDeviceBinding.inflate(
                        LayoutInflater.from(parent.getContext()), parent, false
                )
        );
    }

    @Override
    public void onBindViewHolder(final Holder holder, int position) {
        holder.binding.setDevice(devices.get(position));
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    void addDevices(@NonNull Collection<Device> devices) {
        this.devices.clear();
        this.devices.addAll(devices);
        notifyDataSetChanged();
    }

    void deviceConnected(String address) {
        for (int i = 0; i < devices.size(); i++) {
            if (devices.get(i).getAddress().equals(address)) {
                devices.get(i).setConnected(true);
                notifyItemChanged(i);
                break;
            }
        }
    }

    void deviceDisconnected(String address) {
        for (int i = 0; i < devices.size(); i++) {
            if (devices.get(i).getAddress().equals(address)) {
                devices.get(i).setConnected(false);
                notifyItemChanged(i);
                break;
            }
        }
    }

    String getDeviceAddressFrom(int index) {
        return devices.get(index).getAddress();
    }

    void deviceDeleted(int index) {
        devices.remove(index);
        notifyItemRemoved(index);
    }

    class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ItemDeviceBinding binding;

        Holder(final ItemDeviceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.binding.getRoot().setOnClickListener(this);
            this.binding.setAlarm(false);
            this.binding.fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    binding.setAlarm(!binding.getAlarm());
                    listener.onDeviceAlerted(binding.getDevice().getAddress(), binding.getAlarm());
                }
            });
        }

        @Override
        public void onClick(View v) {
            listener.onDeviceClick(devices.get(getAdapterPosition()).getAddress());
        }
    }

    interface Listener {
        void onDeviceClick(String address);
        void onDeviceAlerted(String address, boolean isAlerted);
    }
}
