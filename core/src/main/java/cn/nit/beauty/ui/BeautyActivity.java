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
import com.baidu.mobads.AdView;
import com.baidu.mobstat.StatService;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.lucasr.smoothie.AsyncGridView;
import org.lucasr.smoothie.ItemManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import cn.nit.beauty.R;
import cn.nit.beauty.adapter.StaggeredAdapter;
import cn.nit.beauty.database.LaucherDataBase;
import cn.nit.beauty.database.Category;
import cn.nit.beauty.model.ImageInfo;
import cn.nit.beauty.utils.Configure;
import cn.nit.beauty.utils.Data;
import cn.nit.beauty.widget.ScaleImageView;

public class BeautyActivity extends SherlockActivity implements ActionBar.OnNavigationListener {

    LaucherDataBase database;
    private AsyncGridView mAdapterView = null;
    private StaggeredAdapter mAdapter = null;
    private List<String> filters = new ArrayList<String>();
    ;
    private String selectedFilter = "全部";
    private String category = "";
    private Category launcher;
    private List<String> folders, selectedFolders = new ArrayList<String>();

    /**
     * 添加内容
     */
    private void AddItemToContainer() {

        List<ImageInfo> imageInfos = new ArrayList<ImageInfo>();


        for(int i = 0; i < selectedFolders.size(); i++) {
            ImageInfo newsInfo1 = new ImageInfo();
            newsInfo1.setKey(selectedFolders.get(i));
            newsInfo1.setUrl(selectedFolders.get(i) + "thumb/cover.jpg");
            newsInfo1.setTitle(selectedFolders.get(i));
            imageInfos.add(newsInfo1);
        }

            mAdapter.addItemLast(imageInfos);
            mAdapter.notifyDataSetChanged();
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

        setTitle(launcher.getTITLE());


        folders = Data.categoryMap.get(category);

        if (folders == null) folders = new ArrayList<String>();

        database = new LaucherDataBase(getApplicationContext());

        mAdapterView = (AsyncGridView) findViewById(R.id.list);

        mAdapter = new StaggeredAdapter(this, null, new OnClickListener() {

            @Override
            public void onClick(View v) {
                StaggeredAdapter.ViewHolder holder = (StaggeredAdapter.ViewHolder) v.getTag();

                Intent intent = new Intent(BeautyActivity.this,
                        ImageListActivity.class);
                intent.putExtra("objectKey", holder.objectKey + "thumb/");
                startActivity(intent);
            }
        });

        mAdapterView.setAdapter(mAdapter);

        GalleryLoader loader = new GalleryLoader(this);

        ItemManager.Builder builder = new ItemManager.Builder(loader);
        builder.setPreloadItemsEnabled(true).setPreloadItemsCount(10);
        builder.setThreadPoolSize(4);
        ItemManager itemManager = builder.build();

        mAdapterView.setItemManager(itemManager);

        updateFilters();

        Context context = getSupportActionBar().getThemedContext();
        ArrayAdapter<String> list = new ArrayAdapter(context, R.layout.sherlock_spinner_item);

        list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
        list.addAll(filters);
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getSupportActionBar().setListNavigationCallbacks(list, this);
        getSupportActionBar().setSelectedNavigationItem(list.getPosition(selectedFilter));

        AddItemToContainer();
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

        if (Configure.accessToken != null) {
            AdView adView = (AdView)findViewById(R.id.adView);
            adView.setVisibility(adView.INVISIBLE);
        }

        StatService.onResume(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


    @Override
    protected void onPause() {
        super.onPause();
        StatService.onPause(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        selectedFilter = filters.get(itemPosition);
        updateFilterFolder();
        mAdapter.clear();
        AddItemToContainer();
        return true;
    }

}
