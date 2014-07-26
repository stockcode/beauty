package cn.nit.beauty.adapter;

import java.util.List;

import cn.nit.beauty.R;
import cn.nit.beauty.database.Category;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DragGridAdapter extends BaseAdapter {

	private Context context;
	private List<Category> lstDate;
	private TextView txt;private ImageView img;private RelativeLayout relate;
	@SuppressWarnings("unused")
	private GridView listview;

	public DragGridAdapter(Context mContext,GridView listview, List<Category> list) {
		this.context = mContext;
		lstDate = list;
		this.listview = listview;
	}

	@Override
	public int getCount() {
		return lstDate.size();
	}

	@Override
	public Object getItem(int position) {
		return lstDate.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void exchange(int startPosition, int endPosition) {
		Object endObject = getItem(endPosition);
		Object startObject = getItem(startPosition);
		lstDate.add(startPosition, (Category) endObject);
		lstDate.remove(startPosition + 1);
		lstDate.add(endPosition, (Category) startObject);
		lstDate.remove(endPosition + 1);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = LayoutInflater.from(context).inflate(R.layout.griditem_milaucher, null);
		txt = (TextView) convertView.findViewById(R.id.item_text);
		img = (ImageView) convertView.findViewById(R.id.item_img);
		relate = (RelativeLayout) convertView.findViewById(R.id.item_relate);
		Category map = lstDate.get(position);
		
		if(map!=null && map.getTITLE()==null){
			txt.setText("");
			img.setImageBitmap(null);
			//relate.setBackgroundResource(R.drawable.red_add);
		}
		else if(map!=null &&map.getTITLE().equals("none")){
			txt.setText("");
			img.setImageBitmap(null);
			relate.setBackgroundDrawable(null);
		}else {
			txt.setText(map.getTITLE().toString());
			int url = map.getCATEGORY_ICON();
			if(url!=-1)

                try {
                    img.setBackgroundResource(url);
                } catch (Exception e) {
                    e.printStackTrace();
                }

			//relate.setBackgroundResource(R.drawable.blue);
		}
		
		return convertView;
	}

}
