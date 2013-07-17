package cn.nit.beauty.ui;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import cn.nit.beauty.Helper;
import cn.nit.beauty.R;
import cn.nit.beauty.utils.Data;
import me.maxwin.view.XListView;
import me.maxwin.view.XListView.IXListViewListener;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import cn.nit.beauty.model.FolderInfo;
import cn.nit.beauty.widget.ScaleImageView;

import com.aliyun.android.oss.OSSClient;
import com.aliyun.android.oss.model.OSSObjectSummary;
import com.aliyun.android.util.Pagination;
import com.baidu.mobstat.StatService;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ImageListActivity extends FragmentActivity implements IXListViewListener {
    private XListView mAdapterView = null;
    private StaggeredAdapter mAdapter = null;
    private int currentPage = 0;
    private Pagination<OSSObjectSummary> pagination = null;
    private OSSClient ossClient;
    private String objectKey;
    
    ContentTask task = new ContentTask(this, 2);

    private class ContentTask extends AsyncTask<String, Integer, List<FolderInfo>> {

        private Context mContext;
        private int mType = 1;        
        
        public ContentTask(Context context, int type) {
            super();
            mContext = context;
            mType = type;
            
            
        }

        @Override
        protected List<FolderInfo> doInBackground(String... params) {
        	
        	List<FolderInfo> folderInfos = new ArrayList<FolderInfo>();
        	
				if (!Helper.checkConnection(mContext))
					return null;

				if (pagination == null) {
					pagination = ossClient.viewFolder("nit-photo", objectKey, 10);
				} else {
					pagination = pagination.next();
				}

				for (OSSObjectSummary objectSummary : pagination.getContents()) {
					if (objectSummary.getKey().equals(objectKey))
						continue;


                    String key = objectSummary.getKey();


                    //key = key.substring(0, key.lastIndexOf("/")) + "/thumb" + key.substring(key.lastIndexOf("/"), key.length());
					FolderInfo newsInfo1 = new FolderInfo();
					newsInfo1.setAlbid(key);
					newsInfo1.setIsrc(key);
					newsInfo1.setMsg(objectSummary.getKey());
					folderInfos.add(newsInfo1);
				}

				return folderInfos;
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
    private void AddItemToContainer(int pageindex, int type) {
        if (task.getStatus() != Status.RUNNING) {
        	if (pagination == null || pagination.hasNext()) { 
				ContentTask task = new ContentTask(this, type);
				task.execute("china");
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
                LayoutInflater layoutInflator = LayoutInflater.from(parent.getContext());
                convertView = layoutInflator.inflate(R.layout.infos_list, null);
                holder = new ViewHolder();
                holder.imageView = (ScaleImageView) convertView.findViewById(R.id.news_pic);
                //holder.contentView = (TextView) convertView.findViewById(R.id.news_title);
                convertView.setTag(holder);
                convertView.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						ViewHolder holder = (ViewHolder) v.getTag();
						
						Intent intent = new Intent(ImageListActivity.this, ImageGalleryActivity.class);
						intent.putExtra("objectKey", holder.objectKey);
						intent.putExtra("folder", objectKey);
						
						startActivity(intent);
					}
				});
            }

            holder = (ViewHolder) convertView.getTag();

            //holder.contentView.setText(duitangInfo.getMsg());
            holder.objectKey = duitangInfo.getAlbid();
            ImageLoader.getInstance().displayImage(Data.OSS_URL + duitangInfo.getIsrc(), holder.imageView);
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
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_pull_to_refresh_sample);
        
        Intent intent = getIntent();
        objectKey = intent.getStringExtra("objectKey");
        
        ossClient = new OSSClient();
		ossClient.setAccessId("tEPWqYKJGESwhRo5");
		ossClient.setAccessKey("oUkPZvE5HghfRbkX5wklu6qAiDnMrw");
		
        mAdapterView = (XListView) findViewById(R.id.list);
        mAdapterView.setPullLoadEnable(true);
        mAdapterView.setXListViewListener(this);

        mAdapter = new StaggeredAdapter(this, mAdapterView);

    }


    @Override
    protected void onResume() {
        super.onResume();
        mAdapterView.setAdapter(mAdapter);
        AddItemToContainer(currentPage, 2);
        StatService.onResume(this);
    }

    @Override
	protected void onPause() {
		super.onPause();
		StatService.onPause(this);
	}

	@Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onRefresh() {
        AddItemToContainer(++currentPage, 1);

    }

    @Override
    public void onLoadMore() {
        AddItemToContainer(++currentPage, 2);

    }
}// end of class
