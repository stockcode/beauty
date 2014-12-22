package cn.nit.beauty.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.nit.beauty.R;
import cn.nit.beauty.utils.ActivityUtil;

public class AboutActivity extends BaseActivity {

    @InjectView(R.id.tvVersion)
    TextView mTvVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.inject(this);

        mTvVersion.setText("版本：" + ActivityUtil.getVersionName(this));
    }
}
