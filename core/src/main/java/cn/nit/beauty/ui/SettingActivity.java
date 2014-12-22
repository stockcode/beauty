package cn.nit.beauty.ui;

import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import cn.bmob.v3.update.BmobUpdateAgent;
import cn.nit.beauty.utils.ActivityUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.nit.beauty.R;
import cn.nit.beauty.Utils;
import cn.nit.beauty.utils.Data;
import com.testin.agent.TestinAgent;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;
import com.umeng.fb.FeedbackAgent;

public class SettingActivity extends PreferenceActivity {
    public static final String TAG = "alipay-sdk";

	private static final String[] PREFERENCE_KEYS = {"txtPasswd"};
	private Preference prefCache, prefVersion, prefPay, feedback, about;
    private float cacheSize= 0;
    DecimalFormat df   =   new   DecimalFormat("##0.00");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref);
        prefCache = findPreference("clear_cache");
        prefVersion = findPreference("version");
        prefPay = findPreference("pay");
        feedback = findPreference("feedback");
        about = findPreference("about");

        about.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent();
                intent.setClass(SettingActivity.this, AboutActivity.class);
                startActivity(intent);
                return true;
            }
        });

        feedback.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                com.umeng.fb.util.Log.LOG = true;
                FeedbackAgent agent = new FeedbackAgent(SettingActivity.this);
                agent.startFeedbackActivity();
                return true;
            }
        });

        final File cacheDir = StorageUtils.getCacheDirectory(this);
        float div = 1024*1024;
        cacheSize = Utils.getFolderSize(cacheDir) / div;
        prefCache.setSummary("当前共有缓存" + df.format(cacheSize) + "MB");
        prefCache.setOnPreferenceClickListener(new PrefClickListener());

        prefVersion.setSummary("当前版本为" + ActivityUtil.getVersionName(this));

        prefVersion.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ActivityUtil.show(SettingActivity.this, "正在检查...");

                UmengUpdateAgent.setUpdateAutoPopup(false);
                UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {

                    @Override
                    public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
                        switch (updateStatus) {
                            case UpdateStatus.Yes: // has update
                                UmengUpdateAgent.showUpdateDialog(SettingActivity.this, updateInfo);
                                break;
                            case UpdateStatus.No: // has no update
                                ActivityUtil.show(SettingActivity.this, "当前为最新版本");
                                break;
                            case UpdateStatus.NoneWifi: // none wifi
                                ActivityUtil.show(SettingActivity.this, "没有wifi连接， 只在wifi下更新");
                                break;
                            case UpdateStatus.Timeout: // time out
                                ActivityUtil.show(SettingActivity.this, "请检查网络");
                                break;
                        }
                    }
                });
                UmengUpdateAgent.forceUpdate(SettingActivity.this);
                return true;
            }
        });

        for (String key: PREFERENCE_KEYS) {
	        setPreferenceSummary(key);
        }
    }

	private class PrefClickListener implements OnPreferenceClickListener {
        public boolean onPreferenceClick(Preference preference) {

            new AlertDialog.Builder(SettingActivity.this)
                    .setTitle("提示")
                    .setMessage("确定要清除所有缓存?")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            ImageLoader.getInstance().clearDiscCache();
                            prefCache.setSummary("当前共有缓存0.00MB");
                            Toast.makeText(SettingActivity.this, "已清理缓存,共释放" + df.format(cacheSize) + "空间!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .show();

            return true;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        TestinAgent.onStop(getApplicationContext());
    }

    @Override
    protected void onStart() {
        super.onStart();
        TestinAgent.onStart(getApplicationContext());
    }

    @Override
    protected void onResume() {  
        super.onResume();  
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
    }  
       
	@Override
    protected void onPause() {  
        super.onPause();  
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);  
    }  
      
    private SharedPreferences.OnSharedPreferenceChangeListener listener =
        new SharedPreferences.OnSharedPreferenceChangeListener() {  
           
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {  
			setPreferenceSummary(sharedPreferences, key);
        }

    };  
    
	private void setPreferenceSummary(String key) {
		setPreferenceSummary(PreferenceManager.getDefaultSharedPreferences(this), key);
	}
	private void setPreferenceSummary(SharedPreferences sharedPreferences, String key) {
		@SuppressWarnings("deprecation")
		Preference pref = findPreference(key);  
		if (pref == null) {
			return;
		}
    	if (key.equals("checkbox_key")) {
            pref.setSummary(sharedPreferences.getBoolean(key, false) ? "チェック" : "未チェック");
    	} else {
            pref.setSummary(sharedPreferences.getString(key, ""));
    	}
	}
}
