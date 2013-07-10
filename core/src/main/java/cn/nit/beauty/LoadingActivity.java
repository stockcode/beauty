package cn.nit.beauty;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.aliyun.android.oss.OSSClient;
import com.aliyun.android.oss.model.OSSObject;
import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.baidu.mobstat.StatService;
import com.lurencun.service.autoupdate.AppUpdate;
import com.lurencun.service.autoupdate.AppUpdateService;
import com.lurencun.service.autoupdate.internal.SimpleJSONParser;

import cn.nit.beauty.database.LaucherDataBase;
import cn.nit.beauty.model.Category;
import cn.nit.beauty.utils.Configure;
import cn.nit.beauty.utils.Data;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;

public class LoadingActivity extends Activity {
	
	private AppUpdate appUpdate;
	
	private OSSClient ossClient;
	
	LaucherDataBase database;
	boolean isLaucher;
	boolean isFinish=false;
	public HashMap<Integer, String[]> map = new HashMap<Integer,String[]>();
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_loading);
		
		database = new LaucherDataBase(getApplicationContext());
		
		appUpdate = AppUpdateService.getAppUpdate(this);
		
		appUpdate.checkLatestVersion(Data.UPDATE_URL, 
				new SimpleJSONParser());
		
		PushManager.startWork(getApplicationContext(),
				PushConstants.LOGIN_TYPE_API_KEY, Utils.getMetaValue(LoadingActivity.this, "api_key"));

		Configure.inits(LoadingActivity.this);

        new Thread(runnable).start();
	}

    Runnable runnable = new Runnable(){
        @Override
        public void run() {
            checkItems();
        }
    };

	@Override
	protected void onPause() {
		super.onPause();
		appUpdate.callOnPause();
		StatService.onPause(this);
		
	}
	@Override
	protected void onResume() {
		super.onResume();
		appUpdate.callOnResume();	
		StatService.onResume(this);
	}

	public void checkItems() {
		//database.deleteLauncher();
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
				JSONArray jsonArray = new JSONArray(json);
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jsonObject = jsonArray.getJSONObject(i);					
					Category category = new Category();
                    category.setURL(jsonObject.getString("URL"));
					category.setCATEGORY(jsonObject.getString("CATEGORY"));
					category.setCATEGORY_ICON(jsonObject.getInt("CATEGORY_ICON"));
					category.setTITLE(jsonObject.getString("TITLE"));
					category.setICON(jsonObject.getInt("ICON"));
                    category.setCHOICE(false);
					categories.add(category);
				}
				

				database.insertItems(categories);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			
		}
		
		
			

			
        Intent intent = new Intent();
        intent.setClass(LoadingActivity.this, MainActivity.class);
        startActivityForResult(intent, 10);
        overridePendingTransition(R.anim.anim_fromright_toup6,
                R.anim.anim_down_toleft6);
        finish();
	}

@Override
public boolean dispatchKeyEvent(KeyEvent event) {
	if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
		if (event.getAction() == KeyEvent.ACTION_DOWN
				&& event.getRepeatCount() == 0) {
			isFinish=true;
			finish();
			return true;
		}
	}
	return super.dispatchKeyEvent(event);
}
	

}









