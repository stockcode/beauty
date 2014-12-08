package cn.nit.beauty.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;

import cn.nit.beauty.BeautyApplication;
import cn.nit.beauty.R;
import cn.nit.beauty.entity.User;
import cn.nit.beauty.proxy.UserProxy;
import cn.nit.beauty.utils.ActivityUtil;
import cn.nit.beauty.utils.Constant;
import cn.nit.beauty.utils.L;
import com.google.inject.Inject;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.utils.StorageUtils;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

import static cn.nit.beauty.utils.Constant.SEX_FEMALE;

@ContentView(R.layout.user_settings)
public class UserSettingsActivity extends RoboActivity implements OnClickListener,OnCheckedChangeListener{

    @Inject
    UserProxy userProxy;

    @InjectView(R.id.user_logout)
	TextView logout;

    @InjectView(R.id.sex_choice_switch)
	CheckBox sexSwitch;

    @InjectView(R.id.user_icon)
	RelativeLayout iconLayout;

    @InjectView(R.id.user_icon_image)
	ImageView userIcon;

    @InjectView(R.id.user_nick)
	RelativeLayout nickLayout;

    @InjectView(R.id.user_nick_text)
	TextView nickName;

    @InjectView(R.id.user_sign)
	RelativeLayout signLayout;

    @InjectView(R.id.user_sign_text)
	TextView signature;
	
	static final int UPDATE_SEX = 11;
	static final int UPDATE_ICON = 12;
	static final int GO_LOGIN = 13;
	static final int UPDATE_SIGN = 14;
	static final int EDIT_SIGN = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initPersonalInfo();

