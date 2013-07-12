package cn.nit.beauty.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.baidu.mobstat.StatService;
import com.capricorn.ArcMenu;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.nit.beauty.R;
import cn.nit.beauty.bus.LauncherChangeEvent;
import cn.nit.beauty.database.LaucherDataBase;
import cn.nit.beauty.model.Category;
import cn.nit.beauty.utils.Configure;
import cn.nit.beauty.utils.FileOperation;
import cn.nit.beauty.adapter.DragGridAdapter;
import cn.nit.beauty.widget.DragGridView;
import cn.nit.beauty.widget.MyAnimations;
import cn.nit.beauty.widget.ScrollLayout;
import de.greenrobot.event.EventBus;

public class MainActivity extends Activity {

	LaucherDataBase database;

	private boolean areButtonsShowing;
	private RelativeLayout composerButtonsWrapper;
	private ImageView composerButtonsShowHideButtonIcon;
	private RelativeLayout composerButtonsShowHideButton;

	Category map_none = new Category();
	//Category map_null = new Category();
	List<Category> addDate = new ArrayList<Category>();// 每一页的数据
	/** GridView. */
	private ScrollLayout lst_views;
	TextView tv_page;// int oldPage=1;
	private ImageView runImage, delImage;
	float  bitmap_width, bitmap_height;
	
	LinearLayout.LayoutParams param;

	TranslateAnimation left, right;
	Animation up, down;

	public static final int PAGE_SIZE = 8;public int PAGE_COUNT = 2, PAGE_CURRENT=0;;
	ArrayList<DragGridView> gridviews = new ArrayList<DragGridView>();

	ArrayList<List<Category>> lists = new ArrayList<List<Category>>();// 全部数据的集合集lists.size()==countpage;
	List<Category> lstDate = new ArrayList<Category>();// 每一页的数据
	
	SensorManager sm;SensorEventListener lsn;
	boolean isClean = false;Vibrator vibrator;int rockCount = 0;

	ImageButton btn_skin;SharedPreferences sp_skin;
	IntentFilter setPositionFilter;
    boolean finishCount=false;
	
	Class<?>[] classes ={AddItemActivity.class, AboutActivity.class,UserCenterActivity.class,SetActivity.class,HelpActivity.class,FeedbackActivity.class};
	ProgressDialog progressDialog;
	
