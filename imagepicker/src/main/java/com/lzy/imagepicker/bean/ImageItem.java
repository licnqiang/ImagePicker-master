package com.lzy.imagepicker.bean;

import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

import java.io.Serializable;

/**
 * ================================================
 * 作    者：jeasonlzy（廖子尧 Github地址：https://github.com/jeasonlzy0216
 * 版    本：1.0
 * 创建日期：2016/5/19
 * 描    述：图片信息
 * 修订历史：
 * ================================================
 */
public class ImageItem implements Serializable, Sequenceable {

    public String name;       //图片的名字
    public String uriSchema;       //图片的路径
    public long size;         //图片的大小
    public int width;         //图片的宽度
    public int height;        //图片的高度
    public String mimeType;   //图片的类型
    public long addTime;      //图片的创建时间
    public String path;      //图片的创建时间
    public String id;      //图片的创建时间

    /**
     * 图片的路径和创建时间相同就认为是同一张图片
     * @param o object
     * @return  是否相同
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof ImageItem) {
            ImageItem item = (ImageItem) o;
            return this.uriSchema.equalsIgnoreCase(item.uriSchema) && this.addTime == item.addTime;
        }

        return super.equals(o);
    }


//    @Override
//    public int describeContents() {
//        return 0;
//    }
//
//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//        dest.writeString(this.name);
//        dest.writeString(this.path);
//        dest.writeLong(this.size);
//        dest.writeInt(this.width);
//        dest.writeInt(this.height);
//        dest.writeString(this.mimeType);
//        dest.writeLong(this.addTime);
//    }

    public ImageItem() {
    }

    protected ImageItem(Parcel in) {
        this.name = in.readString();
        this.uriSchema = in.readString();
        this.size = in.readLong();
        this.width = in.readInt();
        this.height = in.readInt();
        this.mimeType = in.readString();
        this.addTime = in.readLong();
    }

    public static final Sequenceable.Producer<ImageItem> PRODUCER = new Sequenceable.Producer<ImageItem>() {
        @Override
        public ImageItem createFromParcel(Parcel source) {
            return new ImageItem(source);
        }

//        @Override
//        public ImageItem[] newArray(int size) {
//            return new ImageItem[size];
//        }
    };

    @Override
    public boolean marshalling(Parcel parcel) {
        parcel.writeString(this.name);
        parcel.writeString(this.uriSchema);
        parcel.writeLong(this.size);
        parcel.writeInt(this.width);
        parcel.writeInt(this.height);
        parcel.writeString(this.mimeType);
        parcel.writeLong(this.addTime);
        return true;
    }

    @Override
    public boolean unmarshalling(Parcel in) {
        this.name = in.readString();
        this.uriSchema = in.readString();
        this.size = in.readLong();
        this.width = in.readInt();
        this.height = in.readInt();
        this.mimeType = in.readString();
        this.addTime = in.readLong();
        return true;
    }

    @Override
    public String toString() {
        return "ImageItem{" +
                "name='" + name + '\'' +
                ", path='" + uriSchema + '\'' +
                ", size=" + size +
                ", width=" + width +
                ", height=" + height +
                ", mimeType='" + mimeType + '\'' +
                ", addTime=" + addTime +
                '}';
    }
}
