package com.fonfon.noloss.lib;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;

public final class SaveImageService extends IntentService {

  public static final String URI = "imageURI";

  public static void start(Activity activity, Uri selectedImage, String address) {
    activity.startService(new Intent(activity, SaveImageService.class)
        .putExtra(SaveImageService.URI, selectedImage)
        .putExtra(Device.ADDRESS, address)
    );
  }

  public SaveImageService() {
    super(SaveImageService.class.getName());
  }

  @Override
  protected void onHandleIntent(@Nullable Intent intent) {
    if (intent != null) {
      final Uri imageUri = intent.getParcelableExtra(URI);
      final String address = intent.getStringExtra(Device.ADDRESS);

      if (imageUri != null && address != null) {
//        Realm.getDefaultInstance().executeTransactionAsync(new Realm.Transaction() {
//          @Override
//          public void execute(Realm realm) {
//            DeviceDB device = realm.where(DeviceDB.class).equalTo(DeviceDB.ADDRESS, address).findFirst();
//            if (device != null) {
//              try {
//                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
//                if (bitmap != null) {
//                  String image = BitmapUtils.bitmapToString(bitmap);
//                  device.setImage(image);
//                } else {
//                  Toast.makeText(getApplicationContext(), R.string.save_image_error, Toast.LENGTH_SHORT).show();
//                }
//              } catch (IOException e) {
//                e.printStackTrace();
//                Toast.makeText(getApplicationContext(), R.string.save_image_error, Toast.LENGTH_SHORT).show();
//              }
//            }
//          }
//        });

      }
    }
  }

}
