package cn.nit.beauty.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import cn.nit.beauty.R;
import cn.nit.beauty.database.Category;

public class ItemContentAdapter extends BaseAdapter {
	List<Category> listData;
	Context context;ListView listview;

	// private String id;

	public ItemContentAdapter(Context context,ListView listview, List<Category> tdlist) {
		super();
		this.listData = tdlist;
		this.context = context;
		this.listview =listview;
	}

	@Override
	public int getCount() {
		return listData.size();
	}

	@Override
	public Object getItem(int position) {
		return listData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.listitem_additem_content, null);
		}
        Category item = listData.get(position);
		final ListHolder holder = new ListHolder();
		holder.des = (TextView) convertView.findViewById(R.id.content_text);
		holder.choice = (TextView) convertView.findViewById(R.id.content_choice);

		holder.des.setText((String) item.getTITLE());
		holder.choice.setVisibility(item.getCHOICE()?0:8);
		return convertView;
	}

	public class ListHolder {
		public TextView des;
		public TextView choice;
	}
}
