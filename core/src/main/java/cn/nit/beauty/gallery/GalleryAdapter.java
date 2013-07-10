/**  
 * GalleryAdapter.java
 * @version 1.0
 */
package cn.nit.beauty.gallery;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import uk.co.senab.photoview.PhotoView;
import me.maxwin.view.XListView;
import cn.nit.beauty.BeautyActivity;
import cn.nit.beauty.ImageListActivity;
import cn.nit.beauty.R;
import cn.nit.beauty.android.bitmapfun.util.DiskLruCache;
import cn.nit.beauty.android.bitmapfun.util.ImageFetcher;
import cn.nit.beauty.model.FolderInfo;
import cn.nit.beauty.widget.ScaleImageView;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ShareActionProvider;
import android.widget.TextView;

public class GalleryAdapter extends PagerAdapter {
	
	private Context mContext;
    private LinkedList<FolderInfo> mInfos;
    private ImageFetcher mImageFetcher;
    
    public GalleryAdapter(Context context, ImageFetcher mImageFetcher) {
        mContext = context;
        mInfos = new LinkedList<FolderInfo>();
        this.mImageFetcher = mImageFetcher;
    }


	@Override
	public View instantiateItem(ViewGroup container, int position) {
		FolderInfo folderInfo = mInfos.get(position);
		PhotoView photoView = new PhotoView(container.getContext());
		
		mImageFetcher.loadImage(folderInfo.getIsrc(), photoView);		
		
		// Now just add PhotoView to ViewPager and return it
		container.addView(photoView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		return photoView;
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

    public FolderInfo getItem(int location){
    	return mInfos.get(location);
    }
}
