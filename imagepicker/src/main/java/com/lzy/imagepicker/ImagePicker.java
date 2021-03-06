package com.lzy.imagepicker;


import com.lzy.imagepicker.bean.ImageFolder;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.loader.ImageLoader;
import com.lzy.imagepicker.view.CropImageView;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;
import ohos.app.Context;
import ohos.app.Environment;
import ohos.media.image.PixelMap;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * ================================================
 * 作    者：jeasonlzy（廖子尧 Github地址：https://github.com/jeasonlzy0216
 * 版    本：1.0
 * 创建日期：2016/5/19
 * 描    述：图片选择的入口类
 * 修订历史：
 * 2017-03-20
 *
 * @author nanchen
 * 采用单例和弱引用解决Intent传值限制导致的异常
 * ================================================
 */
public class ImagePicker {

    public static final String TAG = ImagePicker.class.getSimpleName();
    public static final int REQUEST_CODE_TAKE = 1001;
    public static final int REQUEST_CODE_CROP = 1002;
    public static final int REQUEST_CODE_PREVIEW = 1003;
    public static final int RESULT_CODE_ITEMS = 1004;
    public static final int RESULT_CODE_BACK = 1005;
    public static final int RESULT_OK = 1001;
    public static final int RESULT_CANCELED = 1002;

    public static final String EXTRA_RESULT_ITEMS = "extra_result_items";
    public static final String EXTRA_SELECTED_IMAGE_POSITION = "selected_image_position";
    public static final String EXTRA_IMAGE_ITEMS = "extra_image_items";
    public static final String EXTRA_FROM_ITEMS = "extra_from_items";
    public static final String EXTRA_TARGET_FILE_PATH = "extra_target_file_path";

    private boolean multiMode = true;    //图片选择模式
    private int selectLimit = 9;         //最大选择图片数量
    private boolean crop = true;         //裁剪
    private boolean showCamera = true;   //显示相机
    private boolean isSaveRectangle = false;  //裁剪后的图片是否是矩形，否者跟随裁剪框的形状
    private int outPutX = 800;           //裁剪保存宽度
    private int outPutY = 800;           //裁剪保存高度
    private int focusWidth = 280;         //焦点框的宽度
    private int focusHeight = 280;        //焦点框的高度
    private ImageLoader imageLoader;     //图片加载器
    private CropImageView.Style style = CropImageView.Style.RECTANGLE; //裁剪框的形状
    private File cropCacheFolder;
    private File takeImageFile;
    public PixelMap cropBitmap;

    private ArrayList<ImageItem> mSelectedImages = new ArrayList<>();   //选中的图片集合
    private List<ImageFolder> mImageFolders;      //所有的图片文件夹
    private int mCurrentImageFolderPosition = 0;  //当前选中的文件夹位置 0表示所有图片
    private List<OnImageSelectedListener> mImageSelectedListeners;          // 图片选中的监听回调

    private static ImagePicker mInstance;
    private static Class takePhotoAbilityClass;

    private ImagePicker() {
    }

    public static ImagePicker getInstance() {
        if (mInstance == null) {
            synchronized (ImagePicker.class) {
                if (mInstance == null) {
                    mInstance = new ImagePicker();
                }
            }
        }
        return mInstance;
    }

    public static void setTakePhotoAbility(Class clz) {
       takePhotoAbilityClass = clz;
    }

    public boolean isMultiMode() {
        return multiMode;
    }

    public void setMultiMode(boolean multiMode) {
        this.multiMode = multiMode;
    }

    public int getSelectLimit() {
        return selectLimit;
    }

    public void setSelectLimit(int selectLimit) {
        this.selectLimit = selectLimit;
    }

    public boolean isCrop() {
        return crop;
    }

    public void setCrop(boolean crop) {
        this.crop = crop;
    }

    public boolean isShowCamera() {
        return showCamera;
    }

