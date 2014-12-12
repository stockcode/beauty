package cn.nit.beauty.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import android.widget.TextView;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.UpdateListener;
import cn.nit.beauty.R;
import cn.nit.beauty.utils.ActivityUtil;
import com.google.inject.Inject;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.input_activity)
public class InputActivity extends RoboActivity{


	@InjectView(R.id.content)
	private EditText etContent;

	@InjectView(R.id.hint)
	private TextView tvHint;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();

		setTitle(intent.getStringExtra("title"));

		etContent.setText(intent.getStringExtra("content"));

		tvHint.setText(intent.getStringExtra("hint"));

	}

	public void onSave(View view) {
		String content = etContent.getText().toString().trim();
		if(TextUtils.isEmpty(content)){
			ActivityUtil.show(this, "内容不能为空");
		}else{
			Intent data=new Intent();
			data.putExtra("content", content);
			setResult(Activity.RESULT_OK, data);

			finish();
		}
	}
}