	//0227更新壁纸切换：
	IntentFilter setbgFilter;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_milaucher);
		
		database = new LaucherDataBase(getApplicationContext());
		
		lstDate = database.getLauncher();
		addDate = lstDate;

		map_none.setTITLE("none");

		if(lstDate.size()==0)
			Toast.makeText(MainActivity.this, "网络有点不给力哦", 2200).show();
		init();
		initData();
		initPath();
		//initBroadCast();
		// initBgBroadCast();
		for (int i = 0; i < Configure.countPages; i++) {
			lst_views.addView(addGridView(i));
		}

		lst_views.setPageListener(new ScrollLayout.PageListener() {
			@Override
			public void page(int page) {
				setCurPage(page);
			}
		});

		runImage = (ImageView) findViewById(R.id.run_image);
		setImageBgAndRun();
		
		delImage = (ImageView) findViewById(R.id.dels);

		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		Sensor sensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		lsn = new SensorEventListener() {
			public void onSensorChanged(SensorEvent e) {
				if (e.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
					if (!isClean && rockCount >= 7) {
						isClean = true;
						rockCount = 0;
						vibrator.vibrate(100);
						CleanItems();
						return;
					}
					float newX = e.values[SensorManager.DATA_X];
					float newY = e.values[SensorManager.DATA_Y];
					float newZ = e.values[SensorManager.DATA_Z];
					// if ((newX >= 18 || newY >= 20||newZ >= 20 )&&rockCount<4)
					// {
					if ((newX >= 16 || newY >= 18 || newZ >= 18)
							&& rockCount % 2 == 0) {
						rockCount++;
						return;
					}
					if ((newX <= -16 || newY <= -18 || newZ <= -18)
							&& rockCount % 2 == 1) {
						rockCount++;
						return;
					}

				}
			}

			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {

			}
		};

		sm.registerListener(lsn, sensor, SensorManager.SENSOR_DELAY_GAME);
	}

	public void init() {
		// relate = (RelativeLayout) findViewById(R.id.relate);
		lst_views = (ScrollLayout) findViewById(R.id.views);
		tv_page = (TextView) findViewById(R.id.tv_page);
		tv_page.setText("1");
		Configure.inits(MainActivity.this);
		param = new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.FILL_PARENT);
		param.rightMargin = 100;
		param.leftMargin = 20;
	}

	public void initData() {
		Configure.countPages = (int) Math.ceil(lstDate.size()
				/ (float) PAGE_SIZE);
		
		if(Configure.countPages==0) return;
		lists = new ArrayList<List<Category>>();
		for (int i = 0; i < Configure.countPages; i++) {
			lists.add(new ArrayList<Category>());
			for (int j = PAGE_SIZE * i; j < (PAGE_SIZE * (i + 1) > lstDate
					.size() ? lstDate.size() : PAGE_SIZE * (i + 1)); j++)
				lists.get(i).add(lstDate.get(j));
		}

		for (int i = lists.get(Configure.countPages - 1).size(); i < PAGE_SIZE; i++) {
				lists.get(Configure.countPages - 1).add(map_none);
		}
	}

	public void CleanItems() {
		lstDate = new ArrayList<Category>();
		for (int i = 0; i < lists.size(); i++) {
			for (int j = 0; j < lists.get(i).size(); j++) {
				if (lists.get(i).get(j).getTITLE() != null
						&& !lists.get(i).get(j).getTITLE().equals("none")) {
					lstDate.add(lists.get(i).get(j));
				}
			}
		}
		initData();
		lst_views.removeAllViews();
		gridviews = new ArrayList<DragGridView>();
		for (int i = 0; i < Configure.countPages; i++) {
			lst_views.addView(addGridView(i));
		}
		isClean = false;
		lst_views.snapToScreen(0);
	}

	public int getFristNonePosition(List<Category> array) {
		for (int i = 0; i < array.size(); i++) {
            Category category = array.get(i);
			if (category.getTITLE().equals("none")) {
				return i;
			}
		}
		return -1;
	}

	public LinearLayout addGridView(int i) {
		// if (lists.get(i).size() < PAGE_SIZE)
		// lists.get(i).add(null);

		LinearLayout linear = new LinearLayout(MainActivity.this);
		DragGridView gridView = new DragGridView(MainActivity.this);
		gridView.setAdapter(new DragGridAdapter(MainActivity.this,gridView, lists
				.get(i)));
		gridView.setNumColumns(2);
		gridView.setHorizontalSpacing(0);
		gridView.setVerticalSpacing(0);
		final int ii = i;
		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					final int arg2, long arg3) {

				Category launcher = lists.get(ii).get(arg2);

                if (launcher.getTITLE().equals("none")) return;

                if (launcher.getURL().equals("favorite") || launcher.getURL().equals("vip") || launcher.getURL().equals("more")) {
                    Toast.makeText(getApplicationContext(), "该功能正在开发中... 敬请期待", Toast.LENGTH_SHORT).show();
                    return;
                }

                String text = launcher.getTITLE();
				Intent intent  = new Intent();
				if(text != null && !text.equals("none")){

					intent.putExtra("launcher", launcher);
					intent.setClass(MainActivity.this, BeautyActivity.class);
                    startActivity(intent);
                    //overridePendingTransition(R.anim.anim_fromright_toup6, R.anim.anim_down_toleft6);
				}


			}
		});
		gridView.setSelector(R.drawable.selector_null);
		gridView.setPageListener(new DragGridView.G_PageListener() {
			@Override
			public void page(int cases, int page) {
				switch (cases) {
				case 0:// 滑动页面
					lst_views.snapToScreen(page);
					setCurPage(page);
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							Configure.isChangingPage = false;
						}
					}, 800);
					break;
				case 1:// 删除按钮上来
					delImage.setBackgroundResource(R.drawable.del);
					delImage.setVisibility(0);
					delImage.startAnimation(up);
					break;
				case 2:// 删除按钮变深
					delImage.setBackgroundResource(R.drawable.del_check);
					Configure.isDelDark = true;
					break;
				case 3:// 删除按钮变淡
					delImage.setBackgroundResource(R.drawable.del);
					Configure.isDelDark = false;
					break;
				case 4:// 删除按钮下去
					delImage.startAnimation(down);
					break;
				case 5:// 松手动作
					delImage.startAnimation(down);
					Category delLauncher = lists.get(Configure.curentPage).remove(
							Configure.removeItem);
                    lists.get(Configure.curentPage).add(map_none);

					((DragGridAdapter) ((gridviews.get(Configure.curentPage))
							.getAdapter())).notifyDataSetChanged();

                    delLauncher.setCHOICE(false);
                    database.updateChoice(delLauncher);

					break;
				}
			}
		});
		gridView.setOnItemChangeListener(new DragGridView.G_ItemChangeListener() {
			@Override
			public void change(int from, int to, int count) {
				Category toString = (Category) lists.get(
						Configure.curentPage - count).get(from);

				lists.get(Configure.curentPage - count).add(from,
						(Category) lists.get(Configure.curentPage).get(to));
				lists.get(Configure.curentPage - count).remove(from + 1);
				lists.get(Configure.curentPage).add(to, toString);
				lists.get(Configure.curentPage).remove(to + 1);

				((DragGridAdapter) ((gridviews
						.get(Configure.curentPage - count)).getAdapter()))
						.notifyDataSetChanged();
				((DragGridAdapter) ((gridviews.get(Configure.curentPage))
						.getAdapter())).notifyDataSetChanged();
			}
		});
		gridviews.add(gridView);
		linear.addView(gridView, param);
		return linear;
	}
