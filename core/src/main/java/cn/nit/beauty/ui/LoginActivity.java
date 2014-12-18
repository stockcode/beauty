package cn.nit.beauty.ui;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UploadFileListener;
import cn.nit.beauty.R;
import cn.nit.beauty.Utils;
import cn.nit.beauty.entity.PhotoGallery;
import cn.nit.beauty.entity.User;
import cn.nit.beauty.proxy.UserProxy;
import cn.nit.beauty.utils.*;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import cn.smssdk.gui.RegisterPage;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.tencent.connect.UserInfo;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class LoginActivity extends BaseActivity implements OnClickListener, UserProxy.ILoginListener, UserProxy.ISignUpListener {

    @InjectView(R.id.regist)
	TextView mBtnRegister;

    @InjectView(R.id.login)
	Button mBtnLogin;

    @InjectView(R.id.sm_progressbar)
    SmoothProgressBar progressbar;

    UserProxy userProxy;

    @InjectView(R.id.accounts)
    EditText username;

    @InjectView(R.id.password)
    EditText passwd;

	private boolean mShowMenu = false;

    private Tencent mTencent;

    private User user;
    IWXAPI api;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        ButterKnife.inject(this);

        userProxy = new UserProxy(this);

        initView();

        api = WXAPIFactory.createWXAPI(this, Data.WEIXIN_APP_ID, true);

        api.registerApp(Data.WEIXIN_APP_ID);

    }
    
    
    public void initView()
    {

        mBtnRegister.setPaintFlags(mBtnRegister.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
    	mBtnRegister.setOnClickListener(this);



    	mBtnLogin.setOnClickListener(this);
        userProxy.setOnLoginListener(this);
        userProxy.setOnSignUpListener(this);
    }


    public void onWeiXinClick(View v) {
        final SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "beauty";
        api.sendReq(req);
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

    private BaseUiListener baseUiListener = new BaseUiListener();

    public void onQQClick(View v) {
        mTencent = Tencent.createInstance(Data.QQ_APP_ID, getApplicationContext());
        if (!mTencent.isSessionValid())
        {
            user = new User();
            mTencent.login(this, "", baseUiListener);
        }
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

                    StartRegister(phone);
                }
            }
        });
        registerPage.show(this);

        //StartRegister("13613803575");
    }

    private void StartRegister(String phone) {
        Intent intent = new Intent(this, RegisterActivity.class);
        intent.putExtra("phone", phone);
        startActivityForResult(intent, Utils.REGISTER);
    }

	private void showRequestDialog()
	{
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
            Toast.makeText(LoginActivity.this, "注册成功，您的有效期至" + userProxy.getCurrentUser().getExpiredDate(), Toast.LENGTH_SHORT).show();
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

        getFavorites();

        if (mTencent != null) mTencent.logout(getApplicationContext());
    }

    @Override
    public void onLoginFailure(String msg) {
        dimissProgressbar();
        ActivityUtil.show(this, "用户名密码错误，请重新输入");
        L.i("login failed!"+msg);
    }

    @Override
    public void onSignUpSuccess() {
        userProxy.login(user.getUsername(), user.getPassword());
    }

    @Override
    public void onSignUpFailure(String msg) {
        ActivityUtil.show(this, "注册失败，请检查网络");
        L.i("signup failed!"+msg);

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (userProxy.getCurrentUser() != null) { //for WEIXIN
            getFavorites();
        }
    }

    private void getFavorites() {
        BmobQuery<PhotoGallery> query = new BmobQuery<PhotoGallery>();
        query.addWhereRelatedTo("favorite", new BmobPointer(userProxy.getCurrentUser()));
        query.include("user");
        query.order("createdAt");
        query.findObjects(this, new FindListener<PhotoGallery>() {

            @Override
            public void onSuccess(List<PhotoGallery> data) {
                L.i("get fav success!" + data.size());
                List<String> favs = Data.categoryMap.get("favorite");
                favs.clear();
                for(PhotoGallery photoGallery : data) {
                    favs.add(photoGallery.getUrl());
                }
                closeLoginUI(RESULT_OK);
            }

            @Override
            public void onError(int arg0, String arg1) {
                L.e("get fav error! reason:" + arg1);
                closeLoginUI(RESULT_OK);
            }
        });
    }

    private class BaseUiListener implements IUiListener {

        @Override
        public void onComplete(Object o) {
            JSONObject json = (JSONObject) o;

            try {

                if (json.has("openid")) {
                    user.setUsername(json.get("openid").toString());
                    user.setPassword(json.get("openid").toString());
                    user.login(LoginActivity.this, new SaveListener() {
                        @Override
                        public void onSuccess() {
                            onLoginSuccess();
                        }

                        @Override
                        public void onFailure(int i, String s) {
                            UserInfo info = new UserInfo(getApplicationContext(), mTencent.getQQToken());
                            info.getUserInfo(baseUiListener);
                        }
                    });
                    return;
                }

                if (json.has("nickname")) {



                    String figureurl = json.get("figureurl_qq_1").toString();

                    user.setNickname(json.get("nickname").toString());
                    user.setLogintype("QQ");

                    AsyncHttpClient client = new AsyncHttpClient();

                    File avatarFile = File.createTempFile("avatar", ".jpg");

                    client.get(figureurl, new FileAsyncHttpResponseHandler(avatarFile) {
                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {

                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, File file) {
                            final BmobFile bmobFile = new BmobFile(file);

                            bmobFile.upload(LoginActivity.this, new UploadFileListener() {
                                @Override
                                public void onSuccess() {
                                    user.setAvatar(bmobFile);
                                    userProxy.signUp(user);
                                }

                                @Override
                                public void onFailure(int i, String s) {

                                }
                            });
                        }
                    });


                    progressbar.setVisibility(View.VISIBLE);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
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
