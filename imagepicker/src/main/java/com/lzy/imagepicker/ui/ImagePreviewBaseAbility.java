package com.lzy.imagepicker.ui;


import com.lzy.imagepicker.DataHolder;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.ResourceTable;
import com.lzy.imagepicker.adapter.ImagePageAdapter;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.util.ResUtil;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Component;
import ohos.agp.components.PageSlider;
import ohos.agp.components.Text;

import java.util.ArrayList;

/**
 * ================================================
 * 作    者：jeasonlzy（廖子尧 Github地址：https://github.com/jeasonlzy0216
 * 版    本：1.0
 * 创建日期：2016/5/19
 * 描    述：
 * 修订历史：图片预览的基类
 * ================================================
 */
public abstract class ImagePreviewBaseAbility extends ImageBaseAbility {

    protected ImagePicker imagePicker;
    protected ArrayList<ImageItem> mImageItems;      //跳转进ImagePreviewFragment的图片文件夹
    protected int mCurrentPosition = 0;              //跳转进ImagePreviewFragment时的序号，第几个图片
    protected Text mTitleCount;                  //显示当前图片的位置  例如  5/31
    protected ArrayList<ImageItem> selectedImages;   //所有已经选中的图片
    protected Component content;
    protected Component topBar;
    protected PageSlider mViewPager;
    protected ImagePageAdapter pro;
    protected boolean isFromItems = false;


    @Override
    protected void onStart(Intent intent) {
        super.onStart(intent);
        setUIContent(ResourceTable.Layout_ability_image_preview);

        mCurrentPosition = intent.getIntParam(ImagePicker.EXTRA_SELECTED_IMAGE_POSITION, 0);
        isFromItems = intent.getBooleanParam(ImagePicker.EXTRA_FROM_ITEMS, false);

        if (isFromItems) {
            // 据说这样会导致大量图片崩溃
            mImageItems = (ArrayList<ImageItem>) getIntent().getSerializableParam(ImagePicker.EXTRA_IMAGE_ITEMS);
        } else {
            // 下面采用弱引用会导致预览崩溃
            mImageItems = (ArrayList<ImageItem>) DataHolder.getInstance().retrieve(DataHolder.DH_CURRENT_IMAGE_FOLDER_ITEMS);
        }

        imagePicker = ImagePicker.getInstance();
        selectedImages = imagePicker.getSelectedImages();

        //初始化控件
        content = findComponentById(ResourceTable.Id_content);

        //因为状态栏透明后，布局整体会上移，所以给头部加上状态栏的margin值，保证头部不会被覆盖
        topBar = findComponentById(ResourceTable.Id_top_bar);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            DependentLayout.LayoutConfig params = (DependentLayout.LayoutConfig) topBar.getLayoutConfig();
//            params.topMargin = Utils.getStatusHeight(this);
//            topBar.setLayoutConfig(params);
//        }
        topBar.findComponentById(ResourceTable.Id_btn_ok).setVisibility(Component.HIDE);
        topBar.findComponentById(ResourceTable.Id_btn_back).setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component v) {
               terminateAbility();
            }
        });

        mTitleCount = (Text) findComponentById(ResourceTable.Id_tv_des);

        mViewPager = (PageSlider) findComponentById(ResourceTable.Id_viewpager);
        pro = new ImagePageAdapter(this, mImageItems);
        pro.setPhotoViewClickListener(new ImagePageAdapter.PhotoViewClickListener() {
            @Override
            public void OnPhotoTapListener(Component view, float v, float v1) {
                onImageSingleTap();
            }
        });
        mViewPager.setProvider(pro);
        mViewPager.setCurrentPage(mCurrentPosition, false);

        //初始化当前页面的状态
        mTitleCount.setText(ResUtil.getString(this,ResourceTable.String_ip_preview_image_count, mCurrentPosition + 1, mImageItems.size()));
    }


    /** 单击时，隐藏头和尾 */
    public abstract void onImageSingleTap();

//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        ImagePicker.getInstance().restoreInstanceState(savedInstanceState);
//    }
//
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        ImagePicker.getInstance().saveInstanceState(outState);
//    }
}