    public void setShowCamera(boolean showCamera) {
        this.showCamera = showCamera;
    }

    public boolean isSaveRectangle() {
        return isSaveRectangle;
    }

    public void setSaveRectangle(boolean isSaveRectangle) {
        this.isSaveRectangle = isSaveRectangle;
    }

    public int getOutPutX() {
        return outPutX;
    }

    public void setOutPutX(int outPutX) {
        this.outPutX = outPutX;
    }

    public int getOutPutY() {
        return outPutY;
    }

    public void setOutPutY(int outPutY) {
        this.outPutY = outPutY;
    }

    public int getFocusWidth() {
        return focusWidth;
    }

    public void setFocusWidth(int focusWidth) {
        this.focusWidth = focusWidth;
    }

    public int getFocusHeight() {
        return focusHeight;
    }

    public void setFocusHeight(int focusHeight) {
        this.focusHeight = focusHeight;
    }

    public File getTakeImageFile() {
        return takeImageFile;
    }

    public void setTakeImageFile(File takeImageFile) {
        this.takeImageFile = takeImageFile;
    }

    public File getCropCacheFolder(Context context) {
        if (cropCacheFolder == null) {
            cropCacheFolder = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/ImagePicker/cropTemp/");
        }
        return cropCacheFolder;
    }

    public void setCropCacheFolder(File cropCacheFolder) {
        this.cropCacheFolder = cropCacheFolder;
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }

    public void setImageLoader(ImageLoader imageLoader) {
        this.imageLoader = imageLoader;
    }

    public CropImageView.Style getStyle() {
        return style;
    }

    public void setStyle(CropImageView.Style style) {
        this.style = style;
    }

    public List<ImageFolder> getImageFolders() {
        return mImageFolders;
    }

    public void setImageFolders(List<ImageFolder> imageFolders) {
        mImageFolders = imageFolders;
    }

    public int getCurrentImageFolderPosition() {
        return mCurrentImageFolderPosition;
    }

    public void setCurrentImageFolderPosition(int mCurrentSelectedImageSetPosition) {
        mCurrentImageFolderPosition = mCurrentSelectedImageSetPosition;
    }

    public ArrayList<ImageItem> getCurrentImageFolderItems() {
        return mImageFolders.get(mCurrentImageFolderPosition).images;
    }

    public boolean isSelect(ImageItem item) {
        return mSelectedImages.contains(item);
    }

    public int getSelectImageCount() {
        if (mSelectedImages == null) {
            return 0;
        }
        return mSelectedImages.size();
    }

    public ArrayList<ImageItem> getSelectedImages() {
        return mSelectedImages;
    }

    public void clearSelectedImages() {
        if (mSelectedImages != null) mSelectedImages.clear();
    }

    public void clear() {
        if (mImageSelectedListeners != null) {
            mImageSelectedListeners.clear();
            mImageSelectedListeners = null;
        }
        if (mImageFolders != null) {
            mImageFolders.clear();
            mImageFolders = null;
        }
        if (mSelectedImages != null) {
            mSelectedImages.clear();
        }
        mCurrentImageFolderPosition = 0;
    }

    /**
     * 拍照的方法
     * @param ability 上下文对象
     */
    public void takePicture(Ability ability) {
        Operation operation = new Intent.OperationBuilder()
                .withDeviceId("")
                .withBundleName(takePhotoAbilityClass.getPackage().getName())
                .withAbilityName(takePhotoAbilityClass.getName())
                .build();
        //打开预览
        Intent itent = new Intent();
        itent.setOperation(operation);
        ability.startAbilityForResult(itent, REQUEST_CODE_PREVIEW);
    }

    /**
     * 根据系统时间、前缀、后缀产生一个文件
     * @param folder 文件夹
     * @param prefix prefix
     * @param suffix suffix
     * @return  file
     */
    public static File createFile(File folder, String prefix, String suffix) {
        if (!folder.exists() || !folder.isDirectory()) {
            boolean mkdirs = folder.mkdirs();
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA);
        String filename = prefix + dateFormat.format(new Date(System.currentTimeMillis())) + suffix;
        return new File(folder, filename);
    }

