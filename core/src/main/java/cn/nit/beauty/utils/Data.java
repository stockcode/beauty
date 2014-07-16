package cn.nit.beauty.utils;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.util.Log;
import cn.nit.beauty.R;


public class Data {
	public static int ItemCount = 8;

    public final static String OSS_URL = "http://beauty-photo.oss.aliyuncs.com/";
	public final static String UPDATE_URL = "http://beauty-app.oss.aliyuncs.com/beauty/update.json";
    public final static String mbApiKey = "6NxlGErC78G5tGB2aWPblquO";//请替换申请客户端应用时获取的Api Key串

    public final static int PAGE_COUNT = 10;
	public final static String INDEX_KEY = "index.json";
	
	public static String[] Items_url = { "animal", "car", "human", "landscape"};
	public static int[] Items_icon = { R.drawable.animal, R.drawable.car,
			R.drawable.human, R.drawable.landscape,
			R.drawable.daily, R.drawable.favorite, R.drawable.add_fun,
			R.drawable.add_weibo };

	public static String[] Item0 = { "动物馆", "汽车馆", "人物馆","风景馆"};
	public static Map<String, List<String>> categoryMap = new HashMap<String, List<String>>();
	public static int[] Item0_icon = { R.drawable.c_love_channel,R.drawable.c_net_new, R.drawable.c_user
		,R.drawable.c_entertainment,R.drawable.c_financial,R.drawable.c_technoledge};


    public static String getRandomKey() {
        Random rd = new Random();
        Object[] lists = categoryMap.values().toArray();
        if (lists.length == 0) return "";

        List<String> list = (List<String>) lists[rd.nextInt(lists.length)];

        while (list.size() == 0) {
            list = (List<String>) lists[rd.nextInt(lists.length)];
        }

        int index = rd.nextInt(list.size());
        return  list.get(index);
    }
}