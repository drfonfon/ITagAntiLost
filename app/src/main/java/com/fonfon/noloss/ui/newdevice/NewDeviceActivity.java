package com.fonfon.noloss.ui.newdevice;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.fonfon.noloss.BleService;
import com.fonfon.noloss.NewDevicesViewState;
import com.fonfon.noloss.R;
import com.fonfon.noloss.db.DbHelper;
import com.fonfon.noloss.db.DeviceDB;
import com.fonfon.noloss.ui.LocationActivity;
import com.google.android.gms.common.api.Result;
import com.hannesdorfmann.mosby3.mvi.MviPresenter;
import com.hannesdorfmann.mosby3.mvp.MvpView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import io.reactivex.subjects.PublishSubject;
import nl.nl2312.rxcupboard2.RxCupboard;

public final class NewDeviceActivity extends LocationActivity {

    public final static int REQUEST_ENABLE_BT = 451;

    RecyclerView recycler;
    SwipeRefreshLayout refresh;
    Toolbar toolbar;
    TextView empty;

    private final PublishSubject<Pair<String, String>> newDeviceSubject = PublishSubject.create();
    private final PublishSubject<Location> locationPublishSubject = PublishSubject.create();
    private NewDevicesAdapter adapter;

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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_device);

        recycler = findViewById(R.id.recycler);
        refresh = findViewById(R.id.refresh);
        toolbar = findViewById(R.id.toolbar);
        empty = findViewById(R.id.empty_text);

        adapter = new NewDevicesAdapter((address, name) -> newDeviceSubject.onNext(new Pair<>(address, name)));
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v -> finish());

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        bluetoothAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();

        new SwipeRefreshLayoutRefreshObservable(refresh).subscribe(o -> refresh());
        newDeviceSubject
                .flatMapSingle(pair -> {
                    String address = pair.first;
                    String name = pair.second.trim();
                    return RxCupboard.withDefault(DbHelper.getConnection(this)).put(
                            new DeviceDB(address, name, "img", currentLocation)
                    );
                })
                .subscribe(device -> finish(),
                        throwable -> Toast.makeText(this, R.string.add_device_error, Toast.LENGTH_SHORT).show()
                );

        locationPublishSubject.subscribe(location -> currentLocation = location);
        viewStatePublisher.subscribe(newDevicesViewState -> render(newDevicesViewState));
    }

    @Override
    public void onResume() {
        super.onResume();
        resume();
    }

    @NonNull
    @Override
    public MviPresenter createPresenter() {
        return new MviPresenter() {
            @Override
            public void attachView(@NonNull MvpView view) {

            }

            @Override
            public void detachView(boolean retainInstance) {

            }

            @Override
            public void detachView() {

            }

            @Override
            public void destroy() {

            }
        };
    }

    @Override
    protected void onPause() {
        pause();
        super.onPause();
    }

    @Override
    public void onLocationChanged(Location location) {
        super.onLocationChanged(location);
        locationPublishSubject.onNext(location);
    }

    public void render(@NonNull NewDevicesViewState state) {
        if (state instanceof NewDevicesViewState.LoadingState) {
            renderLoadingState((NewDevicesViewState.LoadingState) state);
        } else if (state instanceof NewDevicesViewState.NewDeviceState) {
            renderDataState((NewDevicesViewState.NewDeviceState) state);
        } else {
            throw new IllegalArgumentException("Don't know how to render viewState " + state);
        }
    }

    private void renderLoadingState(NewDevicesViewState.LoadingState state) {
        if (state.isLoading()) {
            adapter.clear();
        }
        refresh.setRefreshing(state.isLoading());
        empty.setVisibility(state.isLoading() || recycler.getAdapter().getItemCount() > 0 ? View.GONE : View.VISIBLE);
    }

    private void renderDataState(NewDevicesViewState.NewDeviceState state) {
        adapter.add(state.getAddress(), state.getName());
    }

    private void resume() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            startActivityForResult(
                    new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                    NewDeviceActivity.REQUEST_ENABLE_BT
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
                        .withDefault(DbHelper.getConnection(this))
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

    @Override
    public void onResult(@NonNull Result result) {

    }
}
