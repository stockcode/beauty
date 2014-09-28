package cn.nit.beauty.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import cn.nit.beauty.R;
import cn.nit.beauty.Utils;
import cn.nit.beauty.model.Person;
import cn.nit.beauty.request.LoginRequest;
import cn.nit.beauty.utils.Authenticator;
import cn.nit.beauty.utils.Configure;
import cn.nit.beauty.utils.Data;
import cn.nit.beauty.utils.DialogFactory;
import com.google.inject.Inject;
import com.octo.android.robospice.GsonSpringAndroidSpiceService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.tencent.connect.UserInfo;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import org.json.JSONException;
import org.json.JSONObject;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

public class LoginActivity extends RoboActivity implements OnClickListener{

    @Inject
    Authenticator authenticator;

    @InjectView(R.id.btnQQ)
    ImageButton btnQQ;

    @InjectView(R.id.btnWeiXin)
    ImageButton btnWeiXin;

    private SpiceManager spiceManager = new SpiceManager(
            GsonSpringAndroidSpiceService.class);

    @InjectView(R.id.regist)
	private TextView mBtnRegister;

    @InjectView(R.id.login)
	private Button mBtnLogin;

    private EditText username, passwd;

	private boolean mShowMenu = false;

    private Tencent mTencent;

    private Person person;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        initView();

    }
    
    
    public void initView()
    {
        username = (EditText) findViewById(R.id.accounts);
        passwd = (EditText)findViewById(R.id.password);

        mBtnRegister.setPaintFlags(mBtnRegister.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
    	mBtnRegister.setOnClickListener(this);



    	mBtnLogin.setOnClickListener(this);

        btnQQ.setOnClickListener(this);
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
        case R.id.btnQQ:
            showQQDialog();
            break;
			default:
		}
	}

    private void showQQDialog() {
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
    	Intent intent = new Intent();
    	intent.setClass(this, RegisterActivity.class);

        startActivityForResult(intent, Utils.REGISTER);
    }
	   

	private Dialog mDialog = null;
	private void showRequestDialog()
	{
        Person person = new Person();
        person.setUsername(username.getText().toString());
        person.setPasswd(passwd.getText().toString());
        person.setLogintype("beauty");

        LoginRequest loginRequest = new LoginRequest(person);
        spiceManager.execute(loginRequest, "login", DurationInMillis.ALWAYS_EXPIRED, new LoginRequestListener());

        showProcessDialog("正在验证账号...");
	}

    private void showProcessDialog(String message) {
        if (mDialog != null)
        {
            mDialog.dismiss();
            mDialog = null;
        }
        mDialog = DialogFactory.creatRequestDialog(this, message);
        mDialog.show();
    }


    private class LoginRequestListener implements RequestListener<Person> {
        @Override
        public void onRequestFailure(SpiceException e) {
            mDialog.dismiss();
            mDialog = null;
            Toast.makeText(LoginActivity.this, "网络不给力,错误: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onRequestSuccess(Person person) {
            mDialog.dismiss();
            mDialog = null;
            if (person == null) {
                Toast.makeText(LoginActivity.this, "用户名密码错误，请重新输入", Toast.LENGTH_LONG).show();
            } else {
                authenticator.Save(person);

                closeLoginUI(RESULT_OK);

                Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_LONG).show();
            }

        }
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

    private class BaseUiListener implements IUiListener {

        @Override
        public void onComplete(Object o) {
            JSONObject json = (JSONObject) o;

            try {

                if (json.has("openid")) {
                    person.setUsername(json.get("openid").toString());
                    person.setPasswd(json.get("openid").toString());
                }

                if (json.has("nickname")) {
                    person.setNickname(json.get("nickname").toString());
                    person.setLogintype("QQ");

                    LoginRequest loginRequest = new LoginRequest(person);
                    spiceManager.execute(loginRequest, "login", DurationInMillis.ALWAYS_EXPIRED, new LoginRequestListener());

                    showProcessDialog("正在验证账号...");

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
