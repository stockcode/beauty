package cn.nit.beauty.adapter;

import cn.nit.beauty.R;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.LinkedList;
import java.util.List;

import cn.nit.beauty.model.ImageInfo;
import cn.nit.beauty.ui.ImageGalleryActivity;
import cn.nit.beauty.utils.Data;
import cn.nit.beauty.widget.ScaleImageView;
import me.maxwin.view.XListView;

/**
 * Created by Administrator on 13-7-24.
 */
public class StaggeredAdapter extends BaseAdapter {
    private Context mContext;
    private LinkedList<ImageInfo> mInfos;
    private XListView mListView;
    private String folder;

    public StaggeredAdapter(Context context, XListView xListView, String folder) {
        mContext = context;
        mInfos = new LinkedList<ImageInfo>();
        mListView = xListView;
        this.folder = folder;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        ImageInfo duitangInfo = mInfos.get(position);

        if (convertView == null) {
            LayoutInflater layoutInflator = LayoutInflater.from(parent.getContext());
            convertView = layoutInflator.inflate(R.layout.infos_list, null);
            holder = new ViewHolder();
            holder.imageView = (ScaleImageView) convertView.findViewById(R.id.news_pic);
            //holder.contentView = (TextView) convertView.findViewById(R.id.news_title);
            convertView.setTag(holder);
            convertView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    ViewHolder holder = (ViewHolder) v.getTag();

                    Intent intent = new Intent(mContext, ImageGalleryActivity.class);
                    intent.putExtra("objectKey", holder.objectKey);
                    intent.putExtra("folder", folder);

                    mContext.startActivity(intent);
                }
            });
        }

        holder = (ViewHolder) convertView.getTag();

        //holder.contentView.setText(duitangInfo.getMsg());
        holder.objectKey = duitangInfo.getKey();
        ImageLoader.getInstance().displayImage(Data.OSS_URL + duitangInfo.getUrl(), holder.imageView);
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

    public void addItemLast(List<ImageInfo> datas) {
        mInfos.addAll(datas);
    }

    public void addItemTop(ImageInfo imageInfo) {
        mInfos.addFirst(imageInfo);
    }
}