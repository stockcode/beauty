package cn.nit.beauty.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.aliyun.android.oss.OSSClient;
import com.baidu.mobstat.StatService;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import cn.nit.beauty.R;
import cn.nit.beauty.android.bitmapfun.util.ImageFetcher;
import cn.nit.beauty.database.LaucherDataBase;
import cn.nit.beauty.database.Category;
import cn.nit.beauty.model.FolderInfo;
import cn.nit.beauty.utils.Data;
import cn.nit.beauty.widget.ScaleImageView;
import me.maxwin.view.XListView;
import me.maxwin.view.XListView.IXListViewListener;

public class BeautyActivity extends SherlockActivity implements ActionBar.OnNavigationListener,
        IXListViewListener {

    LaucherDataBase database;
    private ImageFetcher mImageFetcher;
    private XListView mAdapterView = null;
    private StaggeredAdapter mAdapter = null;
    private int currentPage = 0;
    private int pageCount = 10;
    private OSSClient ossClient;
    private List<String> filters = new ArrayList<String>();
    ;
    private String selectedFilter = "全部";
    private String category = "";
    private Category launcher;
    private List<String> folders, selectedFolders = new ArrayList<String>();

    /**
     * 添加内容
     *
     * @param pageindex
     * @param type      1为下拉刷新 2为加载更多
     */
    private void AddItemToContainer(int pageindex, int type) {
        if (type == 3) {
            mAdapter.clear();
        }

        List<FolderInfo> folderInfos = new ArrayList<FolderInfo>();

        for (int i = pageindex * pageCount; i < (pageindex + 1) * pageCount; i++) {
            if (i >= selectedFolders.size()) break;

            FolderInfo newsInfo1 = new FolderInfo();
            newsInfo1.setAlbid(selectedFolders.get(i));
            newsInfo1.setIsrc(selectedFolders.get(i) + "thumb/cover.jpg");
            newsInfo1.setMsg(selectedFolders.get(i));
            folderInfos.add(newsInfo1);
        }

        if (folderInfos.size() > 0) {
            mAdapter.addItemLast(folderInfos);
            mAdapter.notifyDataSetChanged();
        }
        mAdapterView.stopRefresh();
        mAdapterView.stopLoadMore();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_pull_to_refresh_sample);

        Intent intent = getIntent();
        launcher = (Category) intent.getSerializableExtra("launcher");

        if (launcher.getCATEGORY().equals("ROOT")) {
            category = launcher.getURL();
        } else {
            category = launcher.getCATEGORY();
            selectedFilter = launcher.getTITLE();
        }


        folders = Data.categoryMap.get(category);

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

        updateFilters();

        Context context = getSupportActionBar().getThemedContext();
        ArrayAdapter<String> list = new ArrayAdapter(context, R.layout.sherlock_spinner_item);

        list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
        list.addAll(filters);
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getSupportActionBar().setListNavigationCallbacks(list, this);
        getSupportActionBar().setSelectedNavigationItem(list.getPosition(selectedFilter));

    }

    private void updateFilterFolder() {
        String startStr = category + "/" + selectedFilter;
        selectedFolders.clear();

        for (String folder : folders) {
            if (selectedFilter.equals("全部") || folder.startsWith(startStr))
                selectedFolders.add(folder);
        }

    }

    private void updateFilters() {

        filters.add("全部");
        List<Category> categories = database.getItems(category);
        for (Category category : categories) {
            filters.add(category.getTITLE());
        }
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
        AddItemToContainer(++currentPage, 1);

    }

    @Override
    public void onLoadMore() {
        AddItemToContainer(++currentPage, 2);

    }

    @Override
    protected void onPause() {
        super.onPause();
        StatService.onPause(this);
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        selectedFilter = filters.get(itemPosition);
        updateFilterFolder();
        AddItemToContainer(0, 3);
        return true;
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
            //holder.contentView.setText(duitangInfo.getMsg());
            holder.objectKey = duitangInfo.getAlbid();
            mImageFetcher.loadImage(duitangInfo.getIsrc(), holder.imageView);
            return convertView;
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

        class ViewHolder {
            ScaleImageView imageView;
            TextView contentView;
            TextView timeView;
            String objectKey;
        }
    }


}
