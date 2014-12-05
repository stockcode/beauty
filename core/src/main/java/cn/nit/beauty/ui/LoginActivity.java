package cn.nit.beauty.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import cn.bmob.v3.listener.SaveListener;
import cn.nit.beauty.R;
import cn.nit.beauty.Utils;
import cn.nit.beauty.model.Person;
import cn.nit.beauty.proxy.UserProxy;
import cn.nit.beauty.request.LoginRequest;
import cn.nit.beauty.utils.*;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import cn.smssdk.gui.RegisterPage;
import com.google.inject.Inject;
import com.octo.android.robospice.GsonSpringAndroidSpiceService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.tencent.connect.UserInfo;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import org.json.JSONException;
import org.json.JSONObject;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

import java.util.HashMap;

public class LoginActivity extends RoboActivity implements OnClickListener, UserProxy.ILoginListener {

    @Inject
    Authenticator authenticator;

    private SpiceManager spiceManager = new SpiceManager(
            GsonSpringAndroidSpiceService.class);

    @InjectView(R.id.regist)
	private TextView mBtnRegister;

    @InjectView(R.id.login)
	private Button mBtnLogin;

    @InjectView(R.id.sm_progressbar)
    SmoothProgressBar progressbar;

    UserProxy userProxy;

    private EditText username, passwd;

	private boolean mShowMenu = false;

    private Tencent mTencent;

    private Person person;
    IWXAPI api;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        initView();

        api = WXAPIFactory.createWXAPI(this, Data.WEIXIN_APP_ID, false);

        api.registerApp(Data.WEIXIN_APP_ID);

    }
    
    
    public void initView()
    {
        username = (EditText) findViewById(R.id.accounts);
        passwd = (EditText)findViewById(R.id.password);

        mBtnRegister.setPaintFlags(mBtnRegister.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
    	mBtnRegister.setOnClickListener(this);



    	mBtnLogin.setOnClickListener(this);

    }


    public void onWeiXinClick(View v) {
        final SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "beauty";
        api.sendReq(req);
        finish();
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		switch(v.getId())
		{
		case R.id.regist:
			goRegisterActivity();
			break;
		case R.id.login:
			showRequestDialog();
			break;
			default:
		}
	}

    public void onQQClick(View v) {
        mTencent = Tencent.createInstance(Data.QQ_APP_ID, getApplicationContext());
        if (!mTencent.isSessionValid())
        {
            person = new Person();
            mTencent.login(this, "", new BaseUiListener());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        spiceManager.start( this );
    }

    @Override
    protected void onStop() {
        spiceManager.shouldStop();
        super.onStop();
    }

    public void goRegisterActivity()
    {
        RegisterPage registerPage = new RegisterPage();

        registerPage.setRegisterCallback(new EventHandler() {
            public void afterEvent(int event, int result, Object data) {
                // 解析注册结果
                if (result == SMSSDK.RESULT_COMPLETE) {
                    @SuppressWarnings("unchecked")
                    HashMap<String,Object> phoneMap = (HashMap<String, Object>) data;
                    String country = (String) phoneMap.get("country");
                    String phone = (String) phoneMap.get("phone");

                    Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                    intent.putExtra("phone", phone);
                    startActivityForResult(intent, Utils.REGISTER);
                }
            }
        });
        registerPage.show(this);
    }

    SaveListener saveListener = new SaveListener() {
        @Override
        public void onSuccess() {
            DialogFactory.dismiss();

            closeLoginUI(RESULT_OK);
            Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onFailure(int i, String s) {
            DialogFactory.dismiss();

            Toast.makeText(LoginActivity.this, "用户名密码错误，请重新输入", Toast.LENGTH_LONG).show();
        }
    };

	private void showRequestDialog()
	{
        userProxy.setOnLoginListener(this);
        L.i("login begin....");
        progressbar.setVisibility(View.VISIBLE);
        userProxy.login(username.getText().toString().trim(), passwd.getText().toString().trim());
	}

    private void closeLoginUI(int result) {
        setResult(result);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.REGISTER && resultCode == RESULT_OK) {
            closeLoginUI(RESULT_OK);
            Toast.makeText(LoginActivity.this, "注册成功，您的有效期至" + authenticator.getExpiredDate(), Toast.LENGTH_SHORT).show();
        }
    }

    private void dimissProgressbar(){
        if(progressbar!=null&&progressbar.isShown()){
            progressbar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoginSuccess() {
        dimissProgressbar();
        ActivityUtil.show(this, "登录成功。");
        L.i("login sucessed!");
        closeLoginUI(RESULT_OK);
    }

    @Override
    public void onLoginFailure(String msg) {
        dimissProgressbar();
        ActivityUtil.show(this, "用户名密码错误，请重新输入");
        L.i("login failed!"+msg);
    }

    private class BaseUiListener implements IUiListener {

        @Override
        public void onComplete(Object o) {
            JSONObject json = (JSONObject) o;

            try {

                if (json.has("openid")) {
                    person.setUsername(json.get("openid").toString());
                    person.setPassword(json.get("openid").toString());
                }

                if (json.has("nickname")) {

                    mTencent.logout(getApplicationContext());

                    person.setNickname(json.get("nickname").toString());
                    person.setLogintype("QQ");

                    person.login(LoginActivity.this, saveListener);

                    DialogFactory.showDialog(LoginActivity.this, "正在验证账号...");

                } else {
                    UserInfo info = new UserInfo(getApplicationContext(), mTencent.getQQToken());
                    info.getUserInfo(this);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        @Override
        public void onError(UiError e) {
//            showResult("onError:", "code:" + e.errorCode + ", msg:"
//                    + e.errorMessage + ", detail:" + e.errorDetail);
        }
        @Override
        public void onCancel() {
            Toast.makeText(LoginActivity.this, "登录已取消", Toast.LENGTH_SHORT).show();
        }
    }
}
