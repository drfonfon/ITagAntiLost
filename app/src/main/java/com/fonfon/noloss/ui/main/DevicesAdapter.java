package com.fonfon.noloss.ui.main;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fonfon.noloss.App;
import com.fonfon.noloss.databinding.ItemDeviceBinding;

import java.util.ArrayList;

public class DevicesAdapter extends RecyclerView.Adapter<DevicesAdapter.Holder> {

    private final Listener listener;

    public DevicesAdapter(Listener listener) {
        this.listener = listener;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(
                ItemDeviceBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent, false
                )
        );
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        holder.binding.setDevice(new ArrayList<>(App.getDevices().values()).get(position));
    }

    @Override
    public int getItemCount() {
        return App.getDevices().size();
    }

    class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ItemDeviceBinding binding;

        Holder(ItemDeviceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.binding.buttonAction.setOnClickListener(this);
            this.binding.getRoot().setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return true;
                }
            });
        }

        @Override
        public void onClick(View v) {
            listener.onDevice(new ArrayList<>(App.getDevices().values()).get(getAdapterPosition()).getScanResult().getDevice().getAddress());
            notifyDataSetChanged();
        }
    }

    interface Listener {
        void onDevice(String result);
    }
}
