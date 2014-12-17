package cn.nit.beauty.ui;

import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockActivity;
import com.testin.agent.TestinAgent;

/**
 * Created by vicky on 2014/12/17.
 */
public class BaseActivity extends RoboSherlockActivity{

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
