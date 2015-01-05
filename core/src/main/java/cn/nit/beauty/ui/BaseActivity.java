package cn.nit.beauty.ui;

import android.os.Bundle;
import butterknife.ButterKnife;
import cn.nit.beauty.BeautyApplication;
import com.actionbarsherlock.app.SherlockActivity;
import com.octo.android.robospice.GsonSpringAndroidSpiceService;
import com.octo.android.robospice.SpiceManager;
import com.testin.agent.TestinAgent;
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.PushAgent;

/**
 * Created by vicky on 2014/12/17.
 */
public class BaseActivity extends SherlockActivity{

    private SpiceManager spiceManager = new SpiceManager(
            GsonSpringAndroidSpiceService.class);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PushAgent.getInstance(this).onAppStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);       //统计时长
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onStart() {
        spiceManager.start(this);

        super.onStart();
        TestinAgent.onStart(this);
    }

    @Override
    protected void onStop() {
        spiceManager.shouldStop();
        super.onStop();
        TestinAgent.onStop(this);
    }

    protected SpiceManager getSpiceManager() {
        return spiceManager;
    }
}
