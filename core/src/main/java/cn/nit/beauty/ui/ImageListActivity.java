package cn.nit.beauty.ui;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.lucasr.smoothie.AsyncGridView;
import org.lucasr.smoothie.ItemManager;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.listener.GetListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.nit.beauty.BeautyApplication;
import cn.nit.beauty.R;
import cn.nit.beauty.Utils;
import cn.nit.beauty.adapter.StaggeredAdapter;
import cn.nit.beauty.entity.PhotoGallery;
import cn.nit.beauty.entity.User;
import cn.nit.beauty.model.ImageInfo;
import cn.nit.beauty.model.ImageInfos;
import cn.nit.beauty.request.ImageListRequest;
import cn.nit.beauty.utils.ActivityUtil;
import cn.nit.beauty.utils.Data;
import cn.nit.beauty.utils.L;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.socialization.QuickCommentBar;

public class ImageListActivity extends BaseActivity {

    @InjectView(R.id.qcBar)
    QuickCommentBar qcBar;

    private AsyncGridView mAdapterView = null;
    private StaggeredAdapter mAdapter = null;
    private ArrayList<ImageInfo> imageInfoList = new ArrayList<ImageInfo>();
    private String objectId, objectKey, title;

    private PhotoGallery photoGallery;

    private MenuItem mnuFav;

    private User currentUser;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //这里可以取得下载的id，这样就可以知道哪个文件下载完成了。适用与多个下载任务的监听
            Toast.makeText(getApplicationContext(), "ID:" + intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0), Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * 添加内容
     *
     *            1为下拉刷新 2为加载更多
     */
    private void AddItemToContainer() {
        mAdapter.clear();

        for(int i = 0 ; i < imageInfoList.size(); i++) {
            ImageInfo imageInfo = imageInfoList.get(i);
            imageInfo.init(objectKey);
            imageInfo.setSmall(true);

            if ((objectKey.contains("origin") && i >= 3)
                    || (i >= Data.DISPLAY_COUNT)) {

                if (currentUser ==null || currentUser.hasExpired()) {
                    imageInfo.setFilter(true);
                }else if (imageInfo.isFilter()) {
                    imageInfo.setFilter(false);
                }
            }

            mAdapter.addItemTop(imageInfo);
        }
        mAdapter.notifyDataSetChanged();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_imagelist);
        ButterKnife.inject(this);

        Intent intent = getIntent();

        objectId = intent.getStringExtra("objectId");
        objectKey = intent.getStringExtra("objectKey");

        String[] strs = objectKey.split("/");
        title = strs[strs.length - 1];
        setTitle(title);


        mAdapterView = (AsyncGridView) findViewById(R.id.list);

        mAdapter = new StaggeredAdapter(this, objectKey, new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                StaggeredAdapter.ViewHolder holder = (StaggeredAdapter.ViewHolder) v.getTag();

                if (holder.imageInfo.isFilter()) {
                    if (currentUser == null) {
                        Intent intent = new Intent(ImageListActivity.this, LoginActivity.class);
                        startActivityForResult(intent, Utils.LOGIN);
                        Toast.makeText(ImageListActivity.this, "查看更多图片请先登录", Toast.LENGTH_SHORT).show();
                        return;
                    } else if (currentUser.hasExpired()) {
                        Intent intent = new Intent(ImageListActivity.this, VipProductActivity.class);
                        startActivityForResult(intent, Utils.VIP);
                        Toast.makeText(ImageListActivity.this, "有效期为" + currentUser.getExpiredDate() + "，请续费", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                final Intent intent = new Intent(ImageListActivity.this, ImagePagerActivity.class);
                intent.putExtra("folder", objectKey);
                intent.putExtra("title", title);
                intent.putExtra("imageList", imageInfoList);
                intent.putExtra("position", holder.position);
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

        ImageListRequest imageListRequest = new ImageListRequest(Data.OSS_URL + objectKey + Data.INDEX_KEY);
        getSpiceManager().execute(imageListRequest, title, DurationInMillis.ONE_WEEK, new ImageListRequestListener());

        BmobQuery<PhotoGallery> query = new BmobQuery<PhotoGallery>();

        query.getObject(this, objectId, new GetListener<PhotoGallery>() {
            @Override
            public void onSuccess(PhotoGallery gallery) {
                photoGallery = gallery;
                qcBar.setTopic(photoGallery.getObjectId(), photoGallery.getTitle(), photoGallery.getCreatedAt(), photoGallery.getTitle());
                qcBar.getBackButton().setVisibility(View.INVISIBLE);
                qcBar.setOnekeyShare(new OnekeyShare());
            }

            @Override
            public void onFailure(int i, String s) {
                L.i("get photoGallery failure !" + s);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.imagelist_action, menu);

        mnuFav = menu.findItem(R.id.mnuFavoriate);
        if (Data.containFav(objectId))
            mnuFav.setIcon(R.drawable.ic_action_fav_choose);
        else
            mnuFav.setIcon(R.drawable.ic_action_fav_normal);


        return true;
    }

    public boolean onOptionsItemSelected(MenuItem mi) {
        switch (mi.getItemId()) {
            case R.id.mnuFavoriate:
                doFav();
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

    private void doFav() {
        if (currentUser == null) {
            Intent intent = new Intent(ImageListActivity.this, LoginActivity.class);
            startActivityForResult(intent, Utils.FAVORITE);
            Toast.makeText(ImageListActivity.this, "收藏图片请先登录", Toast.LENGTH_SHORT).show();
            return;
        }

        BmobRelation favRelaton = new BmobRelation();
        if (Data.containFav(objectId)) {
            mnuFav.setIcon(R.drawable.ic_action_fav_normal);
            favRelaton.remove(photoGallery);
            Data.removeFav(photoGallery.getUrl());
            ActivityUtil.show(this, "取消收藏。");
        }
        else {
            mnuFav.setIcon(R.drawable.ic_action_fav_choose);
            favRelaton.add(photoGallery);
            Data.addFav(photoGallery.getUrl());
            ActivityUtil.show(this, "收藏成功。");
        }
        currentUser.setFavorite(favRelaton);
        currentUser.update(this, new UpdateListener() {
            @Override
            public void onSuccess() {
                L.i("收藏成功。");
            }

            @Override
            public void onFailure(int arg0, String arg1) {
                L.i("收藏失败。错误原因："+ arg1);
                ActivityUtil.show(ImageListActivity.this, "收藏失败。请检查网络~");
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();


        setProgressBarIndeterminateVisibility(true);

        currentUser = BeautyApplication.getInstance().getCurrentUser();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.LOGIN) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(ImageListActivity.this, "取消登录，已返回", Toast.LENGTH_SHORT).show();
            } else {
                AddItemToContainer();
            }
        } else if (requestCode == Utils.VIP) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(ImageListActivity.this, "取消续费，已返回", Toast.LENGTH_SHORT).show();
            } else {
                AddItemToContainer();
            }
        } else if (requestCode == Utils.FAVORITE && resultCode == RESULT_OK) {
            doFav();
        }
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
