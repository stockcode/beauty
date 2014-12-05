package cn.nit.beauty.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.widget.Toast;

import cn.bmob.v3.Bmob;
import cn.nit.beauty.Helper;
import cn.smssdk.SMSSDK;
import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.baidu.mobstat.StatService;
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
import cn.nit.beauty.utils.Configure;
import cn.nit.beauty.utils.Data;

public class SplashActivity extends Activity {

    private SpiceManager spiceManager = new SpiceManager(
            GsonSpringAndroidSpiceService.class);

    LaucherDataBase database;
    boolean isLaucher, isDaily;
    boolean isFinish = false;

    private SharedPreferences settings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_loading);

        Intent intent = getIntent();

        isDaily = intent.hasExtra("isDaily");

        database = new LaucherDataBase(getApplicationContext());

        PushManager.startWork(getApplicationContext(),
                PushConstants.LOGIN_TYPE_API_KEY, Utils.getMetaValue(SplashActivity.this, "api_key"));

        Configure.inits(SplashActivity.this);

        settings = PreferenceManager.getDefaultSharedPreferences(this);

        if (!settings.getBoolean("shortcut", false)) {
            createShortCut(this);
            settings.edit().putBoolean("shortcut", true).apply();
        }

        Toast.makeText( SplashActivity.this, "检测到网络:" + Helper.getNetworkName(this), Toast.LENGTH_SHORT ).show();

        IndexRequest indexRequest = new IndexRequest(Data.OSS_URL + Data.INDEX_KEY);
        spiceManager.execute(indexRequest, "beauty.index", DurationInMillis.ALWAYS_EXPIRED, new IndexRequestListener());

        SMSSDK.initSDK(this, Data.SMS_APP_ID, Data.SMS_APP_SECRET);

        Bmob.initialize(this, "19fee4b5da44fc283e4c58e9f860ea96");
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

    @Override
    protected void onStart() {
        super.onStart();
        spiceManager.start( this );
    }

    @Override
    protected void onStop() {
        spiceManager.shouldStop();
        super.onStop();
    }

    public void checkItems() {
        //database.upgrade();
        isLaucher = database.hasLauncher();
        if (!isLaucher) {// 没有预存列表数据
            List<Category> launchers = new ArrayList<Category>();

            for (int j = 0; j < Data.Item0.length; j++) {
                Category item = new Category();
                item.setCATEGORY_ICON(Data.Items_icon[j]);
                item.setCATEGORY("ROOT");
                item.setICON(Data.Item0_icon[j]);
                item.setTITLE(Data.Item0[j]);
                item.setURL(Data.Items_url[j]);
                item.setCHOICE(true);
                launchers.add(item);
            }

            database.insertLauncher(launchers);
        }


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
                Data.categoryMap.put("favorite", database.getFavoriteList());
            }
            startMain();
        }

        private void startMain() {
            checkItems();

            Intent intent = new Intent();

            if (isDaily) {
                Category launcher = new Category();
                launcher.setTITLE("每日更新");
                launcher.setURL("daily");
                launcher.setCATEGORY("daily");
                intent.putExtra("launcher", launcher);
                intent.setClass(SplashActivity.this, BeautyActivity.class);
            } else {
                intent.setClass(SplashActivity.this, MainActivity.class);
            }
            startActivity(intent);
            finish();
        }
    }
}









