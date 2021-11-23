package com.lzy.imagepicker.ui;


import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.ResourceTable;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.util.AnimatorUtil;
import com.lzy.imagepicker.util.ElementUtil;
import com.lzy.imagepicker.util.Formatter;
import com.lzy.imagepicker.util.ResUtil;
import com.lzy.imagepicker.view.SuperCheckBox;
import ohos.aafwk.content.Intent;
import ohos.agp.components.*;
import ohos.agp.components.element.StateElement;

/**
 * ================================================
 * 作    者：jeasonlzy（廖子尧 Github地址：https://github.com/jeasonlzy0216
 * 版    本：1.0
 * 创建日期：2016/5/19
 * 描    述：
 * 修订历史：
 * ================================================
 */
public class ImagePreviewAbility extends ImagePreviewBaseAbility implements ImagePicker.OnImageSelectedListener, Component.ClickedListener, AbsButton.CheckedStateChangedListener {

    public static final String ISORIGIN = "isOrigin";

    private boolean isOrigin;                      //是否选中原图
    private SuperCheckBox mCbCheck;                //是否选中当前图片的Checkbox
    private SuperCheckBox mCbOrigin;               //原图
    private Button mBtnOk;                         //确认图片的选择
    private Component bottomBar;
    private Component marginView;

