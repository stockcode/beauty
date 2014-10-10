package cn.nit.beauty.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import cn.nit.beauty.model.Person;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.tencent.tauth.Tencent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by vicky on 2014/7/29.
 */
@Singleton
public class Authenticator {
    private SharedPreferences settings;


    @Inject
    public Authenticator(Context context) {
        settings = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void Save(Person person) {
        settings.edit().putString("pkid", person.getPkid())
                .putString("username", person.getUsername())
                .putString("nickname", person.getNickname())
                .putString("logintype", person.getLogintype())
                .putInt("type", person.getType())
                .putString("expiredDate",android.text.format.DateFormat.format("yyyy-MM-dd",person.getExpiredDate()).toString())
                .apply();
    }

    public boolean isLogin() {
        return settings.contains("username");
    }

    public String getUsername(){
        return settings.getString("username", "");
    }

    public String getNickname(){
        return settings.getString("nickname", "");
    }

    public String getLogintype(){
        return settings.getString("logintype", "beauty");
    }

    public String getExpiredDate(){
        return settings.getString("expiredDate", "");
    }

    public String getId() {
        return settings.getString("pkid", "");
    }

    public boolean hasDiscount() {
        return settings.getInt("type", 0) == 0;
    }

    public void Logout() {
        settings.edit().remove("pkid").remove("username").remove("type").remove("expiredDate").apply();
    }

    public void Upgrade(String totalfee) {

        try {
            Date expiredDate = new SimpleDateFormat("yyyy-MM-dd").parse(settings.getString("expiredDate", ""));
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(expiredDate);

            if (totalfee.equals("0.1")) {
                calendar.add(Calendar.MONTH, 1);
                settings.edit().putInt("type", 1)
                        .putString("expiredDate",android.text.format.DateFormat.format("yyyy-MM-dd",calendar.getTime()).toString())
                        .apply();
            } else if (totalfee.equals("10")) {
                calendar.add(Calendar.MONTH, 1);
                settings.edit()
                        .putString("expiredDate",android.text.format.DateFormat.format("yyyy-MM-dd",calendar.getTime()).toString())
                        .apply();
            } else if (totalfee.equals("100")) {
                calendar.add(Calendar.YEAR, 1);
                settings.edit()
                        .putString("expiredDate",android.text.format.DateFormat.format("yyyy-MM-dd",calendar.getTime()).toString())
                        .apply();
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public boolean hasExpired() {
        try {
            Date expiredDate = new SimpleDateFormat("yyyy-MM-dd").parse(settings.getString("expiredDate", ""));
            return expiredDate.before(new Date());

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return true;
    }
}
