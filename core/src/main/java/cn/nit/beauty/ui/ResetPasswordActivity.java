package cn.nit.beauty.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.bmob.v3.AsyncCustomEndpoints;
import cn.bmob.v3.listener.CloudCodeListener;
import cn.nit.beauty.R;
import cn.nit.beauty.proxy.UserProxy;
import cn.nit.beauty.proxy.UserProxy.ILoginListener;
import cn.nit.beauty.proxy.UserProxy.ISignUpListener;
import cn.nit.beauty.utils.ActivityUtil;
import cn.nit.beauty.utils.L;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class ResetPasswordActivity extends BaseActivity implements OnClickListener, ISignUpListener, ILoginListener {

    @InjectView(R.id.phone)
    TextView phone;

    @InjectView(R.id.password)
    EditText password;

    @InjectView(R.id.confirm_password)
    EditText confirm_password;

    @InjectView(R.id.sm_progressbar)
    SmoothProgressBar progressbar;

    UserProxy userProxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_reset_password);
        ButterKnife.inject(this);

        Intent intent = getIntent();

        phone.setText(intent.getStringExtra("phone"));

        userProxy = new UserProxy(this);
    }

    @Override
    public void onClick(View v) {
        String passwd = password.getText().toString().trim();
        String confirm_passwd = confirm_password.getText().toString().trim();

        if (passwd.equals("")) {
            Toast.makeText(ResetPasswordActivity.this, "密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!passwd.equals(confirm_passwd)) {
            Toast.makeText(ResetPasswordActivity.this, "两次填写的密码不一致", Toast.LENGTH_SHORT).show();
            return;
        }

        progressbar.setVisibility(View.VISIBLE);

        try {
            String cloudCodeName = "resetpasswd";
            JSONObject params = new JSONObject();
            params.put("phone", phone.getText().toString());
            params.put("passwd", passwd);
            AsyncCustomEndpoints cloudCode = new AsyncCustomEndpoints();

            cloudCode.callEndpoint(this, cloudCodeName, params, new CloudCodeListener() {

                //执行成功时调用，返回result对象
                @Override
                public void onSuccess(Object result) {
                    L.i("result = " + result.toString());
                    setResult(RESULT_OK);
                    finish();
                }

                //执行失败时调用
                @Override
                public void onFailure(int arg0, String err) {
                    L.i("BmobException = " + err);
                    ActivityUtil.show(ResetPasswordActivity.this, "重置密码失败");
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSignUpSuccess() {
        userProxy.login(phone.getText().toString(), password.getText().toString());
    }

    @Override
    public void onSignUpFailure(String msg) {
        dimissProgressbar();
        ActivityUtil.show(this, "注册失败。请确认网络连接后再重试。");
        L.i("register failed！");
    }

    private void dimissProgressbar() {
        if (progressbar != null && progressbar.isShown()) {
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