    @Override
    protected void onStart(Intent intent) {
        super.onStart(intent);
        isOrigin = intent.getBooleanParam(ImagePreviewAbility.ISORIGIN, false);
        imagePicker.addOnImageSelectedListener(this);
        mBtnOk = (Button) findComponentById(ResourceTable.Id_btn_ok);
        mBtnOk.setVisibility(Component.VISIBLE);
        mBtnOk.setClickedListener(this);
        new ElementUtil.StateElementBuilder()
                .addState(new int[]{ComponentState.COMPONENT_STATE_PRESSED}, ResUtil.getElement(this, ResourceTable.Graphic_bg_btn_pre))
                .addState(new int[]{ComponentState.COMPONENT_STATE_EMPTY}, ResUtil.getElement(this, ResourceTable.Graphic_bg_btn_nor))
                .bind(mBtnOk);

        bottomBar = findComponentById(ResourceTable.Id_bottom_bar);
        bottomBar.setVisibility(Component.VISIBLE);

        mCbCheck = (SuperCheckBox) findComponentById(ResourceTable.Id_cb_check);
        mCbOrigin = (SuperCheckBox) findComponentById(ResourceTable.Id_cb_origin);
        marginView = findComponentById(ResourceTable.Id_margin_bottom);
        mCbOrigin.setText(ResUtil.getString(this, ResourceTable.String_ip_origin));
        mCbOrigin.setCheckedStateChangedListener(this);
        mCbOrigin.setChecked(isOrigin);
        StateElement stateElement = new StateElement();
        stateElement.addState(new int[]{ComponentState.COMPONENT_STATE_CHECKED}, ResUtil.getPixelMapDrawable(this, ResourceTable.Media_checkbox_checked));
        stateElement.addState(new int[]{ComponentState.COMPONENT_STATE_EMPTY}, ResUtil.getPixelMapDrawable(this, ResourceTable.Media_checkbox_normal));
        mCbCheck.setButtonElement(stateElement);
        //初始化当前页面的状态
        onImageSelected(0, null, false);
        ImageItem item = mImageItems.get(mCurrentPosition);
        boolean isSelected = imagePicker.isSelect(item);
        mTitleCount.setText(ResUtil.getString(this, ResourceTable.String_ip_preview_image_count, mCurrentPosition + 1, mImageItems.size()));
        mCbCheck.setChecked(isSelected);
        //滑动ViewPager的时候，根据外界的数据改变当前的选中状态和当前的图片的位置描述文本
        mViewPager.addPageChangedListener(new PageSlider.PageChangedListener() {
            @Override
            public void onPageSliding(int i, float v, int i1) {

            }

            @Override
            public void onPageSlideStateChanged(int i) {

            }

            @Override
            public void onPageChosen(int position) {
                mCurrentPosition = position;
                ImageItem item = mImageItems.get(mCurrentPosition);
                boolean isSelected = imagePicker.isSelect(item);

                mCbCheck.setChecked(isSelected);
                mTitleCount.setText(ResUtil.getString(getContext(), ResourceTable.String_ip_preview_image_count, mCurrentPosition + 1, mImageItems.size()));
            }

        });
        //当点击当前选中按钮的时候，需要根据当前的选中状态添加和移除图片
        mCbCheck.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component v) {
                ImageItem imageItem = mImageItems.get(mCurrentPosition);
                int selectLimit = imagePicker.getSelectLimit();
                if (mCbCheck.isChecked() && selectedImages.size() >= selectLimit) {

//                    Toast.makeText(ImagePreviewActivity.this, getString(ResourceTable.String_ip_select_limit, selectLimit), Toast.LENGTH_SHORT).show();
                    mCbCheck.setChecked(false);
                } else {
                    boolean isChecked = mCbCheck.isChecked();

                    imagePicker.addSelectedImageItem(mCurrentPosition, imageItem, !isChecked);
                }
            }
        });
    }


    /**
     * 图片添加成功后，修改当前图片的选中数量
     * 当调用 addSelectedImageItem 或 deleteSelectedImageItem 都会触发当前回调
     * @param isAdd 是否天机
     * @param item 图片对象
     * @param position 位置
     */
    @Override
    public void onImageSelected(int position, ImageItem item, boolean isAdd) {
        if (imagePicker.getSelectImageCount() > 0) {
            mBtnOk.setText(ResUtil.getString(this, ResourceTable.String_ip_select_complete, imagePicker.getSelectImageCount(), imagePicker.getSelectLimit()));
        } else {
            mBtnOk.setText(ResUtil.getString(this, ResourceTable.String_ip_complete));
        }

        if (mCbOrigin.isChecked()) {
            long size = 0;
            for (ImageItem imageItem : selectedImages)
                size += imageItem.size;
            String fileSize = Formatter.formatFileSize(this, size);
            mCbOrigin.setText(ResUtil.getString(this, ResourceTable.String_ip_origin_size, fileSize));
        }
    }

    @Override
    public void onClick(Component v) {
        int id = v.getId();
        if (id == ResourceTable.Id_btn_ok) {
            if (imagePicker.getSelectedImages().size() == 0) {
                mCbCheck.setChecked(true);
                ImageItem imageItem = mImageItems.get(mCurrentPosition);
                imagePicker.addSelectedImageItem(mCurrentPosition, imageItem, mCbCheck.isChecked());
            }
            Intent intent = new Intent();
            intent.setParam(ImagePicker.EXTRA_RESULT_ITEMS, imagePicker.getSelectedImages());
            setResult(ImagePicker.RESULT_CODE_ITEMS, intent);
            terminateAbility();

        } else if (id == ResourceTable.Id_btn_back) {
            Intent intent = new Intent();
            intent.setParam(ImagePreviewAbility.ISORIGIN, isOrigin);
            setResult(ImagePicker.RESULT_CODE_BACK, intent);
            terminateAbility();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.setParam(ImagePreviewAbility.ISORIGIN, isOrigin);
        setResult(ImagePicker.RESULT_CODE_BACK, intent);
        terminateAbility();
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        imagePicker.removeOnImageSelectedListener(this);
        super.onStop();
    }

    /**
     * 单击时，隐藏头和尾
     */
    @Override
    public void onImageSingleTap() {
        if (topBar.getVisibility() == Component.VISIBLE) {
            AnimatorUtil.topOut(topBar);
            AnimatorUtil.fadeOut(bottomBar);
            topBar.setVisibility(Component.HIDE);
            bottomBar.setVisibility(Component.HIDE);
        } else {
            AnimatorUtil.topIn(topBar);
            AnimatorUtil.fadeIn(bottomBar);
            topBar.setVisibility(Component.VISIBLE);
            bottomBar.setVisibility(Component.VISIBLE);
        }
    }

    @Override
    public void onCheckedChanged(AbsButton absButton, boolean b) {
        int id = absButton.getId();
        if (id == ResourceTable.Id_cb_origin) {
            if (b) {
                long size = 0;
                for (ImageItem item : selectedImages)
                    size += item.size;

                String fileSize = Formatter.formatFileSize(this, size);
                isOrigin = true;
                mCbOrigin.setText(ResUtil.getString(this, ResourceTable.String_ip_origin_size, fileSize));
            } else {
                isOrigin = false;
                mCbOrigin.setText(ResUtil.getString(this, ResourceTable.String_ip_origin));
            }
        }
    }
}
