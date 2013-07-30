package cn.nit.beauty.ui;

import java.util.ArrayList;
import java.util.List;

import cn.nit.beauty.R;
import cn.nit.beauty.adapter.StaggeredAdapter;
import cn.nit.beauty.database.LaucherDataBase;
import cn.nit.beauty.model.ImageInfos;
import cn.nit.beauty.request.ImageListRequest;
import cn.nit.beauty.utils.Configure;
import cn.nit.beauty.utils.Data;
import me.maxwin.view.XListView;
import me.maxwin.view.IXListViewLoadMore;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import cn.nit.beauty.model.ImageInfo;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.baidu.mobads.AdView;
import com.baidu.mobstat.StatService;
import com.octo.android.robospice.GsonSpringAndroidSpiceService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.youxiachai.onexlistview.XMultiColumnListView;

public class ImageListActivity extends SherlockActivity implements IXListViewLoadMore {
    private XMultiColumnListView mAdapterView = null;
    private StaggeredAdapter mAdapter = null;
    private int currentPage = 0;
    private List<ImageInfo> imageInfoList = new ArrayList<ImageInfo>();
    private String objectKey;
    private LaucherDataBase database;
    private SpiceManager spiceManager = new SpiceManager(
            GsonSpringAndroidSpiceService.class);

    /**
     * 添加内容
     * 
     * @param pageindex
     * @param type
     *            1为下拉刷新 2为加载更多
     */
    private void AddItemToContainer(int pageindex, int type) {
        int count = Math.min(imageInfoList.size(), (pageindex + 1) * Data.PAGE_COUNT);

        for(int i = pageindex * Data.PAGE_COUNT ; i < count; i++) {
            mAdapter.addItemTop(imageInfoList.get(i));
        }
        mAdapter.notifyDataSetChanged();
        mAdapterView.stopLoadMore();
    }



    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_pull_to_refresh_sample);
        
        Intent intent = getIntent();
        objectKey = intent.getStringExtra("objectKey");

        String[] strs = objectKey.split("/");
        setTitle(strs[strs.length - 2]);

        database = new LaucherDataBase(getApplicationContext());

        mAdapterView = (XMultiColumnListView) findViewById(R.id.list);
        mAdapterView.setPullLoadEnable(this);

        mAdapter = new StaggeredAdapter(this, objectKey);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.imagelist_action, menu);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem mi) {
        switch (mi.getItemId()) {
            case R.id.mnuFavoriate:
                database.updateFavorite(objectKey.replaceAll("/thumb", ""));
                Data.categoryMap.put("favorite", database.getFavoriteList());
                Toast.makeText(getApplicationContext(), "收藏完毕", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(mi);
        }


    }
    @Override
    protected void onResume() {
        super.onResume();

        if (Configure.accessToken != null) {
            AdView adView = (AdView)findViewById(R.id.adView);
            adView.setVisibility(adView.INVISIBLE);
        }

        setProgressBarIndeterminateVisibility(true);

        ImageListRequest imageListRequest = new ImageListRequest(Data.OSS_URL + objectKey.replaceAll("thumb/", "") + Data.INDEX_KEY);
        spiceManager.execute(imageListRequest, objectKey, DurationInMillis.ONE_DAY, new ImageListRequestListener());

        mAdapterView.setAdapter(mAdapter);
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
    protected void onStart() {
        super.onStart();
        spiceManager.start( this );
    }

    @Override
    protected void onStop() {
        spiceManager.shouldStop();
        super.onStop();
    }




    @Override
    public void onLoadMore() {
        AddItemToContainer(++currentPage, 2);

    }

    private class ImageListRequestListener implements RequestListener<ImageInfos> {
        @Override
        public void onRequestFailure(SpiceException e) {
            Toast.makeText(ImageListActivity.this, "网络不给力,错误: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onRequestSuccess(ImageInfos imageInfos) {
            imageInfoList = imageInfos.getResults();
            AddItemToContainer(currentPage, 2);
            setProgressBarIndeterminateVisibility(false);
        }
    }
}// end of class
