package com.fonfon.noloss.ui.main;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fonfon.noloss.R;
import com.fonfon.noloss.lib.Device;
import com.jakewharton.rxbinding2.view.RxView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

final class DevicesAdapter extends RecyclerView.Adapter<DevicesAdapter.Holder> {

  private final ArrayList<Device> devices = new ArrayList<>();
  private final DeviceAdapterListener listener;

  private final int red;
  private final int green;
  private final String connected;
  private final String disconnected;

  DevicesAdapter(Context context, DeviceAdapterListener listener) {
    this.listener = listener;
    red = ContextCompat.getColor(context, R.color.mojo);
    green = ContextCompat.getColor(context, R.color.fern);

    connected = context.getString(R.string.status_connected);
    disconnected = context.getString(R.string.status_disconnected);
  }

  @Override
  public DevicesAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new Holder(
        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device, parent, false)
    );
  }

  @Override
  public void onBindViewHolder(final DevicesAdapter.Holder holder, int position) {
    Device device = devices.get(position);

    holder.image.setImageBitmap(Device.getBitmapImage(device.getImage(), holder.image.getContext().getResources()));

    int tintColor = device.isConnected() ? Color.BLACK : Color.WHITE;
    holder.toolbar.setTitle(device.getName());
    holder.toolbar.setTitleTextColor(tintColor);

    holder.toolbar.setSubtitle(device.getAddress());
    holder.toolbar.setSubtitleTextColor(tintColor);

    holder.toolbar.setBackgroundColor(device.isConnected() ? green : red);

    Drawable moreIcon = ContextCompat.getDrawable(holder.toolbar.getContext(), R.drawable.ic_more);
    DrawableCompat.setTint(moreIcon, tintColor);
    holder.toolbar.setOverflowIcon(moreIcon);

    holder.viewStatus.setEnabled(device.isConnected());
    holder.fabAlert.setImageResource(device.isAlerted() ? R.drawable.ic_volume_off : R.drawable.ic_volume_up);
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
      fabAlert.setOnClickListener(v -> listener.onAlert(devices.get(getAdapterPosition())));

      toolbar.setOnMenuItemClickListener(item -> {
        switch (item.getItemId()) {
          case R.id.menu_delete:
            listener.onDelete(devices.get(getAdapterPosition()));
            break;
          case R.id.menu_rename:
            listener.onRename(devices.get(getAdapterPosition()));
            break;
          case R.id.menu_image:
            listener.onEditImage(devices.get(getAdapterPosition()));
        }
        return false;
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
