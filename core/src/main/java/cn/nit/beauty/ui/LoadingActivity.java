package cn.nit.beauty.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.widget.Toast;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.baidu.mobstat.StatService;
import com.lurencun.service.autoupdate.AppUpdate;
import com.lurencun.service.autoupdate.AppUpdateService;
import com.lurencun.service.autoupdate.internal.SimpleJSONParser;
import com.octo.android.robospice.GsonSpringAndroidSpiceService;
import com.octo.android.robospice.JacksonSpringAndroidSpiceService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.UncachedSpiceService;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.nit.beauty.R;
import cn.nit.beauty.Utils;
import cn.nit.beauty.database.LaucherDataBase;
import cn.nit.beauty.database.Category;
import cn.nit.beauty.model.Index;
import cn.nit.beauty.request.IndexRequest;
import cn.nit.beauty.utils.Configure;
import cn.nit.beauty.utils.Data;

public class LoadingActivity extends Activity {

    private SpiceManager spiceManager = new SpiceManager(
            GsonSpringAndroidSpiceService.class);

    LaucherDataBase database;
    boolean isLaucher;
    boolean isFinish = false;

    private SharedPreferences settings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_loading);

        database = new LaucherDataBase(getApplicationContext());

        PushManager.startWork(getApplicationContext(),
                PushConstants.LOGIN_TYPE_API_KEY, Utils.getMetaValue(LoadingActivity.this, "api_key"));

        Configure.inits(LoadingActivity.this);

        settings = PreferenceManager.getDefaultSharedPreferences(this);
        Configure.save(settings);

        IndexRequest indexRequest = new IndexRequest(Data.OSS_URL + Data.INDEX_KEY);
        spiceManager.execute(indexRequest, "beauty.index", DurationInMillis.ALWAYS_EXPIRED, new IndexRequestListener());
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


    private class IndexRequestListener implements RequestListener<Index> {
        @Override
        public void onRequestFailure(SpiceException e) {
            Toast.makeText( LoadingActivity.this, "网络不给力,错误: " + e.getMessage(), Toast.LENGTH_LONG ).show();
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
            intent.setClass(LoadingActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}









