package cn.nit.beauty.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.nit.beauty.R;
import cn.nit.beauty.utils.ActivityUtil;

public class InputActivity extends BaseActivity {

    @InjectView(R.id.content)
    EditText mContent;
    @InjectView(R.id.hint)
    TextView mHint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.input_activity);
        ButterKnife.inject(this);

        Intent intent = getIntent();

        setTitle(intent.getStringExtra("title"));

        mContent.setText(intent.getStringExtra("content"));

        mHint.setText(intent.getStringExtra("hint"));

    }

    public void onSave(View view) {
        String content = mContent.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            ActivityUtil.show(this, "内容不能为空");
        } else {
            Intent data = new Intent();
            data.putExtra("content", content);
            setResult(Activity.RESULT_OK, data);

            finish();
        }
    }
}
