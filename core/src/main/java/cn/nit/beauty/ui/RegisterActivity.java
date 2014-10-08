package cn.nit.beauty.ui;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import cn.nit.beauty.R;
import cn.nit.beauty.model.Person;
import cn.nit.beauty.request.RegisterRequest;
import cn.nit.beauty.utils.Authenticator;
import cn.nit.beauty.utils.DialogFactory;
import com.google.inject.Inject;
import com.octo.android.robospice.GsonSpringAndroidSpiceService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.register)
public class RegisterActivity extends RoboActivity implements OnClickListener{
    @Inject
    Authenticator authenticator;

    private SpiceManager spiceManager = new SpiceManager(
            GsonSpringAndroidSpiceService.class);

    @InjectView(R.id.username)
    private TextView username;

    @InjectView(R.id.password)
    private TextView password;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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

	private Dialog mDialog = null;


	@Override
	public void onClick(View v) {
        Person person = new Person();
        person.setUsername(username.getText().toString());
        person.setPasswd(password.getText().toString());
        person.setLogintype("beauty");

        if (person.getUsername().equals("")) {
            Toast.makeText(RegisterActivity.this, "用户名不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        RegisterRequest registerRequest = new RegisterRequest(person);

        spiceManager.execute(registerRequest, "register", DurationInMillis.ONE_SECOND, new RegisterRequestListener());

        if (mDialog != null)
        {
            mDialog.dismiss();
            mDialog = null;
        }
        mDialog = DialogFactory.creatRequestDialog(this, "正在注册中...");
        mDialog.show();
	}

    private class RegisterRequestListener implements RequestListener<Person> {
        @Override
        public void onRequestFailure(SpiceException e) {
            mDialog.dismiss();
            mDialog = null;
            Toast.makeText(RegisterActivity.this, "网络不给力,错误: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onRequestSuccess(Person person) {
            mDialog.dismiss();
            mDialog = null;

            authenticator.Save(person);

            setResult(RESULT_OK);
            finish();
        }
    }
}
