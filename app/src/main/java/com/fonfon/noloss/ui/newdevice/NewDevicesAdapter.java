package com.fonfon.noloss.ui.newdevice;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fonfon.noloss.R;

import java.util.HashMap;
import java.util.Set;

final class NewDevicesAdapter extends RecyclerView.Adapter<NewDevicesAdapter.Holder> {

  private final HashMap<String, String> devices = new HashMap<>();
  private final NewDevicesAdapter.Listener listener;

  NewDevicesAdapter(NewDevicesAdapter.Listener listener) {
    this.listener = listener;
  }

  @Override
  public NewDevicesAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new NewDevicesAdapter.Holder(
        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_new_device, parent, false)
    );
  }

  @Override
  public void onBindViewHolder(Holder holder, int position) {
    Set<String> keySet = devices.keySet();
    String[] addresses = keySet.toArray(new String[keySet.size()]);
    holder.textName.setText(devices.get(addresses[position]));
    holder.textAddress.setText(addresses[position]);
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

    final TextView textName;
    final TextView textAddress;

    Holder(View view) {
      super(view);
      textName = (TextView) view.findViewById(R.id.text_name);
      textAddress = (TextView) view.findViewById(R.id.text_address);
      view.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
      Set<String> keySet = devices.keySet();
      String[] addresses = keySet.toArray(new String[keySet.size()]);
      listener.onDevice(addresses[getAdapterPosition()], devices.get(addresses[getAdapterPosition()]));
    }
  }

  interface Listener {
    void onDevice(String address, String name);
  }
}

