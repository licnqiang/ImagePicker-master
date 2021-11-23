/*******************************************************************************
 * Copyright 2013-2014 Sergey Tarasevich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.nostra13.universalimageloader.core.imageaware;


import ohos.media.image.PixelMap;
import ohos.agp.components.element.Element;
import ohos.agp.components.Component;
import ohos.agp.components.element.FrameAnimationElement;
import ohos.agp.components.Image;
import com.nostra13.universalimageloader.core.assist.ViewScaleType;
import com.nostra13.universalimageloader.utils.L;

import java.lang.reflect.Field;

/**
 * Wrapper for Android {@link ohos.agp.components.Image ImageView}. Keeps weak reference of ImageView to prevent memory
 * leaks.
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @since 1.9.0
 */
public class ImageViewAware extends ViewAware {

    /**
     * Constructor. <br />
     * References {@link #ImageViewAware(ohos.agp.components.Image, boolean) ImageViewAware(imageView, true)}.
     *
     * @param imageView {@link ohos.agp.components.Image ImageView} to work with
     */
    public ImageViewAware(Image imageView) {
        super(imageView);
    }

    /**
     * Constructor
     *
     * @param imageView           {@link ohos.agp.components.Image ImageView} to work with
     * @param checkActualViewSize <b>true</b> - then {@link #getWidth()} and {@link #getHeight()} will check actual
     *                            size of ImageView. It can cause known issues like
     *                            <a href="https://github.com/nostra13/Android-Universal-Image-Loader/issues/376">this</a>.
     *                            But it helps to save memory because memory cache keeps bitmaps of actual (less in
     *                            general) size.
     *                            <p/>
     *                            <b>false</b> - then {@link #getWidth()} and {@link #getHeight()} will <b>NOT</b>
     *                            consider actual size of ImageView, just layout parameters. <br /> If you set 'false'
     *                            it's recommended 'android:layout_width' and 'android:layout_height' (or
     *                            'android:maxWidth' and 'android:maxHeight') are set with concrete values. It helps to
     *                            save memory.
     *                            <p/>
     */
    public ImageViewAware(Image imageView, boolean checkActualViewSize) {
        super(imageView, checkActualViewSize);
    }


    @Override
    public int getWidth() {
        int width = super.getWidth();
        if (width <= 0) {
            Image imageView = (Image) viewRef.get();
            if (imageView != null) {
                width = imageView.getHeight();
            }
        }
        return width;
    }


    @Override
    public int getHeight() {
        int height = super.getHeight();
        if (height <= 0) {
            Image imageView = (Image) viewRef.get();
            if (imageView != null) {
                height = imageView.getHeight();
            }
        }
        return height;
    }

    @Override
    public ViewScaleType getScaleType() {
        Image imageView = (Image) viewRef.get();
        if (imageView != null) {
            return ViewScaleType.fromImageView(imageView);
        }
        return super.getScaleType();
    }

    @Override
    public Image getWrappedView() {
        return (Image) super.getWrappedView();
    }

    @Override
    protected void setImageDrawableInto(Element drawable, Component view) {
        ((Image) view).setImageElement(drawable);
        if (drawable instanceof FrameAnimationElement) {
            ((FrameAnimationElement) drawable).start();
        }
    }

    @Override
    protected void setImageBitmapInto(PixelMap bitmap, Component view, float corner) {
        ((Image) view).setCornerRadius(corner);
        ((Image) view).setPixelMap(bitmap);
    }

    @Override
    protected void setImageBitmapInto(PixelMap bitmap, Component view) {

        ((Image) view).setPixelMap(bitmap);
    }
}
