package cn.nit.beauty.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.alipay.android.app.sdk.AliPay;
import com.lurencun.service.autoupdate.AppUpdate;
import com.lurencun.service.autoupdate.AppUpdateService;
import com.lurencun.service.autoupdate.internal.SimpleJSONParser;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;
import java.text.DecimalFormat;

import cn.nit.beauty.R;
import cn.nit.beauty.Utils;
import cn.nit.beauty.utils.Data;

public class SettingActivity extends PreferenceActivity {
	private static final String[] PREFERENCE_KEYS = {"txtPasswd"};
	private Preference prefCache, prefVersion, prefPay;
    private float cacheSize= 0;
    DecimalFormat df   =   new   DecimalFormat("##0.00");

    private Handler mHandler = new Handler(){

        public void handleMessage(Message msg) {
            Log.e("pay", msg.toString());
        };
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref);
        prefCache = findPreference("clear_cache");
        prefVersion = findPreference("version");
        prefPay = findPreference("pay");


        final File cacheDir = StorageUtils.getCacheDirectory(this);
        float div = 1024*1024;
        cacheSize = Utils.getFolderSize(cacheDir) / div;
        prefCache.setSummary("当前共有缓存" + df.format(cacheSize) + "MB");
        prefCache.setOnPreferenceClickListener(new PrefClickListener());

        prefVersion.setSummary("当前版本为" + getPackageVersion());
        prefVersion.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AppUpdate appUpdate = AppUpdateService.getAppUpdate(SettingActivity.this);

                appUpdate.checkLatestVersion(Data.UPDATE_URL,
                        new SimpleJSONParser());
                return true;
            }
        });

        for (String key: PREFERENCE_KEYS) {
	        setPreferenceSummary(key);
        }

        prefPay.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new Thread() {
                    public void run() {
                        String orderInfo = "partner=\"2088101568358171\"&seller_id=\"alipay-test09@alipay.com\"&out_trade_no=" +
                                "\"0819145412-6177\"&subject=\"《暗黑破坏神3:凯恩之书》\"&body=\"暴雪唯一官方授权" +
                                "中文版!玩家必藏!附赠暗黑精致手绘地图!绝不仅仅是一本暗黑的故事或画册，而是一个" +
                                "栩栩如生的游戏再现。是游戏玩家珍藏的首选。" +
                                "\"&total_fee=\"0.01\"&notify_url=\"http%3A%2F%2Fnotify.msp.hk%2Fnotify.htm\"&servic" +
                                "e=\"mobile.securitypay.pay\"&payment_type=\"1\"&_input_charset=\"utf-8\"&it_b_pay=\"30" +
                                "m\"&show_url=\"m.alipay.com\"&sign=\"lBBK%2F0w5LOajrMrji7DUgEqNjIhQbidR13Gov" +
                                "A5r3TgIbNqv231yC1NksLdw%2Ba3JnfHXoXuet6XNNHtn7VE%2BeCoRO1O%2BR1" +
                                "KugLrQEZMtG5jmJIe2pbjm%2F3kb%2FuGkpG%2BwYQYI51%2BhA3YBbvZHVQBY" +
                                "veBqK%2Bh8mUyb7GM1HxWs9k4%3D\"&sign_type=\"RSA\" ";

                        //获取Alipay对象，构造参数为当前Activity和Handler实例对象
                        AliPay alipay = new AliPay(SettingActivity.this, mHandler);
                        //调用pay方法，将订单信息传入
                        String result = alipay.pay(orderInfo);
                        //处理返回结果
                        Log.e("pay", result);
                    }
                }.start();

                return true;
            }
        });
    }

    public String getPackageVersion() {
        String version = "";
        try {
            PackageManager pm = getApplication().getPackageManager();
            PackageInfo pi = null;
            pi = pm.getPackageInfo(getApplication().getPackageName(), 0);
            version = pi.versionName;
        } catch (Exception e) {
            version = ""; // failed, ignored
        }
        return version;
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
