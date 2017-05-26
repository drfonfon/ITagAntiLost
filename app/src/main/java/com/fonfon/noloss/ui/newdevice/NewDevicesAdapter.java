package com.fonfon.noloss.ui.newdevice;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fonfon.noloss.databinding.ItemNewDeviceBinding;

import java.util.HashMap;
import java.util.Set;

final class NewDevicesAdapter extends RecyclerView.Adapter<NewDevicesAdapter.Holder> {

    private final HashMap<String, String> devices = new HashMap<>();
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
    public void onBindViewHolder(Holder holder, int position) {
        Set<String> keySet = devices.keySet();
        String[] addresses = keySet.toArray(new String[keySet.size()]);
        holder.binding.setName(devices.get(addresses[position]));
        holder.binding.setAddress(addresses[position]);
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    void add(String address, String name) {
        devices.put(address, name);
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
            listener.onDevice(binding.getAddress(), binding.getName());
        }
    }

    interface Listener {
        void onDevice(String address, String name);
    }
}

