package cn.nit.beauty.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import cn.nit.beauty.model.Person;
import com.google.inject.Inject;
import com.google.inject.Singleton;

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
                .putInt("type", person.getType())
                .putString("expiredDate",android.text.format.DateFormat.format("yyyy-MM-dd",person.getExpiredDate()).toString())
                .apply();
    }

    public boolean isLogin() {
        return settings.contains("username");
    }

    public String username(){
        return settings.getString("username", "");
    }

    public String expiredDate(){
        return settings.getString("expiredDate", "");
    }

    public void Logout() {
        settings.edit().remove("username").remove("expiredDate").apply();
    }
}
