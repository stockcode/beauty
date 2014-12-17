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
import com.baidu.mobstat.StatService;

import java.util.ArrayList;
import java.util.List;

import cn.nit.beauty.R;
import cn.nit.beauty.database.Category;
import cn.nit.beauty.database.LaucherDataBase;
import cn.nit.beauty.utils.Configure;
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

    LaucherDataBase database;

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

    @InjectView(R.id.btnUser)
    ImageButton btnUser;

    @InjectView(R.id.btnSettings)
    ImageButton btnSettings;

    @InjectView(R.id.btnSearch)
    ImageButton btnSearch;

    @Inject
    UserProxy userProxy;

    String category;

    LinearLayout.LayoutParams param;

    boolean finishCount = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (userProxy.getCurrentUser() != null) {
            TestinAgent.setUserInfo(userProxy.getCurrentUser().getUsername());
        }

        UmengUpdateAgent.update(this);

        database = new LaucherDataBase(getApplicationContext());

        init();

        //    lst_views.addView(addGridView());


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

    private void initButtons() {


        btnUser.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, UserCenterActivity.class);
                startActivity(intent);
            }
        });


        btnSearch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onSearchRequested();
            }
        });

        btnSettings.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });

        ivAsia.setOnClickListener(this);
        ivOccident.setOnClickListener(this);
        ivChina.setOnClickListener(this);
        ivFavorite.setOnClickListener(this);
        ivDaily.setOnClickListener(this);
        ivOrigin.setOnClickListener(this);
    }


    public void init() {

        Configure.inits(MainActivity.this);
        param = new LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.FILL_PARENT,
                android.view.ViewGroup.LayoutParams.FILL_PARENT);
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
    protected void onPause() {
        super.onPause();

        StatService.onPause(this);
        mShaker.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();


        StatService.onResume(this);

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
