package cn.nit.beauty.proxy;

import cn.bmob.v3.AsyncCustomEndpoints;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.CloudCodeListener;
import cn.bmob.v3.listener.ResetPasswordListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

import android.content.Context;
import cn.nit.beauty.entity.User;
import cn.nit.beauty.utils.Constant;
import cn.nit.beauty.utils.L;
import com.testin.agent.TestinAgent;
import org.json.JSONException;
import org.json.JSONObject;

public class UserProxy {

	public static final String TAG = "UserProxy";

	private static UserProxy userProxy = null;

	public static UserProxy getInstance(){
		return userProxy;
	}

	public static void createInstance(Context context){
		if (userProxy ==null) {
			userProxy = new UserProxy(context);
		}
	}

	private Context mContext;

	private UserProxy(Context context){
		this.mContext = context;
	}
	
	public void signUp(final User user){
        user.setSex(Constant.SEX_MALE);
		user.setSignature("这个家伙很懒，什么也不说。。。");
		user.signUp(mContext, new SaveListener() {
			
			@Override
			public void onSuccess() {

                try {
                    String cloudCodeName = "firstRegister";
                    JSONObject params = new JSONObject();
                    params.put("objectId", user.getObjectId());
                    AsyncCustomEndpoints cloudCode = new AsyncCustomEndpoints();

                    cloudCode.callEndpoint(mContext, cloudCodeName, params, new CloudCodeListener() {

                        //执行成功时调用，返回result对象
                        @Override
                        public void onSuccess(Object result) {
                            if(signUpLister != null){
                                signUpLister.onSignUpSuccess();
                            }else{
                                L.i("signup listener is null,you must set one!");
                            }
                            L.i("result = "+result.toString());
                        }

                        //执行失败时调用
                        @Override
                        public void onFailure(int arg0, String err) {
                            L.i("BmobException = "+err);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }

			}

			@Override
			public void onFailure(int arg0, String msg) {
				if(signUpLister != null){
					signUpLister.onSignUpFailure(msg);
				}else{
					L.i("signup listener is null,you must set one!");
				}
			}
		});
	}

    public boolean hasExpired() {
        return getCurrentUser() == null || getCurrentUser().hasExpired();
    }

    public interface ISignUpListener{
		void onSignUpSuccess();
		void onSignUpFailure(String msg);
	}
	private ISignUpListener signUpLister;
	public void setOnSignUpListener(ISignUpListener signUpLister){
		this.signUpLister = signUpLister;
	}
	
	
	public User getCurrentUser(){
		User user = BmobUser.getCurrentUser(mContext, User.class);
		if(user != null){
			L.i("本地用户信息" + user.getObjectId() + "-"
					+ user.getUsername() + "-"
					+ user.getSessionToken() + "-"
					+ user.getCreatedAt() + "-"
					+ user.getUpdatedAt() + "-"
					+ user.getSignature() + "-"
					+ user.getSex());
			return user;
		}else{
			L.i("本地用户为null,请登录。");
		}
		return null;
	}
	
	public void login(final String userName,String password){
		final BmobUser user = new BmobUser();
		user.setUsername(userName);
		user.setPassword(password);
		user.login(mContext, new SaveListener() {
			
			@Override
			public void onSuccess() {

				TestinAgent.setUserInfo(userName);

				if(loginListener != null){
					loginListener.onLoginSuccess();
				}else{
					L.i("login listener is null,you must set one!");
				}
			}

			@Override
			public void onFailure(int arg0, String msg) {
				// TODO Auto-generated method stub
				if(loginListener != null){
					loginListener.onLoginFailure(msg);
				}else{
					L.i("login listener is null,you must set one!");
				}
			}
		});
	}
	
	public interface ILoginListener{
		void onLoginSuccess();
		void onLoginFailure(String msg);
	}
	private ILoginListener loginListener;
	public void setOnLoginListener(ILoginListener loginListener){
		this.loginListener  = loginListener;
	}
	
	public void logout(){
		BmobUser.logOut(mContext);
		TestinAgent.setUserInfo("");
		L.i("logout result:"+(null == getCurrentUser()));
	}
	
	public void update(String... args){
		User user = getCurrentUser();
		user.setUsername(args[0]);
		user.setEmail(args[1]);
		user.setPassword(args[2]);
		user.setSex(args[3]);
		user.setSignature(args[4]);
		//...
		user.update(mContext, new UpdateListener() {
			
			@Override
			public void onSuccess() {
				if(updateListener != null){
					updateListener.onUpdateSuccess();
				}else{
					L.i("update listener is null,you must set one!");
				}
			}

			@Override
			public void onFailure(int arg0, String msg) {
				if(updateListener != null){
					updateListener.onUpdateFailure(msg);
				}else{
					L.i("update listener is null,you must set one!");
				}
			}
		});
	}
	
	public interface IUpdateListener{
		void onUpdateSuccess();
		void onUpdateFailure(String msg);
	}
	private IUpdateListener updateListener;
	public void setOnUpdateListener(IUpdateListener updateListener){
		this.updateListener = updateListener;
	}
	
	public void resetPassword(String email){
		BmobUser.resetPassword(mContext, email, new ResetPasswordListener() {
			
			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				if(resetPasswordListener != null){
					resetPasswordListener.onResetSuccess();
				}else{
					L.i("reset listener is null,you must set one!");
				}
			}

			@Override
			public void onFailure(int arg0, String msg) {
				// TODO Auto-generated method stub
				if(resetPasswordListener != null){
					resetPasswordListener.onResetFailure(msg);
				}else{
					L.i("reset listener is null,you must set one!");
				}
			}
		});
	}
	public interface IResetPasswordListener{
		void onResetSuccess();
		void onResetFailure(String msg);
	}
	private IResetPasswordListener resetPasswordListener;
	public void setOnResetPasswordListener(IResetPasswordListener resetPasswordListener){
		this.resetPasswordListener = resetPasswordListener;
	}

}
