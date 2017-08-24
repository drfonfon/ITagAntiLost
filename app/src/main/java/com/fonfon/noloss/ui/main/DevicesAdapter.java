package com.fonfon.noloss.ui.main;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fonfon.noloss.R;
import com.fonfon.noloss.lib.Device;
import com.jakewharton.rxbinding2.support.v7.widget.RxToolbar;
import com.jakewharton.rxbinding2.view.RxView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

final class DevicesAdapter extends RecyclerView.Adapter<DevicesAdapter.Holder> {

  private final List<Device> devices = new ArrayList<>();
  private final DeviceAdapterListener listener;

  DevicesAdapter(DeviceAdapterListener listener) {
    this.listener = listener;
  }

  @Override
  public DevicesAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new Holder(
        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device, parent, false)
    );
  }

  @Override
  public void onBindViewHolder(final DevicesAdapter.Holder holder, int position) {
    final Device device = devices.get(position);
    holder.toolbar.setTitle(device.getName());
    holder.toolbar.setTitleTextColor(device.isConnected() ? Color.BLACK : Color.WHITE);

    holder.toolbar.setSubtitle(device.getAddress());
    holder.toolbar.setSubtitleTextColor(device.isConnected() ? Color.BLACK : Color.WHITE);

    int red = ContextCompat.getColor(holder.toolbar.getContext(), R.color.mojo);
    int green = ContextCompat.getColor(holder.toolbar.getContext(), R.color.fern);
    holder.toolbar.setBackgroundColor(device.isConnected() ? green : red);

    Drawable moreIcon = ContextCompat.getDrawable(holder.toolbar.getContext(), R.drawable.ic_more);
    DrawableCompat.setTint(moreIcon, device.isConnected() ? Color.BLACK : Color.WHITE);
    holder.toolbar.setOverflowIcon(moreIcon);

    holder.viewStatus.setEnabled(device.isConnected());
    holder.fabAlert.setImageResource(device.isAlerted() ? R.drawable.ic_volume_off : R.drawable.ic_volume_up);

    String connected = holder.textStatus.getContext().getString(R.string.status_connected);
    String disconnected = holder.textStatus.getContext().getString(R.string.status_disconnected);

    holder.textStatus.setText(device.isConnected() ? connected : disconnected);
  }

  @Override
  public int getItemCount() {
    return devices.size();
  }

  void setDevices(@NonNull List<Device> devices) {
    this.devices.clear();
    this.devices.addAll(devices);
    notifyDataSetChanged();
  }

  class Holder extends RecyclerView.ViewHolder {

    @BindView(R.id.imageDevice)
    ImageView image;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.viewStatus)
    TextView viewStatus;
    @BindView(R.id.textStatus)
    TextView textStatus;
    @BindView(R.id.fabAlert)
    FloatingActionButton fabAlert;

    Holder(View view) {
      super(view);
      ButterKnife.bind(this, view);

      toolbar.inflateMenu(R.menu.device_detail);
      RxView.clicks(fabAlert).subscribe(o -> listener.onAlert(devices.get(getAdapterPosition())));
      RxToolbar.itemClicks(toolbar).subscribe(menuItem -> {
        switch (menuItem.getItemId()) {
          case R.id.menu_delete:
            listener.onDelete(devices.get(getAdapterPosition()));
            break;
          case R.id.menu_rename:
            listener.onRename(devices.get(getAdapterPosition()));
            break;
          case R.id.menu_image:
            listener.onEditImage(devices.get(getAdapterPosition()));
        }
      });
    }
  }

  interface DeviceAdapterListener {
    void onRename(Device device);

    void onEditImage(Device device);

    void onDelete(Device device);

    void onAlert(Device device);
  }
}
