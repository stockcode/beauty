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

import cn.nit.beauty.ui.listener.ShakeListener;
import com.actionbarsherlock.app.ActionBar;
import com.baidu.mobstat.StatService;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockActivity;
import com.lurencun.service.autoupdate.AppUpdate;
import com.lurencun.service.autoupdate.AppUpdateService;

import java.util.ArrayList;
import java.util.List;

import cn.nit.beauty.R;
import cn.nit.beauty.adapter.DragGridAdapter;
import cn.nit.beauty.bus.LauncherChangeEvent;
import cn.nit.beauty.database.Category;
import cn.nit.beauty.database.LaucherDataBase;
import cn.nit.beauty.utils.Configure;
import cn.nit.beauty.utils.Data;
import cn.nit.beauty.widget.DragGridView;
import cn.nit.beauty.widget.ScrollLayout;
import de.greenrobot.event.EventBus;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.main)
public class MainActivity extends RoboSherlockActivity implements ShakeListener.OnShakeListener, ActionBar.TabListener {

    private ShakeListener mShaker;
    Vibrator vibe;

    public static final int PAGE_SIZE = 8;
    public int PAGE_COUNT = 2, PAGE_CURRENT = 0;
    LaucherDataBase database;
    private AppUpdate appUpdate;

    Category map_none = new Category();
    //Category map_null = new Category();
    List<Category> addDate = new ArrayList<Category>();// 每一页的数据
    /**
     * GridView.
     */

    @InjectView(R.id.views)
    ScrollLayout lst_views;

    @InjectView(R.id.btnUser)
    ImageButton btnUser;

    @InjectView(R.id.btnSettings)
    ImageButton btnSettings;

    @InjectView(R.id.btnSearch)
    ImageButton btnSearch;

    LinearLayout.LayoutParams param;
    ArrayList<DragGridView> gridviews = new ArrayList<DragGridView>();
    ArrayList<List<Category>> lists = new ArrayList<List<Category>>();// 全部数据的集合集lists.size()==countpage;
    List<Category> lstDate = new ArrayList<Category>();// 每一页的数据

    boolean isClean = false;
    Vibrator vibrator;

    boolean finishCount = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appUpdate = AppUpdateService.getAppUpdate(this);
//        appUpdate.checkLatestVersionSilent(Data.UPDATE_URL,
//                new SimpleJSONParser());

        database = new LaucherDataBase(getApplicationContext());

        lstDate = database.getLauncher();
        addDate = lstDate;

        map_none.setTITLE("none");

        if (lstDate.size() == 0)
            Toast.makeText(MainActivity.this, "网络有点不给力哦", Toast.LENGTH_LONG).show();
        init();
        initData();

        for (int i = 0; i < Configure.countPages; i++) {
            lst_views.addView(addGridView(i));
        }

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
                if (onSearchRequested())
                    Log.e("search", "true");
                else
                    Log.e("search", "false");
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

    public void initData() {
        Configure.countPages = (int) Math.ceil(lstDate.size()
                / (float) PAGE_SIZE);

        if (Configure.countPages == 0) return;
        lists = new ArrayList<List<Category>>();
        for (int i = 0; i < Configure.countPages; i++) {
            lists.add(new ArrayList<Category>());
            for (int j = PAGE_SIZE * i; j < (PAGE_SIZE * (i + 1) > lstDate
                    .size() ? lstDate.size() : PAGE_SIZE * (i + 1)); j++)
                lists.get(i).add(lstDate.get(j));
        }

        for (int i = lists.get(Configure.countPages - 1).size(); i < PAGE_SIZE; i++) {
            lists.get(Configure.countPages - 1).add(map_none);
        }
    }

    public void CleanItems() {
        lstDate = new ArrayList<Category>();
        for (int i = 0; i < lists.size(); i++) {
            for (int j = 0; j < lists.get(i).size(); j++) {
                if (lists.get(i).get(j).getTITLE() != null
                        && !lists.get(i).get(j).getTITLE().equals("none")) {
                    lstDate.add(lists.get(i).get(j));
                }
            }
        }
        initData();
        lst_views.removeAllViews();
        gridviews = new ArrayList<DragGridView>();
        for (int i = 0; i < Configure.countPages; i++) {
            lst_views.addView(addGridView(i));
        }
        isClean = false;
        lst_views.snapToScreen(0);
    }



