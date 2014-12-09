package cn.nit.beauty.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.update.BmobUpdateAgent;
import cn.nit.beauty.ui.listener.ShakeListener;
import com.actionbarsherlock.app.ActionBar;
import com.baidu.mobstat.StatService;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockActivity;

import java.util.ArrayList;
import java.util.List;

import cn.nit.beauty.R;
import cn.nit.beauty.adapter.DragGridAdapter;
import cn.nit.beauty.database.Category;
import cn.nit.beauty.database.LaucherDataBase;
import cn.nit.beauty.utils.Configure;
import cn.nit.beauty.utils.Data;
import cn.nit.beauty.widget.DragGridView;
import cn.nit.beauty.widget.ScrollLayout;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.main)
public class MainActivity extends RoboSherlockActivity implements ShakeListener.OnShakeListener {

    private ShakeListener mShaker;
    Vibrator vibe;

    LaucherDataBase database;

    @InjectView(R.id.views)
    LinearLayout lst_views;

    @InjectView(R.id.btnUser)
    ImageButton btnUser;

    @InjectView(R.id.btnSettings)
    ImageButton btnSettings;

    @InjectView(R.id.btnSearch)
    ImageButton btnSearch;

    LinearLayout.LayoutParams param;
    List<Category> lstDate = new ArrayList<Category>();// 每一页的数据

    boolean finishCount = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BmobUpdateAgent.setUpdateOnlyWifi(false);

        BmobUpdateAgent.update(this);

        database = new LaucherDataBase(getApplicationContext());

        lstDate = database.getLauncher();

        if (lstDate.size() == 0)
            Toast.makeText(MainActivity.this, "网络有点不给力哦", Toast.LENGTH_LONG).show();

        init();

            lst_views.addView(addGridView());


        vibe = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

        mShaker = new ShakeListener(this);
        mShaker.setOnShakeListener(this);

        initButtons();
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
    }


    public void init() {

        Configure.inits(MainActivity.this);
        param = new LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.FILL_PARENT,
                android.view.ViewGroup.LayoutParams.FILL_PARENT);
    }

    public LinearLayout addGridView() {

        LinearLayout linear = new LinearLayout(MainActivity.this);

        GridView gridView = new GridView(MainActivity.this);

        gridView.setAdapter(new DragGridAdapter(MainActivity.this, gridView, lstDate));
        gridView.setNumColumns(2);
        gridView.setHorizontalSpacing(0);
        gridView.setVerticalSpacing(0);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    final int arg2, long arg3) {

                final Category launcher = lstDate.get(arg2);

                if (launcher.getTITLE().equals("none")) return;

                String text = launcher.getTITLE();
                Intent intent = new Intent();
                if (text != null && !text.equals("none")) {

                    intent.putExtra("launcher", launcher);
                    intent.setClass(MainActivity.this, BeautyActivity.class);
                    startActivity(intent);
                }


            }
        });
        gridView.setSelector(R.drawable.selector_null);

        linear.addView(gridView, param);
        return linear;
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
    protected void onDestroy() {
        super.onDestroy();
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
}
