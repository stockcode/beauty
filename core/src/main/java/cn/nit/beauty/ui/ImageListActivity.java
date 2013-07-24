package cn.nit.beauty.ui;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import cn.nit.beauty.Helper;
import cn.nit.beauty.R;
import cn.nit.beauty.adapter.StaggeredAdapter;
import cn.nit.beauty.model.ImageInfos;
import cn.nit.beauty.request.ImageListRequest;
import cn.nit.beauty.utils.Configure;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import cn.nit.beauty.model.ImageInfo;
import cn.nit.beauty.widget.ScaleImageView;

import com.baidu.mobads.AdView;
import com.baidu.mobstat.StatService;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.octo.android.robospice.GsonSpringAndroidSpiceService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

public class ImageListActivity extends FragmentActivity implements IXListViewListener {
    private XListView mAdapterView = null;
    private StaggeredAdapter mAdapter = null;
    private int currentPage = 0;
    private List<ImageInfo> imageInfoList = new ArrayList<ImageInfo>();
    private String objectKey;

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
        mAdapterView.stopRefresh();
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


        mAdapterView = (XListView) findViewById(R.id.list);
        mAdapterView.setPullLoadEnable(true);
        mAdapterView.setXListViewListener(this);

        mAdapter = new StaggeredAdapter(this, mAdapterView, objectKey);
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (Configure.accessToken != null) {
            AdView adView = (AdView)findViewById(R.id.adView);
            adView.setVisibility(adView.INVISIBLE);
        }

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
    public void onRefresh() {
        AddItemToContainer(++currentPage, 1);

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
        }
    }
}// end of class
