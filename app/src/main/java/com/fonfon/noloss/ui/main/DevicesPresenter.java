package com.fonfon.noloss.ui.main;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;

import com.fonfon.noloss.BleService;
import com.fonfon.noloss.R;
import com.fonfon.noloss.db.DbHelper;
import com.fonfon.noloss.db.DeviceDB;
import com.fonfon.noloss.lib.Device;
import com.fonfon.noloss.lib.LocationChangeService;
import com.fonfon.noloss.ui.newdevice.NewDeviceActivity;
import com.hannesdorfmann.mosby3.mvi.MviBasePresenter;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.PublishSubject;
import nl.nl2312.rxcupboard2.RxCupboard;

final class DevicesPresenter extends MviBasePresenter<DevicesView, DevicesViewState> {

  private static int REQUEST_LOCATION = 1;
  private static String[] PERMISSIONS_LOCATION = new String[]{
      Manifest.permission.ACCESS_FINE_LOCATION,
      Manifest.permission.ACCESS_COARSE_LOCATION
  };

  private final Activity activity;
  private PublishSubject<DevicesViewState> viewStatePublisher = PublishSubject.create();
  private ArrayList<Device> currentDevices = new ArrayList<>();

  private final BroadcastReceiver receiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (intent != null) {
        String action = intent.getAction();
        String address = intent.getStringExtra(BleService.DEVICE_ADDRESS);
        if (action != null && address != null) {
          switch (action) {
            case BleService.DEVICE_CONNECTED:
              deviceConnect(address, true);
              viewStatePublisher.onNext(new DevicesViewState.DataState(currentDevices));
              break;
            case BleService.DEVICE_DISCONNECTED:
              deviceConnect(address, false);
              viewStatePublisher.onNext(new DevicesViewState.DataState(currentDevices));
              break;
            case LocationChangeService.LOCATION_CHANGED:
              viewStatePublisher.onNext(new DevicesViewState.DataState(currentDevices));
              break;
          }
        }
      }
    }
  };

  DevicesPresenter(Activity activity) {
    this.activity = activity;
  }

  @Override
  protected void bindIntents() {
    intent(DevicesView::newDeviceIntent)
        .subscribe(o -> activity.startActivity(NewDeviceActivity.getIntent(activity)));

    intent(DevicesView::onLifecycleIntent)
        .subscribe(activityEvent -> {
          switch (activityEvent) {
            case RESUME:
              resume();
              return;
            case PAUSE:
              pause();
          }
        });


    intent(DevicesView::alertDeviceIntent).subscribe(device -> {
      int index = currentDevices.indexOf(device);
      if (index > -1) {
        currentDevices.get(index).setAlerted(!currentDevices.get(index).isAlerted());
        BleService.alert(activity, currentDevices.get(index).getAddress(), currentDevices.get(index).isAlerted());
        viewStatePublisher.onNext(new DevicesViewState.DataState(currentDevices));
      }
    });

    intent(DevicesView::deleteDeviceIntent)
        .doOnNext(device -> {
          BleService.disconnect(activity, device.getAddress());
          currentDevices.remove(device);
        })
        .flatMapSingle(device ->
            RxCupboard
                .withDefault(DbHelper.getConnection(activity))
                .delete(DeviceDB.class, device.get_id())
        )
        .subscribe(aLong -> viewStatePublisher.onNext(new DevicesViewState.DataState(currentDevices)));

    intent(DevicesView::editImageDeviceIntent)
        .subscribe();

    intent(DevicesView::renameDeviceIntent)
        .doOnNext(device -> {
          for (Device curDevice : currentDevices) {
            if (curDevice.getAddress().equals(device.getAddress())) {
              curDevice.setName(device.getName());
              break;
            }
          }
        })
        .flatMapSingle(device ->
            RxCupboard
                .withDefault(DbHelper.getConnection(activity))
                .put(new DeviceDB(device))
        )
        .subscribe(aLong -> viewStatePublisher.onNext(new DevicesViewState.DataState(currentDevices)),
            Throwable::printStackTrace
        );

    subscribeViewState(viewStatePublisher.hide().observeOn(AndroidSchedulers.mainThread()), DevicesView::render);
  }

  private boolean checkPermission() {
    return ActivityCompat.checkSelfPermission(activity, PERMISSIONS_LOCATION[0]) == PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(activity, PERMISSIONS_LOCATION[1]) == PackageManager.PERMISSION_GRANTED;
  }

  private void requestLocationPermission() {
    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, PERMISSIONS_LOCATION[0])
        && ActivityCompat.shouldShowRequestPermissionRationale(activity, PERMISSIONS_LOCATION[1])) {
      new AlertDialog.Builder(activity)
          .setTitle(R.string.app_name)
          .setMessage(R.string.ble_permission)
          .setPositiveButton(android.R.string.ok, (dialog, which) -> {
            ActivityCompat.requestPermissions(activity, PERMISSIONS_LOCATION, REQUEST_LOCATION);
            dialog.dismiss();
          })
          .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss()).show();
    } else {
      ActivityCompat.requestPermissions(activity, PERMISSIONS_LOCATION, REQUEST_LOCATION);
    }
  }

  private void resume() {
    if (!checkPermission()) requestLocationPermission();
    IntentFilter intentFilter = new IntentFilter(BleService.DEVICE_CONNECTED);
    intentFilter.addAction(BleService.DEVICE_DISCONNECTED);
    intentFilter.addAction(LocationChangeService.LOCATION_CHANGED);
    activity.registerReceiver(receiver, intentFilter);
    loadData();
  }

  private void pause() {
    activity.unregisterReceiver(receiver);
    activity.startService(new Intent(activity, BleService.class));
  }

  private void loadData() {
    RxCupboard.withDefault(DbHelper.getConnection(activity))
        .query(DeviceDB.class)
        .doOnNext(device -> BleService.connect(activity, device.getAddress()))
        .map(Device::new)
        .toList()
        .doOnSuccess(devices -> {
          currentDevices.clear();
          currentDevices.addAll(devices);
        })
        .map(DevicesViewState.DataState::new)
        .cast(DevicesViewState.class)
        .subscribe(devicesViewState -> viewStatePublisher.onNext(devicesViewState))
        .dispose();
  }

  private void deviceConnect(String address, boolean connected) {
    for (int i = 0; i < currentDevices.size(); i++) {
      if (currentDevices.get(i).getAddress().equals(address)) {
        currentDevices.get(i).setConnected(connected);
        break;
      }
    }
  }

}
