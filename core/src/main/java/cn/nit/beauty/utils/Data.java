package cn.nit.beauty.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class Data {
    public static final String TAG = Data.class.getSimpleName();
    public final static String AUTH_URL = "http://www.matesapp.cn:8080/beauty-ajax/api";
    public final static String OSS_URL = "http://beauty-photo.oss.aliyuncs.com/";
    public final static String mbApiKey = "6NxlGErC78G5tGB2aWPblquO";//请替换申请客户端应用时获取的Api Key串
    public final static String QQ_APP_ID = "1102386164";
    public final static String WEIXIN_APP_ID = "wx337be4a60c68e521";
    public final static String WEIXIN_APP_SECRET = "a912aeab7224fcbcbdbd5b71b60434a5";
    public final static String SMS_APP_ID = "455ec0cd2f3f";
    public final static String SMS_APP_SECRET = "7669818cd03556600b7b34b0d472521c";
    public final static String BMOB_APP_ID = "19fee4b5da44fc283e4c58e9f860ea96";
    //合作身份者id，以2088开头的16位纯数字
    public static final String DEFAULT_PARTNER = "2088511310355822";
    //收款支付宝账号
    public static final String DEFAULT_SELLER = "hurryrunner@aliyun.com";
    //商户私钥，自助生成
    public static final String PRIVATE = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBALKmWWzoYV/xaRBbOLoQdpi03jXev3dNd5sYqwsw/Q3ZEP8UoK2ZY2AIdUEkcWVQdu2kPKk5ONhgJXeJy3SZNB5snQGhW3Vx937nZPWiU9rc7BcoHtDhSSeTKyER+NLr9FgxrOynn6MBCkINkOM5J1rsWbdUoD16Uo44OMPQb1EhAgMBAAECgYEAjuNibytGhviNshlkO93jHay8dpXcyk1ZtuF6HQ9nt2XApGUZRwCCbVMWha3iTMiY9uX/5tHv15C/Juc9pdrLxZpxdgkeFnOF6SVVUaFk8jnsz4C1IdhaFzRbhJ8FNyEmT/oCCn5vLXbtyeYMokdaQj4/OQWv2S0qRAZeJ4utNYECQQDpRpD0CmX8c1yf/jcAQhdB+pOkZIf/YfVeboKukqR0BXbf6nvo79jFShT45k5+y+wyNaMWZjjB0cdxjXh1f+xZAkEAxA2Fu7QqOWoDELNHduTENOLV9K1yg75PmroYCCQSy4R8TQgrS24fZZvRIQURFu6kftgepqNvs2C0GORn+afSCQJAYN7T7POwfFAvo6T+lBXd8KEs1HSG4S99pFRB4lq0/hUS01NdV1Lacrsb0GxbJl5qXENX0UJryVji+K2l2y1fKQJAPJuBkDluJDmloPALU7H9BexqAC3ujNO0gDyvWTTtqoTWeGniGHt+sUauK+sJEXHM4HmnYBR+X5Gxm1Bg5EkBIQJBALI3+Zocdd9S/+72Ydr9d9hjr5p0n4GVZkipzr/UiUgkLOz9agbOYTELNIZJUtUECSP2jj4mYsu4JTuqmKF+5IM=";
    public static final String PUBLIC = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnxj/9qwVfgoUh/y2W89L6BkRAFljhNhgPdyPuBV64bfQNN1PjbCzkIM6qRdKBoLPXmKKMiFYnkd6rAoprih3/PrQEB/VsW8OoM8fxn67UDYuyBTqA23MML9q1+ilIZwBC2AQ2UBVOrFXfFl75p6/B5KsiNG9zpgmLCUYuLkxpLQIDAQAB";
    public final static int PAGE_COUNT = 10;
	public final static String INDEX_KEY = "index.json";
    public static int DISPLAY_COUNT = 12;

    public static Map<String, List<String>> categoryMap = new HashMap<String, List<String>>();
    private static Map<String,String> titleMap = new HashMap<String, String>(){{
        put("asia", "岛国风情");
        put("occident", "欧美情调");
        put("china", "中国气质");
        put("favorite", "我的最爱");
        put("daily", "每日更新");
        put("origin", "原创自拍");
    }};

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

    public static boolean containFav(String objectId) {
        for(String url : categoryMap.get("favorite")) {
            if (url.contains(objectId)) return true;
        }
        return false;
    }


    public static void removeFav(String url) {
        categoryMap.get("favorite").remove(url);
    }

    public static void addFav(String url) {
        categoryMap.get("favorite").add(url);
    }

    public static String getTitle(String category) {
        return titleMap.get(category);
    }
}