package com.lzy.imagepicker.ui;


import com.lzy.imagepicker.*;
import com.lzy.imagepicker.adapter.ImageFolderAdapter;
import com.lzy.imagepicker.adapter.ImageRecyclerProvider;
import com.lzy.imagepicker.adapter.ImageRecyclerProvider.OnImageItemClickListener;
import com.lzy.imagepicker.bean.ImageFolder;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.util.ElementUtil;
import com.lzy.imagepicker.util.ResUtil;
import com.lzy.imagepicker.view.FolderPopUpWindow;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;
import ohos.agp.components.*;
import ohos.agp.utils.Color;
import ohos.bundle.IBundleManager;
import ohos.security.SystemPermission;

import java.util.ArrayList;
import java.util.List;

/**
 * ================================================
 * 作    者：jeasonlzy（廖子尧 Github地址：https://github.com/jeasonlzy0216
 * 版    本：1.0
 * 创建日期：2016/5/19
 * 描    述：
 * 修订历史：
 * 2017-03-17
 *
 * @author nanchen
 * 新增可直接传递是否裁剪参数，以及直接拍照
 * ================================================
 */
public class ImageGridAbility extends ImageBaseAbility implements ImageDataSource.OnImagesLoadedListener, OnImageItemClickListener, ImagePicker.OnImageSelectedListener, Component.ClickedListener {

    public static final int REQUEST_PERMISSION_STORAGE = 0x01;
    public static final int REQUEST_PERMISSION_CAMERA = 0x02;
    public static final String EXTRAS_TAKE_PICKERS = "TAKE";
    public static final String EXTRAS_IMAGES = "IMAGES";

    private ImagePicker imagePicker;

    private boolean isOrigin = false;  //是否选中原图
    private Component mFooterBar;     //底部栏
    private Button mBtnOk;       //确定按钮
    private Image mBtnBack;       //确定按钮
    private Component mllDir; //文件夹切换按钮
    private Text mtvDir; //显示当前文件夹
    private Text mBtnPre;      //预览按钮
    private ImageFolderAdapter mImageFolderAdapter;    //图片文件夹的适配器
    private FolderPopUpWindow mFolderPopupWindow;  //ImageSet的PopupWindow
    private List<ImageFolder> mImageFolders;   //所有的图片文件夹
    //    private ImageGridAdapter mImageGridAdapter;  //图片九宫格展示的适配器
    private boolean directPhoto = false; // 默认不是直接调取相机
    private ListContainer listContainer;
    private ImageRecyclerProvider listProvider;


    @Override
    protected void onStart(Intent data) {
        super.onStart(data);

        setUIContent(ResourceTable.Layout_ability_image_grid);

        imagePicker = ImagePicker.getInstance();
        imagePicker.clear();
        imagePicker.addOnImageSelectedListener(this);

        // 新增可直接拍照
        if (data != null && data.getParams() != null) {
            directPhoto = data.getBooleanParam(EXTRAS_TAKE_PICKERS, false); // 默认不是直接打开相机
            if (directPhoto) {
                if (verifySelfPermission(SystemPermission.CAMERA) != IBundleManager.PERMISSION_GRANTED) {
                    requestPermissionsFromUser(new String[]{SystemPermission.CAMERA}, ImageGridAbility.REQUEST_PERMISSION_CAMERA);
                } else {
                    takePic();
                }
            }
            ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableParam(EXTRAS_IMAGES);
            imagePicker.setSelectedImages(images);
        }

        listContainer = (ListContainer) findComponentById(ResourceTable.Id_recycler);
        TableLayoutManager tableLayoutManager = new TableLayoutManager();
        tableLayoutManager.setColumnCount(3);
        listContainer.setLayoutManager(tableLayoutManager);

        mBtnBack = (Image) findComponentById(ResourceTable.Id_btn_back);
        mBtnBack.setClickedListener(this);

        mBtnOk = (Button) findComponentById(ResourceTable.Id_btn_ok);
        mBtnOk.setClickedListener(this);
        new ElementUtil.StateElementBuilder()
                .addState(new int[]{ComponentState.COMPONENT_STATE_PRESSED}, ResUtil.getElement(this, ResourceTable.Graphic_bg_btn_pre))
                .addState(new int[]{ComponentState.COMPONENT_STATE_EMPTY}, ResUtil.getElement(this, ResourceTable.Graphic_bg_btn_nor))
                .bind(mBtnOk);
        mBtnPre = (Text) findComponentById(ResourceTable.Id_btn_preview);
        mBtnPre.setClickedListener(this);
        mFooterBar = findComponentById(ResourceTable.Id_footer_bar);
        mllDir = findComponentById(ResourceTable.Id_ll_dir);
        mllDir.setClickedListener(this);
        mtvDir = (Text) findComponentById(ResourceTable.Id_tv_dir);
        if (imagePicker.isMultiMode()) {
            mBtnOk.setVisibility(Component.VISIBLE);
            mBtnPre.setVisibility(Component.VISIBLE);
        } else {
            mBtnOk.setVisibility(Component.HIDE);
            mBtnPre.setVisibility(Component.HIDE);
        }

        mImageFolderAdapter = new ImageFolderAdapter(this, null);
        listProvider = new ImageRecyclerProvider(this, null);

        onImageSelected(0, null, false);

        if (verifySelfPermission(SystemPermission.READ_USER_STORAGE) == IBundleManager.PERMISSION_GRANTED &&
                verifySelfPermission(SystemPermission.READ_MEDIA) == IBundleManager.PERMISSION_GRANTED &&
                verifySelfPermission(SystemPermission.MEDIA_LOCATION) == IBundleManager.PERMISSION_GRANTED) {
            new ImageDataSource(this, null, this);
        } else {
            requestPermissionsFromUser(new String[]{SystemPermission.READ_USER_STORAGE, SystemPermission.READ_MEDIA, SystemPermission.MEDIA_LOCATION}, REQUEST_PERMISSION_STORAGE);

        }
    }

