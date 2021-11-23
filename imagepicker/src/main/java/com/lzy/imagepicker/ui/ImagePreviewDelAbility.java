package com.lzy.imagepicker.ui;


import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.ResourceTable;
import com.lzy.imagepicker.util.AnimatorUtil;
import com.lzy.imagepicker.util.ResUtil;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Component;
import ohos.agp.components.Image;
import ohos.agp.components.PageSlider;
import ohos.agp.window.dialog.CommonDialog;
import ohos.agp.window.dialog.IDialog;

/**
 * ================================================
 * 作    者：jeasonlzy（廖子尧），ikkong （ikkong@163.com）
 * 版    本：1.0
 * 创建日期：2016/5/19
 * 描    述：
 * 修订历史：预览已经选择的图片，并可以删除, 感谢 ikkong 的提交
 * ================================================
 */
public class ImagePreviewDelAbility extends ImagePreviewBaseAbility implements Component.ClickedListener {

    @Override
    protected void onStart(Intent intent) {
        super.onStart(intent);
        Image mBtnDel = (Image) findComponentById(ResourceTable.Id_btn_del);
        mBtnDel.setClickedListener(this);
        mBtnDel.setVisibility(Component.VISIBLE);
        topBar.findComponentById(ResourceTable.Id_btn_back).setClickedListener(this);

        mTitleCount.setText(ResUtil.getString(this, ResourceTable.String_ip_preview_image_count, mCurrentPosition + 1, mImageItems.size()));
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
                mTitleCount.setText(ResUtil.getString(ImagePreviewDelAbility.this, ResourceTable.String_ip_preview_image_count, mCurrentPosition + 1, mImageItems.size()));
            }

        });
    }


    @Override
    public void onClick(Component v) {
        int id = v.getId();
        if (id == ResourceTable.Id_btn_del) {
            showDeleteDialog();
        } else if (id == ResourceTable.Id_btn_back) {
            onBackPressed();
        }
    }

    /**
     * 是否删除此张图片
     */
    private void showDeleteDialog() {

        CommonDialog dialog = new CommonDialog(this);
        dialog.setTitleText("提示");
        dialog.setContentText("要删除这张照片吗？");
        dialog.setButton(0, "取消", new IDialog.ClickedListener() {
            @Override
            public void onClick(IDialog iDialog, int i) {
                dialog.hide();
            }
        });
        dialog.setButton(1, "确定", new IDialog.ClickedListener() {
            @Override
            public void onClick(IDialog iDialog, int i) {
                //移除当前图片刷新界面
                mImageItems.remove(mCurrentPosition);
                if (mImageItems.size() > 0) {
                    pro.setData(mImageItems);
                    pro.notifyDataChanged();
                    mTitleCount.setText(ResUtil.getString(ImagePreviewDelAbility.this, ResourceTable.String_ip_preview_image_count, mCurrentPosition + 1, mImageItems.size()));
                } else {
                    onBackPressed();
                }
            }
        });
        dialog.show();

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        //带回最新数据
        intent.setParam(ImagePicker.EXTRA_IMAGE_ITEMS, mImageItems);
        setResult(ImagePicker.RESULT_CODE_BACK, intent);
        terminateAbility();
        super.onBackPressed();
    }

    /**
     * 单击时，隐藏头和尾
     */
    @Override
    public void onImageSingleTap() {
        if (topBar.getVisibility() == Component.VISIBLE) {
            AnimatorUtil.topOut(topBar);
            topBar.setVisibility(Component.HIDE);
            //给最外层布局加上这个属性表示，Activity全屏显示，且状态栏被隐藏覆盖掉。
//            if (Build.VERSION.SDK_INT >= 16) content.setSystemUiVisibility(Component.SYSTEM_UI_FLAG_FULLSCREEN);
        } else {
            AnimatorUtil.topIn(topBar);
            topBar.setVisibility(Component.VISIBLE);
            //Activity全屏显示，但状态栏不会被隐藏覆盖，状态栏依然可见，Activity顶端布局部分会被状态遮住
//            if (Build.VERSION.SDK_INT >= 16) content.setSystemUiVisibility(Component.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }
}