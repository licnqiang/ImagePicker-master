package com.lzy.imagepicker.adapter;

import com.github.chrisbanes.photoview.PhotoView;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;

import java.util.ArrayList;

import ohos.aafwk.ability.Ability;
import ohos.agp.components.Component;
import ohos.agp.components.ComponentContainer;
import ohos.agp.components.PageSliderProvider;
import ohos.agp.window.service.Display;
import ohos.agp.window.service.DisplayManager;

/**
 * ================================================
 * 作    者：jeasonlzy（廖子尧 Github地址：https://github.com/jeasonlzy0216
 * 版    本：1.0
 * 创建日期：2016/5/19
 * 描    述：
 * 修订历史：
 * ================================================
 */
public class ImagePageAdapter extends PageSliderProvider {

    private int screenWidth;
    private int screenHeight;
    private ImagePicker imagePicker;
    private ArrayList<ImageItem> images = new ArrayList<>();
    private Ability ability;
    public PhotoViewClickListener listener;

    public ImagePageAdapter(Ability ability, ArrayList<ImageItem> images) {
        this.ability = ability;
        this.images = images;

        DisplayManager displayManager = DisplayManager.getInstance();
        Display display = displayManager.getDefaultDisplay(ability).get();

//        DisplayMetrics dm = Utils.getScreenPix(ability);
        screenWidth = display.getAttributes().width;
        screenHeight = display.getAttributes().height;
        imagePicker = ImagePicker.getInstance();
    }

    public void setData(ArrayList<ImageItem> images) {
        this.images = images;
    }

    public void setPhotoViewClickListener(PhotoViewClickListener listener) {
        this.listener = listener;
    }


    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public Object createPageInContainer(ComponentContainer componentContainer, int position) {
//        Image photoView = new Image(mActivity);
        PhotoView photoView = new PhotoView(ability);
        ImageItem imageItem = images.get(position);
        photoView.setLayoutConfig(new ComponentContainer.LayoutConfig(ComponentContainer.LayoutConfig.MATCH_PARENT, ComponentContainer.LayoutConfig.MATCH_PARENT));

        imagePicker.getImageLoader().displayImagePreview(ability, imageItem.uriSchema, photoView, screenWidth, screenHeight);
//        photoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
//            @Override
//            public void onPhotoTap(Component view, float x, float y) {
//            }
//        });
        photoView.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                if (listener != null) listener.OnPhotoTapListener(component, 0, 0);

            }
        });
        componentContainer.addComponent(photoView);
        return photoView;
    }

    @Override
    public void destroyPageFromContainer(ComponentContainer componentContainer, int i, Object o) {
        componentContainer.removeComponent((Component) o);
    }

    @Override
    public boolean isPageMatchToObject(Component component, Object o) {
        return component == o;
    }

    @Override
    public int getPageIndex(Object object) {
        return super.getPageIndex(object);
    }


    public interface PhotoViewClickListener {
        void OnPhotoTapListener(Component view, float v, float v1);
    }
}
