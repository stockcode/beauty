package cn.nit.beauty.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.*;
import android.preference.PreferenceManager;
import cn.nit.beauty.Helper;
import cn.nit.beauty.R;
import cn.nit.beauty.adapter.StaggeredAdapter;
import cn.nit.beauty.database.LaucherDataBase;
import cn.nit.beauty.model.ImageInfos;
import cn.nit.beauty.request.ImageListRequest;
import cn.nit.beauty.utils.Configure;
import cn.nit.beauty.utils.Data;

import android.app.DownloadManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
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

import org.lucasr.smoothie.AsyncGridView;
import org.lucasr.smoothie.ItemManager;

public class ImageListActivity extends SherlockActivity {
    private AsyncGridView mAdapterView = null;
    private StaggeredAdapter mAdapter = null;
    private List<ImageInfo> imageInfoList = new ArrayList<ImageInfo>();
    private String objectKey, title;
    private LaucherDataBase database;
    private SpiceManager spiceManager = new SpiceManager(
            GsonSpringAndroidSpiceService.class);

    private SharedPreferences settings;

    /**
     * 添加内容
     * 
     *            1为下拉刷新 2为加载更多
     */
    private void AddItemToContainer() {

        for(int i = 0 ; i < imageInfoList.size(); i++) {
            mAdapter.addItemTop(imageInfoList.get(i));
        }
        mAdapter.notifyDataSetChanged();
    }



    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_pull_to_refresh_sample);

        settings = PreferenceManager.getDefaultSharedPreferences(ImageListActivity.this);

        Intent intent = getIntent();
        objectKey = intent.getStringExtra("objectKey");

        String[] strs = objectKey.split("/");
        title = strs[strs.length - 2];
        setTitle(title);

        database = new LaucherDataBase(getApplicationContext());

        mAdapterView = (AsyncGridView) findViewById(R.id.list);

        mAdapter = new StaggeredAdapter(this, objectKey, new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                StaggeredAdapter.ViewHolder holder = (StaggeredAdapter.ViewHolder) v.getTag();

                final Intent intent = new Intent(ImageListActivity.this, ImageGalleryActivity.class);
                intent.putExtra("objectKey", holder.objectKey);
                intent.putExtra("folder", objectKey);
                intent.putExtra("title", title);

                if (Helper.isWifi(getApplicationContext()) || settings.getBoolean("notifyWIFI", false)) {
                    startActivity(intent);
                }
                else {
                    new AlertDialog.Builder(ImageListActivity.this)
                            .setTitle("温馨提示")
                            .setMessage("当前非WIFI网络，继续浏览会消耗您的流量(每张图片约1MB)")
                            .setNegativeButton("继续", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(intent);
                                }
                            })
                            .setNeutralButton("取消", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            })
                            .setPositiveButton("不再提示", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    settings.edit().putBoolean("notifyWIFI", true).apply();
                                    startActivity(intent);
                                }
                            })
                            .show();
                }



            }
        });

        mAdapterView.setAdapter(mAdapter);

        GalleryLoader loader = new GalleryLoader(this);

        ItemManager.Builder builder = new ItemManager.Builder(loader);
        builder.setPreloadItemsEnabled(true).setPreloadItemsCount(10);
        builder.setThreadPoolSize(4);
        ItemManager itemManager = builder.build();

        mAdapterView.setItemManager(itemManager);

        ImageListRequest imageListRequest = new ImageListRequest(Data.OSS_URL + objectKey + Data.INDEX_KEY);
        spiceManager.execute(imageListRequest, objectKey, DurationInMillis.ONE_DAY, new ImageListRequestListener());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.imagelist_action, menu);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem mi) {
        switch (mi.getItemId()) {
            case R.id.mnuFavoriate:
                database.updateFavorite(objectKey.replaceAll("/smallthumb", ""));
                Data.categoryMap.put("favorite", database.getFavoriteList());
                Toast.makeText(getApplicationContext(), "收藏完毕", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.mnuDownload:
                String url = Data.OSS_URL + objectKey.replaceAll("/smallthumb/", "/original.zip");
                DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                downloadManager.enqueue(request);

                registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

                        Toast.makeText(getApplicationContext(), "套图开始下载，请稍等", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(mi);
        }


    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //这里可以取得下载的id，这样就可以知道哪个文件下载完成了。适用与多个下载任务的监听
            Toast.makeText(getApplicationContext(), "ID:" + intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0), Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        //if (Configure.accessToken != null) {
            AdView adView = (AdView)findViewById(R.id.adView);
            adView.setVisibility(adView.INVISIBLE);
        //}

        setProgressBarIndeterminateVisibility(true);

        StatService.onResume(this);
    }

    @Override
	protected void onPause() {
		super.onPause();
		StatService.onPause(this);
        //unregisterReceiver(receiver);
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







    private class ImageListRequestListener implements RequestListener<ImageInfos> {
        @Override
        public void onRequestFailure(SpiceException e) {
            Toast.makeText(ImageListActivity.this, "网络不给力,错误: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onRequestSuccess(ImageInfos imageInfos) {
            imageInfoList = imageInfos.getResults();
            AddItemToContainer();
            setProgressBarIndeterminateVisibility(false);
        }
    }
}// end of class
