package cn.nit.beauty.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by vicky on 2014/12/10.
 */
public class DateUtil {
    static DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    public static String getStandardDate(String timeStr) {

        StringBuffer sb = new StringBuffer();

        long t = 0;
        try {
            t = df.parse(timeStr).getTime();

            long time = System.currentTimeMillis() - t;
            long mill = (long) Math.ceil(time /1000);//秒前

            long minute = (long) Math.ceil(time/60/1000.0f);// 分钟前

            long hour = (long) Math.ceil(time/60/60/1000.0f);// 小时

            long day = (long) Math.ceil(time/24/60/60/1000.0f);// 天前

            if (day - 1 > 0) {
                sb.append(day + "天");
            } else if (hour - 1 > 0) {
                if (hour >= 24) {
                    sb.append("1天");
                } else {
                    sb.append(hour + "小时");
                }
            } else if (minute - 1 > 0) {
                if (minute == 60) {
                    sb.append("1小时");
                } else {
                    sb.append(minute + "分钟");
                }
            } else if (mill - 1 > 0) {
                if (mill == 60) {
                    sb.append("1分钟");
                } else {
                    sb.append(mill + "秒");
                }
            } else {
                sb.append("刚刚");
            }
            if (!sb.toString().equals("刚刚")) {
                sb.append("前");
            }
            return sb.toString();

        } catch (ParseException e) {
            L.e(e.getMessage());
            return  "刚刚";
        }

    }
}