    public int getFristNonePosition(List<Category> array) {
        for (int i = 0; i < array.size(); i++) {
            Category category = array.get(i);
            if (category.getTITLE().equals("none")) {
                return i;
            }
        }
        return -1;
    }

    public LinearLayout addGridView(int i) {
        // if (lists.get(i).size() < PAGE_SIZE)
        // lists.get(i).add(null);

        LinearLayout linear = new LinearLayout(MainActivity.this);

        DragGridView gridView = new DragGridView(MainActivity.this);
        gridView.setAdapter(new DragGridAdapter(MainActivity.this, gridView, lists
                .get(i)));
        gridView.setNumColumns(2);
        gridView.setHorizontalSpacing(0);
        gridView.setVerticalSpacing(0);
        final int ii = i;
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    final int arg2, long arg3) {

                final Category launcher = lists.get(ii).get(arg2);

                if (launcher.getTITLE().equals("none")) return;

                String text = launcher.getTITLE();
                Intent intent = new Intent();
                if (text != null && !text.equals("none")) {

                    intent.putExtra("launcher", launcher);
                    intent.setClass(MainActivity.this, BeautyActivity.class);
                    startActivity(intent);
                    //overridePendingTransition(R.anim.anim_fromright_toup6, R.anim.anim_down_toleft6);
                }


            }
        });
        gridView.setSelector(R.drawable.selector_null);

        gridviews.add(gridView);
        linear.addView(gridView, param);
        return linear;
    }

    public void onEvent(LauncherChangeEvent event) {


        Configure.countPages = lists.size();

        List<Category> launchers = event.getLaunchers();
        for(Category launcher : launchers) {



            if (launcher.getCHOICE()) { //添加launcher
                if (getFristNonePosition(lists.get(lists.size() - 1)) > 0) {
                    lists.get(lists.size() - 1).set(getFristNonePosition(lists.get(lists.size() - 1)), launcher);
                    ((DragGridAdapter) ((gridviews.get(gridviews.size() - 1)).getAdapter())).notifyDataSetChanged();
                } else {//当前最后页面已经填满
                    lists.add(new ArrayList<Category>());
                    lists.get(lists.size() - 1).add(launcher);
                    for (int i = 1; i < PAGE_SIZE; i++)
                        lists.get(lists.size() - 1).add(map_none);
                    lst_views.addView(addGridView(Configure.countPages));
                    Configure.countPages++;
                }
            } else { //删除launcher
                for (int i = 0; i < lists.size(); i++) {
                    if (lists.get(i).remove(launcher)) {
                        lists.get(Configure.curentPage).add(map_none);
                        ((DragGridAdapter) ((gridviews.get(i)).getAdapter())).notifyDataSetChanged();
                    }
                }
            }
        }

        EventBus.getDefault().removeStickyEvent(event);
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

        // Always unregister when an object no longer should be on the bus.
        EventBus.getDefault().unregister(this);

        PAGE_COUNT = Configure.countPages;
        PAGE_CURRENT = Configure.curentPage;
        StatService.onPause(this);
        appUpdate.callOnPause();
        mShaker.pause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Configure.countPages = PAGE_COUNT;
        Configure.curentPage = PAGE_CURRENT;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Register ourselves so that we can provide the initial value.
        EventBus.getDefault().registerSticky(this);

        StatService.onResume(this);

        appUpdate.callOnResume();
        mShaker.resume();
    }

    @Override
    public void onShake() {
        vibe.vibrate(100);

        String objectkey = Data.getRandomKey();

        if (!objectkey.equals("")) {
            Intent intent = new Intent(MainActivity.this,
                    ImageListActivity.class);
            intent.putExtra("objectKey", objectkey.split(":")[0] + "smallthumb/");
            startActivity(intent);
        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }
}
