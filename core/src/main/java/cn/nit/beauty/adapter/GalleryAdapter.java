/**
 * GalleryAdapter.java
 * @version 1.0
 */
package cn.nit.beauty.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

import java.util.LinkedList;
import java.util.List;

import cn.nit.beauty.R;
import cn.nit.beauty.Utils;
import cn.nit.beauty.bus.ImageChangeEvent;
import cn.nit.beauty.model.FolderInfo;
import cn.nit.beauty.utils.Data;
import de.greenrobot.event.EventBus;
import uk.co.senab.photoview.PhotoView;

public class GalleryAdapter extends PagerAdapter {

    private LayoutInflater inflater;
    private Context mContext;
    private LinkedList<FolderInfo> mInfos;

    public GalleryAdapter(Context context) {
        mContext = context;
        mInfos = new LinkedList<FolderInfo>();
        inflater = (LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View instantiateItem(ViewGroup container, int position) {
        View imageLayout = inflater.inflate(R.layout.item_pager_image, container, false);

        FolderInfo folderInfo = mInfos.get(position);

        PhotoView photoView = (PhotoView) imageLayout.findViewById(R.id.image);
        final ProgressBar spinner = (ProgressBar) imageLayout.findViewById(R.id.loading);


        ImageLoader.getInstance().displayImage(Data.OSS_URL + folderInfo.getIsrc(), photoView, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                spinner.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                String message = null;
                switch (failReason.getType()) {
                    case IO_ERROR:
                        message = "Input/Output error";
                        break;
                    case DECODING_ERROR:
                        message = "Image can't be decoded";
                        break;
                    case NETWORK_DENIED:
                        message = "Downloads are denied";
                        break;
                    case OUT_OF_MEMORY:
                        message = "Out Of Memory error";
                        break;
                    case UNKNOWN:
                        message = "Unknown error";
                        break;
                }
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();

                spinner.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                spinner.setVisibility(View.GONE);
                EventBus.getDefault().post(new ImageChangeEvent(imageUri));
            }
        });

        ((ViewPager) container).addView(imageLayout, 0);
        return imageLayout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public int getCount() {
        return mInfos.size();
    }

    public void addItemLast(List<FolderInfo> datas) {
        mInfos.addAll(datas);
    }

    public void addItemTop(List<FolderInfo> datas) {
        for (FolderInfo info : datas) {
            mInfos.addFirst(info);
        }
    }

    public FolderInfo getItem(int location) {
        return mInfos.get(location);
    }


}


