package cn.nit.beauty.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import cn.nit.beauty.Helper;
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
    @InjectView(R.id.btn_myyouku_renew)
    TextView tvRenew;
    @InjectView(R.id.txt_myyouku_renew_info)
    TextView tvRenew_info;
    @InjectView(R.id.myyouku_viewflipper)
    ViewFlipper my_viewflipper;

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
                            checkUserStatus();
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



        ibSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserCenterActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });

        btnLogin.setOnClickListener(loginClickListener);
        btnPay.setOnClickListener(payClickListener);
        tvRenew.setOnClickListener(payClickListener);
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkUserStatus();
    }

    private void checkUserStatus() {
        if (authenticator.isLogin()) {
            tvNickname.setText(authenticator.getNickname());
            tvRenew_info.setText("会员到期日:\r\n" + authenticator.getExpiredDate());
            my_viewflipper.setDisplayedChild(1);
            ivLogout.setOnClickListener(logoutClickListener);
        } else {
            tvNickname.setText(R.string.txt_nickname);
            my_viewflipper.setDisplayedChild(0);
            ivLogout.setOnClickListener(loginClickListener);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.VIP && resultCode == RESULT_OK) {
            Toast.makeText(UserCenterActivity.this, "支付成功，您的有效期至" + authenticator.getExpiredDate(), Toast.LENGTH_SHORT).show();
        }
    }
}
