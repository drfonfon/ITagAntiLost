package com.fonfon.noloss.lib;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public final class BitmapUtils {

  private static final int BITMAP_SCALE = 256;

  private static Bitmap transform(Bitmap source) {
    int size = Math.min(source.getWidth(), source.getHeight());

    Bitmap squaredBitmap = Bitmap.createBitmap(
        source,
        (source.getWidth() - size) / 2,
        (source.getHeight() - size) / 2,
        size,
        size
    );
    if (squaredBitmap != source) source.recycle();

    Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

    Paint paint = new Paint();
    BitmapShader shader = new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
    paint.setShader(shader);
    paint.setAntiAlias(true);

    float r = size / 2f;
    new Canvas(bitmap).drawCircle(r, r, r, paint);

    squaredBitmap.recycle();
    Bitmap result = Bitmap.createScaledBitmap(bitmap, BITMAP_SCALE, BITMAP_SCALE, false);
    bitmap.recycle();

    return result;
  }

  public static String bitmapToString(Bitmap bmp) {
    final Bitmap bitmap = BitmapUtils.transform(bmp);
    bmp.recycle();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
    String imageString = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    bitmap.recycle();
    return imageString;
  }

  static Bitmap stringToBitMap(String encodedString) {
    try {
      byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
      return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
    } catch (Exception e) {
      return null;
    }
  }

}