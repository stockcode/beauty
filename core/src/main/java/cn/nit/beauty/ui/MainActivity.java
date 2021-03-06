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

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.nit.beauty.BeautyApplication;
import cn.nit.beauty.Utils;
import cn.nit.beauty.entity.User;
import cn.nit.beauty.proxy.UserProxy;
import cn.nit.beauty.ui.listener.ShakeListener;

import cn.nit.beauty.R;
import cn.nit.beauty.utils.Data;
import cn.nit.beauty.utils.L;
import com.testin.agent.TestinAgent;
import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.FeedbackAgent;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengRegistrar;
import com.umeng.update.UmengUpdateAgent;

public class MainActivity extends BaseActivity implements ShakeListener.OnShakeListener {

    private ShakeListener mShaker;
    Vibrator vibe;


    String category;

    User currentUser;

    boolean finishCount = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        UmengUpdateAgent.update(this);

        vibe = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

        mShaker = new ShakeListener(this);
        mShaker.setOnShakeListener(this);

        FeedbackAgent fb = new FeedbackAgent(this);
        // check if the app developer has replied to the feedback or not.
        fb.sync();

        fb.openFeedbackPush();

        Data.DISPLAY_COUNT = Integer.parseInt(MobclickAgent.getConfigParams(this, "display_num"));
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

        currentUser = BeautyApplication.getInstance().getCurrentUser();

        if (currentUser != null) {
            TestinAgent.setUserInfo(currentUser.getUsername());
        }
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

    @OnClick({R.id.ivAsia, R.id.ivFavorite, R.id.ivOrigin, R.id.ivDaily, R.id.ivChina, R.id.ivOccident})
    public void onCategoryClick(View v) {

        Intent intent = new Intent();

        if (v.getId() == R.id.ivFavorite && currentUser == null) {
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
