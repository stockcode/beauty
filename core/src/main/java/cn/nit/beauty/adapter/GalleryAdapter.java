/**
 * GalleryAdapter.java
 * @version 1.0
 */
package cn.nit.beauty.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
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
import cn.nit.beauty.fragment.ImageFragment;
import cn.nit.beauty.model.ImageInfo;
import cn.nit.beauty.utils.Data;
import uk.co.senab.photoview.PhotoView;

public class GalleryAdapter extends FragmentStatePagerAdapter {

    private LayoutInflater inflater;
    private Context mContext;
    private LinkedList<ImageInfo> mInfos = new LinkedList<ImageInfo>();;
    DecimalFormat df   =   new   DecimalFormat("##0.000");

    public GalleryAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return ImageFragment.init(mInfos.get(position).getUrl(), position);
    }

    @Override
    public int getCount() {
        return mInfos.size();
    }

    public void addItemLast(List<ImageInfo> datas) {
        mInfos.addAll(datas);
    }

    public void addItemTop(List<ImageInfo> datas) {
        for (ImageInfo info : datas) {
            mInfos.addFirst(info);
        }
    }

    public ImageInfo getImageInfo(int currentItem) {
        return mInfos.get(currentItem);
    }
}


