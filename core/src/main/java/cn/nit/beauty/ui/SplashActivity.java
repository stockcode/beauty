package cn.nit.beauty.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.widget.Toast;

import butterknife.ButterKnife;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.listener.FindListener;
import cn.nit.beauty.BeautyApplication;
import cn.nit.beauty.Helper;
import cn.nit.beauty.entity.BeautyPlatform;
import cn.nit.beauty.entity.PhotoGallery;
import cn.nit.beauty.proxy.UserProxy;
import cn.nit.beauty.utils.L;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.socialization.Socialization;
import cn.smssdk.SMSSDK;
import com.octo.android.robospice.GsonSpringAndroidSpiceService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.ArrayList;
import java.util.List;

import cn.nit.beauty.R;
import cn.nit.beauty.Utils;
import cn.nit.beauty.database.LaucherDataBase;
import cn.nit.beauty.database.Category;
import cn.nit.beauty.model.Index;
import cn.nit.beauty.request.IndexRequest;
import cn.nit.beauty.utils.Data;
import com.testin.agent.TestinAgent;
import com.umeng.analytics.MobclickAgent;

public class SplashActivity extends BaseActivity {

    private SpiceManager spiceManager = new SpiceManager(
            GsonSpringAndroidSpiceService.class);

    LaucherDataBase database;
    boolean isDaily;
    boolean isFinish = false;

    private SharedPreferences settings;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        TestinAgent.init(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splashing);


        MobclickAgent.updateOnlineConfig(this);

        Intent intent = getIntent();

        isDaily = intent.hasExtra("isDaily");

        database = new LaucherDataBase(getApplicationContext());


        settings = PreferenceManager.getDefaultSharedPreferences(this);

        if (!settings.getBoolean("shortcut", false)) {
            createShortCut(this);
            settings.edit().putBoolean("shortcut", true).apply();
        }

        Toast.makeText( SplashActivity.this, "检测到网络:" + Helper.getNetworkName(this), Toast.LENGTH_SHORT ).show();

        IndexRequest indexRequest = new IndexRequest(Data.OSS_URL + Data.INDEX_KEY);
        spiceManager.execute(indexRequest, "beauty.index", DurationInMillis.ALWAYS_EXPIRED, new IndexRequestListener());

        ShareSDK.initSDK(this);

        ShareSDK.registerService(Socialization.class);

        SMSSDK.initSDK(this, Data.SMS_APP_ID, Data.SMS_APP_SECRET);

        Bmob.initialize(this, "19fee4b5da44fc283e4c58e9f860ea96");

        Socialization service = ShareSDK.getService(Socialization.class);
        BeautyPlatform beautyPlatform = new BeautyPlatform(SplashActivity.this);
        service.setCustomPlatform(beautyPlatform);
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
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (event.getAction() == KeyEvent.ACTION_DOWN
                    && event.getRepeatCount() == 0) {
                isFinish = true;
                finish();
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    public static void createShortCut(Context context) {
        final Intent myIntent = new Intent(context,SplashActivity.class);
        myIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        myIntent.setAction(Intent.ACTION_MAIN);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        final Intent addIntent = new Intent(
                "com.android.launcher.action.INSTALL_SHORTCUT");
        final Parcelable icon = Intent.ShortcutIconResource.fromContext(
                context, R.drawable.icon); // 获取快捷键的图标
        addIntent.putExtra("duplicate", false);

        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME,
                context.getString(R.string.app_name));// 快捷方式的标题
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);// 快捷方式的图标
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, myIntent);// 快捷方式的动作


        context.sendBroadcast(addIntent);
    }


    private class IndexRequestListener implements RequestListener<Index> {
        @Override
        public void onRequestFailure(SpiceException e) {
            Toast.makeText( SplashActivity.this, "网络不给力,错误: " + e.getMessage(), Toast.LENGTH_LONG ).show();
            startMain();
        }

        @Override
        public void onRequestSuccess(Index index) {
            if ( index != null ) {
                database.insertItems(index.getCategories());
                Data.categoryMap = index.getRoots();
                Data.categoryMap.put("favorite", new ArrayList<String>());
                if (BeautyApplication.getInstance().getCurrentUser() == null)
                    startMain();
                else {

                    BeautyApplication.getInstance().authorize();

                    getFavorites();
                }
            }

        }
    }

    private void startMain() {

        Intent intent = new Intent();

        if (isDaily) {
            intent.putExtra("category", "daily");
            intent.setClass(SplashActivity.this, BeautyActivity.class);
        } else {
            intent.setClass(SplashActivity.this, MainActivity.class);
        }
        startActivity(intent);
        finish();
    }

    private void getFavorites() {
        BmobQuery<PhotoGallery> query = new BmobQuery<PhotoGallery>();
        query.addWhereRelatedTo("favorite", new BmobPointer(BeautyApplication.getInstance().getCurrentUser()));
        query.include("user");
        query.order("createdAt");
        query.findObjects(this, new FindListener<PhotoGallery>() {

            @Override
            public void onSuccess(List<PhotoGallery> data) {
                L.i("get fav success!" + data.size());
                List<String> favs = Data.categoryMap.get("favorite");

                for(PhotoGallery photoGallery : data) {
                    favs.add(photoGallery.getUrl());
                }

                startMain();
            }

            @Override
            public void onError(int arg0, String arg1) {
                L.e("get fav error! reason:" + arg1);
                startMain();
            }
        });
    }
}









