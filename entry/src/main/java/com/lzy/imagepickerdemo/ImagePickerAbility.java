package com.lzy.imagepickerdemo;


import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.ui.ImageGridAbility;
import com.lzy.imagepicker.util.Utils;
import com.lzy.imagepicker.view.CropImageView;
import com.lzy.imagepickerdemo.imageloader.UILImageLoader;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;
import ohos.agp.colors.RgbColor;
import ohos.agp.components.*;
import ohos.agp.components.element.ShapeElement;
import ohos.agp.utils.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * ================================================
 * 作    者：jeasonlzy（廖子尧 Github地址：https://github.com/jeasonlzy0216
 * 版    本：1.0
 * 创建日期：2016/5/19
 * 描    述：
 * 修订历史：
 * ================================================
 */
public class ImagePickerAbility extends Ability implements Slider.ValueChangedListener, AbsButton.CheckedStateChangedListener, Component.ClickedListener {

    private ImagePicker imagePicker;

    private Checkbox rb_uil;
    //    private Checkbox rb_glide;
//    private Checkbox rb_picasso;
//    private Checkbox rb_fresco;
//    private Checkbox rb_xutils3;
//    private Checkbox rb_xutils;
    private Checkbox rb_single_select;
    private Checkbox rb_muti_select;
    private Checkbox rb_crop_square;
    private Checkbox rb_crop_circle;
    private Text tv_select_limit;
    private ListContainer gridView;
    private TextField et_crop_width;
    private TextField et_crop_height;
    private TextField et_crop_radius;
    private TextField et_outputx;
    private TextField et_outputy;
    public static DisplayImageOptions imageLoaderOptions = new DisplayImageOptions.Builder()//
            .showImageOnLoading(com.lzy.imagepicker.ResourceTable.Graphic_ic_default_image)         //设置图片在下载期间显示的图片
            .showImageForEmptyUri(com.lzy.imagepicker.ResourceTable.Graphic_ic_default_image)       //设置图片Uri为空或是错误的时候显示的图片
            .showImageOnFail(com.lzy.imagepicker.ResourceTable.Graphic_ic_default_image)            //设置图片加载/解码过程中错误时候显示的图片
            .cacheInMemory(true)                                //设置下载的图片是否缓存在内存中
            .cacheOnDisk(true)                                  //设置下载的图片是否缓存在SD卡中
            .build();


    @Override
    protected void onStart(Intent intent) {
        super.onStart(intent);
        setUIContent(ResourceTable.Layout_activity_image_picker);
        ImageLoaderConfiguration config = ImageLoaderConfiguration.createDefault(this);
        ImageLoader.getInstance().init(config);     //UniversalImageLoader初始化
        ImagePicker.setTakePhotoAbility(CameraAbility.class);
        imagePicker = ImagePicker.getInstance();
        imagePicker.setImageLoader(new UILImageLoader());

        rb_uil = (Checkbox) findComponentById(ResourceTable.Id_rb_uil);
//        rb_glide = (Checkbox) findComponentById(ResourceTable.Id_rb_glide);
//        rb_picasso = (Checkbox) findComponentById(ResourceTable.Id_rb_picasso);
//        rb_fresco = (Checkbox) findComponentById(ResourceTable.Id_rb_fresco);
//        rb_xutils3 = (Checkbox) findComponentById(ResourceTable.Id_rb_xutils3);
//        rb_xutils = (Checkbox) findComponentById(ResourceTable.Id_rb_xutils);
        rb_single_select = (Checkbox) findComponentById(ResourceTable.Id_rb_single_select);
        rb_muti_select = (Checkbox) findComponentById(ResourceTable.Id_rb_muti_select);
        rb_crop_square = (Checkbox) findComponentById(ResourceTable.Id_rb_crop_square);
        rb_crop_circle = (Checkbox) findComponentById(ResourceTable.Id_rb_crop_circle);
        rb_uil.setChecked(true);
        rb_single_select.setChecked(true);
        rb_crop_circle.setChecked(true);

        et_crop_width = (TextField) findComponentById(ResourceTable.Id_et_crop_width);
        et_crop_width.setText("280");
        et_crop_height = (TextField) findComponentById(ResourceTable.Id_et_crop_height);
        et_crop_height.setText("280");
        et_crop_radius = (TextField) findComponentById(ResourceTable.Id_et_crop_radius);
        et_crop_radius.setText("140");
        et_outputx = (TextField) findComponentById(ResourceTable.Id_et_outputx);
        et_outputx.setText("800");
        et_outputy = (TextField) findComponentById(ResourceTable.Id_et_outputy);
        et_outputy.setText("800");

        tv_select_limit = (Text) findComponentById(ResourceTable.Id_tv_select_limit);
        Slider sb_select_limit = (Slider) findComponentById(ResourceTable.Id_sb_select_limit);
        sb_select_limit.setMaxValue(15);
        sb_select_limit.setValueChangedListener(this);
        sb_select_limit.setProgressValue(9);

        Checkbox cb_show_camera = (Checkbox) findComponentById(ResourceTable.Id_cb_show_camera);
        cb_show_camera.setCheckedStateChangedListener(this);
        cb_show_camera.setChecked(true);
        Checkbox cb_crop = (Checkbox) findComponentById(ResourceTable.Id_cb_crop);
        cb_crop.setCheckedStateChangedListener(this);
        cb_crop.setChecked(true);
        Checkbox cb_isSaveRectangle = (Checkbox) findComponentById(ResourceTable.Id_cb_isSaveRectangle);
        cb_isSaveRectangle.setCheckedStateChangedListener(this);
        cb_isSaveRectangle.setChecked(true);

        Button btn_open_gallery = (Button) findComponentById(ResourceTable.Id_btn_open_gallery);
        btn_open_gallery.setClickedListener(this);
        gridView = (ListContainer) findComponentById(ResourceTable.Id_gridview);
        TableLayoutManager tableLayoutManager = new TableLayoutManager();
        tableLayoutManager.setColumnCount(3);
        gridView.setLayoutManager(tableLayoutManager);
    }


