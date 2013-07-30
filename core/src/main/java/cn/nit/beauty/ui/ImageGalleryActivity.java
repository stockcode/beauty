package cn.nit.beauty.ui;

import android.app.WallpaperManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.baidu.mobstat.StatService;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.DiscCacheUtil;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.octo.android.robospice.GsonSpringAndroidSpiceService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import cn.nit.beauty.R;
import cn.nit.beauty.adapter.GalleryAdapter;
import cn.nit.beauty.gallery.HackyViewPager;
import cn.nit.beauty.model.ImageInfo;
import cn.nit.beauty.model.ImageInfos;
import cn.nit.beauty.request.ImageListRequest;
import cn.nit.beauty.utils.Data;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class ImageGalleryActivity extends SherlockFragmentActivity {

    // 屏幕宽度
    public static int screenWidth;
    // 屏幕高度
    public static int screenHeight;
    List<ImageInfo> imageInfoList;
    private GalleryAdapter mAdapter;
    private ViewPager mViewPager;
    private ShareActionProvider actionProvider;
    private String objectKey, folder;
    private Message message;
    private Boolean autoPlay = false;
    private SpiceManager spiceManager = new SpiceManager(
            GsonSpringAndroidSpiceService.class);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Intent intent = getIntent();

        objectKey = intent.getStringExtra("objectKey");
        folder = intent.getStringExtra("folder");

        screenWidth = getWindow().getWindowManager().getDefaultDisplay().getWidth();
        screenHeight = getWindow().getWindowManager().getDefaultDisplay().getHeight();


        mViewPager = new HackyViewPager(this);
        setContentView(mViewPager);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int i) {
                if (actionProvider != null) {
                    createShareIntent(Data.OSS_URL + imageInfoList.get(i).getUrl());
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        mAdapter = new GalleryAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mViewPager.setId(R.id.view_pager);

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

    public boolean onOptionsItemSelected(MenuItem mi) {
        switch (mi.getItemId()) {
            case R.id.mnuPlay:

                autoPlay = true;
                AutoPlayHandler autoPlayHandler = new AutoPlayHandler();
                message = new Message();
                autoPlayHandler.sendMessageDelayed(message, 2000);// 延迟两秒发送消息
                Toast.makeText(this, "开启自动播放，按返回键即可关闭", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.mnuWallpaper:
                changeWallpaper();
                return true;
            case R.id.mnuOriginal:
                changeOriginal();
                return true;

            default:
                return super.onOptionsItemSelected(mi);
        }


    }

    private void changeOriginal() {
        ImageInfo imageInfo = mAdapter.getImageInfo(mViewPager.getCurrentItem());
        String imageSrc = imageInfo.getUrl().replaceAll("thumb", "original");
        imageInfo.setUrl(imageSrc);

        View imageLayout = mViewPager.findViewWithTag(mViewPager.getCurrentItem());

        final PhotoView photoView = (PhotoView) imageLayout.findViewById(R.id.image);

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
                Toast.makeText(ImageGalleryActivity.this, message, Toast.LENGTH_SHORT).show();

                spinner.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                spinner.setVisibility(View.GONE);
                Toast.makeText(ImageGalleryActivity.this, "原图已加载", Toast.LENGTH_SHORT).show();
                //Toast.makeText(ImageGalleryActivity.this, "文件大小：" + loadedImage.getByteCount() / (1024*1024) + "MB", Toast.LENGTH_SHORT).show();
                PhotoViewAttacher mAttacher = new PhotoViewAttacher(photoView);
                mAttacher.update();
            }
        });
    }

    private void changeWallpaper() {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        try {
            File cacheFile = DiscCacheUtil.findInCache(Data.OSS_URL + imageInfoList.get(mViewPager.getCurrentItem()).getUrl(), ImageLoader.getInstance().getDiscCache());
            InputStream is = new FileInputStream(cacheFile);
            wallpaperManager.setStream(is);
            is.close();
            Toast.makeText(ImageGalleryActivity.this, "壁纸已换好", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if (autoPlay) {
            autoPlay = false;
            Toast.makeText(this, "自动播放已关闭", Toast.LENGTH_SHORT).show();
            return true;
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (event.getAction() == KeyEvent.ACTION_UP) {
                int nextItem = (mViewPager.getCurrentItem() + 1) % imageInfoList.size();
                mViewPager.setCurrentItem(nextItem);
            }
            return true;
        } else if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
            if (event.getAction() == KeyEvent.ACTION_UP) {
                int prevItem = (mViewPager.getCurrentItem() - 1) % imageInfoList.size();
                mViewPager.setCurrentItem(prevItem);
            }
            return true;
        }


        return super.dispatchKeyEvent(event);
    }

    /**
     * Creates a sharing {@link Intent}.
     *
     * @return The sharing intent.
     */
    private void createShareIntent(String key) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");

        File cacheFile = DiscCacheUtil.findInCache(key, ImageLoader.getInstance().getDiscCache());

        if (cacheFile != null) {
            Uri uri = Uri.fromFile(cacheFile);
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            actionProvider.setShareIntent(shareIntent);
        }
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
        spiceManager.start(this);
    }

    @Override
    protected void onStop() {
        spiceManager.shouldStop();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        StatService.onResume(this);

        ImageListRequest imageListRequest = new ImageListRequest(Data.OSS_URL + folder.replaceAll("thumb/", "") + Data.INDEX_KEY);
        spiceManager.execute(imageListRequest, objectKey, DurationInMillis.ONE_DAY, new ImageListRequestListener());
    }

    public int getCurrentItem() {
        for (int i = 0; i < imageInfoList.size(); i++) {
            if (imageInfoList.get(i).getKey().equals(objectKey)) return i;
        }
        return 0;
    }

    private class ImageListRequestListener implements RequestListener<ImageInfos> {
        @Override
        public void onRequestFailure(SpiceException e) {
            Toast.makeText(ImageGalleryActivity.this, "网络不给力,错误: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onRequestSuccess(ImageInfos imageInfos) {
            imageInfoList = imageInfos.getResults();
            mAdapter.addItemLast(imageInfoList);
            mAdapter.notifyDataSetChanged();
            mViewPager.setCurrentItem(getCurrentItem());
        }
    }

    class AutoPlayHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            if (autoPlay) {
                int nextItem = (mViewPager.getCurrentItem() + 1) % imageInfoList.size();
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