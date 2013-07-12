package cn.nit.beauty.bus;

/**
 * Created by Administrator on 13-7-12.
 */
public class ImageChangeEvent {
    private String objectkey = "";

    public ImageChangeEvent(String objectkey) {
        this.objectkey = objectkey;
    }

    public String getObjectkey() {
        return objectkey;
    }
}
