package cn.nit.beauty.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.nit.beauty.R;
import cn.nit.beauty.model.ImageInfo;
import cn.nit.beauty.widget.ScaleImageView;

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
        ImageInfo imageInfo = mInfos.get(position);

        if (convertView == null) {
            LayoutInflater layoutInflator = LayoutInflater.from(parent.getContext());
            convertView = layoutInflator.inflate(R.layout.infos_list, null);
            holder = new ViewHolder(convertView);

            convertView.setTag(holder);
            convertView.setOnClickListener(mOnClickListener);
        }

        holder = (ViewHolder) convertView.getTag();

        String title = imageInfo.getTitle();
        String[] strs = title.split("/");
        title = strs[strs.length - 1];

        if (title.equals("")) holder.contentView.setVisibility(View.GONE);

        holder.contentView.setText(title);
        holder.imageInfo = imageInfo;
        holder.position = position;
        //ImageLoader.getInstance().displayImage(Data.OSS_URL + duitangInfo.getUrl(), holder.imageView);
        return convertView;
    }

    public void clear() {
        mInfos.clear();
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

    public class ViewHolder {
        @InjectView(R.id.news_pic)
        public ScaleImageView imageView;
        public ImageInfo imageInfo;
        public int position;
        @InjectView(R.id.news_title)
        TextView contentView;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}