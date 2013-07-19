package cn.nit.beauty.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;

import com.aliyun.android.oss.OSSClient;
import com.aliyun.android.oss.model.OSSObject;
import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.baidu.mobstat.StatService;
import com.lurencun.service.autoupdate.AppUpdate;
import com.lurencun.service.autoupdate.AppUpdateService;
import com.lurencun.service.autoupdate.internal.SimpleJSONParser;

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
import cn.nit.beauty.utils.Configure;
import cn.nit.beauty.utils.Data;

public class LoadingActivity extends Activity {

    LaucherDataBase database;
    boolean isLaucher;
    boolean isFinish = false;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            checkItems();
        }
    };

    private SharedPreferences settings;
    private OSSClient ossClient;

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

        new Thread(runnable).start();
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

        ArrayList<Category> add_items = new ArrayList<Category>();


        ossClient = new OSSClient();
        ossClient.setAccessId(Data.OSS_ACCESSID);
        ossClient.setAccessKey(Data.OSS_ACCESSKEY);

        OSSObject ossObject = ossClient.getObject(Data.BUCKET_NAME, Data.INDEX_KEY);

        if (ossObject != null) {


            try {

                List<Category> categories = new ArrayList<Category>();

                String json = new String(ossObject.getData(), "UTF-8");

                JSONObject jsonObject = new JSONObject(json);
                JSONArray jsonCategories = jsonObject.getJSONArray("categories");
                for (int i = 0; i < jsonCategories.length(); i++) {
                    JSONObject obj = jsonCategories.getJSONObject(i);
                    Category category = new Category();
                    category.setURL(obj.getString("URL"));
                    category.setCATEGORY(obj.getString("CATEGORY"));
                    category.setCATEGORY_ICON(obj.getInt("CATEGORY_ICON"));
                    category.setTITLE(obj.getString("TITLE"));
                    category.setICON(obj.getInt("ICON"));
                    category.setCHOICE(false);
                    categories.add(category);
                }
                database.insertItems(categories);

                JSONObject mapObj = jsonObject.getJSONObject("roots");
                Iterator iter = mapObj.keys();
                while (iter.hasNext()) {
                    String category = iter.next().toString();
                    JSONArray jsonUrls = mapObj.getJSONArray(category);

                    List<String> urls = new ArrayList<String>();
                    for (int i = 0; i < jsonUrls.length(); i++) {
                        urls.add(jsonUrls.getString(i));
                    }

                    Data.categoryMap.put(category, urls);

                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }


        Intent intent = new Intent();
        intent.setClass(LoadingActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
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


}









