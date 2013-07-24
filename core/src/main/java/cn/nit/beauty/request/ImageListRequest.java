package cn.nit.beauty.request;

import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

import java.util.List;

import cn.nit.beauty.model.ImageInfo;
import cn.nit.beauty.model.ImageInfos;
import cn.nit.beauty.model.Index;

/**
 * Created by Administrator on 13-7-24.
 */
public class ImageListRequest extends SpringAndroidSpiceRequest<ImageInfos> {
    private String url;

    public ImageListRequest(String url) {
        super(ImageInfos.class);
        this.url = url;
    }

    @Override
    public ImageInfos loadDataFromNetwork() throws Exception {
        return getRestTemplate().getForObject( url, ImageInfos.class );
    }
}
