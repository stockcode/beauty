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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import cn.nit.beauty.model.ImageInfo;
import cn.nit.beauty.ui.ImageGalleryActivity;
import cn.nit.beauty.utils.Data;
import cn.nit.beauty.widget.ScaleImageView;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by Administrator on 13-7-24.
 */
public class StaggeredAdapter extends BaseAdapter {
    private Context mContext;
    private List<ImageInfo> mInfos;
    private String folder;
    private View.OnClickListener mOnClickListener;

    public StaggeredAdapter(Context context, String folder, View.OnClickListener onClickListener) {
        mContext = context;
        mInfos = new ArrayList<ImageInfo>();
        this.folder = folder;
        mOnClickListener = onClickListener;
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
            holder.contentView = (TextView) convertView.findViewById(R.id.news_title);
            convertView.setTag(holder);
            convertView.setOnClickListener(mOnClickListener);
        }

        holder = (ViewHolder) convertView.getTag();

        String title = duitangInfo.getTitle();
        String[] strs = title.split("/");
        holder.contentView.setText(strs[strs.length-1]);
        holder.objectKey = title;
        //ImageLoader.getInstance().displayImage(Data.OSS_URL + duitangInfo.getUrl(), holder.imageView);
        return convertView;
    }

    public void clear() {
        mInfos.clear();
    }

    public class ViewHolder {
        public ScaleImageView imageView;
        TextView contentView;
        TextView timeView;
        public String objectKey;
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
        mInfos.add(imageInfo);
    }
}