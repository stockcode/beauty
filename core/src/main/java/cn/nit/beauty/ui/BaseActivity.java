package cn.nit.beauty.ui;

import android.os.Bundle;
import butterknife.ButterKnife;
import com.actionbarsherlock.app.SherlockActivity;
import com.testin.agent.TestinAgent;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by vicky on 2014/12/17.
 */
public class BaseActivity extends SherlockActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
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
    }

    @Override
    protected void onStop() {
        super.onStop();
        TestinAgent.onStop(this);
    }
}
