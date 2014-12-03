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

    @InjectView(R.id.nickname)
    private EditText nickname;

    @InjectView(R.id.phone)
    private TextView phone;

    @InjectView(R.id.password)
    private EditText password;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        phone.setText(intent.getStringExtra("phone"));

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
        final Person person = new Person();
        person.setUsername(phone.getText().toString());
        person.setPhone(phone.getText().toString());
        person.setNickname(nickname.getText().toString());
        person.setPassword(password.getText().toString());
        person.setLogintype("beauty");

        if (person.getNickname().equals("")) {
            Toast.makeText(RegisterActivity.this, "昵称不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        person.signUp(this, new SaveListener() {
            @Override
            public void onSuccess() {
                if (!person.getErr().equals("success")) {
                    Toast.makeText(RegisterActivity.this,person.getErr(), Toast.LENGTH_SHORT).show();
                    return;
                }

                authenticator.Save(person);

                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onFailure(int i, String s) {

            }
        });

        RegisterRequest registerRequest = new RegisterRequest(person);

        spiceManager.execute(registerRequest, "register", DurationInMillis.ONE_SECOND, new RegisterRequestListener());

        DialogFactory.showDialog(this, "正在注册中...");
	}

    private class RegisterRequestListener implements RequestListener<Person> {
        @Override
        public void onRequestFailure(SpiceException e) {
            mDialog.dismiss();
            mDialog = null;
            Toast.makeText(RegisterActivity.this, "网络不给力,错误: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRequestSuccess(Person person) {
            DialogFactory.dismiss();

            if (!person.getErr().equals("success")) {
                Toast.makeText(RegisterActivity.this,person.getErr(), Toast.LENGTH_SHORT).show();
                return;
            }

            authenticator.Save(person);

            setResult(RESULT_OK);
            finish();
        }
    }
}
