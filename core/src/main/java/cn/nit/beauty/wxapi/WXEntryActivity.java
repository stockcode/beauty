package cn.nit.beauty.wxapi;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import cn.nit.beauty.model.Person;
import cn.nit.beauty.request.LoginRequest;
import cn.nit.beauty.utils.Authenticator;
import cn.nit.beauty.utils.Data;
import cn.nit.beauty.utils.DialogFactory;
import com.google.inject.Inject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.octo.android.robospice.GsonSpringAndroidSpiceService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;
import roboguice.activity.RoboActivity;

import java.util.Date;

/**
 * Created by vicky on 2014/10/9.
 */
public class WXEntryActivity extends RoboActivity implements IWXAPIEventHandler {

    @Inject
    Authenticator authenticator;

    private IWXAPI api;

    private SpiceManager spiceManager = new SpiceManager(
            GsonSpringAndroidSpiceService.class);

    AsyncHttpClient client = new AsyncHttpClient();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        api = WXAPIFactory.createWXAPI(this, Data.WEIXIN_APP_ID, false);

        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq baseReq) {
    }

    @Override
    public void onResp(BaseResp baseResp) {

        SendAuth.Resp resp = (SendAuth.Resp) baseResp;

        Log.e("weixin", resp.errCode + ":resp");

        if (resp.errCode == 0) {
            final RequestParams params = new RequestParams();
            params.put("appid", Data.WEIXIN_APP_ID);
            params.put("secret", Data.WEIXIN_APP_SECRET);
            params.put("code", resp.code);
            params.put("grant_type", "authorization_code");

            client.get("https://api.weixin.qq.com/sns/oauth2/access_token", params, new JsonHttpResponseHandler(){
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        RequestParams params = new RequestParams();
                        params.put("access_token", response.getString("access_token"));
                        params.put("openid", response.getString("openid"));
                        client.get("https://api.weixin.qq.com/sns/userinfo", params, new JsonHttpResponseHandler(){
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                try {
                                    Person person = new Person();

                                    person.setUsername(response.getString("openid"));
                                    person.setPasswd(response.getString("openid"));
                                    person.setNickname(response.getString("nickname"));
                                    person.setLogintype("WEIXIN");

                                    LoginRequest loginRequest = new LoginRequest(person);
                                    spiceManager.execute(loginRequest, "login", DurationInMillis.ALWAYS_EXPIRED, new LoginRequestListener());

                                    showProcessDialog("正在验证账号...");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
        }
    }

    private Dialog mDialog = null;
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
            Toast.makeText(WXEntryActivity.this, "网络不给力,错误: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onRequestSuccess(Person person) {
            mDialog.dismiss();
            mDialog = null;
            if (person == null) {
                Toast.makeText(WXEntryActivity.this, "用户名密码错误，请重新输入", Toast.LENGTH_LONG).show();
            } else {
                authenticator.Save(person);

                finish();

                Toast.makeText(WXEntryActivity.this, "登录成功", Toast.LENGTH_LONG).show();
            }

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
}
