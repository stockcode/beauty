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
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

import java.text.DecimalFormat;
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
    DecimalFormat df   =   new   DecimalFormat("##0.000");

    public GalleryAdapter(Context context) {
        mContext = context;
        mInfos = new LinkedList<FolderInfo>();
        inflater = (LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View instantiateItem(ViewGroup container, int position) {
        View imageLayout = inflater.inflate(R.layout.item_pager_image, container, false);

        FolderInfo folderInfo = mInfos.get(position);
        String imageSrc = folderInfo.getIsrc();

        PhotoView photoView = (PhotoView) imageLayout.findViewById(R.id.image);
        final TextView tvInfo = (TextView) imageLayout.findViewById(R.id.tvInfo);
        tvInfo.setText((position+1) + "/" + mInfos.size());
        final ProgressBar spinner = (ProgressBar) imageLayout.findViewById(R.id.loading);


        ImageLoader.getInstance().displayImage(Data.OSS_URL + imageSrc, photoView, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                spinner.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                String message = null;
                switch (failReason.getType()) {
                    case IO_ERROR:
                        message = "读取错误";
                        break;
                    case DECODING_ERROR:
                        message = "图片解码错误";
                        break;
                    case NETWORK_DENIED:
                        message = "下载图片被拒绝";
                        break;
                    case OUT_OF_MEMORY:
                        message = "内存溢出";
                        break;
                    case UNKNOWN:
                        message = "未知错误";
                        break;
                }
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();

                spinner.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                spinner.setVisibility(View.GONE);
                //float size = loadedImage.getByteCount() / (1024*1024);
                //tvInfo.setText(tvInfo.getText() + "    " + df.format(size) + "MB");
            }
        });

        ((ViewPager) container).addView(imageLayout, 0);
        imageLayout.setTag(position);
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


