package cn.nit.beauty.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.nostra13.universalimageloader.core.ImageLoader;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.bmob.v3.datatype.BmobFile;
import cn.nit.beauty.BeautyApplication;
import cn.nit.beauty.R;
import cn.nit.beauty.Utils;
import cn.nit.beauty.entity.User;


public class UserCenterActivity extends BaseActivity {

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

    User currentUser;

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
            Intent intent = new Intent(UserCenterActivity.this, UserSettingsActivity.class);
            startActivity(intent);

        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_myyouku);
        ButterKnife.inject(this);

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
    public void onResume() {
        super.onResume();

        checkUserStatus();
    }

    private void checkUserStatus() {
        currentUser = BeautyApplication.getInstance().getCurrentUser();

        if (currentUser != null) {
            tvNickname.setText(currentUser.getNickname());
            tvRenew_info.setText("会员到期日:\r\n" + currentUser.getExpiredDate());
            my_viewflipper.setDisplayedChild(1);
            ivLogout.setOnClickListener(logoutClickListener);

            BmobFile avatarFile = currentUser.getAvatar();
            if (null != avatarFile) {
                ImageLoader.getInstance()
                        .displayImage(avatarFile.getFileUrl(), ivLogout,
                                BeautyApplication.getInstance().getOptions(R.drawable.icon));
            }
        } else {
            tvNickname.setText(R.string.txt_nickname);
            my_viewflipper.setDisplayedChild(0);
            ivLogout.setOnClickListener(loginClickListener);

            ivLogout.setImageResource(R.drawable.ic_home_circle);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.VIP && resultCode == RESULT_OK) {
            Toast.makeText(UserCenterActivity.this, "支付成功，您的有效期至" + currentUser.getExpiredDate(), Toast.LENGTH_SHORT).show();
        }
    }
}
