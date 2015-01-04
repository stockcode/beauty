/**
 * GalleryAdapter.java
 * @version 1.0
 */
package cn.nit.beauty.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.LayoutInflater;
import cn.nit.beauty.fragment.ImageFragment;
import cn.nit.beauty.model.ImageInfo;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

public class GalleryAdapter extends FragmentStatePagerAdapter {

    DecimalFormat df = new DecimalFormat("##0.000");
    private LayoutInflater inflater;
    private Context mContext;
    private LinkedList<ImageInfo> mInfos = new LinkedList<ImageInfo>();

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


