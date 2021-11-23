package com.lzy.imagepickerdemo.imageloader;


import com.lzy.imagepicker.loader.ImageLoader;
import ohos.aafwk.ability.Ability;
import ohos.agp.components.Image;


/**
 * ================================================
 * 作    者：jeasonlzy（廖子尧 Github地址：https://github.com/jeasonlzy0216
 * 版    本：1.0
 * 创建日期：2016/5/19
 * 描    述：
 * 修订历史：
 * ================================================
 */
public class GlideImageLoader implements ImageLoader {

    @Override
    public void displayImage(Ability ability, String path, Image imageView, int width, int height) {

//        Glide.with(ability)                             //配置上下文
//                .load(Uri.parse(path))      //设置图片路径(fix #8,文件名包含%符号 无法识别和显示)
//                .error(ResourceTable.Graphic_ic_default_image)           //设置错误图片
//                .placeholder(ResourceTable.Graphic_ic_default_image)     //设置占位图片
//                .diskCacheStrategy(DiskCacheStrategy.ALL)//缓存全尺寸
//                .into(imageView);
    }

    @Override
    public void displayImagePreview(Ability ability, String path, Image imageView, int width, int height) {
//        Glide.with(ability)                             //配置上下文
//                .load(Uri.parse(path))        //设置图片路径(fix #8,文件名包含%符号 无法识别和显示)
//                .diskCacheStrategy(DiskCacheStrategy.ALL)//缓存全尺寸
//                .into(imageView);
    }

    @Override
    public void clearMemoryCache() {
    }
}
