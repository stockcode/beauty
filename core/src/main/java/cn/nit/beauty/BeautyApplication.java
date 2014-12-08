package cn.nit.beauty;

import android.app.Activity;
import android.app.Application;
import android.graphics.Bitmap;

import cn.bmob.v3.BmobUser;
import cn.nit.beauty.entity.User;
import cn.nit.beauty.utils.ActivityManagerUtils;
import com.baidu.frontia.FrontiaApplication;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.utils.StorageUtils;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import java.io.File;

import cn.nit.beauty.widget.RotateBitmapProcessor;

/**
 * Created by gengke on 13-7-15.
 */
@ReportsCrashes(
        formUri = "http://www.bugsense.com/api/acra?api_key=fcc709f3",
        formKey=""
)
public class BeautyApplication extends FrontiaApplication {

    private static BeautyApplication myApplication = null;

    public static BeautyApplication getInstance(){
        return myApplication;
    }

    public void addActivity(Activity ac){
        ActivityManagerUtils.getInstance().addActivity(ac);
    }

    public void exit(){
        ActivityManagerUtils.getInstance().removeAllActivity();
    }

    public Activity getTopActivity(){
        return ActivityManagerUtils.getInstance().getTopActivity();
    }

    public User getCurrentUser() {
        User user = BmobUser.getCurrentUser(myApplication, User.class);
        if(user!=null){
            return user;
        }
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // The following line triggers the initialization of ACRA
        //ACRA.init(this);

        //由于Application类本身已经单例，所以直接按以下处理即可。
        myApplication = this;

        // Create global configuration and initialize ImageLoader with this configuration
        initImageLoader();

    }

    private void initImageLoader() {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .showStubImage(R.drawable.xlistview_arrow)
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .preProcessor(new RotateBitmapProcessor())
                .build();

        File cacheDir = StorageUtils.getCacheDirectory(this.getApplicationContext());

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .memoryCacheSize(2 * 1024 * 1024)
                .memoryCacheSizePercentage(13) // default
                .discCache(new UnlimitedDiscCache(cacheDir)) // default
                .discCacheSize(200 * 1024 * 1024)
                .discCacheFileCount(1000)
                .discCacheFileNameGenerator(new HashCodeFileNameGenerator())
                .build();
        ImageLoader.getInstance().init(config);
    }

    public DisplayImageOptions getOptions(int drawableId){
        return new DisplayImageOptions.Builder()
                .showImageOnLoading(drawableId)
                .showImageForEmptyUri(drawableId)
                .showImageOnFail(drawableId)
                .resetViewBeforeLoading(true)
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }
}
