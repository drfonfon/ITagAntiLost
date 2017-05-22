package com.fonfon.noloss.lib;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.io.IOException;

import io.realm.Realm;

public class SaveImageService extends IntentService {

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
            Uri imageUri = intent.getParcelableExtra(URI);
            String address = intent.getStringExtra(Device.ADDRESS);

            if (imageUri != null && address != null) {
                try {
                    final Bitmap bitmap = new CircleTransform().transform(MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri));
                    Realm realm = Realm.getDefaultInstance();
                    realm.beginTransaction();
                    Device device1 = realm.where(Device.class).equalTo(Device.ADDRESS, address).findFirst();
                    device1.setBitmapImage(bitmap);
                    realm.commitTransaction();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Не удалось сохранить изображение", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
