package cn.nit.beauty.wxapi;

import android.content.Intent;
import android.os.Bundle;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UploadFileListener;
import cn.nit.beauty.entity.User;
import cn.nit.beauty.proxy.UserProxy;
import cn.nit.beauty.ui.BaseActivity;
import cn.nit.beauty.utils.ActivityUtil;
import cn.nit.beauty.utils.Data;
import cn.nit.beauty.utils.DialogFactory;
import cn.nit.beauty.utils.L;

/**
 * Created by vicky on 2014/10/9.
 */
public class WXEntryActivity extends BaseActivity implements IWXAPIEventHandler, UserProxy.ISignUpListener, UserProxy.ILoginListener {

    UserProxy userProxy;

    User user;
    AsyncHttpClient client = new AsyncHttpClient();
    private IWXAPI api;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userProxy = new UserProxy(this);

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

        if (!(baseResp instanceof SendAuth.Resp)) {
            finish();
            return;
        }

        SendAuth.Resp resp = (SendAuth.Resp) baseResp;

        L.e("weixin:" + resp.errCode + ":resp");

        if (resp.errCode == 0) {
            final RequestParams params = new RequestParams();
            params.put("appid", Data.WEIXIN_APP_ID);
            params.put("secret", Data.WEIXIN_APP_SECRET);
            params.put("code", resp.code);
            params.put("grant_type", "authorization_code");

            client.get("https://api.weixin.qq.com/sns/oauth2/access_token", params, new JsonHttpResponseHandler() {
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        final RequestParams params = new RequestParams();
                        params.put("access_token", response.getString("access_token"));
                        params.put("openid", response.getString("openid"));

                        user = new User();

                        user.setUsername(response.getString("openid"));
                        user.setPassword(response.getString("openid"));
                        user.setLogintype("WEIXIN");

                        user.login(WXEntryActivity.this, new SaveListener() {
                            @Override
                            public void onSuccess() {
                                onLoginSuccess();
                            }

                            @Override
                            public void onFailure(int i, String s) {
                                client.get("https://api.weixin.qq.com/sns/userinfo", params, new JsonHttpResponseHandler() {
                                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                        try {

                                            user.setNickname(response.getString("nickname"));


                                            String headimgurl = response.getString("headimgurl");

                                            File avatarFile = File.createTempFile("avatar", ".jpg");

                                            client.get(headimgurl, new FileAsyncHttpResponseHandler(avatarFile) {
                                                @Override
                                                public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {

                                                }

                                                @Override
                                                public void onSuccess(int statusCode, Header[] headers, File file) {
                                                    final BmobFile bmobFile = new BmobFile(file);

                                                    bmobFile.upload(WXEntryActivity.this, new UploadFileListener() {
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


                                            DialogFactory.showDialog(WXEntryActivity.this, "正在验证账号...");
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
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
        ActivityUtil.show(this, "注册失败。" + msg);
    }

    @Override
    public void onLoginSuccess() {
        ActivityUtil.show(this, "登录成功。");
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onLoginFailure(String msg) {
        ActivityUtil.show(this, "登录失败。" + msg);
        setResult(RESULT_CANCELED);
        finish();
    }
}
