package cn.nit.beauty.wxapi;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import cn.nit.beauty.entity.User;
import cn.nit.beauty.model.Person;
import cn.nit.beauty.proxy.UserProxy;
import cn.nit.beauty.request.LoginRequest;
import cn.nit.beauty.utils.*;
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
public class WXEntryActivity extends RoboActivity implements IWXAPIEventHandler, UserProxy.ISignUpListener, UserProxy.ILoginListener {

    @Inject
    UserProxy userProxy;

    User user;

    private IWXAPI api;

    AsyncHttpClient client = new AsyncHttpClient();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        api = WXAPIFactory.createWXAPI(this, Data.WEIXIN_APP_ID, false);

        api.handleIntent(getIntent(), this);

        userProxy.setOnSignUpListener(this);
        userProxy.setOnLoginListener(this);
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

        L.e("weixin:" + resp.errCode + ":resp");

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
                                    user = new User();

                                    user.setUsername(response.getString("openid"));
                                    user.setPassword(response.getString("openid"));
                                    user.setNickname(response.getString("nickname"));
                                    user.setLogintype("WEIXIN");

                                    userProxy.signUp(user);

                                    DialogFactory.showDialog(WXEntryActivity.this, "正在验证账号...");
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

    @Override
    public void onSignUpSuccess() {
        DialogFactory.dismiss();
        userProxy.login(user.getUsername(), user.getPassword());
    }

    @Override
    public void onSignUpFailure(String msg) {
        DialogFactory.dismiss();
        userProxy.login(user.getUsername(), user.getPassword());
    }

    @Override
    public void onLoginSuccess() {
        ActivityUtil.show(this, "登录成功。");
        finish();
    }

    @Override
    public void onLoginFailure(String msg) {
        ActivityUtil.show(this, "登录失败。"+msg);
        finish();
    }
}
