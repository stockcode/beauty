package cn.nit.beauty.ui;

import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;
import com.testin.agent.TestinAgent;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

import cn.nit.beauty.BeautyApplication;
import cn.nit.beauty.Helper;
import cn.nit.beauty.R;
import cn.nit.beauty.Utils;
import cn.nit.beauty.entity.User;
import cn.nit.beauty.model.ImageInfo;
import cn.nit.beauty.utils.Data;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.onekeyshare.ShareContentCustomizeCallback;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.weibo.TencentWeibo;
import uk.co.senab.photoview.PhotoView;

public class ImagePagerActivity extends SherlockFragmentActivity {

    private static final String STATE_POSITION = "STATE_POSITION";

    List<ImageInfo> imageInfoList;
    private ViewPager mViewPager;
    private ImageAdapter mAdapter;

    private String objectKey, folder;
    private Message message;
    private Boolean autoPlay = false, isOriginal = false;

    private MenuItem mnuSave;

    private SharedPreferences settings;

    private User currentUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_pager);

        mViewPager = (ViewPager) findViewById(R.id.pager);

        settings = PreferenceManager.getDefaultSharedPreferences(ImagePagerActivity.this);

        Intent intent = getIntent();

        int pagerPosition = savedInstanceState == null ? intent.getIntExtra("position", 0) : savedInstanceState.getInt(STATE_POSITION);

        folder = intent.getStringExtra("folder");
        imageInfoList = (List<ImageInfo>) intent.getSerializableExtra("imageList");
        setBigPicture();

        setTitle(intent.getStringExtra("title"));

        currentUser = BeautyApplication.getInstance().getCurrentUser();


        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
            }

            @Override
            public void onPageSelected(int i) {
                if ((folder.startsWith("origin") && i >= 3)
                        || i >= Data.DISPLAY_COUNT) {

                    if (currentUser == null) {
                        autoPlay = false;
                        Intent intent = new Intent(ImagePagerActivity.this, LoginActivity.class);
                        startActivityForResult(intent, Utils.LOGIN);
                        Toast.makeText(ImagePagerActivity.this, "查看更多图片请先登录", Toast.LENGTH_SHORT).show();
                    } else if (currentUser.hasExpired()) {
                        autoPlay = false;
                        Intent intent = new Intent(ImagePagerActivity.this, VipProductActivity.class);
                        startActivityForResult(intent, Utils.VIP);
                        Toast.makeText(ImagePagerActivity.this, "有效期为" + currentUser.getExpiredDate() + "，请续费", Toast.LENGTH_SHORT).show();
                    }
                }

                isOriginal = imageInfoList.get(i).isOriginal();
                invalidateOptionsMenu();
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });

        mAdapter = new ImageAdapter();
        mAdapter.addItems(imageInfoList);

        mViewPager.setAdapter(mAdapter);


        mViewPager.setCurrentItem(pagerPosition);

    }

    private void setBigPicture() {
        for(ImageInfo imageInfo:imageInfoList) {
            imageInfo.setBig(true);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_POSITION, mViewPager.getCurrentItem());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate your menu.
        getSupportMenuInflater().inflate(R.menu.share_action_provider, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem mnuOriginal = menu.findItem(R.id.mnuOriginal);

        if (isOriginal) {
            mnuOriginal.setTitle("保存");
        } else {
            mnuOriginal.setTitle("原图");
        }

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem mi) {

        if (imageInfoList == null) return true;

        switch (mi.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

            case R.id.mnuShare:
                showShare();
                return true;

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
                if (currentUser == null) {
                    Intent intent = new Intent(ImagePagerActivity.this, LoginActivity.class);
                    startActivityForResult(intent, Utils.LOGIN);
                    Toast.makeText(ImagePagerActivity.this, "查看原图请先登录", Toast.LENGTH_SHORT).show();
                    return true;
                }

                if (mi.getTitle().equals("原图")) {
                    if (Helper.isWifi(getApplicationContext()) || settings.getBoolean("notifyWIFI", false)) {
                        changeOriginal();
                    }
                    else {
                        new AlertDialog.Builder(ImagePagerActivity.this)
                                .setTitle("温馨提示")
                                .setMessage("当前非WIFI网络，继续浏览会消耗您的流量(每张图片约1MB)")
                                .setNegativeButton("继续", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        changeOriginal();
                                    }
                                })
                                .setNeutralButton("取消", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                })
                                .setPositiveButton("不再提示", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        settings.edit().putBoolean("notifyWIFI", true).apply();
                                        changeOriginal();
                                    }
                                })
                                .show();
                    }


                } else {
                    savePicture();
                }
                return true;


            default:
                return super.onOptionsItemSelected(mi);
        }


    }

    private void savePicture() {
        ImageInfo imageInfo = mAdapter.getImageInfo(mViewPager.getCurrentItem());
        String imageSrc = imageInfo.getOriginalUrl();

        File cacheFile = DiskCacheUtils.findInCache(Data.OSS_URL + imageSrc, ImageLoader.getInstance().getDiskCache());

        String dir = Utils.getRootDirectory().getPath();

        String dstFile = dir + imageSrc.substring(imageSrc.lastIndexOf("/"));

        Utils.copyFile(cacheFile.getAbsolutePath(), dstFile);

        Toast.makeText(ImagePagerActivity.this, "图片已保存至" + dir + "文件夹", Toast.LENGTH_LONG).show();

        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        scanIntent.setData(Uri.fromFile(new File(dstFile)));
        getApplicationContext().sendBroadcast(scanIntent);
    }

    private void changeOriginal() {

        final ImageInfo imageInfo = mAdapter.getImageInfo(mViewPager.getCurrentItem());
        imageInfo.setOriginal(true);
        String imageSrc = imageInfo.getUrl();

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
                Toast.makeText(ImagePagerActivity.this, message, Toast.LENGTH_SHORT).show();

                spinner.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                spinner.setVisibility(View.GONE);
                Toast.makeText(ImagePagerActivity.this, "原图已加载", Toast.LENGTH_SHORT).show();
                //Toast.makeText(ImagePagerActivity.this, "文件大小：" + loadedImage.getByteCount() / (1024*1024) + "MB", Toast.LENGTH_SHORT).show();
                isOriginal = true;
                invalidateOptionsMenu();
                imageInfo.setOriginal(true);
            }
        });
    }

    private void changeWallpaper() {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        try {
            File cacheFile = DiskCacheUtils.findInCache(Data.OSS_URL + imageInfoList.get(mViewPager.getCurrentItem()).getUrl(), ImageLoader.getInstance().getDiskCache());
            InputStream is = new FileInputStream(cacheFile);
            wallpaperManager.setStream(is);
            is.close();
            Toast.makeText(ImagePagerActivity.this, "壁纸已换好", Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        TestinAgent.onStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        TestinAgent.onStop(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    private void showShare() {
        ShareSDK.initSDK(this);

        ImageInfo imageInfo = imageInfoList.get(mViewPager.getCurrentItem());

        final String url = MobclickAgent.getConfigParams(this, "share_url") + "?p=" + URLEncoder.encode(imageInfo.getUrl());

        final String share_text = MobclickAgent.getConfigParams(this, "share_text");

        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();
        oks.setDialogMode();

// 分享时Notification的图标和文字
        oks.setNotification(R.drawable.icon, getString(R.string.app_name));
        // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
        oks.setTitle(getString(R.string.app_name));
        // titleUrl是标题的网络链接，仅在人人网和QQ空间使用
        oks.setTitleUrl(url);
        // text是分享文本，所有平台都需要这个字段
        oks.setText(share_text);
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        File cacheFile = DiskCacheUtils.findInCache(Data.OSS_URL + imageInfo.getUrl(), ImageLoader.getInstance().getDiskCache());
        if (cacheFile != null) oks.setImagePath(cacheFile.getAbsolutePath());//确保SDcard下面存在此张图片

        // url仅在微信（包括好友和朋友圈）中使用
        oks.setUrl(url);
        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
        oks.setComment("我是测试评论文本");
        // site是分享此内容的网站名称，仅在QQ空间使用
        oks.setSite(getString(R.string.app_name));
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        oks.setSiteUrl(url);

        oks.setShareContentCustomizeCallback(new ShareContentCustomizeCallback() {
            @Override
            public void onShare(Platform platform, Platform.ShareParams paramsToShare) {
                if (SinaWeibo.NAME.equals(platform.getName()) || TencentWeibo.NAME.equals(platform.getName())) {
                    paramsToShare.setText(share_text + "→ →" + url + " @丽图-美腿套图");
                }
            }
        });
        // 启动分享GUI
        oks.show(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.LOGIN) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(ImagePagerActivity.this, "取消登录，已返回", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if (requestCode == Utils.VIP) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(ImagePagerActivity.this, "取消续费，已返回", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    class AutoPlayHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            if (autoPlay) {
                int nextItem = (mViewPager.getCurrentItem() + 1) % imageInfoList.size();
                if (nextItem == 0) {
                    Toast.makeText(ImagePagerActivity.this, "已经播放完了，从头开始", Toast.LENGTH_SHORT).show();
                }
                mViewPager.setCurrentItem(nextItem);// 换页，同时实现了循环播放
                message = obtainMessage(0);// 重新给message赋值，因为前一个message“还在使用中”
                sendMessageDelayed(message, 2000);
            }
        }
    }

    private class ImageAdapter extends PagerAdapter {

        private LayoutInflater inflater;

        private LinkedList<ImageInfo> mInfos = new LinkedList<ImageInfo>();

        ImageAdapter() {
            inflater = LayoutInflater.from(ImagePagerActivity.this);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return mInfos.size();
        }

        @Override
        public Object instantiateItem(ViewGroup view, int position) {
            View imageLayout = inflater.inflate(R.layout.item_pager_image, view, false);
            assert imageLayout != null;
            ImageView imageView = (ImageView) imageLayout.findViewById(R.id.image);
            final ProgressBar spinner = (ProgressBar) imageLayout.findViewById(R.id.loading);

            ImageLoader.getInstance().displayImage(Data.OSS_URL + mInfos.get(position).getUrl(), imageView, new SimpleImageLoadingListener() {
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
                    Toast.makeText(ImagePagerActivity.this, message, Toast.LENGTH_SHORT).show();

                    spinner.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    spinner.setVisibility(View.GONE);
                }
            });

            view.addView(imageLayout, 0);
            imageLayout.setTag(position);
            return imageLayout;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        public void addItems(List<ImageInfo> datas) {
            mInfos.addAll(datas);
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        public ImageInfo getImageInfo(int currentItem) {
            return mInfos.get(currentItem);
        }
    }

}