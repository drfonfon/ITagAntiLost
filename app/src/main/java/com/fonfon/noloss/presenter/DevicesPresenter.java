package com.fonfon.noloss.presenter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;

import com.fonfon.noloss.BleService;
import com.fonfon.noloss.DevicesViewState;
import com.fonfon.noloss.db.DbHelper;
import com.fonfon.noloss.db.DeviceDB;
import com.fonfon.noloss.lib.Device;
import com.fonfon.noloss.lib.LocationChangeService;
import com.fonfon.noloss.ui.main.DevicesView;
import com.fonfon.noloss.ui.newdevice.NewDeviceActivity;
import com.hannesdorfmann.mosby3.mvi.MviBasePresenter;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import nl.nl2312.rxcupboard2.RxCupboard;

public final class DevicesPresenter extends MviBasePresenter<DevicesView, DevicesViewState> {

  private final Activity activity;
  private PublishSubject<DevicesViewState> viewStatePublisher = PublishSubject.create();
  private ArrayList<Device> currentDevices = new ArrayList<>();

  private boolean receiverRegistered = false;
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
              break;
            case BleService.DEVICE_DISCONNECTED:
              deviceConnect(address, false);
              break;
            case LocationChangeService.LOCATION_CHANGED:
              deviceLocationChange(address, intent.getParcelableExtra(LocationChangeService.LOCATION));
              break;
          }
          updateDevices();
        }
      }
    }
  };

  public DevicesPresenter(Activity activity) {
    this.activity = activity;
  }

  @Override
  protected void bindIntents() {

    intent(DevicesView::onLifecycleIntent)
        .subscribe(isResumed -> {
          if (isResumed) {
            resume();
          } else {
            pause();
          }
        });


    intent(DevicesView::alertDeviceIntent).subscribe(device -> {
      int index = currentDevices.indexOf(device);
      if (index > -1) {
        currentDevices.get(index).setAlerted(!currentDevices.get(index).isAlerted());
        BleService.alert(activity, currentDevices.get(index).getAddress(), currentDevices.get(index).isAlerted());
        updateDevices();
      }
    });

    intent(DevicesView::deleteDeviceIntent)
        .doOnNext(device -> {
          BleService.disconnect(activity, device.getAddress());
          currentDevices.remove(device);
        })
        .flatMapSingle(device -> RxCupboard
            .withDefault(DbHelper.getConnection(activity))
            .delete(DeviceDB.class, device.get_id())
        )
        .subscribe(p -> updateDevices());

    intent(DevicesView::updateDeviceIntent)
        .doOnNext(device -> {
          for (Device curDevice : currentDevices) {
            if (curDevice.get_id().equals(device.get_id())) {
              curDevice.setName(device.getName());
              curDevice.setImage(device.getImage());
              break;
            }
          }
        })
        .flatMapSingle(device -> RxCupboard
            .withDefault(DbHelper.getConnection(activity))
            .put(new DeviceDB(device))
            .subscribeOn(Schedulers.newThread())
        )
        .subscribe(p -> updateDevices());

    intent(DevicesView::refreshIntent)
        .subscribe(o -> loadData());

    subscribeViewState(viewStatePublisher.hide().observeOn(AndroidSchedulers.mainThread()), DevicesView::render);
  }

  public void resume() {
    IntentFilter intentFilter = new IntentFilter(BleService.DEVICE_CONNECTED);
    intentFilter.addAction(BleService.DEVICE_DISCONNECTED);
    intentFilter.addAction(LocationChangeService.LOCATION_CHANGED);
    activity.registerReceiver(receiver, intentFilter);
    receiverRegistered = true;
    loadData();
  }

  public void pause() {
    if (receiverRegistered) {
      activity.unregisterReceiver(receiver);
      receiverRegistered = false;
    }
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

  private void updateDevices() {
    viewStatePublisher.onNext(new DevicesViewState.DataState(currentDevices));
  }

  private void deviceConnect(String address, boolean connected) {
    for (int i = 0; i < currentDevices.size(); i++) {
      if (currentDevices.get(i).getAddress().equals(address)) {
        currentDevices.get(i).setConnected(connected);
        break;
      }
    }
  }

  private void deviceLocationChange(String address, Location location) {
    for (int i = 0; i < currentDevices.size(); i++) {
      if (currentDevices.get(i).getAddress().equals(address)) {
        currentDevices.get(i).setLocation(location);
        break;
      }
    }
  }

}
