package cn.nit.beauty.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

import cn.nit.beauty.R;
import cn.nit.beauty.model.ImageInfo;
import cn.nit.beauty.utils.Data;
import uk.co.senab.photoview.PhotoView;

/**
 * Created by Administrator on 13-7-25.
 */
public class ImageFragment extends Fragment {
    int position;
    String imageSrc;
    PhotoView photoView;
    ProgressBar spinner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imageSrc = getArguments().getString("imageSrc");
            position = getArguments().getInt("position");
        }
    }

    public static ImageFragment init(String imageSrc, int position) {
        ImageFragment imageFragment = new ImageFragment();
        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString("imageSrc", imageSrc);
        args.putInt("position", position);
        imageFragment.setArguments(args);
        return  imageFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View imageLayout = inflater.inflate(R.layout.item_pager_image, container, false);

        //ImageInfo imageInfo = mInfos.get(position);
        //String imageSrc = imageInfo.getUrl();

        photoView = (PhotoView) imageLayout.findViewById(R.id.image);

        final TextView tvInfo = (TextView) imageLayout.findViewById(R.id.tvInfo);
        //tvInfo.setText((position+1) + "/" + mInfos.size());
        spinner = (ProgressBar) imageLayout.findViewById(R.id.loading);


        imageLayout.setTag(position);
        return imageLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        ImageLoader.getInstance().displayImage(Data.OSS_URL + imageSrc, photoView, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                spinner.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                String message = null;
                switch (failReason.getType()) {
                    case IO_ERROR:
                        message = "读取错误";
                        break;
                    case DECODING_ERROR:
                        message = "图片解码错误";
                        break;
                    case NETWORK_DENIED:
                        message = "下载图片被拒绝";
                        break;
                    case OUT_OF_MEMORY:
                        message = "内存溢出";
                        break;
                    case UNKNOWN:
                        message = "未知错误";
                        break;
                }
                //Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();

                spinner.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                spinner.setVisibility(View.GONE);
                //float size = loadedImage.getByteCount() / (1024*1024);
                //tvInfo.setText(tvInfo.getText() + "    " + df.format(size) + "MB");
            }
        });
    }
}
