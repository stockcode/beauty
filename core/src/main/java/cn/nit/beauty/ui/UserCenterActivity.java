package cn.nit.beauty.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cn.nit.beauty.R;
import cn.nit.beauty.model.Person;
import cn.nit.beauty.utils.Authenticator;
import cn.nit.beauty.utils.Configure;
import com.google.inject.Inject;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;


@ContentView(R.layout.activity_myyouku)
public class UserCenterActivity extends RoboActivity {

    private static final int LOGIN = 0;
    private static final int VIP = 1;

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

    @Inject
    Authenticator authenticator;

    private View.OnClickListener loginClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(UserCenterActivity.this, LoginActivity.class);
            startActivityForResult(intent, LOGIN);

        }
    };

    private View.OnClickListener payClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(UserCenterActivity.this, VipProductActivity.class);
            startActivityForResult(intent, VIP);
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
    }

    private void LoginUI() {
        btnLogin.setText(R.string.btn_myyouku_viprenew);
        btnLogin.setOnClickListener(payClickListener);

        ivLogout.setOnClickListener(logoutClickListener);

        tvNickname.setText(authenticator.username());
        ivVip.setImageResource(R.drawable.vip_yes);
        //btnLogin.setWidth(getResources().getDimension(R.dimen.btn_myyouku_renew_width)));

        btnPay.setOnClickListener(null);
        btnPay.setText(authenticator.expiredDate());
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

        if (requestCode == LOGIN && resultCode == RESULT_OK) {
            LoginUI();
        }
    }
}
