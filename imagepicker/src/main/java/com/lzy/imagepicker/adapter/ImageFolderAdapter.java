package com.lzy.imagepicker.adapter;


import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.ResourceTable;
import com.lzy.imagepicker.util.ResUtil;
import com.lzy.imagepicker.util.Utils;
import com.lzy.imagepicker.bean.ImageFolder;
import ohos.aafwk.ability.Ability;
import ohos.agp.components.*;

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
public class ImageFolderAdapter extends BaseItemProvider {

    private ImagePicker imagePicker;
    private Ability mActivity;
    private LayoutScatter mInflater;
    private int mImageSize;
    private List<ImageFolder> imageFolders;
    private int lastSelected = 0;

    public ImageFolderAdapter(Ability ability, List<ImageFolder> folders) {
        mActivity = ability;
        if (folders != null && folders.size() > 0) imageFolders = folders;
        else imageFolders = new ArrayList<>();

        imagePicker = ImagePicker.getInstance();
        mImageSize = Utils.getImageItemWidth(mActivity);
        mInflater = (LayoutScatter)LayoutScatter.getInstance(ability) ;
    }

    public void refreshData(List<ImageFolder> folders) {
        if (folders != null && folders.size() > 0) imageFolders = folders;
        else imageFolders.clear();
        notifyDataChanged();
    }

    @Override
    public int getCount() {
        return imageFolders.size();
    }

    @Override
    public ImageFolder getItem(int position) {
        return imageFolders.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Component getComponent(int position, Component component, ComponentContainer componentContainer) {
        ViewHolder holder;
        if (component == null) {
            component = mInflater.parse(ResourceTable.Layout_adapter_folder_list_item, componentContainer, false);
            holder = new ViewHolder(component);
        } else {
            holder = (ViewHolder) component.getTag();
        }

        ImageFolder folder = getItem(position);
        holder.folderName.setText(folder.name);
        holder.imageCount.setText(ResUtil.getString(mActivity,ResourceTable.String_ip_folder_image_count, folder.images.size()));
        imagePicker.getImageLoader().displayImage(mActivity, folder.cover.uriSchema, holder.cover, mImageSize, mImageSize);

        if (lastSelected == position) {
            holder.folderCheck.setVisibility(Component.VISIBLE);
        } else {
            holder.folderCheck.setVisibility(Component.INVISIBLE);
        }

        return component;
    }


    public void setSelectIndex(int i) {
        if (lastSelected == i) {
            return;
        }
        lastSelected = i;
        notifyDataChanged();
    }

    public int getSelectIndex() {
        return lastSelected;
    }

    private class ViewHolder {
        Image cover;
        Text folderName;
        Text imageCount;
        Image folderCheck;

        public ViewHolder(Component view) {
            cover = (Image) view.findComponentById(ResourceTable.Id_iv_cover);
            folderName = (Text) view.findComponentById(ResourceTable.Id_tv_folder_name);
            imageCount = (Text) view.findComponentById(ResourceTable.Id_tv_image_count);
            folderCheck = (Image) view.findComponentById(ResourceTable.Id_iv_folder_check);
            view.setTag(this);
        }
    }
}
