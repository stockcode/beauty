package cn.nit.beauty.model;

import java.io.Serializable;

public class ImageInfo implements Serializable{


    private String objectId;
	private String key = "";
	private String title = "";
    private String bigUrl;
    private String smallUrl;
    private String filterUrl;
    private String originalUrl;

    private boolean original = false;

    private boolean filter = false;

    private boolean small = false;

    private boolean big = false;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {

        this.key = key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isOriginal() {
        return original;
    }

    public void setOriginal(boolean original) {
        reset(original);
        this.original = original;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getBigUrl() {
        return bigUrl;
    }

    public void setBigUrl(String bigUrl) {
        this.bigUrl = bigUrl;
    }

    public String getSmallUrl() {
        return smallUrl;
    }

    public void setSmallUrl(String smallUrl) {
        this.smallUrl = smallUrl;
    }

    public String getFilterUrl() {
        return filterUrl;
    }

    public void setFilterUrl(String filterUrl) {
        this.filterUrl = filterUrl;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public boolean isFilter() {
        return filter;
    }

    public void setFilter(boolean filter) {
        reset(filter);
        this.filter = filter;
    }

    public boolean isSmall() {
        return small;
    }

    public void setSmall(boolean small) {
        reset(small);
        this.small = small;
    }

    private void reset(boolean value) {
        this.small = !value;
        this.big = !value;
        this.filter = !value;
        this.original = !value;
    }

    public boolean isBig() {
        return big;
    }

    public void setBig(boolean big) {
        reset(big);
        this.big = big;
    }

    public String getUrl() {
        if (isSmall())
            return smallUrl;
        else if(isBig())
            return  bigUrl;
        else if (isFilter())
            return  filterUrl;
        else if (isOriginal())
            return  originalUrl;
        else return  null;

    }

    public void init(String baseUrl) {
        this.smallUrl = baseUrl + "smallthumb/" + this.key;
        this.bigUrl = baseUrl + "bigthumb/" + this.key;
        this.filterUrl = baseUrl + "filterthumb/" + this.key;
        this.originalUrl = baseUrl + "original/" + this.key;
    }
}
