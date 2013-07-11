package cn.nit.beauty.ui;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import cn.nit.beauty.database.LaucherDataBase;
import cn.nit.beauty.model.Category;
import me.maxwin.view.XListView;
import me.maxwin.view.XListView.IXListViewListener;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import cn.nit.beauty.Helper;
import cn.nit.beauty.R;
import cn.nit.beauty.android.bitmapfun.util.ImageFetcher;
import cn.nit.beauty.model.FolderInfo;
import cn.nit.beauty.utils.Data;
import cn.nit.beauty.widget.ScaleImageView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.aliyun.android.oss.OSSClient;
import com.aliyun.android.oss.model.OSSObjectSummary;
import com.aliyun.android.util.Pagination;
import com.baidu.mobstat.StatService;

public class BeautyActivity extends SherlockActivity implements ActionBar.OnNavigationListener, 
		IXListViewListener {
	
	private ImageFetcher mImageFetcher;
	private XListView mAdapterView = null;
	private StaggeredAdapter mAdapter = null;
	private int currentPage = 0;
	private Pagination<OSSObjectSummary> pagination = null;
	private OSSClient ossClient;
    LaucherDataBase database;

	private String[] mLocations;
	
	private Category launcher;
	
	ContentTask task = new ContentTask(this, 2);

	
	private class ContentTask extends
			AsyncTask<String, Integer, List<FolderInfo>> {

		private Context mContext;
		private int mType = 1;

		public ContentTask(Context context, int type) {
			super();
			mContext = context;
			mType = type;

		}

		@Override
		protected List<FolderInfo> doInBackground(String... params) {
			String category = params[0] + "/";
			
			List<FolderInfo> folderInfos = new ArrayList<FolderInfo>();

			try {
				if (!Helper.checkConnection(mContext))
					return null;

				if (pagination == null) {
					pagination = ossClient
							.viewFolder("nit-photo", category, 10);
				} else {
					pagination = pagination.next();
				}

				for (OSSObjectSummary objectSummary : pagination.getContents()) {
					if (objectSummary.getKey().equals(category))
						continue;
					
					FolderInfo newsInfo1 = new FolderInfo();
					newsInfo1.setAlbid(objectSummary.getKey());
					newsInfo1.setIsrc(objectSummary.getKey() + "cover.jpg");
					newsInfo1.setMsg(objectSummary.getKey());
					folderInfos.add(newsInfo1);
				}

				return folderInfos;

			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(List<FolderInfo> result) {
			if (mType == 1) {

				mAdapter.addItemTop(result);
				mAdapter.notifyDataSetChanged();
				mAdapterView.stopRefresh();

			} else if (mType == 2) {
				mAdapterView.stopLoadMore();
				mAdapter.addItemLast(result);
				mAdapter.notifyDataSetChanged();
			} else if (mType == 3) {
				mAdapter.clear();
				mAdapter.addItemLast(result);
				mAdapter.notifyDataSetChanged();
			}

		}

		@Override
		protected void onPreExecute() {
		}
	}

	/**
	 * 添加内容
	 * 
	 * @param pageindex
	 * @param type
	 *            1为下拉刷新 2为加载更多
	 */
	private void AddItemToContainer(int pageindex, int type, String category) {
		if (task.getStatus() != Status.RUNNING) {
			if (pagination == null || pagination.hasNext()) {
				ContentTask task = new ContentTask(this, type);
				task.execute(category);
			} else {
				mAdapterView.stopRefresh();
				mAdapterView.stopLoadMore();
			}

		}
	}

	public class StaggeredAdapter extends BaseAdapter {
		private Context mContext;
		private LinkedList<FolderInfo> mInfos;
		private XListView mListView;

		public StaggeredAdapter(Context context, XListView xListView) {
			mContext = context;
			mInfos = new LinkedList<FolderInfo>();
			mListView = xListView;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder;
			FolderInfo duitangInfo = mInfos.get(position);

			if (convertView == null) {
				LayoutInflater layoutInflator = LayoutInflater.from(parent
						.getContext());
				convertView = layoutInflator.inflate(R.layout.infos_list, null);
				holder = new ViewHolder();
				holder.imageView = (ScaleImageView) convertView
						.findViewById(R.id.news_pic);
				holder.contentView = (TextView) convertView
						.findViewById(R.id.news_title);
				convertView.setTag(holder);
				convertView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						ViewHolder holder = (ViewHolder) v.getTag();

						Intent intent = new Intent(BeautyActivity.this,
								ImageListActivity.class);
						intent.putExtra("objectKey", holder.objectKey);
						startActivity(intent);
					}
				});
			}

			holder = (ViewHolder) convertView.getTag();
			holder.imageView.setImageWidth(duitangInfo.getWidth());
			holder.imageView.setImageHeight(duitangInfo.getHeight());
			holder.contentView.setText(duitangInfo.getMsg());
			holder.objectKey = duitangInfo.getAlbid();
			mImageFetcher.loadImage(duitangInfo.getIsrc(), holder.imageView);
			return convertView;
		}

		class ViewHolder {
			ScaleImageView imageView;
			TextView contentView;
			TextView timeView;
			String objectKey;
		}

		@Override
		public int getCount() {
			return mInfos.size();
		}

		@Override
		public Object getItem(int arg0) {
			return mInfos.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		public void addItemLast(List<FolderInfo> datas) {
			mInfos.addAll(datas);
		}

		public void addItemTop(List<FolderInfo> datas) {
			for (FolderInfo info : datas) {
				mInfos.addFirst(info);
			}
		}
		
		public void clear() {
			mInfos.clear();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_pull_to_refresh_sample);

		Intent intent = getIntent();
        launcher = (Category) intent.getSerializableExtra("launcher");




        database = new LaucherDataBase(getApplicationContext());

		ossClient = new OSSClient();
		ossClient.setAccessId(Data.OSS_ACCESSID);
		ossClient.setAccessKey(Data.OSS_ACCESSKEY);

		mAdapterView = (XListView) findViewById(R.id.list);
		mAdapterView.setPullLoadEnable(true);
		mAdapterView.setXListViewListener(this);

		mAdapter = new StaggeredAdapter(this, mAdapterView);

		mImageFetcher = new ImageFetcher(this, 240);
		mImageFetcher.setOssClient(ossClient);
		mImageFetcher.setLoadingImage(R.drawable.empty_photo);
		
		
		
		mLocations = getResources().getStringArray(R.array.types);		

        Context context = getSupportActionBar().getThemedContext();
        ArrayAdapter<CharSequence> list = ArrayAdapter.createFromResource(context, R.array.types, R.layout.sherlock_spinner_item);
        list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);

        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getSupportActionBar().setListNavigationCallbacks(list, this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mImageFetcher.setExitTasksEarly(false);
		mAdapterView.setAdapter(mAdapter);		
		StatService.onResume(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

	}

	@Override
	public void onRefresh() {
		AddItemToContainer(++currentPage, 1, launcher.getURL());

	}

	@Override
	public void onLoadMore() {
		AddItemToContainer(++currentPage, 2, launcher.getURL());

	}

	

	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPause(this);
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		//category = Data.categoryMap.get(mLocations[itemPosition]);
		pagination = null;
		AddItemToContainer(currentPage, 3, launcher.getURL());
		return true;
	}
	
	
}
