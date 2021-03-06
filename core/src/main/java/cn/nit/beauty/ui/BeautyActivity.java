package cn.nit.beauty.ui;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;

import com.actionbarsherlock.app.ActionBar;

import org.lucasr.smoothie.AsyncGridView;
import org.lucasr.smoothie.ItemManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.nit.beauty.BeautyApplication;
import cn.nit.beauty.R;
import cn.nit.beauty.adapter.StaggeredAdapter;
import cn.nit.beauty.database.Category;
import cn.nit.beauty.entity.User;
import cn.nit.beauty.model.ImageInfo;
import cn.nit.beauty.utils.Data;

public class BeautyActivity extends BaseActivity implements ActionBar.OnNavigationListener {

    private AsyncGridView mAdapterView = null;
    private StaggeredAdapter mAdapter = null;
    private List<String> filters = new ArrayList<String>();

    private String selectedFilter = "全部";
    private String category = "";

    private List<String> folders, selectedFolders = new ArrayList<String>();

    private User currentUser;

    /**
     * 添加内容
     */
    private void AddItemToContainer() {

        List<ImageInfo> imageInfos = new ArrayList<ImageInfo>();


        for(int i = 0; i < selectedFolders.size(); i++) {
            String[] strs = selectedFolders.get(i).split(":");
            String url = strs[0];
            String objectId = strs[2];

            ImageInfo imageInfo = new ImageInfo();
            imageInfo.setObjectId(objectId);
            imageInfo.setKey(url);
            imageInfo.setSmall(true);
            imageInfo.setSmallUrl(url + "smallthumb/cover.jpg");
            imageInfo.setTitle(url);
            imageInfos.add(imageInfo);
        }

        mAdapter.clear();
            mAdapter.addItemLast(imageInfos);
            mAdapter.notifyDataSetChanged();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_category);

        currentUser = BeautyApplication.getInstance().getCurrentUser();

        mAdapterView = (AsyncGridView) findViewById(R.id.list);

        mAdapter = new StaggeredAdapter(this, null, new OnClickListener() {

            @Override
            public void onClick(View v) {
                StaggeredAdapter.ViewHolder holder = (StaggeredAdapter.ViewHolder) v.getTag();

                Intent intent = new Intent(BeautyActivity.this,
                        ImageListActivity.class);
                intent.putExtra("objectKey", holder.imageInfo.getKey());
                intent.putExtra("objectId", holder.imageInfo.getObjectId());
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


        Intent intent = getIntent();

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.e("query", query);
            setTitle(query);

            selectedFolders = doSearch(query.toUpperCase());
            AddItemToContainer();

        } else {

            category = intent.getStringExtra("category");

            setTitle(Data.getTitle(category));

            //setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.china);


                folders = Data.categoryMap.get(category);

                if (folders == null) folders = new ArrayList<String>();



                updateFilters();

                Context context = getSupportActionBar().getThemedContext();
                ArrayAdapter<String> list = new ArrayAdapter(context, R.layout.sherlock_spinner_item);

                list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    list.addAll(filters);
                } else {
                    for(String filter :filters) {
                        list.add(filter);
                    }
                }
                getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
                getSupportActionBar().setListNavigationCallbacks(list, this);
                getSupportActionBar().setSelectedNavigationItem(list.getPosition(selectedFilter));

        }


    }

    private List<String> doSearch(String query) {
        List<String> result = new ArrayList<String>();
        for(Map.Entry<String, List<String>> entry : Data.categoryMap.entrySet()) {
            for(String str : entry.getValue()) {
                if (str.toUpperCase().contains(query)) result.add(str);
            }
        }
        return  result;
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
        List<Category> categories = Data.getCategoryItems(category);
        for (Category category : categories) {
            filters.add(category.getTITLE());
        }
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        selectedFilter = filters.get(itemPosition);
        updateFilterFolder();
        AddItemToContainer();
        return true;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (event.getAction() == KeyEvent.ACTION_DOWN
                    && event.getRepeatCount() == 0) {

                Intent intent = new Intent();
                intent.setClass(BeautyActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }


}
