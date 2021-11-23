package com.lzy.imagepickerdemo.imageloader;

/**
 * ================================================
 * 作    者：jeasonlzy（廖子尧）
 * 版    本：1.0
 * 创建日期：2016/3/28
 * 描    述：我的Github地址  https://github.com/jeasonlzy0216
 * 修订历史：
 * ================================================
 */


import com.lzy.imagepicker.loader.ImageLoader;
import ohos.aafwk.ability.Ability;
import ohos.agp.components.Image;

public class XUtils3ImageLoader implements ImageLoader {
    @Override
    public void displayImage(Ability ability, String uriSchema, Image imageView, int width, int height) {
//        ImageOptions options = new ImageOptions.Builder()//
//                .setLoadingDrawableId(ResourceTable.Graphic_ic_default_image)//
//                .setFailureDrawableId(ResourceTable.Graphic_ic_default_image)//
////                .setConfig(Bitmap.Config.RGB_565)//
//                .setSize(width, height)//
//                .setCrop(false)//
//                .setUseMemCache(true)//
//                .build();
//        x.image().bind(imageView, uriSchema, options);
    }

    @Override
    public void displayImagePreview(Ability ability, String uriSchema, Image imageView, int width, int height) {
//        ImageOptions options = new ImageOptions.Builder()//
////                .setConfig(Bitmap.Config.RGB_565)//
//                .setSize(width, height)//
//                .setCrop(false)//
//                .setUseMemCache(true)//
//                .build();
//        x.image().bind(imageView,uriSchema, options);
    }

    @Override
    public void clearMemoryCache() {
    }
}
