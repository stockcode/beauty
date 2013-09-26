/*
 * Copyright (C) 2012 Lucas Rocha
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.nit.beauty.ui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.lucasr.smoothie.SimpleItemLoader;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.support.v4.util.LruCache;
import android.view.View;
import android.widget.Adapter;

import com.nostra13.universalimageloader.core.ImageLoader;

import cn.nit.beauty.adapter.StaggeredAdapter;
import cn.nit.beauty.model.ImageInfo;
import cn.nit.beauty.utils.Data;

public class GalleryLoader extends SimpleItemLoader<String, String> {
    private final Context mContext;

    public GalleryLoader(Context context) {
        mContext = context;
    }

    @Override
    public String loadItemFromMemory(String url) {
        return url;
    }

    @Override
    public String getItemParams(Adapter adapter, int position) {

        ImageInfo imageInfo = (ImageInfo) adapter.getItem(position);
        return imageInfo.getUrl();

    }


    @Override
    public String loadItem(String url) {
        return url;
    }

    @Override
    public void displayItem(View itemView, String result, boolean fromMemory) {
        if (result == null) {
            return;
        }

        StaggeredAdapter.ViewHolder holder = (StaggeredAdapter.ViewHolder) itemView.getTag();
        ImageLoader.getInstance().displayImage(Data.OSS_URL + result, holder.imageView);
//
//        BitmapDrawable imageDrawable = new BitmapDrawable(itemView.getResources(), result);
//
//        if (fromMemory) {
//            holder.image.setImageDrawable(imageDrawable);
//        } else {
//            BitmapDrawable emptyDrawable = new BitmapDrawable(itemView.getResources());
//
//            TransitionDrawable fadeInDrawable =
//                    new TransitionDrawable(new Drawable[] { emptyDrawable, imageDrawable });
//
//            holder.image.setImageDrawable(fadeInDrawable);
//            fadeInDrawable.startTransition(200);
//        }
    }
}
