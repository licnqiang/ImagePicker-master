package com.lzy.imagepicker.adapter;


import com.lzy.imagepicker.DataHolder;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.ResourceTable;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.ui.ImageGridAbility;
import com.lzy.imagepicker.util.ResUtil;
import com.lzy.imagepicker.util.Utils;
import com.lzy.imagepicker.util.view.LogUtil;
import com.lzy.imagepicker.util.view.RoundedImageView;
import com.lzy.imagepicker.view.SuperCheckBox;
import com.lzy.imagepicker.view.ViewHolder;
import ohos.aafwk.ability.Ability;
import ohos.agp.components.*;
import ohos.agp.components.element.PixelMapElement;
import ohos.agp.components.element.StateElement;
import ohos.bundle.IBundleManager;
import ohos.media.image.PixelMap;
import ohos.media.image.common.Size;
import ohos.media.photokit.metadata.AVMetadataHelper;
import ohos.media.photokit.metadata.AVStorage;
import ohos.media.photokit.metadata.AVThumbnailUtils;
import ohos.security.SystemPermission;

import java.io.File;
import java.util.ArrayList;

/**
 * 加载相册图片的RecyclerView适配器
 * <p>
 * 用于替换原项目的GridView，使用局部刷新解决选中照片出现闪动问题
 * <p>
 * 替换为RecyclerView后只是不再会导致全局刷新，
 * <p>
 * 但还是会出现明显的重新加载图片，可能是picasso图片加载框架的问题
 * <p>
 * Author: nanchen
 * Email: liushilin520@foxmail.com
 * Date: 2017-04-05  10:04
 */

public class ImageRecyclerProvider extends RecycleItemProvider {


    private static final int ITEM_TYPE_CAMERA = 0;  //第一个条目是相机
    private static final int ITEM_TYPE_NORMAL = 1;  //第一个条目不是相机
    private ImagePicker imagePicker;
    private Ability ability;
    private ArrayList<ImageItem> images;       //当前需要显示的所有的图片数据
    private ArrayList<ImageItem> mSelectedImages; //全局保存的已经选中的图片数据
    private boolean isShowCamera;         //是否显示拍照按钮
    private int mImageSize;               //每个条目的大小
    private LayoutScatter mInflater;
    private OnImageItemClickListener listener;   //图片被点击的监听

