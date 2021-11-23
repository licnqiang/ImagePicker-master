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
import com.nostra13.universalimageloader.core.assist.ImageSize;
import ohos.aafwk.ability.Ability;
import ohos.agp.components.Image;

public class UILImageLoader implements ImageLoader {

    @Override
    public void displayImage(Ability ability, String uriScheme, Image imageView, int width, int height) {

        ImageSize size = new ImageSize(width, height);
        com.nostra13.universalimageloader.core.ImageLoader.getInstance().displayImage(uriScheme, imageView, size);
    }

    @Override
    public void displayImagePreview(Ability ability, String uriScheme, Image imageView, int width, int height) {
        ImageSize size = new ImageSize(width, height);
        com.nostra13.universalimageloader.core.ImageLoader.getInstance().displayImage(uriScheme, imageView, size);
    }

    @Override
    public void clearMemoryCache() {
    }
}
