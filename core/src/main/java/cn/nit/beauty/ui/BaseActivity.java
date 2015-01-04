package cn.nit.beauty.ui;

import android.os.Bundle;
import butterknife.ButterKnife;
import cn.nit.beauty.BeautyApplication;
import com.actionbarsherlock.app.SherlockActivity;
import com.octo.android.robospice.SpiceManager;
import com.testin.agent.TestinAgent;
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.PushAgent;

/**
 * Created by vicky on 2014/12/17.
 */
public class BaseActivity extends SherlockActivity{

    protected SpiceManager spiceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        spiceManager = BeautyApplication.getInstance().getSpiceManager();

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
        super.onStart();
        TestinAgent.onStart(this);

        if (!spiceManager.isStarted())  spiceManager.start(this);
    }

    @Override
    protected void onStop() {
        if (spiceManager.isStarted())  spiceManager.shouldStop();
        super.onStop();
        TestinAgent.onStop(this);
    }
}