    public void setOnImageItemClickListener(OnImageItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnImageItemClickListener {
        void onImageItemClick(Component view, ImageItem imageItem, int position);
    }

    public void refreshData(ArrayList<ImageItem> images) {
        if (images == null || images.size() == 0) this.images = new ArrayList<>();
        else this.images = images;
        notifyDataChanged();
    }

    /**
     * 构造方法
     */
    public ImageRecyclerProvider(Ability ability, ArrayList<ImageItem> images) {
        this.ability = ability;
        if (images == null || images.size() == 0) this.images = new ArrayList<>();
        else this.images = images;

        mImageSize = Utils.getImageItemWidth(this.ability);
        imagePicker = ImagePicker.getInstance();
        isShowCamera = imagePicker.isShowCamera();
        mSelectedImages = imagePicker.getSelectedImages();
        mInflater = LayoutScatter.getInstance(ability);

    }


    public ViewHolder onCreateViewHolder(ComponentContainer parent, int viewType) {
        if (viewType == ITEM_TYPE_CAMERA) {
            return new CameraViewHolder(mInflater.parse(ResourceTable.Layout_adapter_camera_item, parent, false));
        }
        return new ImageViewHolder(mInflater.parse(ResourceTable.Layout_adapter_image_list_item, parent, false));
    }

    public void onBindViewHolder(ViewHolder holder, int position) {
        if (holder instanceof CameraViewHolder) {
            ((CameraViewHolder) holder).bindCamera();
        } else if (holder instanceof ImageViewHolder) {
            ((ImageViewHolder) holder).bind(position);
        }
    }

    @Override
    public int getItemComponentType(int position) {
        if (isShowCamera) return position == 0 ? ITEM_TYPE_CAMERA : ITEM_TYPE_NORMAL;
        return ITEM_TYPE_NORMAL;
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Component getComponent(int i, Component component, ComponentContainer componentContainer) {
        int type = getItemComponentType(i);
        ViewHolder viewHolder = onCreateViewHolder(componentContainer, type);
        onBindViewHolder(viewHolder, i);

        return viewHolder.getRootView();
    }


    @Override
    public int getCount() {
        return isShowCamera ? images.size() + 1 : images.size();
    }

    public ImageItem getItem(int position) {
        if (isShowCamera) {
            if (position == 0) return null;
            return images.get(position - 1);
        } else {
            return images.get(position);
        }
    }

    private class ImageViewHolder extends ViewHolder {

        Component rootView;
        Image ivThumb;
        Component mask;
        Component checkView;
        SuperCheckBox cbCheck;


        ImageViewHolder(Component itemView) {
            rootView = itemView;
            ivThumb = (Image) itemView.findComponentById(ResourceTable.Id_iv_thumb);
            mask = itemView.findComponentById(ResourceTable.Id_mask);
            checkView = itemView.findComponentById(ResourceTable.Id_checkView);
            cbCheck = (SuperCheckBox) itemView.findComponentById(ResourceTable.Id_cb_check);
            itemView.setLayoutConfig(new ListContainer.LayoutConfig(mImageSize, mImageSize)); //让图片是个正方形
        }

        void bind(final int position) {
            final ImageItem imageItem = getItem(position);
            ivThumb.setClickedListener(new Component.ClickedListener() {
                @Override
                public void onClick(Component v) {
                    if (listener != null) listener.onImageItemClick(rootView, imageItem, position);
                }
            });
            checkView.setClickedListener(new Component.ClickedListener() {
                @Override
                public void onClick(Component v) {
                    cbCheck.setChecked(!cbCheck.isChecked());
                    int selectLimit = imagePicker.getSelectLimit();
                    if (cbCheck.isChecked() && mSelectedImages.size() >= selectLimit) {
//                        Toast.makeText(mActivity.getApplicationContext(), mActivity.getString(ResourceTable.String_ip_select_limit, selectLimit), Toast.LENGTH_SHORT).show();
                        cbCheck.setChecked(false);
                        mask.setVisibility(Component.HIDE);
                    } else {
                        imagePicker.addSelectedImageItem(position, imageItem, cbCheck.isChecked());
                        mask.setVisibility(Component.VISIBLE);
                    }
                }
            });
            StateElement stateElement = new StateElement();
            stateElement.addState(new int[]{ComponentState.COMPONENT_STATE_CHECKED}, ResUtil.getPixelMapDrawable(ability, ResourceTable.Media_checkbox_checked));
            stateElement.addState(new int[]{ComponentState.COMPONENT_STATE_EMPTY}, ResUtil.getPixelMapDrawable(ability, ResourceTable.Media_checkbox_normal));
            cbCheck.setButtonElement(stateElement);
            //根据是否多选，显示或隐藏checkbox
            if (imagePicker.isMultiMode()) {
                cbCheck.setVisibility(Component.VISIBLE);
                boolean checked = mSelectedImages.contains(imageItem);
                if (checked) {
                    mask.setVisibility(Component.VISIBLE);
                    cbCheck.setChecked(true);
                } else {
                    mask.setVisibility(Component.HIDE);
                    cbCheck.setChecked(false);
                }
            } else {
                cbCheck.setVisibility(Component.HIDE);
            }
            LogUtil.error("测试", "uriSchema: " + imageItem.uriSchema);
            LogUtil.error("测试", "name: " + imageItem.name);
            LogUtil.error("测试", "path: " + imageItem.path);
            LogUtil.error("测试", "id: " + imageItem.id);
            PixelMap resMap = AVThumbnailUtils.createVideoThumbnail(new File("/storage/emulated/0/DCIM/Camera/VID_20211123_224738.mp4"), new Size(40, 40));
//            AVMetadataHelper  avMetadataHelper = new AVMetadataHelper();
//            avMetadataHelper.setSource(imageItem.path);
//            PixelMap resMap=avMetadataHelper.fetchVideoPixelMapByTime();
            ivThumb.setPixelMap(resMap);
//            imagePicker.getImageLoader().displayImage(ability, "/storage/emulated/0/DCIM/Camera/VID_20211123_224738.mp4" , ivThumb, mImageSize, mImageSize); //显示图片
        }

        @Override
        public Component getRootView() {
            return rootView;
        }
    }

    private class CameraViewHolder extends ViewHolder {

        Component mItemView;

        CameraViewHolder(Component itemView) {
            mItemView = itemView;
        }

        void bindCamera() {
            mItemView.setLayoutConfig(new ListContainer.LayoutConfig(mImageSize, mImageSize)); //让图片是个正方形
            mItemView.setTag(null);
            mItemView.setClickedListener(new Component.ClickedListener() {
                @Override
                public void onClick(Component v) {
                    if (ability.verifySelfPermission(SystemPermission.CAMERA) != IBundleManager.PERMISSION_GRANTED) {
                        ability.requestPermissionsFromUser(new String[]{SystemPermission.CAMERA}, ImageGridAbility.REQUEST_PERMISSION_CAMERA);
                    } else {
                        imagePicker.takePicture(ability);
                    }
                }
            });
        }

        @Override
        public Component getRootView() {
            return mItemView;
        }
    }
}
