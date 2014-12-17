package cn.nit.beauty.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

import cn.nit.beauty.Utils;
import cn.nit.beauty.proxy.UserProxy;
import cn.nit.beauty.ui.listener.ShakeListener;

import cn.nit.beauty.R;
import cn.nit.beauty.utils.Data;
import com.google.inject.Inject;
import com.testin.agent.TestinAgent;
import com.umeng.fb.FeedbackAgent;
import com.umeng.message.PushAgent;
import com.umeng.update.UmengUpdateAgent;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_main)
public class MainActivity extends BaseActivity implements ShakeListener.OnShakeListener, OnClickListener {

    private ShakeListener mShaker;
    Vibrator vibe;

    @InjectView(R.id.ivAsia)
    ImageView ivAsia;

    @InjectView(R.id.ivOccident)
    ImageView ivOccident;

    @InjectView(R.id.ivChina)
    ImageView ivChina;

    @InjectView(R.id.ivFavorite)
    ImageView ivFavorite;

    @InjectView(R.id.ivDaily)
    ImageView ivDaily;

    @InjectView(R.id.ivOrigin)
    ImageView ivOrigin;

    @Inject
    UserProxy userProxy;

    String category;

    boolean finishCount = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (userProxy.getCurrentUser() != null) {
            TestinAgent.setUserInfo(userProxy.getCurrentUser().getUsername());
        }

        UmengUpdateAgent.update(this);

        init();


        vibe = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

        mShaker = new ShakeListener(this);
        mShaker.setOnShakeListener(this);

        initButtons();

        FeedbackAgent fb = new FeedbackAgent(this);
        // check if the app developer has replied to the feedback or not.
        fb.sync();

        fb.openFeedbackPush();
        PushAgent.getInstance(this).enable();
    }

    public void onUser (View v) {
        Intent intent = new Intent(MainActivity.this, UserCenterActivity.class);
        startActivity(intent);
    }

    public void onSearch(View v) {
        onSearchRequested();
    }

    public void onSetting(View v) {
        Intent intent = new Intent(MainActivity.this, SettingActivity.class);
        startActivity(intent);
    }
    private void initButtons() {


        ivAsia.setOnClickListener(this);
        ivOccident.setOnClickListener(this);
        ivChina.setOnClickListener(this);
        ivFavorite.setOnClickListener(this);
        ivDaily.setOnClickListener(this);
        ivOrigin.setOnClickListener(this);
    }


    public void init() {
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (event.getAction() == KeyEvent.ACTION_DOWN
                    && event.getRepeatCount() == 0) {
                if (finishCount) {

                        finish();
                        return true;

                } else {
                    finishCount = true;
                    Toast.makeText(MainActivity.this, "再按一次返回键退出", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finishCount = false;
                        }
                    }, 2000);
                }
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onPause() {
        super.onPause();

        mShaker.pause();
    }

    @Override
    public void onResume() {
        super.onResume();

        mShaker.resume();
    }

    @Override
    public void onShake() {
        vibe.vibrate(100);

        String objectkey = Data.getRandomKey();

        if (!objectkey.equals("")) {
            String[] strs = objectkey.split(":");

            Intent intent = new Intent(MainActivity.this,
                    ImageListActivity.class);
            intent.putExtra("objectKey", strs[0] + "smallthumb/");
            intent.putExtra("objectId", strs[2]);
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.FAVORITE && resultCode == RESULT_OK) {
            Intent intent = new Intent();
            intent.putExtra("category", "favorite");
            intent.setClass(MainActivity.this, BeautyActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onClick(View v) {

        Intent intent = new Intent();

        if (v.getId() == R.id.ivFavorite && userProxy.getCurrentUser() == null) {
            intent.setClass(MainActivity.this, LoginActivity.class);
            startActivityForResult(intent, Utils.FAVORITE);
            Toast.makeText(MainActivity.this, "查看我的最爱请先登录", Toast.LENGTH_SHORT).show();
        } else {
            intent.putExtra("category", v.getTag().toString());
            intent.setClass(MainActivity.this, BeautyActivity.class);
            startActivity(intent);
        }
    }
}
