package com.fonfon.noloss.ui.main;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.fonfon.noloss.R;
import com.fonfon.noloss.databinding.ItemDeviceBinding;
import com.fonfon.noloss.lib.Device;
import com.fonfon.noloss.lib.StringBitmapConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

final class DevicesAdapter extends RecyclerView.Adapter<DevicesAdapter.Holder> {

    private final Listener listener;
    private List<Device> adapterData = new ArrayList<>();
    private List<String> addresses = new ArrayList<>();
    private boolean isFirstLoad = true;

    DevicesAdapter(Listener listener) {
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
    public void onBindViewHolder(final Holder holder, int position) {
        Device device = adapterData.get(position);
        holder.binding.setDevice(device);
        final String image = device.getImage();
        if (image != null) {
            Bitmap bitmap = StringBitmapConverter.stringToBitMap(image);
            if (bitmap != null) {
                holder.binding.deviceImage.setImageBitmap(bitmap);
            }
        } else {
            holder.binding.deviceImage.setImageResource(R.mipmap.ic_launcher);
        }
        holder.itemView.setTag(adapterData.get(position).getAddress());

        if (isFirstLoad) {
            final Animation animShake = AnimationUtils.loadAnimation(holder.binding.getRoot().getContext(), R.anim.shake);
            holder.binding.getRoot().startAnimation(animShake);
        }
    }

    @Override
    public int getItemCount() {
        return adapterData.size();
    }

    void addDevices(@NonNull Collection<Device> devices) {
        adapterData.clear();
        adapterData.addAll(devices);
        addresses.clear();
        for (Device device : adapterData) {
            addresses.add(device.getAddress());
        }
        notifyDataSetChanged();
    }

    void deviceConnected(String address) {
        isFirstLoad = false;
        int index = addresses.indexOf(address);
        if (index != -1) {
            adapterData.get(index).setConnected(true);
            notifyItemChanged(index);
        }
    }

    void deviceDisconnected(String address) {
        isFirstLoad = false;
        int index = addresses.indexOf(address);
        if (index != -1) {
            adapterData.get(index).setConnected(false);
            notifyItemChanged(index);
        }
    }

    String deviceDeleted(int index) {
        final String address = adapterData.get(index).getAddress();
        isFirstLoad = false;
        adapterData.remove(index);
        addresses.remove(index);
        notifyItemRemoved(index);
        return address;
    }

    String deviceAlerted(int index) {
        isFirstLoad = false;
        Device device = adapterData.get(index);
        device.setAlarmed(!device.isAlarmed());
        notifyItemChanged(index);
        return device.getAddress();
    }

    boolean getDeviceAlertStatus(int index) {
        return adapterData.get(index).isAlarmed();
    }

    class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ItemDeviceBinding binding;

        Holder(ItemDeviceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.binding.getRoot().setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onDeviceClick(adapterData.get(getAdapterPosition()));
        }
    }

    interface Listener {
        void onDeviceClick(Device device);
    }
}
