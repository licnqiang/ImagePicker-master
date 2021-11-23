package com.lzy.imagepicker.ui;


import com.lzy.imagepicker.ResourceTable;
import com.lzy.imagepicker.util.BitmapUtil;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.util.ElementUtil;
import com.lzy.imagepicker.util.ResUtil;
import com.lzy.imagepicker.util.Utils;
import com.lzy.imagepicker.view.CropImageView;
import ohos.aafwk.ability.DataAbilityHelper;
import ohos.aafwk.ability.DataAbilityRemoteException;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Button;
import ohos.agp.components.Component;
import ohos.agp.components.ComponentState;
import ohos.agp.components.Text;
import ohos.media.image.ImageSource;
import ohos.media.image.PixelMap;
import ohos.utils.net.Uri;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * ================================================
 * 作    者：jeasonlzy（廖子尧 Github地址：https://github.com/jeasonlzy0216
 * 版    本：1.0
 * 创建日期：2016/5/19
 * 描    述：
 * 修订历史：
 * ================================================
 */
public class ImageCropAbility extends ImageBaseAbility implements Component.ClickedListener, CropImageView.OnBitmapSaveCompleteListener {

    private static final int RESULT_CANCELED = -1;
    private CropImageView mCropImageView;
    private PixelMap mBitmap;
    private boolean mIsSaveRectangle;
    private int mOutputX;
    private int mOutputY;
    private ArrayList<ImageItem> mImageItems;
    private ImagePicker imagePicker;


    @Override
    protected void onStart(Intent intent) {
        super.onStart(intent);
        imagePicker = ImagePicker.getInstance();
        setUIContent(ResourceTable.Layout_ability_image_crop);

        //初始化View
        findComponentById(ResourceTable.Id_btn_back).setClickedListener(this);
        Button btn_ok = (Button) findComponentById(ResourceTable.Id_btn_ok);
        btn_ok.setText(ResUtil.getString(this, ResourceTable.String_ip_complete));
        btn_ok.setClickedListener(this);
        new ElementUtil.StateElementBuilder()
                .addState(new int[]{ComponentState.COMPONENT_STATE_PRESSED}, ResUtil.getElement(this, ResourceTable.Graphic_bg_btn_pre))
                .addState(new int[]{ComponentState.COMPONENT_STATE_EMPTY}, ResUtil.getElement(this, ResourceTable.Graphic_bg_btn_nor))
                .bind(btn_ok);
        Text tv_des = (Text) findComponentById(ResourceTable.Id_tv_des);
        tv_des.setText(ResUtil.getString(this, ResourceTable.String_ip_photo_crop));
        mCropImageView = (CropImageView) findComponentById(ResourceTable.Id_cv_crop_image);
        mCropImageView.setOnBitmapSaveCompleteListener(this);

        //获取需要的参数
        mOutputX = imagePicker.getOutPutX();
        mOutputY = imagePicker.getOutPutY();
        mIsSaveRectangle = imagePicker.isSaveRectangle();
        mImageItems = imagePicker.getSelectedImages();
        String imagePath = mImageItems.get(0).uriSchema;

        mCropImageView.setFocusStyle(imagePicker.getStyle());
        mCropImageView.setFocusWidth(imagePicker.getFocusWidth());
        mCropImageView.setFocusHeight(imagePicker.getFocusHeight());


        ImageSource.DecodingOptions options = new ImageSource.DecodingOptions();
        options.allowPartialImage = true;
        ImageSource.SourceOptions sourceOptions = new ImageSource.SourceOptions();
        sourceOptions.formatHint = "image/png";
        PixelMap pixelMap;
        try {
            pixelMap = ImageSource.create(DataAbilityHelper.creator(this).openFile(Uri.parse(imagePath), "r"), sourceOptions).createPixelmap(options);
        } catch (DataAbilityRemoteException | FileNotFoundException e) {
            pixelMap = null;
        }

        int[] screenSze = Utils.getScreenSize(this);
//        options.sampleSize = calculateInSampleSize(options, screenSze[0], screenSze[1]);
        options.allowPartialImage = false;
        try {
            mBitmap = ImageSource.create(DataAbilityHelper.creator(this).openFile(Uri.parse(imagePath), "r"), sourceOptions).createPixelmap(options);
        } catch (DataAbilityRemoteException | FileNotFoundException e) {
            mBitmap = null;
        }
        mCropImageView.setPixelMap(mCropImageView.rotate(mBitmap, BitmapUtil.getBitmapDegree(this, imagePath)));
        //缩放图片
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//        BitmapFactory.decodeFile(imagePath, options);
//        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
//        options.inSampleSize = calculateInSampleSize(options, displayMetrics.widthPixels, displayMetrics.heightPixels);
//        options.inJustDecodeBounds = false;
//        mBitmap = BitmapFactory.decodeFile(imagePath, options);
////        mCropImageView.setImageBitmap(mBitmap);
//        //设置默认旋转角度
//        mCropImageView.setImageBitmap(mCropImageView.rotate(mBitmap, BitmapUtil.getBitmapDegree(imagePath)));
    }


    public int calculateInSampleSize(ImageSource.DecodingOptions options, int reqWidth, int reqHeight) {
        int width = options.desiredSize.width;
        int height = options.desiredSize.height;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = width / reqWidth;
            } else {
                inSampleSize = height / reqHeight;
            }
        }
        return inSampleSize;
    }

    @Override
    public void onClick(Component v) {
        int id = v.getId();
        if (id == ResourceTable.Id_btn_back) {
            setResult(RESULT_CANCELED, null);
            terminateAbility();
        } else if (id == ResourceTable.Id_btn_ok) {
            mCropImageView.saveBitmapToFile(imagePicker.getCropCacheFolder(this), mOutputX, mOutputY, mIsSaveRectangle);
        }
    }

    @Override
    public void onBitmapSaveSuccess(File file) {
//        Toast.makeText(ImageCropActivity.this, "裁剪成功:" + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();

        //裁剪后替换掉返回数据的内容，但是不要改变全局中的选中数据
        mImageItems.remove(0);
        ImageItem imageItem = new ImageItem();
        imageItem.uriSchema = Uri.getUriFromFile(file).toString();
        mImageItems.add(imageItem);

        Intent intent = new Intent();
        intent.setParam(ImagePicker.EXTRA_RESULT_ITEMS, mImageItems);

        setResult(ImagePicker.RESULT_CODE_ITEMS, intent);   //单选不需要裁剪，返回数据
        terminateAbility();
    }

    @Override
    public void onBitmapSaveError(File file) {

    }

    @Override
    protected void onStop() {
        super.onStop();
        mCropImageView.setOnBitmapSaveCompleteListener(null);
        if (null != mBitmap && !mBitmap.isReleased()) {
            mBitmap.release();
            mBitmap = null;
        }
    }

}
