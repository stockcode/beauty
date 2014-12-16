package cn.nit.beauty.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cn.nit.beauty.BeautyApplication;
import cn.nit.beauty.R;
import cn.nit.beauty.entity.Comment;
import cn.nit.beauty.utils.DateUtil;
import cn.nit.beauty.utils.L;
import com.nostra13.universalimageloader.core.ImageLoader;
import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends BaseContentAdapter<Comment>{

	public CommentAdapter(Context context, List<Comment> list) {
		super(context, list);
	}

	@Override
	public View getConvertView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder viewHolder;
		if(convertView == null){
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.comment_item, null);
			viewHolder.userName = (TextView)convertView.findViewById(R.id.userName_comment);
			viewHolder.commentDate = (TextView)convertView.findViewById(R.id.comment_date);
			viewHolder.commentContent = (TextView)convertView.findViewById(R.id.content_comment);
			viewHolder.replycomment = (TextView)convertView.findViewById(R.id.reply_comment);
			viewHolder.userIcon = (CircleImageView) convertView.findViewById(R.id.user_icon_image);
			viewHolder.index = (TextView)convertView.findViewById(R.id.index_comment);
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder)convertView.getTag();
		}
		
		final Comment comment = dataList.get(position);
		if(comment.getUser()!=null){
			viewHolder.userName.setText(comment.getUser().getNickname());
			L.i("NICKNAME:" + comment.getUser().getNickname());
		}else{
			viewHolder.userName.setText("丽友");
		}
		viewHolder.index.setText((position+1)+"楼");
		viewHolder.commentContent.setText(comment.getCommentContent());
		viewHolder.commentDate.setText(DateUtil.getStandardDate(comment.getCreatedAt()));

		if (comment.getReplyContent() != null) {
			viewHolder.replycomment.setText(comment.getReplyContent());
			viewHolder.replycomment.setVisibility(View.VISIBLE);
		}

		if(null != comment.getUser().getAvatar()){
			ImageLoader.getInstance()
					.displayImage(comment.getUser().getAvatar().getFileUrl(), viewHolder.userIcon,
							BeautyApplication.getInstance().getOptions(R.drawable.icon));
		}

		return convertView;
	}

	public static class ViewHolder{
		public TextView userName;
		public TextView commentContent;
		public TextView replycomment;
		public TextView commentDate;
		public TextView index;
		public CircleImageView userIcon;
	}
}