package cn.nit.beauty.entity;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobRelation;

import java.io.Serializable;

public class PhotoGallery extends BmobObject implements Serializable {
	private String key;
	private String category;
	private String title;
	private int commentCount = 0;

    private BmobRelation relation;

	public PhotoGallery(String key) {
		String[] strs = key.split("/");
		
		this.key = key;
		this.category = strs[0];
		this.title = strs[strs.length - 1];
	}
	public PhotoGallery() {
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public BmobRelation getRelation() {
        return relation;
    }

    public void setRelation(BmobRelation relation) {
        this.relation = relation;
    }

	public String getUrl() {
		return key + "::" + getObjectId();
	}
}
