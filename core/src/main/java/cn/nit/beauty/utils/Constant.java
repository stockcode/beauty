package cn.nit.beauty.utils;

/**
 * @author kingofglory
 *         email: kingofglory@yeah.net
 *         blog:  http:www.google.com
 * @date 2014-2-21
 * TODO
 */

public interface Constant {

    public final static int COLUMN_COUNT = 2; // 显示列数

    public final static int PICTURE_COUNT_PER_LOAD = 30; // 每次加载30张图片

    public final static int PICTURE_TOTAL_COUNT = 10000;   //允许加载的最多图片数

    public final static int HANDLER_WHAT = 1;

    public final static int MESSAGE_DELAY = 200;

	String BMOB_APP_ID = "";
	String TABLE_AI = "Mood";
	String TABLE_COMMENT = "Comment";
	
	String NETWORK_TYPE_WIFI = "wifi";
	String NETWORK_TYPE_MOBILE = "mobile";
	String NETWORK_TYPE_ERROR = "error";
	
	int AI = 0;
	int HEN = 1;
	int CHUN_LIAN = 2;
	int BIAN_BAI = 3;
	
	int CONTENT_TYPE = 4;
	
	String PRE_NAME = "my_pre";

	public static final int PUBLISH_COMMENT = 1;
	public static final int NUMBERS_PER_PAGE = 15;//每次请求返回评论条数
	public static final int SAVE_FAVOURITE = 2;
	public static final int GET_FAVOURITE = 3;
	public static final int GO_SETTINGS = 4;
	
	public static final String SEX_MALE = "male";
	public static final String SEX_FEMALE = "female";
}
