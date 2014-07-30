package cn.nit.beauty.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import cn.nit.beauty.R;
import cn.nit.beauty.Utils;
import cn.nit.beauty.utils.Authenticator;
import com.google.inject.Inject;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;


@ContentView(R.layout.activity_myyouku)
public class UserCenterActivity extends RoboActivity {

    @InjectView(R.id.btn_myyouku_login)
    Button btnLogin;
    @InjectView(R.id.btn_myyouku_vipopen)
    Button btnPay;
    @InjectView(R.id.nickname)
    TextView tvNickname;
    @InjectView(R.id.img_myyouku_vip)
    ImageView ivVip;
    @InjectView(R.id.portrait2)
    ImageView ivLogout;
    @InjectView(R.id.setting)
    ImageButton ibSetting;

    @Inject
    Authenticator authenticator;

    private View.OnClickListener loginClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(UserCenterActivity.this, LoginActivity.class);
            startActivityForResult(intent, Utils.LOGIN);

        }
    };

    private View.OnClickListener payClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(UserCenterActivity.this, VipProductActivity.class);
            startActivityForResult(intent, Utils.VIP);
        }
    };

    private View.OnClickListener logoutClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new AlertDialog.Builder(UserCenterActivity.this)
                    .setTitle("提示")
                    .setMessage("确认登出吗")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            authenticator.Logout();
                            LogoutUI();
                            Toast.makeText(UserCenterActivity.this, "已退出登录", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .show();

        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (authenticator.isLogin()) {
            LoginUI();
        } else {
            LogoutUI();
        }

        ibSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserCenterActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });
    }

    private void LoginUI() {
        btnLogin.setText(R.string.btn_myyouku_viprenew);
        btnLogin.setOnClickListener(payClickListener);

        ivLogout.setOnClickListener(logoutClickListener);

        tvNickname.setText(authenticator.getUsername());
        ivVip.setImageResource(R.drawable.vip_yes);
        //btnLogin.setWidth(getResources().getDimension(R.dimen.btn_myyouku_renew_width)));

        btnPay.setOnClickListener(null);
        btnPay.setText(authenticator.getExpiredDate());
    }

    private void LogoutUI() {
        btnLogin.setText(R.string.btn_myyouku_reglogin);
        btnLogin.setOnClickListener(loginClickListener);
        ivLogout.setOnClickListener(loginClickListener);

        btnPay.setOnClickListener(payClickListener);
        btnPay.setText(R.string.btn_myyouku_vipopen);

        tvNickname.setText(R.string.txt_nickname);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.LOGIN && resultCode == RESULT_OK) {
            LoginUI();
        }
        if (requestCode == Utils.VIP && resultCode == RESULT_OK) {
            LoginUI();

            Toast.makeText(UserCenterActivity.this, "支付成功，您的有效期至" + authenticator.getExpiredDate(), Toast.LENGTH_SHORT).show();
        }
    }
}
