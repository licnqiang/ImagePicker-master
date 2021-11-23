/*
 * Copyright (C) 2021 Huawei Device Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nostra13.universalimageloader.utils;

import ohos.media.image.PixelMap;
import ohos.media.image.common.Size;


/**
 * Pixel map utils
 */
public class PixelMapUtils {

    /**
     * Crop bitmap pixel map
     *
     * @param bitmap bitmap
     * @return the pixel map
     */
    public static PixelMap cropBitmap(PixelMap bitmap) {//从中间截取一个正方形
        int w = getWidth(bitmap); // 得到图片的宽，高
        int h = getHeight(bitmap);
        int cropWidth = w >= h ? h : w;// 裁切后所取的正方形区域边长
        PixelMap.InitializationOptions options = new PixelMap.InitializationOptions();
        options.size = new Size(cropWidth, cropWidth);
        return PixelMap.create(bitmap, options);
    }


    /**
     * Gets width *
     *
     * @param bitmap bitmap
     * @return the width
     */
    public static int getWidth(PixelMap bitmap) {
        return bitmap.getImageInfo().size.width;
    }

    /**
     * Gets height *
     *
     * @param bitmap bitmap
     * @return the height
     */
    public static int getHeight(PixelMap bitmap) {
        return bitmap.getImageInfo().size.height;
    }

}
