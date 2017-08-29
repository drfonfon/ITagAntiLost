package com.fonfon.noloss.lib;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

import io.reactivex.Observable;

public final class BitmapUtils {

  private static final int BITMAP_SCALE = 256;

  public static Observable<String> bitmapToString(Bitmap sourceBitmap) {
    return Observable.just(sourceBitmap)
        .map(source -> {
          int size = Math.min(source.getWidth(), source.getHeight());

          Bitmap squaredBitmap = Bitmap.createBitmap(
              source,
              (source.getWidth() - size) / 2,
              (source.getHeight() - size) / 2,
              size,
              size
          );

          Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

          Paint paint = new Paint();
          paint.setShader(new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
          paint.setAntiAlias(true);

          float r = size / 2f;
          new Canvas(bitmap).drawCircle(r, r, r, paint);

          squaredBitmap.recycle();
          Bitmap result = Bitmap.createScaledBitmap(bitmap, BITMAP_SCALE, BITMAP_SCALE, false);
          bitmap.recycle();
          source.recycle();
          return result;
        })
        .map(bitmap -> {
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
          String imageString = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
          bitmap.recycle();
          return imageString;
        });
  }

  static Bitmap stringToBitMap(String encodedString) {
    try {
      byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
     return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
    } catch (Exception ex) {
      return null;
    }
  }

}