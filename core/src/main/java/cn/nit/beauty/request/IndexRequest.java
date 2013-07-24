package cn.nit.beauty.request;

import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

import cn.nit.beauty.model.Index;

/**
 * Created by Administrator on 13-7-24.
 */
public class IndexRequest extends SpringAndroidSpiceRequest<Index> {
    private String url;

    public IndexRequest(String url) {
        super(Index.class);
        this.url = url;
    }

    @Override
    public Index loadDataFromNetwork() throws Exception {
        return getRestTemplate().getForObject( url, Index.class );
    }
}