    private void takePic() {
        imagePicker.takePicture(this);
    }

    @Override
    public void onRequestPermissionsFromUserResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsFromUserResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == IBundleManager.PERMISSION_GRANTED) {
                new ImageDataSource(this, null, this);
            } else {
                showToast("权限被禁止，无法选择本地图片");
            }
        } else if (requestCode == REQUEST_PERMISSION_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == IBundleManager.PERMISSION_GRANTED) {
                takePic();
            } else {
                showToast("权限被禁止，无法打开相机");
            }
        }
    }


    @Override
    protected void onStop() {
        imagePicker.removeOnImageSelectedListener(this);
        super.onStop();
    }


    @Override
    public void onClick(Component v) {
        int id = v.getId();
        if (id == ResourceTable.Id_btn_ok) {
            Intent intent = new Intent();
            intent.setParam(ImagePicker.EXTRA_RESULT_ITEMS, imagePicker.getSelectedImages());
            setResult(ImagePicker.RESULT_CODE_ITEMS, intent);  //多选不允许裁剪裁剪，返回数据
            terminateAbility();
        } else if (id == ResourceTable.Id_ll_dir) {
            if (mImageFolders == null) {

                return;
            }
            //点击文件夹按钮
            createPopupFolderList();
            mImageFolderAdapter.refreshData(mImageFolders);  //刷新数据
            if (mFolderPopupWindow.isShowing()) {
                mFolderPopupWindow.hide();
            } else {
                mFolderPopupWindow.showOnCertainPosition(1, (int) mFooterBar.getContentPositionX(), (int) mFooterBar.getContentPositionY()/2);
                //默认选择当前选择的上一个，当目录很多时，直接定位到已选中的条目
                int index = mImageFolderAdapter.getSelectIndex();
                index = index == 0 ? index : index - 1;
                mFolderPopupWindow.setSelection(index);
            }
        } else if (id == ResourceTable.Id_btn_preview) {
            Intent intent = new Intent();
            Operation operation = new Intent.OperationBuilder()
                    .withDeviceId("")
                    .withBundleName(getBundleName())
                    .withAbilityName(ImagePreviewAbility.class.getName())
                    .build();
            intent.setOperation(operation);
            intent.setParam(ImagePicker.EXTRA_SELECTED_IMAGE_POSITION, 0);
            intent.setParam(ImagePicker.EXTRA_IMAGE_ITEMS, imagePicker.getSelectedImages());
            intent.setParam(ImagePreviewAbility.ISORIGIN, isOrigin);
            intent.setParam(ImagePicker.EXTRA_FROM_ITEMS, true);
            startAbilityForResult(intent, ImagePicker.REQUEST_CODE_PREVIEW);
        } else if (id == ResourceTable.Id_btn_back) {
            //点击返回按钮
            terminateAbility();
        }
    }

    /**
     * 创建弹出的ListView
     */
    private void createPopupFolderList() {
        mFolderPopupWindow = new FolderPopUpWindow(this, mImageFolderAdapter);
        mFolderPopupWindow.setOnItemClickListener(new FolderPopUpWindow.OnItemClickListener() {
            @Override
            public void onItemClick(ListContainer listContainer, Component view, int position, long l) {
                mImageFolderAdapter.setSelectIndex(position);
                imagePicker.setCurrentImageFolderPosition(position);
                mFolderPopupWindow.hide();
                ImageFolder imageFolder = (ImageFolder) listContainer.getItemProvider().getItem(position);
                if (null != imageFolder) {
//                    mImageGridAdapter.refreshData(imageFolder.images);
                    listProvider.refreshData(imageFolder.images);
                    mtvDir.setText(imageFolder.name);
                }
            }
        });
        mFolderPopupWindow.setMargin(mFooterBar.getHeight());
    }

    @Override
    public void onImagesLoaded(List<ImageFolder> imageFolders) {

        this.mImageFolders = imageFolders;
        imagePicker.setImageFolders(imageFolders);
        if (imageFolders.size() == 0) {
            listProvider.refreshData(null);
        } else {
            listProvider.refreshData(imageFolders.get(0).images);
        }
        listProvider.setOnImageItemClickListener(this);
        listContainer.setItemProvider(listProvider);
        mImageFolderAdapter.refreshData(imageFolders);
    }

    @Override
    public void onImageItemClick(Component view, ImageItem imageItem, int position) {
        //根据是否有相机按钮确定位置
        position = imagePicker.isShowCamera() ? position - 1 : position;
        if (imagePicker.isMultiMode()) {

            Intent intent = new Intent();
            Operation operation = new Intent.OperationBuilder()
                    .withDeviceId("")
                    .withBundleName(getBundleName())
                    .withAbilityName(ImagePreviewAbility.class.getName())
                    .build();
            intent.setOperation(operation);
            intent.setParam(ImagePicker.EXTRA_SELECTED_IMAGE_POSITION, position);

            /**
             * 2017-03-20
             *
             * 依然采用弱引用进行解决，采用单例加锁方式处理
             */

            // 据说这样会导致大量图片的时候崩溃
//            intent.putExtra(ImagePicker.EXTRA_IMAGE_ITEMS, imagePicker.getCurrentImageFolderItems());

            // 但采用弱引用会导致预览弱引用直接返回空指针
            DataHolder.getInstance().save(DataHolder.DH_CURRENT_IMAGE_FOLDER_ITEMS, imagePicker.getCurrentImageFolderItems());
            intent.setParam(ImagePreviewAbility.ISORIGIN, isOrigin);
            startAbilityForResult(intent, ImagePicker.REQUEST_CODE_PREVIEW);  //如果是多选，点击图片进入预览界面
        } else {

            imagePicker.clearSelectedImages();
            imagePicker.addSelectedImageItem(position, imagePicker.getCurrentImageFolderItems().get(position), true);
            if (imagePicker.isCrop()) {
                Intent intent = new Intent();
                Operation operation = new Intent.OperationBuilder()
                        .withDeviceId("")
                        .withBundleName(getBundleName())
                        .withAbilityName(ImageCropAbility.class.getName())
                        .build();
                intent.setOperation(operation);
                startAbilityForResult(intent, ImagePicker.REQUEST_CODE_CROP);  //单选需要裁剪，进入裁剪界面
            } else {
                Intent intent = new Intent();
                intent.setParam(ImagePicker.EXTRA_RESULT_ITEMS, imagePicker.getSelectedImages());
                setResult(ImagePicker.RESULT_CODE_ITEMS, intent);   //单选不需要裁剪，返回数据
                terminateAbility();
            }
        }
    }

    @Override
    public void onImageSelected(int position, ImageItem item, boolean isAdd) {
        if (imagePicker.getSelectImageCount() > 0) {
            mBtnOk.setText(ResUtil.getString(this, ResourceTable.String_ip_select_complete, imagePicker.getSelectImageCount(), imagePicker.getSelectLimit()));
            mBtnOk.setEnabled(true);
            mBtnPre.setEnabled(true);
            mBtnPre.setText(ResUtil.getString(this, ResourceTable.String_ip_preview_count, imagePicker.getSelectImageCount()));
            mBtnPre.setTextColor(new Color(ResUtil.getColor(this, ResourceTable.Color_ip_text_primary_inverted)));
            mBtnOk.setTextColor(new Color(ResUtil.getColor(this, ResourceTable.Color_ip_text_primary_inverted)));
        } else {
            mBtnOk.setText(ResUtil.getString(this, ResourceTable.String_ip_complete));
            mBtnOk.setEnabled(false);
            mBtnPre.setEnabled(false);
            mBtnPre.setText(ResUtil.getString(this, ResourceTable.String_ip_preview));
            mBtnPre.setTextColor(new Color(ResUtil.getColor(this, ResourceTable.Color_ip_text_secondary_inverted)));
            mBtnOk.setTextColor(new Color(ResUtil.getColor(this, ResourceTable.Color_ip_text_secondary_inverted)));
        }
//        mImageGridAdapter.notifyDataChanged();
//        mRecyclerAdapter.notifyItemChanged(position); // 17/4/21 fix the position while click img to preview
//        mRecyclerAdapter.notifyItemChanged(position + (imagePicker.isShowCamera() ? 1 : 0));// 17/4/24  fix the position while click right bottom preview button
        for (int i = imagePicker.isShowCamera() ? 1 : 0; i < listProvider.getCount(); i++) {
            if (listProvider.getItem(i).uriSchema != null && listProvider.getItem(i).uriSchema.equals(item.uriSchema)) {
                listProvider.notifyDataSetItemChanged(i);
                return;
            }
        }
    }

    @Override
    protected void onAbilityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onAbilityResult(requestCode, resultCode, data);

            if (data != null && data.getParams() != null) {

                if (resultCode == ImagePicker.RESULT_CODE_BACK) {
                    isOrigin = data.getBooleanParam(ImagePreviewAbility.ISORIGIN, false);
                } else {

                    //从拍照界面返回
                    //点击 X , 没有选择照片
                    if (data.getSerializableParam(ImagePicker.EXTRA_RESULT_ITEMS) == null) {
                        //什么都不做 直接调起相机
                        terminateAbility();
                    } else {
                        //说明是从裁剪页面过来的数据，直接返回就可以
                        setResult(ImagePicker.RESULT_CODE_ITEMS, data);
                        terminateAbility();
                    }

                }
            } else {

                //如果是裁剪，因为裁剪指定了存储的Uri，所以返回的data一定为null
                if (resultCode == ImagePicker.RESULT_OK && requestCode == ImagePicker.REQUEST_CODE_TAKE) {

                    //发送广播通知图片增加了
//                ImagePicker.galleryAddPic(this, imagePicker.getTakeImageFile());

                    /**
                     * 2017-03-21 对机型做旋转处理
                     */
                    String path = imagePicker.getTakeImageFile().getAbsolutePath();

                    ImageItem imageItem = new ImageItem();
                    imageItem.uriSchema = path;
                    imagePicker.clearSelectedImages();
                    imagePicker.addSelectedImageItem(0, imageItem, true);
                    if (imagePicker.isCrop()) {
                        Intent intent = new Intent();
                        Operation operation = new Intent.OperationBuilder()
                                .withDeviceId("")
                                .withBundleName(getBundleName())
                                .withAbilityName(ImageCropAbility.class.getName())
                                .build();
                        intent.setOperation(operation);
                        startAbilityForResult(intent, ImagePicker.REQUEST_CODE_CROP);  //单选需要裁剪，进入裁剪界面
                    } else {
                        Intent intent = new Intent();
                        intent.setParam(ImagePicker.EXTRA_RESULT_ITEMS, imagePicker.getSelectedImages());
                        setResult(ImagePicker.RESULT_CODE_ITEMS, intent);   //单选不需要裁剪，返回数据
                        terminateAbility();
                    }
                } else if (directPhoto) {
                    terminateAbility();
                }
            }
        }catch (Exception e){
            terminateAbility();
        }

    }


}