        setListener();
    }

	private void initPersonalInfo(){
		User user = userProxy.getCurrentUser();
		if(user != null){
			nickName.setText(user.getNickname());
			signature.setText(user.getSignature());
			if(user.getSex().equals(SEX_FEMALE)){
				sexSwitch.setChecked(true);
				//sputil.setValue("sex_settings", 0);
			}else{
				sexSwitch.setChecked(false);
				//sputil.setValue("sex_settings", 1);
			}
			BmobFile avatarFile = user.getAvatar();
			if(null != avatarFile){
                ImageLoader.getInstance()
                        .displayImage(avatarFile.getFileUrl(), userIcon,
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
			logout.setText("退出登录");
		}else{
			logout.setText("登录");
		}
	}

	
	/**
	 * 判断用户是否登录
	 * @return
	 */
	private boolean isLogined(){
		User user = userProxy.getCurrentUser();
		if(user != null){
			return true;
		}
		return false;
	}
	
	protected void setListener() {
		// TODO Auto-generated method stub
		logout.setOnClickListener(this);

		sexSwitch.setOnCheckedChangeListener(this);
		
		iconLayout.setOnClickListener(this);
		nickLayout.setOnClickListener(this);
		signLayout.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.user_logout:
			if(isLogined()){
				userProxy.logout();
				ActivityUtil.show(this, "登出成功。");
                finish();
			}else{
				redictToLogin(GO_LOGIN);
			}
			break;

		case R.id.user_icon:
			if(isLogined()){
				showAlbumDialog();
			}else{
				redictToLogin(UPDATE_ICON);
			}
			break;
		case R.id.user_nick:
			//无需设置
			break;
		case R.id.user_sign:
			if(isLogined()){
//				Intent intent = new Intent();
//				intent.setClass(this, EditSignActivity.class);
//				startActivityForResult(intent, EDIT_SIGN);
			}else{
				redictToLogin(UPDATE_SIGN);
			}
			break;
		default:
			break;
		}
	}
	
	String dateTime;
	AlertDialog albumDialog;
	public void showAlbumDialog(){
		albumDialog = new AlertDialog.Builder(this).create();
		albumDialog.setCanceledOnTouchOutside(true);
		View v = LayoutInflater.from(this).inflate(R.layout.dialog_usericon, null);
		albumDialog.show();
		albumDialog.setContentView(v);
		albumDialog.getWindow().setGravity(Gravity.CENTER);
		
		
		TextView albumPic = (TextView)v.findViewById(R.id.album_pic);
		TextView cameraPic = (TextView)v.findViewById(R.id.camera_pic);
		albumPic.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				albumDialog.dismiss();
				Date date1 = new Date(System.currentTimeMillis());
				dateTime = date1.getTime() + "";
				getAvataFromAlbum();
			}
		});
		cameraPic.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				albumDialog.dismiss();
				Date date = new Date(System.currentTimeMillis());
				dateTime = date.getTime() + "";
				getAvataFromCamera();
			}
		});
	}
	

	
	private void getAvataFromCamera(){
		File f = new File(StorageUtils.getOwnCacheDirectory(this, "icon") + dateTime);
		if (f.exists()) {
			f.delete();
		}
		try {
			f.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Uri uri = Uri.fromFile(f);
		Log.e("uri", uri + "");
		
		Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		camera.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		startActivityForResult(camera, 1);
	}
	
	private void getAvataFromAlbum(){
		Intent intent2 = new Intent(Intent.ACTION_GET_CONTENT);
		intent2.setType("image/*");
		startActivityForResult(intent2, 2);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		switch (buttonView.getId()) {
		case R.id.sex_choice_switch:
			if(isChecked){
				//sputil.setValue("sex_settings", 0);
				updateSex(0);
			}else{
				//sputil.setValue("sex_settings", 1);
				updateSex(1);
			}
			break;
		default:
			break;
		}
			
	}
	
	private void updateSex(int sex){
		User user = userProxy.getCurrentUser();
		if(user!=null){
			if(sex == 0){
				user.setSex(SEX_FEMALE);
			}else{
				user.setSex(Constant.SEX_MALE);
			}

			user.update(this, new UpdateListener() {
				
				@Override
				public void onSuccess() {

					ActivityUtil.show(UserSettingsActivity.this,"更新信息成功。");
					L.i("更新信息成功。");
				}
				
				@Override
				public void onFailure(int arg0, String arg1) {

					ActivityUtil.show(UserSettingsActivity.this, "更新信息失败。请检查网络~");
					L.i("更新失败1-->"+arg1);
				}
			});
		}else{
			redictToLogin(UPDATE_SEX);
		}
		
	}

	private void redictToLogin(int requestCode){
		Intent intent = new Intent();
		intent.setClass(this, LoginActivity.class);
		startActivityForResult(intent, requestCode);
		ActivityUtil.show(this, "请先登录。");
	}
	
	String iconUrl;
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == Activity.RESULT_OK){
			switch (requestCode) {
			case UPDATE_SEX:
				initPersonalInfo();
				break;
			case UPDATE_ICON:
				initPersonalInfo();
				iconLayout.performClick();
				break;
			case UPDATE_SIGN:
				initPersonalInfo();
				signLayout.performClick();
				break;
			case EDIT_SIGN:
				initPersonalInfo();
				break;
			case 1:
				String files =StorageUtils.getOwnCacheDirectory(this, "icon") + dateTime;
				File file = new File(files);
				if(file.exists()&&file.length() > 0){
					Uri uri = Uri.fromFile(file);
					startPhotoZoom(uri);
				}else{
					
				}
				break;
			case 2:
				if (data == null) {
					return;
				}
				startPhotoZoom(data.getData());
				break;
			case 3:
				if (data != null) {
					Bundle extras = data.getExtras();
					if (extras != null) {
						Bitmap bitmap = extras.getParcelable("data");
						// 锟斤拷锟斤拷图片
						iconUrl = saveToSdCard(bitmap);
						userIcon.setImageBitmap(bitmap);
						updateIcon(iconUrl);
					}
				}
				break;
			case GO_LOGIN:
				initPersonalInfo();
				logout.setText("退出登录");
				break;
			default:
				break;
			}
		}
	}
	
	private void updateIcon(String avataPath){
		if(avataPath!=null){
			final BmobFile file = new BmobFile(new File(avataPath));

			file.upload(this, new UploadFileListener() {
				
				@Override
				public void onSuccess() {

					L.i("上传文件成功。" + file.getFileUrl());
					User currentUser = userProxy.getCurrentUser();
					currentUser.setAvatar(file);

					currentUser.update(UserSettingsActivity.this, new UpdateListener() {
						
						@Override
						public void onSuccess() {
							ActivityUtil.show(UserSettingsActivity.this, "更改头像成功。");
						}

						@Override
						public void onFailure(int arg0, String arg1) {

							ActivityUtil.show(UserSettingsActivity.this, "更新头像失败。请检查网络~");
							L.i("更新失败2-->"+arg1);
						}
					});
				}

				@Override
				public void onProgress(Integer arg0) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onFailure(int arg0, String arg1) {

					ActivityUtil.show(UserSettingsActivity.this, "上传头像失败。请检查网络~");
					L.i("上传文件失败。"+arg1);
				}
			});
		}
	}
	
	
	
	public void startPhotoZoom(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		// 锟斤拷锟斤拷锟斤拷锟絚rop=true锟斤拷锟斤拷锟斤拷锟节匡拷锟斤拷锟斤拷Intent锟斤拷锟斤拷锟斤拷锟斤拷示锟斤拷VIEW锟缴裁硷拷
		// aspectX aspectY 锟角匡拷叩谋锟斤拷锟�
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY 锟角裁硷拷图片锟斤拷锟�
		intent.putExtra("outputX", 120);
		intent.putExtra("outputY", 120);
		intent.putExtra("crop", "true");
		intent.putExtra("scale", true);// 去锟斤拷锟节憋拷
		intent.putExtra("scaleUpIfNeeded", true);// 去锟斤拷锟节憋拷
		// intent.putExtra("noFaceDetection", true);//锟斤拷锟斤拷识锟斤拷
		intent.putExtra("return-data", true);
		startActivityForResult(intent, 3);

	}
	
	public String saveToSdCard(Bitmap bitmap){
		String files =StorageUtils.getOwnCacheDirectory(this, "icon") + dateTime+"_12";
		File file=new File(files);
        try {
            FileOutputStream out=new FileOutputStream(file);
            if(bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)){
                out.flush();
                out.close();
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        L.i(file.getAbsolutePath());
        return file.getAbsolutePath();
	}
}
