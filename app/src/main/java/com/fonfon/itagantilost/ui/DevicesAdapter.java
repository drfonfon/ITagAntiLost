package com.fonfon.itagantilost.ui;

import android.content.res.ColorStateList;
import android.databinding.BindingAdapter;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fonfon.itagantilost.App;
import com.fonfon.itagantilost.R;
import com.fonfon.itagantilost.databinding.LayoutDeviceBinding;

import java.util.ArrayList;

public class DevicesAdapter extends RecyclerView.Adapter<DevicesAdapter.Holder> {

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
        holder.binding.setDevice(new ArrayList<>(App.getDevices().values()).get(position));
    }

    @Override
    public int getItemCount() {
        return App.getDevices().size();
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
            listener.onDevice(new ArrayList<>(App.getDevices().values()).get(getAdapterPosition()).address);
            notifyDataSetChanged();
        }
    }

    public interface Listener {
        void onDevice(String result);
    }

    @BindingAdapter({"isAlarm"})
    public static void setSrcCompat(FloatingActionButton view, boolean isAlarm) {
        int color = isAlarm ? ContextCompat.getColor(view.getContext(), R.color.mojo) : Color.WHITE;
        view.setBackgroundTintList(ColorStateList.valueOf(color));
    }
}
