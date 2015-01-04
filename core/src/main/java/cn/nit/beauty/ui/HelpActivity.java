package cn.nit.beauty.ui;

import android.os.Bundle;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.nit.beauty.R;
import com.umeng.analytics.MobclickAgent;

public class HelpActivity extends BaseActivity {

    @InjectView(R.id.webView) WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_PROGRESS);

        setContentView(R.layout.activity_help);
        ButterKnife.inject(this);
        String url = MobclickAgent.getConfigParams(this, "about_url");

        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                //Activity和Webview根据加载程度决定进度条的进度大小
                //当加载到100%的时候 进度条自动消失
                HelpActivity.this.setProgress(progress * 100);
            }
        });
        webView.loadUrl(url);
    }
}
