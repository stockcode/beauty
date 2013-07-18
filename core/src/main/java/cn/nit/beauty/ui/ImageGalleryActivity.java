package cn.nit.beauty.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.aliyun.android.oss.OSSClient;
import com.aliyun.android.oss.model.OSSObjectSummary;
import com.aliyun.android.util.Pagination;
import com.baidu.mobstat.StatService;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.DiscCacheUtil;
import com.nostra13.universalimageloader.utils.StorageUtils;

import cn.nit.beauty.Helper;
import cn.nit.beauty.R;
import cn.nit.beauty.adapter.GalleryAdapter;
import cn.nit.beauty.bus.ImageChangeEvent;
import cn.nit.beauty.gallery.HackyViewPager;
import cn.nit.beauty.model.FolderInfo;
import cn.nit.beauty.utils.Data;
import de.greenrobot.event.EventBus;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.AsyncTask.Status;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Toast;

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
    private ShareActionProvider actionProvider;

	private OSSClient ossClient;
	
	private String objectKey;

    List<FolderInfo> folderInfos;
    private Message message = new Message();
    private Boolean autoPlay = false;
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

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int i) {
                createShareIntent(Data.OSS_URL + folderInfos.get(i).getIsrc());
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
		mAdapter = new GalleryAdapter(this);
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
        
        actionProvider = (ShareActionProvider) actionItem.getActionProvider();
        actionProvider.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem mi){
        switch (mi.getItemId()) {
            case R.id.mnuPlay:

                autoPlay = true;
                AutoPlayHandler autoPlayHandler = new AutoPlayHandler();
                autoPlayHandler.sendMessageDelayed(message, 2000);// 延迟两秒发送消息
                Toast.makeText(this, "开启自动播放，按返回键即可关闭", Toast.LENGTH_SHORT).show();
                break;
            case R.id.mnuWallpaper:
                WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
                try {
                    File cacheFile = DiscCacheUtil.findInCache(Data.OSS_URL + folderInfos.get(mViewPager.getCurrentItem()).getIsrc(), ImageLoader.getInstance().getDiscCache());
                    InputStream is = new FileInputStream(cacheFile);
                    wallpaperManager.setStream(is);
                    is.close();;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.mnuOriginal:
                break;
        }
        return true;

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && autoPlay) {
            autoPlay = false;
            Toast.makeText(this, "自动播放已关闭", Toast.LENGTH_SHORT).show();
            return  true;
        }
        return super.dispatchKeyEvent(event);
    }

        /**
         * Creates a sharing {@link Intent}.
         *
         * @return The sharing intent.
         */
    private Intent createShareIntent(String key) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");

        File cacheFile = DiscCacheUtil.findInCache(key, ImageLoader.getInstance().getDiscCache());

		Uri uri = Uri.fromFile(cacheFile);
		shareIntent.putExtra(Intent.EXTRA_STREAM, uri);

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
    protected void onStart() {
        super.onStart();
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
        	
        	folderInfos = new ArrayList<FolderInfo>();
        	
        	try {
				if (!Helper.checkConnection(mContext))
					return null;

				FolderInfo newsInfo1 = new FolderInfo();

				Pagination<OSSObjectSummary> pagination = ossClient.viewFolder("nit-photo", folder);				
				for (OSSObjectSummary objectSummary : pagination.getContents()) {

					
					if (objectSummary.getKey().equals(folder))
						continue;

					newsInfo1 = new FolderInfo();
					newsInfo1.setAlbid(objectSummary.getKey());
					newsInfo1.setIsrc(objectSummary.getKey());
					newsInfo1.setMsg(objectSummary.getKey());

                    if (objectSummary.getKey().equals(objectKey))
                        folderInfos.add(0, newsInfo1);
                    else
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

    class AutoPlayHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            if (autoPlay) {
                int nextItem = (mViewPager.getCurrentItem() + 1) % folderInfos.size();
                if (nextItem == 0) {
                    Toast.makeText(ImageGalleryActivity.this, "已经播放完了，从头开始", Toast.LENGTH_SHORT).show();
                }
                mViewPager.setCurrentItem(nextItem);// 换页，同时实现了循环播放
                message = obtainMessage(0);// 重新给message赋值，因为前一个message“还在使用中”
                sendMessageDelayed(message, 2000);
            }
        }
    }
}