    /**
     * 扫描图片
     * @param context 上下文对象
     * @param file 目标文件
     */
    public static void galleryAddPic(Context context, File file) {
//        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//        Uri contentUri = Uri.getUriFromFile(file);
//        mediaScanIntent.setData(contentUri);
//        context.sendBroadcast(mediaScanIntent);
    }

    /**
     * 图片选中的监听
     */
    public interface OnImageSelectedListener {
        void onImageSelected(int position, ImageItem item, boolean isAdd);
    }

    public void addOnImageSelectedListener(OnImageSelectedListener l) {
        if (mImageSelectedListeners == null) mImageSelectedListeners = new ArrayList<>();
        mImageSelectedListeners.add(l);
    }

    public void removeOnImageSelectedListener(OnImageSelectedListener l) {
        if (mImageSelectedListeners == null) return;
        mImageSelectedListeners.remove(l);
    }

    public void addSelectedImageItem(int position, ImageItem item, boolean isAdd) {
        if (isAdd) mSelectedImages.add(item);
        else mSelectedImages.remove(item);
        notifyImageSelectedChanged(position, item, isAdd);
    }

    public void setSelectedImages(ArrayList<ImageItem> selectedImages) {
        if (selectedImages == null) {
            return;
        }
        this.mSelectedImages = selectedImages;
    }

    private void notifyImageSelectedChanged(int position, ImageItem item, boolean isAdd) {
        if (mImageSelectedListeners == null) return;
        for (OnImageSelectedListener l : mImageSelectedListeners) {
            l.onImageSelected(position, item, isAdd);
        }
    }

    /**
     * 用于手机内存不足，进程被系统回收，重启时的状态恢复
     */
//    public void restoreInstanceState(Bundle savedInstanceState) {
//        cropCacheFolder = (File) savedInstanceState.getSerializable("cropCacheFolder");
//        takeImageFile = (File) savedInstanceState.getSerializable("takeImageFile");
//        imageLoader = (ImageLoader) savedInstanceState.getSerializable("imageLoader");
//        style = (CropImageView.Style) savedInstanceState.getSerializable("style");
//        multiMode = savedInstanceState.getBoolean("multiMode");
//        crop = savedInstanceState.getBoolean("crop");
//        showCamera = savedInstanceState.getBoolean("showCamera");
//        isSaveRectangle = savedInstanceState.getBoolean("isSaveRectangle");
//        selectLimit = savedInstanceState.getInt("selectLimit");
//        outPutX = savedInstanceState.getInt("outPutX");
//        outPutY = savedInstanceState.getInt("outPutY");
//        focusWidth = savedInstanceState.getInt("focusWidth");
//        focusHeight = savedInstanceState.getInt("focusHeight");
//    }
//
//    /**
//     * 用于手机内存不足，进程被系统回收时的状态保存
//     */
//    public void saveInstanceState(Bundle outState) {
//        outState.putSerializable("cropCacheFolder", cropCacheFolder);
//        outState.putSerializable("takeImageFile", takeImageFile);
//        outState.putSerializable("imageLoader", imageLoader);
//        outState.putSerializable("style", style);
//        outState.putBoolean("multiMode", multiMode);
//        outState.putBoolean("crop", crop);
//        outState.putBoolean("showCamera", showCamera);
//        outState.putBoolean("isSaveRectangle", isSaveRectangle);
//        outState.putInt("selectLimit", selectLimit);
//        outState.putInt("outPutX", outPutX);
//        outState.putInt("outPutY", outPutY);
//        outState.putInt("focusWidth", focusWidth);
//        outState.putInt("focusHeight", focusHeight);
//    }

}