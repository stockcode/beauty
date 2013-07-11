package cn.nit.beauty.gallery;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.aliyun.android.oss.OSSClient;
import com.aliyun.android.oss.model.OSSObject;
import com.aliyun.android.oss.model.OSSObjectSummary;
import com.aliyun.android.util.Pagination;
import com.baidu.mobstat.StatService;

import cn.nit.beauty.Helper;
import cn.nit.beauty.R;
import cn.nit.beauty.android.bitmapfun.util.DiskLruCache;
import cn.nit.beauty.android.bitmapfun.util.ImageFetcher;
import cn.nit.beauty.model.FolderInfo;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.AsyncTask.Status;
import android.support.v4.view.ViewPager;
import android.util.Log;

public class ImageGalleryActivity extends SherlockActivity {
	
	private static final String SHARED_FILE_NAME = "shared.png";
	private static final int HTTP_CACHE_SIZE = 10 * 1024 * 1024; // 10MB
	public static final String HTTP_CACHE_DIR = "http";
	
	// 屏幕宽度
	public static int screenWidth;
	// 屏幕高度
	public static int screenHeight;
	
	private GalleryAdapter mAdapter;
	private ViewPager mViewPager;
	private ImageFetcher mImageFetcher;
	
	private OSSClient ossClient;
	
	private String objectKey;
	 
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		Intent intent = getIntent();
        objectKey = intent.getStringExtra("objectKey");
        String folder = intent.getStringExtra("folder");
        
		screenWidth = getWindow().getWindowManager().getDefaultDisplay().getWidth();
		screenHeight = getWindow().getWindowManager().getDefaultDisplay().getHeight();
		
		ossClient = new OSSClient();
		ossClient.setAccessId("tEPWqYKJGESwhRo5");
		ossClient.setAccessKey("oUkPZvE5HghfRbkX5wklu6qAiDnMrw");
		
		mViewPager = new HackyViewPager(this);
		setContentView(mViewPager);

		mImageFetcher = new ImageFetcher(this, 240);
        mImageFetcher.setOssClient(ossClient);
        mImageFetcher.setLoadingImage(R.drawable.empty_photo);
        
		mAdapter = new GalleryAdapter(this, mImageFetcher);
		mViewPager.setAdapter(mAdapter);
		
		if (task.getStatus() != Status.RUNNING) {
        	ContentTask task = new ContentTask(this);
			task.execute(folder);			
        }
		
	}
	
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate your menu.
        getSupportMenuInflater().inflate(R.menu.share_action_provider, menu);

        // Set file with share history to the provider and set the share intent.
        MenuItem actionItem = menu.findItem(R.id.menu_item_share_action_provider_action_bar);        
        
        ShareActionProvider actionProvider = (ShareActionProvider) actionItem.getActionProvider();
        actionProvider.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
        // Note that you can set/change the intent any time,
        // say when the user has selected an image.
        actionProvider.setShareIntent(createShareIntent(objectKey));

        return true;
    }

    /**
     * Creates a sharing {@link Intent}.
     *
     * @return The sharing intent.
     */
    private Intent createShareIntent(String key) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        
        

        final File cacheDir = DiskLruCache.getDiskCacheDir(getApplicationContext(),
				HTTP_CACHE_DIR);

		final DiskLruCache cache = DiskLruCache.openCache(getApplicationContext(), cacheDir,
				HTTP_CACHE_SIZE);

		final File cacheFile = new File(cache.createFilePath(key));

		if (cache.containsKey(key)) {
			Uri uri = Uri.fromFile(cacheFile);
			shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
		}        
		
        
        
        return shareIntent;
    }
    
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.i("manager", "onConfigurationChanged...");
		screenWidth = getWindow().getWindowManager().getDefaultDisplay().getWidth();
		screenHeight = getWindow().getWindowManager().getDefaultDisplay().getHeight();
		super.onConfigurationChanged(newConfig);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPause(this);		
	}


	@Override
	protected void onResume() {
		super.onResume();
		StatService.onResume(this);		
	}	
	
	ContentTask task = new ContentTask(this);

    private class ContentTask extends AsyncTask<String, Integer, List<FolderInfo>> {

        private Context mContext;      
        
        public ContentTask(Context context) {
            super();
            mContext = context;
        }

        @Override
        protected List<FolderInfo> doInBackground(String... params) {
        	String folder = params[0];
        	
        	List<FolderInfo> folderInfos = new ArrayList<FolderInfo>();
        	
        	try {
				if (!Helper.checkConnection(mContext))
					return null;

				OSSObject ossObject = ossClient.getObjectSummary("nit-photo", objectKey);
				FolderInfo newsInfo1 = new FolderInfo();
				newsInfo1.setAlbid(ossObject.getObjectKey());
				newsInfo1.setIsrc(ossObject.getObjectKey());
				newsInfo1.setMsg(ossObject.getObjectKey());
				folderInfos.add(newsInfo1);
				
				Pagination<OSSObjectSummary> pagination = ossClient.viewFolder("nit-photo", folder);				
				for (OSSObjectSummary objectSummary : pagination.getContents()) {
					if (objectSummary.getKey().equals(objectKey))
						continue;
					
					if (objectSummary.getKey().equals(folder))
						continue;

					newsInfo1 = new FolderInfo();
					newsInfo1.setAlbid(objectSummary.getKey());
					newsInfo1.setIsrc(objectSummary.getKey());
					newsInfo1.setMsg(objectSummary.getKey());
					folderInfos.add(newsInfo1);
				}

				return folderInfos;

			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
        }

        @Override
        protected void onPostExecute(List<FolderInfo> result) {
                mAdapter.addItemLast(result);
                mAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onPreExecute() {
        }
                
    }
	
}