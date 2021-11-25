package com.lzy.imagepicker;


import com.lzy.imagepicker.bean.ImageFolder;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.util.ResUtil;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.ability.DataAbilityHelper;
import ohos.aafwk.ability.DataAbilityRemoteException;
import ohos.data.dataability.DataAbilityPredicates;
import ohos.data.resultset.ResultSet;
import ohos.hiviewdfx.HiLog;
import ohos.media.photokit.metadata.AVStorage;
import ohos.utils.net.Uri;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ================================================
 * 作    者：jeasonlzy（廖子尧 Github地址：https://github.com/jeasonlzy0216
 * 版    本：1.0
 * 创建日期：2016/5/19
 * 描    述：加载手机图片实现类
 * 修订历史：
 * ================================================
 */
public class ImageDataSource {

    public static final int LOADER_ALL = 0;         //加载所有图片
    public static final int LOADER_CATEGORY = 1;    //分类加载图片
    private final String[] IMAGE_PROJECTION = {     //查询图片需要的数据列
            AVStorage.Video.Media.DISPLAY_NAME,   //图片的显示名称  aaa.jpg
            AVStorage.Video.Media.DATA,           //图片的真实路径  /storage/emulated/0/pp/downloader/wallpaper/aaa.jpg
            AVStorage.Video.Media.SIZE,           //图片的大小，long型  132492
            AVStorage.Video.Media.MIME_TYPE,      //图片的类型     image/jpeg
            AVStorage.Video.Media.DATE_ADDED};    //图片被添加的时间，long型  1450518608

    private Ability ability;
    private OnImagesLoadedListener loadedListener;                     //图片加载完成的回调接口
    private ArrayList<ImageFolder> imageFolders = new ArrayList<>();   //所有的图片文件夹
    private DataAbilityHelper helper;
    private DataAbilityPredicates dataAbilityPredicates;
    private ResultSet resultSet;

    /**
     * @param ability        用于初始化LoaderManager，需要兼容到2.3
     * @param path           指定扫描的文件夹目录，可以为 null，表示扫描所有图片
     * @param loadedListener 图片加载完成的监听
     */
    public ImageDataSource(Ability ability, String path, OnImagesLoadedListener loadedListener) {
        this.ability = ability;
        this.loadedListener = loadedListener;

        helper = DataAbilityHelper.creator(ability);
        dataAbilityPredicates = new DataAbilityPredicates();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (path == null) {
                        resultSet = helper.query(AVStorage.Images.Media.EXTERNAL_DATA_ABILITY_URI, null, null);

            } else {
                resultSet = helper.query(Uri.getUriFromFile(new File(path)), null, null);
            }
                    ability.getUITaskDispatcher().syncDispatch(new Runnable() {
                @Override
                public void run() {
                    onLoadFinished(resultSet);

                }
            });
        } catch (DataAbilityRemoteException e) {
                    onLoadFinished(resultSet);
                }
            }
        }).start();

    }



    public void onLoadFinished(ResultSet data) {
        imageFolders.clear();
        if (data != null) {
            ArrayList<ImageItem> allImages = new ArrayList<>();   //所有图片的集合,不分文件夹
            while (data.goToNextRow()) {
                String[] all = data.getAllColumnNames();
                List<String> a = Arrays.asList(all);
                //查询数据
                String imageName = data.getString(data.getColumnIndexForName(IMAGE_PROJECTION[0]));
                String imagePath = data.getString(data.getColumnIndexForName(IMAGE_PROJECTION[1]));
                int id = data.getInt(data.getColumnIndexForName(AVStorage.Images.Media.ID));
                Uri uri = Uri.appendEncodedPathToUri(AVStorage.Images.Media.EXTERNAL_DATA_ABILITY_URI, String.valueOf(id));

                File file = new File(imagePath);
                if (!file.exists() || file.length() <= 0) {
                    continue;
                }

                long imageSize = data.getLong(data.getColumnIndexForName(IMAGE_PROJECTION[2]));
                int imageWidth = data.getInt(data.getColumnIndexForName("width"));
                int imageHeight = data.getInt(data.getColumnIndexForName("height"));
                String imageMimeType = data.getString(data.getColumnIndexForName(IMAGE_PROJECTION[3]));
                long imageAddTime = data.getLong(data.getColumnIndexForName(IMAGE_PROJECTION[4]));
                //封装实体
                ImageItem imageItem = new ImageItem();
                imageItem.name = imageName;
                imageItem.uriSchema = uri.toString();
                imageItem.size = imageSize;
                imageItem.width = imageWidth;
                imageItem.path = imagePath;
                imageItem.id = id+"";
                imageItem.height = imageHeight;
                imageItem.mimeType = imageMimeType;
                imageItem.addTime = imageAddTime;
                allImages.add(imageItem);


                //根据父路径分类存放图片
                File imageFile = new File(imagePath);
                File imageParentFile = imageFile.getParentFile();
                ImageFolder imageFolder = new ImageFolder();
                imageFolder.name = imageParentFile.getName();
                imageFolder.path = imageParentFile.getAbsolutePath();

                if (!imageFolders.contains(imageFolder)) {
                    ArrayList<ImageItem> images = new ArrayList<>();
                    images.add(imageItem);
                    imageFolder.cover = imageItem;
                    imageFolder.images = images;
                    imageFolders.add(imageFolder);
                } else {
                    imageFolders.get(imageFolders.indexOf(imageFolder)).images.add(imageItem);
                }
            }
            //防止没有图片报异常
            if (data.getColumnCount() > 0 && allImages.size() > 0) {
                //构造所有图片的集合
                ImageFolder allImagesFolder = new ImageFolder();
                allImagesFolder.name = ResUtil.getString(ability, ResourceTable.String_ip_all_images);
                allImagesFolder.path = "/";
                allImagesFolder.cover = allImages.get(0);
                allImagesFolder.images = allImages;
                imageFolders.add(0, allImagesFolder);  //确保第一条是所有图片
            }
        }

        //回调接口，通知图片数据准备完成
        ImagePicker.getInstance().setImageFolders(imageFolders);
        loadedListener.onImagesLoaded(imageFolders);
    }


    /**
     * 所有图片加载完成的回调接口
     */
    public interface OnImagesLoadedListener {
        void onImagesLoaded(List<ImageFolder> imageFolders);
    }
}
