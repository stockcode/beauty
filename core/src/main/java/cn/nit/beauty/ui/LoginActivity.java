package cn.nit.beauty.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import cn.nit.beauty.R;
import cn.nit.beauty.model.Person;
import cn.nit.beauty.request.LoginRequest;
import cn.nit.beauty.utils.Authenticator;
import cn.nit.beauty.utils.Configure;
import cn.nit.beauty.utils.DialogFactory;
import com.google.inject.Inject;
import com.octo.android.robospice.GsonSpringAndroidSpiceService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import roboguice.activity.RoboActivity;

public class LoginActivity extends RoboActivity implements OnClickListener{

    @Inject
    Authenticator authenticator;

    private SpiceManager spiceManager = new SpiceManager(
            GsonSpringAndroidSpiceService.class);

	private Button mBtnRegister;
	private Button mBtnLogin;

    private EditText username, passwd;

	private boolean mShowMenu = false;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        initView();
    }
    
    
    public void initView()
    {
        username = (EditText) findViewById(R.id.accounts);
        passwd = (EditText)findViewById(R.id.password);

        mBtnRegister = (Button) findViewById(R.id.regist);
    	mBtnRegister.setOnClickListener(this);
    	
    	mBtnLogin = (Button) findViewById(R.id.login);
    	mBtnLogin.setOnClickListener(this);
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
				break;
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
    	startActivity(intent);
    }
	   

	private Dialog mDialog = null;
	private void showRequestDialog()
	{
        Person person = new Person();
        person.setUsername(username.getText().toString());
        person.setPasswd(passwd.getText().toString());

        LoginRequest loginRequest = new LoginRequest(person);
        spiceManager.execute(loginRequest, "login", DurationInMillis.ALWAYS_EXPIRED, new LoginRequestListener());

		if (mDialog != null)
		{
			mDialog.dismiss();
			mDialog = null;
		}
		mDialog = DialogFactory.creatRequestDialog(this, "正在验证账号...");
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
            }

        }
    }

    private void closeLoginUI(int result) {
        setResult(result);
        finish();
    }
}
