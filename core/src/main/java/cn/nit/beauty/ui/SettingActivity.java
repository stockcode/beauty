package cn.nit.beauty.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

import cn.nit.beauty.alipay.Rsa;
import com.alipay.android.app.sdk.AliPay;
import com.lurencun.service.autoupdate.AppUpdate;
import com.lurencun.service.autoupdate.AppUpdateService;
import com.lurencun.service.autoupdate.internal.SimpleJSONParser;
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

public class SettingActivity extends PreferenceActivity {
    public static final String TAG = "alipay-sdk";

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
                try {
                    Log.i("SettingActivity", "Paying");
                    String info = getNewOrderInfo();
                    String sign = Rsa.sign(info, Data.PRIVATE);
                    sign = URLEncoder.encode(sign);
                    info += "&sign=\"" + sign + "\"&" + getSignType();
                    Log.i("SettingActivity", "start pay");
                    // start the pay.
                    Log.i(TAG, "info = " + info);

                    final String orderInfo = info;
                    new Thread() {
                        public void run() {
                            AliPay alipay = new AliPay(SettingActivity.this, mHandler);

                            //设置为沙箱模式，不设置默认为线上环境
                            //alipay.setSandBox(true);

                            String result = alipay.pay(orderInfo);

                            Log.i(TAG, "result = " + result);
                            Message msg = new Message();
                            msg.what = 1;
                            msg.obj = result;
                            mHandler.sendMessage(msg);
                        }
                    }.start();

                } catch (Exception ex) {
                    ex.printStackTrace();
                    Toast.makeText(SettingActivity.this, ex.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }

                return true;
            }
        });
    }

    private String getNewOrderInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("partner=\"");
        sb.append(Data.DEFAULT_PARTNER);
        sb.append("\"&out_trade_no=\"");
        sb.append(getOutTradeNo());
        sb.append("\"&subject=\"");
        sb.append("丽图");
        sb.append("\"&body=\"");
        sb.append("丽图包月");
        sb.append("\"&total_fee=\"");
        sb.append("0.01");
        sb.append("\"&notify_url=\"");

        // 网址需要做URL编码
        sb.append(URLEncoder.encode("http://notify.java.jpxx.org/index.jsp"));
        sb.append("\"&service=\"mobile.securitypay.pay");
        sb.append("\"&_input_charset=\"UTF-8");
        sb.append("\"&return_url=\"");
        sb.append(URLEncoder.encode("http://m.alipay.com"));
        sb.append("\"&payment_type=\"1");
        sb.append("\"&seller_id=\"");
        sb.append(Data.DEFAULT_SELLER);

        // 如果show_url值为空，可不传
        // sb.append("\"&show_url=\"");
        sb.append("\"&it_b_pay=\"1m");
        sb.append("\"");

        return new String(sb);
    }

    private String getOutTradeNo() {
        SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss");
        Date date = new Date();
        String key = format.format(date);

        java.util.Random r = new java.util.Random();
        key += r.nextInt();
        key = key.substring(0, 15);
        Log.d(TAG, "outTradeNo: " + key);
        return key;
    }

    private String getSignType() {
        return "sign_type=\"RSA\"";
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
