package com.fonfon.noloss.ui.detail;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fonfon.noloss.databinding.DialogDeviceBinding;
import com.fonfon.noloss.lib.Device;

public class DeviceDetailDialog extends DialogFragment {

    public static final String DEVICE = "device_data";

    public static DeviceDetailDialog getInstance(Device device) {
        DeviceDetailDialog fragment = new DeviceDetailDialog();
        Bundle bundle = new Bundle();
        bundle.putParcelable(DEVICE, device);
        fragment.setArguments(bundle);
        return fragment;
    }

    private DialogDeviceBinding binding;
    private DeviceDialogViewModel model;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            Device device = getArguments().getParcelable(DEVICE);
            if (device != null) {
                model = new DeviceDialogViewModel(getActivity(), device);
            } else {
                throw new NullPointerException("Device must be not null");
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        binding = DialogDeviceBinding.inflate(inflater, container, false);
        binding.setModel(model);
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding.unbind();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        model.onDismiss();
    }
}