//	public void initBgBroadCast(){
//		setbgreceiver = new BroadcastReceiver() {
//			@Override
//			public void onReceive(Context context, Intent intent) {
//				setImageBgAndRun();
//			}
//		};
//		setbgFilter = new IntentFilter(
//				"intentToBgChange");
//		registerReceiver(setbgreceiver, setbgFilter);
//	}

	public void runAnimation() {
		down = AnimationUtils.loadAnimation(MainActivity.this,
				R.anim.griditem_del_down);
		up = AnimationUtils
				.loadAnimation(MainActivity.this, R.anim.griditem_del_up);
		down.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation arg0) {
				delImage.setVisibility(8);
			}
		});

		right = new TranslateAnimation(Animation.ABSOLUTE, 0f,
				Animation.ABSOLUTE, -bitmap_width + Configure.getScreenWidth(MainActivity.this),
				Animation.ABSOLUTE, 0f, Animation.ABSOLUTE, 0f);
		left = new TranslateAnimation(Animation.ABSOLUTE, -bitmap_width
				+ Configure.getScreenWidth(MainActivity.this), Animation.ABSOLUTE, 0f, Animation.ABSOLUTE, 0f,
				Animation.ABSOLUTE, 0f);
		right.setDuration(25000);
		left.setDuration(25000);
		right.setFillAfter(true);
		left.setFillAfter(true);

		right.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				runImage.startAnimation(left);
			}
		});
		left.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				runImage.startAnimation(right);
			}
		});
		runImage.startAnimation(right);
	}
	public void setImageBgAndRun() {
		System.out.println(getSharedPreferences("mysetup", 0).getInt("bg_id", 0)+"==");
		Bitmap bitmap = BitmapFactory.decodeStream(getResources()
				.openRawResource(Configure.images[getSharedPreferences("mysetup", 0).getInt("bg_id", 0)]), null, null);
		bitmap_width = bitmap.getWidth();
		bitmap_height = bitmap.getHeight();

		// if(bitmap_width<=screen_width || bitmap_height <=screen_height){
		Matrix matrix = new Matrix();
		float scaleW = (Configure.getScreenWidth(MainActivity.this) * 3 /2)/ bitmap_width;System.out.println(scaleW+"==");
		float scaleH = Configure.getScreenHeight(MainActivity.this) / bitmap_height;
		matrix.postScale(scaleW, scaleH);
		bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
				bitmap.getHeight(), matrix, true);
		bitmap_width = bitmap.getWidth();
		bitmap_height = bitmap.getHeight();
		// }
		runImage.setImageBitmap(bitmap);
		runAnimation();
	}
	public void setCurPage(final int page) {
		Animation a = MyAnimations.getScaleAnimation(1.0f, 0.0f, 1.0f, 1.0f, 300);
		a.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				tv_page.setText((page + 1) + "");
				tv_page.startAnimation(MyAnimations.getScaleAnimation(0.0f, 1.0f, 1.0f, 1.0f, 300));
			}
		});
		tv_page.startAnimation(a);

	}

    int[] ITEM_DRAWABLES = { R.drawable.composer_add, R.drawable.composer_camera, R.drawable.composer_music,
            R.drawable.composer_place, R.drawable.composer_with };

	public void initPath() {
		MyAnimations.initOffset(MainActivity.this);

		sp_skin = getSharedPreferences("skin", MODE_PRIVATE);

        ArcMenu arcMenu = (ArcMenu) findViewById(R.id.arc_menu);

        final int itemCount = ITEM_DRAWABLES.length;
        for (int i = 0; i < itemCount; i++) {
            ImageView item = new ImageView(this);
            item.setImageResource(ITEM_DRAWABLES[i]);

            final int position = i;
            arcMenu.addItem(item, new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (position > 0) {
                        Toast.makeText(getApplicationContext(), "该功能正在开发中... 敬请期待", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Intent intent = new Intent(MainActivity.this, classes[position]);
                    startActivity(intent);
                    //overridePendingTransition(R.anim.anim_fromright_toup6, R.anim.anim_down_toleft6);
                }
            });// Add a menu item
        }
	}



    public void onEvent(LauncherChangeEvent event) {
        Category launcher = event.getLauncher();

		final String str = launcher.getTITLE();
        boolean isExit=false;
				Configure.countPages = lists.size();
				for (int i = 0; i < lists.size(); i++) {
					for(int j=0;j<lists.get(i).size();j++){
						if(lists.get(i).get(j).getTITLE()!=null && lists.get(i).get(j).getTITLE().equals(str)){
							isExit=true;
							//lists.get(i).add(j,map_null);
							lists.get(i).remove(j + 1);
							((DragGridAdapter) ((gridviews.get(i)).getAdapter())).notifyDataSetChanged();
						}
					}
				}
				if(!isExit){

						if(getFristNonePosition(lists.get(lists.size() - 1)) > 0){
							lists.get(lists.size() - 1).set(getFristNonePosition(lists.get(lists.size() - 1)), launcher);
							//resetNull(lists.size() - 1);
							((DragGridAdapter) ((gridviews.get(gridviews.size()-1)).getAdapter())).notifyDataSetChanged();
						}else{//当前最后页面已经填满
							lists.add(new ArrayList<Category>());
							lists.get(lists.size() - 1).add(launcher);
							for (int i = 1; i < PAGE_SIZE; i++)
								lists.get(lists.size() - 1).add(map_none);
							lst_views.addView(addGridView(Configure.countPages));
							Configure.countPages++;	
						}

				}

        EventBus.getDefault().removeStickyEvent(event);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			if (event.getAction() == KeyEvent.ACTION_DOWN
					&& event.getRepeatCount() == 0) {
				if(finishCount){
					if(lstDate.size()==0){
						finish();return true;
					}
					progressDialog = ProgressDialog.show(MainActivity.this, "请稍等片刻...",
							"正在努力的为您保存状态", true, true);
					new Thread(){
						public void run(){

//							database.deleteLauncher();
//							for (int i = 0; i < lists.size(); i++) {
//								database.insertLauncher(lists.get(i));
//							}


							SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
							boolean isClearImage = settings.getBoolean("checkbox_clearimage", false);
							if(isClearImage){
								File f = new File("/sdcard/night_girls/weibos");
								FileOperation.deleteFile(f);
								f.delete();
							}
							sm.unregisterListener(lsn);


							 if (!android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)){
								clear(MainActivity.this.getCacheDir());
							}
							Message msg = finishHandler.obtainMessage();
							finishHandler.sendMessage(msg);
						}
					}.start();
				}else{
					finishCount=true;
					Toast.makeText(MainActivity.this, "再按一次返回键退出", 2000).show();
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							finishCount=false;
						}
					},2000);
				}
				return true;
			}
		}
		return super.dispatchKeyEvent(event);
	}
	private Handler finishHandler = new Handler() {
		public void handleMessage(Message msg) {
			progressDialog.dismiss();
			finish();
		}
	};
	
	@Override
	protected void onPause() {
		super.onPause();

        // Always unregister when an object no longer should be on the bus.
        EventBus.getDefault().unregister(this);

		PAGE_COUNT=Configure.countPages;PAGE_CURRENT=Configure.curentPage;		
		StatService.onPause(this);

	}

	@Override
	protected void onStart() {
		super.onStart();
		Configure.countPages=PAGE_COUNT;Configure.curentPage=PAGE_CURRENT;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
    public void clear(File cacheDir){
        File[] files=cacheDir.listFiles();
        for(File f:files)
            f.delete();
    }

	@Override
	protected void onResume() {
		super.onResume();

        // Register ourselves so that we can provide the initial value.
        EventBus.getDefault().registerSticky(this);

		StatService.onResume(this);
	}
}
