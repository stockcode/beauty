package cn.nit.beauty.widget;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import com.nostra13.universalimageloader.core.process.BitmapProcessor;

/**
 * Created by Administrator on 13-7-19.
 */
public class RotateBitmapProcessor implements BitmapProcessor {
    //private IImageFilter filter = new MosaicFilter();

    @Override
    public Bitmap process(Bitmap bitmap) {
//        Image img = null;
//
//        try
//        {
//            img = new Image(bitmap);
//
//            img = filter.process(img);
//            img.copyPixelsFromBuffer();
//
//            return img.getImage();
//        } catch(Exception e){
//            if (img != null && img.destImage.isRecycled()) {
//                img.destImage.recycle();
//                img.destImage = null;
//                System.gc(); // 提醒系统及时回收
//            }
//        }
//        finally{
//            if (img != null && img.image.isRecycled()) {
//                img.image.recycle();
//                img.image = null;
//                System.gc(); // 提醒系统及时回收
//            }
//        }
//        return bitmap;


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
        } catch (OutOfMemoryError re) {
        }

        return bitmap;
    }
}
