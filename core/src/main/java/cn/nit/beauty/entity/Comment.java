package cn.nit.beauty.entity;

import cn.bmob.v3.BmobObject;

public class Comment extends BmobObject{
	
	private User user;
	private String commentContent;
	private String replyContent;

	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public String getCommentContent() {
		return commentContent;
	}
	public void setCommentContent(String commentContent) {
		this.commentContent = commentContent;
	}

	public String getReplyContent() {
		return replyContent;
	}

	public void setReplyContent(String replyContent) {
		this.replyContent = replyContent;
	}
}