    ArrayList<ImageItem> images = null;

    @Override
    protected void onAbilityResult(int requestCode, int resultCode, Intent resultData) {
        try {
            super.onAbilityResult(requestCode, resultCode, resultData);
            if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
                if (resultData != null && requestCode == 100) {
                    images = (ArrayList<ImageItem>) resultData.getSerializableParam(ImagePicker.EXTRA_RESULT_ITEMS);
                    MyAdapter adapter = new MyAdapter(images);
                    gridView.setItemProvider(adapter);
                }
            }
        }catch (Exception e){
            images = new ArrayList<>();
        }

    }

    @Override
    public void onProgressUpdated(Slider slider, int progress, boolean b) {
        tv_select_limit.setText(String.valueOf(progress));
        imagePicker.setSelectLimit(progress);
    }

    @Override
    public void onTouchStart(Slider slider) {

    }

    @Override
    public void onTouchEnd(Slider slider) {

    }

    @Override
    public void onCheckedChanged(AbsButton absButton, boolean isChecked) {
        switch (absButton.getId()) {
            case ResourceTable.Id_cb_show_camera:
                imagePicker.setShowCamera(isChecked);
                break;
            case ResourceTable.Id_cb_crop:
                imagePicker.setCrop(isChecked);
                break;
            case ResourceTable.Id_cb_isSaveRectangle:
                imagePicker.setSaveRectangle(isChecked);
                break;
        }
    }

    @Override
    public void onClick(Component component) {
        switch (component.getId()) {
            case ResourceTable.Id_btn_open_gallery:
                if (rb_uil.isChecked()) imagePicker.setImageLoader(new UILImageLoader());
//                else if (rb_glide.isChecked()) imagePicker.setImageLoader(new GlideImageLoader());
//                else if (rb_picasso.isChecked()) imagePicker.setImageLoader(new PicassoImageLoader());
//                else if (rb_fresco.isChecked()) imagePicker.setImageLoader(new GlideImageLoader());
//                else if (rb_xutils3.isChecked()) imagePicker.setImageLoader(new XUtils3ImageLoader());
//                else if (rb_xutils.isChecked()) imagePicker.setImageLoader(new GlideImageLoader());

                if (rb_single_select.isChecked()) imagePicker.setMultiMode(false);
                else if (rb_muti_select.isChecked()) imagePicker.setMultiMode(true);
                boolean isMultiMode = imagePicker.isMultiMode();

                if (rb_crop_square.isChecked()) {
                    imagePicker.setStyle(CropImageView.Style.RECTANGLE);
                    Integer width = Integer.valueOf(et_crop_width.getText().toString());
                    Integer height = Integer.valueOf(et_crop_height.getText().toString());
//                    width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, width, getResources().getDisplayMetrics());
//                    height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height, getResources().getDisplayMetrics());
                    width = Utils.vp2px(width, this);
                    height = Utils.vp2px(height, this);
                    imagePicker.setFocusWidth(width);
                    imagePicker.setFocusHeight(height);
                } else if (rb_crop_circle.isChecked()) {
                    imagePicker.setStyle(CropImageView.Style.CIRCLE);
                    Integer radius = Integer.valueOf(et_crop_radius.getText().toString());
//                    radius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, radius, getResources().getDisplayMetrics());
                    radius = Utils.vp2px(radius, this);
                    imagePicker.setFocusWidth(radius * 2);
                    imagePicker.setFocusHeight(radius * 2);
                }

                imagePicker.setOutPutX(Integer.valueOf(et_outputx.getText().toString()));
                imagePicker.setOutPutY(Integer.valueOf(et_outputy.getText().toString()));

                Intent intent = new Intent();
                Operation operation = new Intent.OperationBuilder()
                        .withDeviceId("")
                        .withBundleName(getBundleName())
                        .withAbilityName(ImageGridAbility.class.getName())
                        .build();
                intent.setOperation(operation);
                intent.setParam(ImageGridAbility.EXTRAS_IMAGES, images);
                //ImagePicker.getInstance().setSelectedImages(images);
                startAbilityForResult(intent, 100);
                break;
        }

    }


    private class MyAdapter extends BaseItemProvider {

        private List<ImageItem> items;

        public MyAdapter(List<ImageItem> items) {
            this.items = items;
        }

        public void setData(List<ImageItem> items) {
            this.items = items;
            notifyDataChanged();
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public ImageItem getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Component getComponent(int position, Component component, ComponentContainer componentContainer) {
            Image imageView;
            int size = gridView.getWidth() / 3;
            if (component == null) {
                imageView = new Image(ImagePickerAbility.this);
                TableLayout.LayoutConfig params = new TableLayout.LayoutConfig(size, size);
                imageView.setLayoutConfig(params);
                ShapeElement element = new ShapeElement();
                element.setRgbColor(RgbColor.fromArgbInt(Color.getIntColor("#88888888")));
                imageView.setBackground(element);
            } else {
                imageView = (Image) component;
            }
            imagePicker.getImageLoader().displayImage(ImagePickerAbility.this, getItem(position).uriSchema, imageView, size, size);
            return imageView;
        }

    }
}
