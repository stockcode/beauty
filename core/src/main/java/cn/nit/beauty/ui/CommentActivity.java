package cn.nit.beauty.ui;


import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

import cn.nit.beauty.R;
import cn.nit.beauty.adapter.CommentAdapter;
import cn.nit.beauty.entity.Comment;
import cn.nit.beauty.entity.PhotoGallery;
import cn.nit.beauty.entity.User;
import cn.nit.beauty.utils.ActivityUtil;
import cn.nit.beauty.utils.Constant;
import cn.nit.beauty.utils.L;
import com.actionbarsherlock.app.SherlockActivity;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_comment)
public class CommentActivity extends RoboActivity implements OnClickListener{

    @InjectView(R.id.comment_list)
	private ListView commentList;

    @InjectView(R.id.loadmore)
	private TextView footer;

    @InjectView(R.id.comment_content)
	private EditText commentContent ;

    @InjectView(R.id.comment_commit)
	private Button commentCommit;
	
	private TextView userName;
	private TextView commentItemContent;
	private ImageView commentItemImage;
	
	private ImageView userLogo;
	private ImageView myFav;
	private TextView comment;
	private TextView share;
	private TextView love;
	private TextView hate;
	
	private PhotoGallery photoGallery;
	private String commentEdit = "";
	
	private CommentAdapter mAdapter;
	
	private List<Comment> comments = new ArrayList<Comment>();
	
	private int pageNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupViews(savedInstanceState);

        setListener();

        fetchComment();
    }

	protected void setupViews(Bundle bundle) {
		// TODO Auto-generated method stub
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		photoGallery = (PhotoGallery)getIntent().getSerializableExtra("photoGallery");

        setTitle(photoGallery.getTitle());

		pageNum = 0;
		
		mAdapter = new CommentAdapter(CommentActivity.this, comments);
		commentList.setAdapter(mAdapter);
		setListViewHeightBasedOnChildren(commentList);
		commentList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ActivityUtil.show(CommentActivity.this, "po" + position);
			}
		});
		commentList.setCacheColorHint(0);
		commentList.setScrollingCacheEnabled(false);
		commentList.setScrollContainer(false);
		commentList.setFastScrollEnabled(true);
		commentList.setSmoothScrollbarEnabled(true);
	}

	protected void setListener() {
		footer.setOnClickListener(this);
		commentCommit.setOnClickListener(this);
	}

	private void fetchComment(){
		BmobQuery<Comment> query = new BmobQuery<Comment>();
		query.addWhereRelatedTo("relation", new BmobPointer(photoGallery));
		query.include("user");
		query.order("createdAt");
		query.setLimit(Constant.NUMBERS_PER_PAGE);
		query.setSkip(Constant.NUMBERS_PER_PAGE*(pageNum++));
		query.findObjects(this, new FindListener<Comment>() {
			
			@Override
			public void onSuccess(List<Comment> data) {
				// TODO Auto-generated method stub
				L.i("get comment success!" + data.size());
				if(data.size()!=0 && data.get(data.size()-1)!=null){
					
					if(data.size()<Constant.NUMBERS_PER_PAGE){
						ActivityUtil.show(CommentActivity.this, "已加载完所有评论~");
						footer.setText("暂无更多评论~");
					}
					
					mAdapter.getDataList().addAll(data);
					mAdapter.notifyDataSetChanged();
					setListViewHeightBasedOnChildren(commentList);
					L.i("refresh");
				}else{
					ActivityUtil.show(CommentActivity.this, "暂无更多评论~");
					footer.setText("暂无更多评论~");
					pageNum--;
				}
			}

			@Override
			public void onError(int arg0, String arg1) {
                L.i("get comment err!" + arg1);
				ActivityUtil.show(CommentActivity.this, "获取评论失败。请检查网络~");
				pageNum--;
			}
		});
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.loadmore:
			onClickLoadMore();
			break;
		case R.id.comment_commit:
			onClickCommit();
			break;
		default:
			break;
		}
	}  

	private void onClickLoadMore() {
		fetchComment();
	}

	
	private void onClickCommit() {
		User currentUser = BmobUser.getCurrentUser(this,User.class);
		if(currentUser != null){//已登录
			commentEdit = commentContent.getText().toString().trim();
			if(TextUtils.isEmpty(commentEdit)){
				ActivityUtil.show(this, "评论内容不能为空。");
				return;
			}
			//comment now
			publishComment(currentUser,commentEdit);
		}else{//未登录
			ActivityUtil.show(this, "发表评论前请先登录。");
			Intent intent = new Intent();
			intent.setClass(this, LoginActivity.class);
			startActivityForResult(intent, Constant.PUBLISH_COMMENT);
		}
		
	}

	private void publishComment(User user,String content){
		
		final Comment comment = new Comment();
		comment.setUser(user);
		comment.setCommentContent(content);
		comment.save(this, new SaveListener() {
			
			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				ActivityUtil.show(CommentActivity.this, "评论成功。");
				if(mAdapter.getDataList().size()<Constant.NUMBERS_PER_PAGE){
					mAdapter.getDataList().add(comment);
					mAdapter.notifyDataSetChanged();
					setListViewHeightBasedOnChildren(commentList);
				}
				commentContent.setText("");
				hideSoftInput();
				
				//将该评论与强语绑定到一起
				BmobRelation relation = new BmobRelation();
				relation.add(comment);
				photoGallery.setRelation(relation);
                photoGallery.update(CommentActivity.this, new UpdateListener() {
					
					@Override
					public void onSuccess() {
						L.i("更新评论成功。");
//						fetchData();
					}

					@Override
					public void onFailure(int arg0, String arg1) {
						L.i("更新评论失败。"+arg1);
					}
				});
				
			}

			@Override
			public void onFailure(int arg0, String arg1) {
				// TODO Auto-generated method stub
				ActivityUtil.show(CommentActivity.this, "评论失败。请检查网络~");
			}
		});
	}

	private void onClickComment() {
		commentContent.requestFocus();

		InputMethodManager imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);  

		imm.showSoftInput(commentContent, 0);  
	}
	
	private void hideSoftInput(){
		InputMethodManager imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);  

		imm.hideSoftInputFromWindow(commentContent.getWindowToken(), 0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			switch (requestCode) {
			case Constant.PUBLISH_COMMENT:
				//登录完成
				commentCommit.performClick();
				break;
			case Constant.SAVE_FAVOURITE:
				myFav.performClick();
				break;
			case Constant.GET_FAVOURITE:
				
				break;
			case Constant.GO_SETTINGS:
				userLogo.performClick();
				break;
			default:
				break;
			}
		}
		
	}


	/***
     * 动态设置listview的高度
     *  item 总布局必须是linearLayout
     * @param listView
     */
    public void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getCount()-1))
                +15;
        listView.setLayoutParams(params);
    }


}
