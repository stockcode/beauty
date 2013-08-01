package cn.nit.beauty.utils;

import cn.nit.beauty.R;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;

public class Configure{
	public static boolean isDatabaseOprating=false;
	
	//==============old==================
	public static boolean isMove=false;
	public static boolean isChangingPage=false;
	public static boolean isDelDark = false;
	public static int screenHeight=0;
	public static int screenWidth=0;
	public static float screenDensity=0;

    public static String userName;
    public static String accessToken;

	public static int curentPage=0;public static int countPages=0;public static int removeItem=0;


	public int[] ret(int[] intArray) {
		int size = intArray.length;
		for (int i =size - 1; i >= 0; i--)
			for (int j = 0; j < i; j++)
				if (intArray[j] > intArray[j + 1]) {
					int t = intArray[j];
					intArray[j] = intArray[j + 1];
					intArray[j + 1] = t;
				}
		return intArray;
	}
	
	
		public static String httpurl=null;
		public static Bitmap NO_IMAGE=null;
		public static Bitmap[] DetailWeiboImages=null;

		public static String N_USER_NAME=null;
		public static long N_USER_ID=0;
		public static String N_USER_KEY = null;
		public static String N_USER_SECRET=null;

		public static void inits(Activity context) {

			if(screenDensity==0||screenWidth==0||screenHeight==0){
				DisplayMetrics dm = new DisplayMetrics();
				context.getWindowManager().getDefaultDisplay().getMetrics(dm);
				Configure.screenDensity = dm.density;
				Configure.screenHeight = dm.heightPixels;
				Configure.screenWidth = dm.widthPixels;
			}
			if(N_USER_NAME==null||N_USER_KEY == null||N_USER_SECRET==null||N_USER_ID==0){
				SharedPreferences refreshtime;
				refreshtime = context.getSharedPreferences("sp_users", 0);
				Configure.N_USER_ID = refreshtime.getLong("UserId", -1);
				Configure.N_USER_NAME = refreshtime.getString("UserName", null);
				Configure.N_USER_KEY = refreshtime.getString("Token", null);
				Configure.N_USER_SECRET = refreshtime.getString("TokenSecret", null);
			}	
		//	System.out.println(Configure.N_USER_ID+"--"+Configure.N_USER_KEY+"--"+Configure.N_USER_SECRET+"--"+Configure.N_USER_NAME);
			
			curentPage=0;countPages=0;
		}
		
		public static int getScreenHeight(Activity context){
			if(screenWidth==0||screenHeight==0){
				DisplayMetrics dm = new DisplayMetrics();
				context.getWindowManager().getDefaultDisplay().getMetrics(dm);
				Configure.screenDensity = dm.density;
				Configure.screenHeight = dm.heightPixels;
				Configure.screenWidth = dm.widthPixels;
			}
			return screenHeight;
		}
		public static int getScreenWidth(Activity context){
			if(screenWidth==0||screenHeight==0){
				DisplayMetrics dm = new DisplayMetrics();
				context.getWindowManager().getDefaultDisplay().getMetrics(dm);
				Configure.screenDensity = dm.density;
				Configure.screenHeight = dm.heightPixels;
				Configure.screenWidth = dm.widthPixels;
			}
			return screenWidth;
		}

		public static int _position;

    public static void save(SharedPreferences settings) {
        userName = settings.getString("userName", null);
        accessToken = settings.getString("accessToken", null);
    }
}