package cn.nit.beauty.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.nit.beauty.BeautyApplication;
import cn.nit.beauty.Helper;
import cn.nit.beauty.R;
import cn.nit.beauty.Utils;
import cn.nit.beauty.entity.User;
import cn.nit.beauty.proxy.UserProxy;
import com.google.inject.Inject;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
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
    UserProxy userProxy;

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
        User currentUser = userProxy.getCurrentUser();

        if (currentUser != null) {
            tvNickname.setText(currentUser.getNickname());
            tvRenew_info.setText("会员到期日:\r\n" + currentUser.getExpiredDate());
            my_viewflipper.setDisplayedChild(1);
            ivLogout.setOnClickListener(logoutClickListener);

            BmobFile avatarFile = currentUser.getAvatar();
            if(null != avatarFile){
                ImageLoader.getInstance()
                        .displayImage(avatarFile.getFileUrl(), ivLogout,
                                BeautyApplication.getInstance().getOptions(R.drawable.icon),
                                new SimpleImageLoadingListener(){

                                    @Override
                                    public void onLoadingComplete(String imageUri, View view,
                                                                  Bitmap loadedImage) {
                                        // TODO Auto-generated method stub
                                        super.onLoadingComplete(imageUri, view, loadedImage);
                                    }

                                });
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
            Toast.makeText(UserCenterActivity.this, "支付成功，您的有效期至" + userProxy.getCurrentUser().getExpiredDate(), Toast.LENGTH_SHORT).show();
        }
    }
}
