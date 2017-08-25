package com.fonfon.noloss.presenter;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.ParcelUuid;
import android.widget.Toast;

import com.fonfon.noloss.BleService;
import com.fonfon.noloss.R;
import com.fonfon.noloss.db.DbHelper;
import com.fonfon.noloss.db.DeviceDB;
import com.fonfon.noloss.viewstate.NewDevicesViewState;
import com.fonfon.noloss.ui.newdevice.NewDeviceView;
import com.hannesdorfmann.mosby3.mvi.MviBasePresenter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.PublishSubject;
import nl.nl2312.rxcupboard2.RxCupboard;

public final class NewDevicePresenter extends MviBasePresenter<NewDeviceView, NewDevicesViewState> {

  private final static int REQUEST_ENABLE_BT = 451;

  private Activity activity;
  private BluetoothAdapter bluetoothAdapter;

  private PublishSubject<NewDevicesViewState> viewStatePublisher = PublishSubject.create();
  private Location currentLocation;
  private final List<String> currentAddresses = new ArrayList<>();

  private final ScanCallback scanCallback = new ScanCallback() {
    @Override
    public void onScanResult(int callbackType, ScanResult result) {
      super.onScanResult(callbackType, result);
      if (result.getScanRecord() != null) {
        List<ParcelUuid> uuids = result.getScanRecord().getServiceUuids();
        if (uuids != null) {
          for (ParcelUuid uuid : uuids) {
            if (uuid.getUuid().equals(BleService.FIND_ME_SERVICE)) {
              if (!currentAddresses.contains(result.getDevice().getAddress()))
                viewStatePublisher.onNext(
                    new NewDevicesViewState.NewDeviceState(
                        result.getDevice().getAddress(),
                        result.getScanRecord().getDeviceName())
                );
              break;
            }
          }
        }
      }
    }
  };

  public NewDevicePresenter(Activity activity) {
    this.activity = activity;

    if (!activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
      Toast.makeText(activity, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
      activity.finish();
    }

    BluetoothManager bluetoothManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
    bluetoothAdapter = bluetoothManager.getAdapter();
  }

  @Override
  protected void bindIntents() {
    intent(NewDeviceView::onLifecycleIntent)
        .subscribe(activityEvent -> {
          switch (activityEvent) {
            case RESUME:
              resume();
              break;
            case PAUSE:
              pause();
              break;
          }
        });

    intent(NewDeviceView::onRefreshIntent).subscribe(o -> refresh());

    intent(NewDeviceView::onDeviceIntent).subscribe(pair -> {
      String address = pair.first;
      String name = pair.second.trim();
      RxCupboard.withDefault(DbHelper.getConnection(activity)).put(
          new DeviceDB(address, name, "img", currentLocation)
      ).subscribe(
          device -> activity.finish(),
          throwable -> Toast.makeText(activity, R.string.add_device_error, Toast.LENGTH_SHORT).show());
    });

    intent(NewDeviceView::onNewLocation)
        .subscribe(location -> currentLocation = location);

    subscribeViewState(viewStatePublisher.observeOn(AndroidSchedulers.mainThread()), NewDeviceView::render);
  }

  private void resume() {
    if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
      activity.startActivityForResult(
          new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
          REQUEST_ENABLE_BT
      );
    } else {
      refresh();
    }
  }

  private void pause() {
    if (bluetoothAdapter != null) {
      BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();
      if (scanner != null && scanCallback != null) {
        scanner.stopScan(scanCallback);
      }
    }
  }

  private void refresh() {
    Single
        .just(new NewDevicesViewState.LoadingState(true))
        .flatMap(state -> RxCupboard
            .withDefault(DbHelper.getConnection(activity))
            .query(DeviceDB.class)
            .map(DeviceDB::getAddress)
            .toList()
        )
        .doOnSuccess(list -> {
          currentAddresses.addAll(list);
          viewStatePublisher.onNext(new NewDevicesViewState.LoadingState(true));
          bluetoothAdapter.getBluetoothLeScanner().startScan(scanCallback);
        })
        .flatMap(state -> Single.timer(8, TimeUnit.SECONDS))
        .doOnSuccess(aLong -> {
          bluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
          viewStatePublisher.onNext(new NewDevicesViewState.LoadingState(false));
        })
        .subscribe()
        .dispose();
  }
}
