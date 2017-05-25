package com.fonfon.noloss.lib;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.widget.Toast;

import com.fonfon.noloss.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import io.realm.Realm;

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
                Realm.getDefaultInstance().executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        try {
                            final Bitmap bitmap = BitmapUtils.transform(
                                    MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri)
                            );
                            Device device = realm.where(Device.class).equalTo(Device.ADDRESS, address).findFirst();
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                            device.setImage(Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT));
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), R.string.save_image_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        }
    }

}
