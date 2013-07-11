/**  
 * GalleryAdapter.java
 * @version 1.0
 */
package cn.nit.beauty.gallery;

import java.util.LinkedList;
import java.util.List;

import uk.co.senab.photoview.PhotoView;
import cn.nit.beauty.android.bitmapfun.util.ImageFetcher;
import cn.nit.beauty.model.FolderInfo;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

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
