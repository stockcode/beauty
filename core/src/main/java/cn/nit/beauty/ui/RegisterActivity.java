package cn.nit.beauty.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import cn.bmob.v3.listener.SaveListener;
import cn.nit.beauty.R;
import cn.nit.beauty.entity.User;
import cn.nit.beauty.model.Person;
import cn.nit.beauty.proxy.UserProxy;
import cn.nit.beauty.request.RegisterRequest;
import cn.nit.beauty.utils.ActivityUtil;
import cn.nit.beauty.utils.Authenticator;
import cn.nit.beauty.utils.DialogFactory;
import cn.nit.beauty.utils.L;
import com.google.inject.Inject;
import com.octo.android.robospice.GsonSpringAndroidSpiceService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import cn.nit.beauty.proxy.UserProxy.ISignUpListener;
import cn.nit.beauty.proxy.UserProxy.ILoginListener;

@ContentView(R.layout.register)
public class RegisterActivity extends RoboActivity implements OnClickListener, ISignUpListener, ILoginListener {

    @InjectView(R.id.nickname)
    private EditText nickname;

    @InjectView(R.id.phone)
    private TextView phone;

    @InjectView(R.id.password)
    private EditText password;

    @InjectView(R.id.sm_progressbar)
    SmoothProgressBar progressbar;

    UserProxy userProxy;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        phone.setText(intent.getStringExtra("phone"));

        userProxy = new UserProxy(this);
	}

	@Override
	public void onClick(View v) {
        final User user = new User();
        user.setUsername(phone.getText().toString());
        user.setPhone(phone.getText().toString());
        user.setNickname(nickname.getText().toString());
        user.setPassword(password.getText().toString());
        user.setLogintype("beauty");

        if (user.getNickname().equals("")) {
            Toast.makeText(RegisterActivity.this, "昵称不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        userProxy.setOnSignUpListener(this);
        L.i("register begin....");
        progressbar.setVisibility(View.VISIBLE);

        userProxy.signUp(user);
    }

    @Override
    public void onSignUpSuccess() {
        userProxy.login(phone.getText().toString(), password.getText().toString());
        ActivityUtil.show(this, "注册成功");
    }

    @Override
    public void onSignUpFailure(String msg) {
        dimissProgressbar();
        ActivityUtil.show(this, "注册失败。请确认网络连接后再重试。");
        L.i("register failed！");
    }

    private void dimissProgressbar(){
        if(progressbar!=null&&progressbar.isShown()){
            progressbar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoginSuccess() {
        dimissProgressbar();
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onLoginFailure(String msg) {
        dimissProgressbar();
        ActivityUtil.show(this, "登录失败。请确认网络连接后再重试。");
        L.i("login failed！");
    }
}
