package cn.nit.beauty.utils;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.nit.beauty.R;


public class Data {
	public static int ItemCount = 8;

    public final static String OSS_URL = "http://oss.aliyuncs.com/nit-photo/";
	public final static String UPDATE_URL = "http://nit-app.oss.aliyuncs.com/beauty/update.json";
    public final static String mbApiKey = "6NxlGErC78G5tGB2aWPblquO";//请替换申请客户端应用时获取的Api Key串

	public final static String INDEX_KEY = "index.ini";
	
	public final static String BUCKET_NAME = "nit-photo";
	
	public final static String OSS_ACCESSID = "tEPWqYKJGESwhRo5";
	
	public final static String OSS_ACCESSKEY = "oUkPZvE5HghfRbkX5wklu6qAiDnMrw";
	
	public static String[] Items_url = { "asia", "occident", "china", "favorite", "vip",
			"more"};
	public static int[] Items_icon = { R.drawable.add_emercy, R.drawable.add_beauty,
			R.drawable.add_constellate, R.drawable.add_emercy,
			R.drawable.add_news, R.drawable.add_ideas, R.drawable.add_fun,
			R.drawable.add_weibo };

	public static String[] Item0 = { "亚洲", "欧美", "中国","我的收藏","VIP专区","更多"};
	public static Map<String, List> categoryMap = new HashMap<String, List>();
	public static int[] Item0_icon = { R.drawable.c_love_channel,R.drawable.c_net_new, R.drawable.c_user
		,R.drawable.c_entertainment,R.drawable.c_financial,R.drawable.c_technoledge};


}