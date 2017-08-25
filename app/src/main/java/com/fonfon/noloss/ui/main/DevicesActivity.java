package com.fonfon.noloss.ui.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.ImageButton;

import com.fonfon.geohash.GeoHash;
import com.fonfon.noloss.lib.ActivityEvent;
import com.fonfon.noloss.R;
import com.fonfon.noloss.lib.BitmapUtils;
import com.fonfon.noloss.lib.Device;
import com.fonfon.noloss.presenter.DevicesPresenter;
import com.fonfon.noloss.viewstate.DevicesViewState;
import com.fonfon.noloss.ui.LocationActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jakewharton.rxbinding2.view.RxView;
import com.mlsdev.rximagepicker.RxImageConverters;
import com.mlsdev.rximagepicker.RxImagePicker;
import com.mlsdev.rximagepicker.Sources;

import java.util.List;
import java.util.Locale;

import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class DevicesActivity extends LocationActivity<DevicesView, DevicesPresenter> implements DevicesView, DevicesAdapter.DeviceAdapterListener {

  @BindView(R.id.recycler)
  RecyclerView recyclerView;

  @BindView(R.id.text_total)
  AppCompatTextView textTotal;

  @BindView(R.id.fab_new_device)
  FloatingActionButton fabNewDevice;

  @BindView(R.id.button_refresh)
  ImageButton buttonRefresh;

  @BindDimen(R.dimen.marker_size)
  int markerSize;

  private Unbinder unbinder;
  private DevicesAdapter adapter;
  private final PublishSubject<ActivityEvent> lifecycleSubject = PublishSubject.create();
  private final PublishSubject<Device> alertDeviceSubject = PublishSubject.create();
  private final PublishSubject<Device> updateDeviceSubject = PublishSubject.create();
  private final PublishSubject<Device> deleteDeviceSubject = PublishSubject.create();

  private List<Device> currentDevices;
  private GoogleMap googleMap;
  private boolean isCameraUpdated = false;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_devices);

    unbinder = ButterKnife.bind(this);

    adapter = new DevicesAdapter(this);

    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    recyclerView.setAdapter(adapter);
    ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
        .getMapAsync(googleMap -> {
          this.googleMap = googleMap;
          if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
              != PackageManager.PERMISSION_GRANTED &&
              ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                  != PackageManager.PERMISSION_GRANTED) {
            return;
          }
          googleMap.setMyLocationEnabled(true);
          if (currentDevices != null) {
            showMarkers();
          }
        });
  }

  @Override
  public void onResume() {
    super.onResume();
    lifecycleSubject.onNext(ActivityEvent.RESUME);
  }

  @Override
  protected void onPause() {
    lifecycleSubject.onNext(ActivityEvent.PAUSE);
    super.onPause();
  }

  @Override
  public Observable<ActivityEvent> onLifecycleIntent() {
    return lifecycleSubject.hide();
  }

  @Override
  public Observable<Object> newDeviceIntent() {
    return RxView.clicks(fabNewDevice).share();
  }

  @Override
  public Observable<Device> deleteDeviceIntent() {
    return deleteDeviceSubject.hide();
  }

  @Override
  public Observable<Device> alertDeviceIntent() {
    return alertDeviceSubject.hide();
  }

  @Override
  public Observable<Device> updateDeviceIntent() {
    return updateDeviceSubject.hide();
  }

  @Override
  public Observable<Object> refreshIntent() {
    return RxView.clicks(buttonRefresh).share();
  }

  @Override
  public void render(DevicesViewState state) {
    if (state instanceof DevicesViewState.DataState) {
      currentDevices = ((DevicesViewState.DataState) state).getData();
      textTotal.setText(String.format(Locale.getDefault(), getString(R.string.total_devices), currentDevices.size()));
      adapter.setDevices(currentDevices);
      if (googleMap != null) {
        googleMap.clear();
        showMarkers();
      }
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    unbinder.unbind();
  }

  @NonNull
  @Override
  public DevicesPresenter createPresenter() {
    return new DevicesPresenter(this);
  }

  @Override
  public void onRename(Device device) {
    @SuppressLint("InflateParams")
    EditText edit = (EditText) LayoutInflater.from(this).inflate(R.layout.layout_edit_name, null);
    new AlertDialog.Builder(this)
        .setTitle(R.string.change_name)
        .setView(edit)
        .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
          if (edit.getText().toString().trim().length() > 0) {
            device.setName(edit.getText().toString().trim());
            updateDeviceSubject.onNext(device);
          }
          dialog.dismiss();
        })
        .show();
  }

  @Override
  public void onEditImage(Device device) {
    RxImagePicker.with(this).requestImage(Sources.GALLERY)
        .observeOn(Schedulers.newThread())
        .flatMap(uri -> RxImageConverters.uriToBitmap(DevicesActivity.this, uri))
        .map(BitmapUtils::bitmapToString)
        .subscribe(s -> {
          device.setImage(s);
          updateDeviceSubject.onNext(device);
        });
  }

  @Override
  public void onDelete(Device device) {
    deleteDeviceSubject.onNext(device);
  }

  @Override
  public void onAlert(Device device) {
    alertDeviceSubject.onNext(device);
  }

  private void showMarkers() {
    Observable
        .fromIterable(currentDevices)
        .subscribe(device -> {
          Location center = GeoHash.fromString(device.getGeoHash()).getCenter();
          Marker marker = googleMap.addMarker(new MarkerOptions()
              .position(new LatLng(center.getLatitude(), center.getLongitude()))
              .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher))
              .flat(true)
              .title(device.getName())
              .snippet(device.getAddress())
          );
          Bitmap bitmap = Device.getBitmapImage(device.getImage(), getResources());
          Bitmap bmp = Bitmap.createScaledBitmap(bitmap, markerSize, markerSize, false);
          bitmap.recycle();
          marker.setIcon(BitmapDescriptorFactory.fromBitmap(bmp));
        });
  }

  @Override
  public void onLocationChanged(Location location) {
    super.onLocationChanged(location);
    if (googleMap != null) {
      if (!googleMap.isMyLocationEnabled()) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
          googleMap.setMyLocationEnabled(true);
        }
      }
      if (!isCameraUpdated) {
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), googleMap.getMaxZoomLevel() - 4));
        isCameraUpdated = true;
      }
    }
  }
}
