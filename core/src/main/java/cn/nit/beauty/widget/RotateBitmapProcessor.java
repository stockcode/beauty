package cn.nit.beauty.widget;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import com.nostra13.universalimageloader.core.process.BitmapProcessor;

/**
 * Created by Administrator on 13-7-19.
 */
public class RotateBitmapProcessor implements BitmapProcessor {
    @Override
    public Bitmap process(Bitmap bitmap) {
        if (bitmap.getWidth() < bitmap.getHeight())
            return bitmap;

        Matrix matrix = new Matrix();
        matrix.reset();
        matrix.setRotate(90);


        try {
            Bitmap tmpBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            bitmap = null;
            return tmpBitmap;
        } catch (RuntimeException re) {
        }

        return bitmap;
    }
}
