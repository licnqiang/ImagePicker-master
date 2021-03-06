/*******************************************************************************
 * Copyright 2011-2014 Sergey Tarasevich
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
package com.nostra13.universalimageloader.core;

import com.nostra13.universalimageloader.utils.L;
import com.nostra13.universalimageloader.utils.ResUtil;
import ohos.global.resource.ResourceManager;
import ohos.media.image.PixelMap;
import ohos.media.image.ImageSource.DecodingOptions;
import ohos.agp.components.element.Element;
import ohos.eventhandler.EventHandler;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.BitmapDisplayer;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;
import ohos.media.image.common.PixelFormat;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contains options for image display. Defines:
 * <ul>
 * <li>whether stub image will be displayed in {@link com.nostra13.universalimageloader.core.imageaware.ImageAware
 * image aware view}* during image loading</li>
 * <li>whether stub image will be displayed in {@link com.nostra13.universalimageloader.core.imageaware.ImageAware
 * image aware view}* if empty URI is passed</li>
 * <li>whether stub image will be displayed in {@link com.nostra13.universalimageloader.core.imageaware.ImageAware
 * image aware view}* if image loading fails</li>
 * <li>whether {@link com.nostra13.universalimageloader.core.imageaware.ImageAware image aware view} should be reset
 * before image loading start</li>
 * <li>whether loaded image will be cached in memory</li>
 * <li>whether loaded image will be cached on disk</li>
 * <li>image scale type</li>
 * <li>decoding options (including bitmap decoding configuration)</li>
 * <li>delay before loading of image</li>
 * <li>whether consider EXIF parameters of image</li>
 * <li>auxiliary object which will be passed to {@link ImageDownloader#getStream(String, Object) ImageDownloader}</li>
 * <li>pre-processor for image Bitmap (before caching in memory)</li>
 * <li>post-processor for image Bitmap (after caching in memory, before displaying)</li>
 * <li>how decoded {@link PixelMap} will be displayed</li>
 * </ul>
 * <p/>
 * You can create instance:
 * <ul>
 * <li>with {@link Builder}:<br />
 * <b>i.e.</b> :
 * <code>new {@link DisplayImageOptions}.Builder().{@link Builder#cacheInMemory() cacheInMemory()}.
 * {@link Builder#showImageOnLoading(int) showImageOnLoading()}.{@link Builder#build() build()}</code><br />
 * </li>
 * <li>or by static method: {@link #createSimple()}</li> <br />
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @since 1.0.0
 */
public final class DisplayImageOptions {

    private final int imageResOnLoading;
    private final int imageResForEmptyUri;
    private final int imageResOnFail;
    private final Element imageOnLoading;
    private final Element imageForEmptyUri;
    private final Element imageOnFail;
    private final boolean resetViewBeforeLoading;
    private final boolean cacheInMemory;
    private final boolean cacheOnDisk;
    private final ImageScaleType imageScaleType;
    private final DecodingOptions decodingOptions;
    private final int delayBeforeLoading;
    private final boolean considerExifParams;
    private final Object extraForDownloader;
    private final BitmapProcessor preProcessor;
    private final BitmapProcessor postProcessor;
    private final BitmapDisplayer displayer;
    private final EventHandler handler;
    private final boolean isSyncLoading;

    private DisplayImageOptions(Builder builder) {
        imageResOnLoading = builder.imageResOnLoading;
        imageResForEmptyUri = builder.imageResForEmptyUri;
        imageResOnFail = builder.imageResOnFail;
        imageOnLoading = builder.imageOnLoading;
        imageForEmptyUri = builder.imageForEmptyUri;
        imageOnFail = builder.imageOnFail;
        resetViewBeforeLoading = builder.resetViewBeforeLoading;
        cacheInMemory = builder.cacheInMemory;
        cacheOnDisk = builder.cacheOnDisk;
        imageScaleType = builder.imageScaleType;
        decodingOptions = builder.decodingOptions;
        delayBeforeLoading = builder.delayBeforeLoading;
        considerExifParams = builder.considerExifParams;
        extraForDownloader = builder.extraForDownloader;
        preProcessor = builder.preProcessor;
        postProcessor = builder.postProcessor;
        displayer = builder.displayer;
        handler = builder.handler;
        isSyncLoading = builder.isSyncLoading;

    }

    /**
     * Should show image on loading boolean
     *
     * @return the boolean
     */
    public boolean shouldShowImageOnLoading() {
        return imageOnLoading != null || imageResOnLoading != 0;
    }

    /**
     * Should show image for empty uri boolean
     *
     * @return the boolean
     */
    public boolean shouldShowImageForEmptyUri() {
        return imageForEmptyUri != null || imageResForEmptyUri != 0;
    }

    /**
     * Should show image on fail boolean
     *
     * @return the boolean
     */
    public boolean shouldShowImageOnFail() {
        return imageOnFail != null || imageResOnFail != 0;
    }

    /**
     * Should pre process boolean
     *
     * @return the boolean
     */
    public boolean shouldPreProcess() {
        return preProcessor != null;
    }

    /**
     * Should post process boolean
     *
     * @return the boolean
     */
    public boolean shouldPostProcess() {
        return postProcessor != null;
    }

    /**
     * Should delay before loading boolean
     *
     * @return the boolean
     */
    public boolean shouldDelayBeforeLoading() {
        return delayBeforeLoading > 0;
    }

    /**
     * Gets image on loading *
     *
     * @param res res
     * @return the image on loading
     */
    public Element getImageOnLoading(ResourceManager res) {
        if(imageResOnLoading != 0){
            try {
                return ResUtil.getDrawable(res,imageResOnLoading);
            }catch (Exception e){
                Logger.getGlobal().log(Level.INFO, "Exception");
            }
        }
        return imageOnLoading;
    }

    /**
     * Gets image for empty uri *
     *
     * @param res res
     * @return the image for empty uri
     */
    public Element getImageForEmptyUri(ResourceManager res) {
        if(imageResForEmptyUri != 0){
            try {
                return ResUtil.getDrawable(res,imageResForEmptyUri);
            }catch (Exception e){
                Logger.getGlobal().log(Level.INFO, "Exception");
            }
        }
        return imageForEmptyUri;
    }

    /**
     * Gets image on fail *
     *
     * @param res res
     * @return the image on fail
     */
    public Element getImageOnFail(ResourceManager res) {
        if(imageResOnFail != 0){
            try {
                return ResUtil.getDrawable(res,imageResOnFail);
            }catch (Exception e){
                Logger.getGlobal().log(Level.INFO, "Exception");
            }
        }
        return imageOnFail;
    }

    /**
     * Is reset view before loading boolean
     *
     * @return the boolean
     */
    public boolean isResetViewBeforeLoading() {
        return resetViewBeforeLoading;
    }

    /**
     * Is cache in memory boolean
     *
     * @return the boolean
     */
    public boolean isCacheInMemory() {
        return cacheInMemory;
    }

    /**
     * Is cache on disk boolean
     *
     * @return the boolean
     */
    public boolean isCacheOnDisk() {
        return cacheOnDisk;
    }

    /**
     * Gets image scale type *
     *
     * @return the image scale type
     */
    public ImageScaleType getImageScaleType() {
        return imageScaleType;
    }

    /**
     * Gets decoding options *
     *
     * @return the decoding options
     */
    public DecodingOptions getDecodingOptions() {
        return decodingOptions;
    }

    /**
     * Gets delay before loading *
     *
     * @return the delay before loading
     */
    public int getDelayBeforeLoading() {
        return delayBeforeLoading;
    }

    /**
     * Is consider exif params boolean
     *
     * @return the boolean
     */
    public boolean isConsiderExifParams() {
        return considerExifParams;
    }

    /**
     * Gets extra for downloader *
     *
     * @return the extra for downloader
     */
    public Object getExtraForDownloader() {
        return extraForDownloader;
    }

    /**
     * Gets pre processor *
     *
     * @return the pre processor
     */
    public BitmapProcessor getPreProcessor() {
        return preProcessor;
    }

    /**
     * Gets post processor *
     *
     * @return the post processor
     */
    public BitmapProcessor getPostProcessor() {
        return postProcessor;
    }

    /**
     * Gets displayer *
     *
     * @return the displayer
     */
    public BitmapDisplayer getDisplayer() {
        return displayer;
    }

    /**
     * Gets handler *
     *
     * @return the handler
     */
    public EventHandler getHandler() {
        return handler;
    }

    /**
     * Is sync loading boolean
     *
     * @return the boolean
     */
    boolean isSyncLoading() {
        return isSyncLoading;
    }

    /**
     * Builder for {@link DisplayImageOptions}
     *
     * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
     */
    public static class Builder {
        private int imageResOnLoading = 0;
        private int imageResForEmptyUri = 0;
        private int imageResOnFail = 0;
        private Element imageOnLoading = null;
        private Element imageForEmptyUri = null;
        private Element imageOnFail = null;
        private boolean resetViewBeforeLoading = false;
        private boolean cacheInMemory = false;
        private boolean cacheOnDisk = false;
        private ImageScaleType imageScaleType = ImageScaleType.IN_SAMPLE_POWER_OF_2;
        private DecodingOptions decodingOptions = new DecodingOptions();
        private int delayBeforeLoading = 0;
        private boolean considerExifParams = false;
        private Object extraForDownloader = null;
        private BitmapProcessor preProcessor = null;
        private BitmapProcessor postProcessor = null;
        private BitmapDisplayer displayer = DefaultConfigurationFactory.createBitmapDisplayer();
        private EventHandler handler = null;
        private boolean isSyncLoading = false;

        /**
         * Stub image will be displayed in {@link com.nostra13.universalimageloader.core.imageaware.ImageAware
         * image aware view}* during image loading
         *
         * @param imageRes Stub image resource
         * @return the builder
         * @deprecated Use {@link #showImageOnLoading(int)} instead
         */
        @Deprecated
        public Builder showStubImage(int imageRes) {
            imageResOnLoading = imageRes;
            return this;
        }

        /**
         * Incoming image will be displayed in {@link com.nostra13.universalimageloader.core.imageaware.ImageAware
         * image aware view}* during image loading
         *
         * @param imageRes Image resource
         * @return the builder
         */
        public Builder showImageOnLoading(int imageRes) {
            imageResOnLoading = imageRes;
            return this;
        }

        /**
         * Incoming drawable will be displayed in {@link com.nostra13.universalimageloader.core.imageaware.ImageAware
         * image aware view}* during image loading.
         * This option will be ignored if {@link Builder#showImageOnLoading(int)} is set.
         *
         * @param drawable drawable
         * @return the builder
         */
        public Builder showImageOnLoading(Element drawable) {
            imageOnLoading = drawable;
            return this;
        }

        /**
         * Incoming image will be displayed in {@link com.nostra13.universalimageloader.core.imageaware.ImageAware
         * image aware view}* if empty URI (null or empty
         * string) will be passed to <b>ImageLoader.displayImage(...)</b> method.
         *
         * @param imageRes Image resource
         * @return the builder
         */
        public Builder showImageForEmptyUri(int imageRes) {
            imageResForEmptyUri = imageRes;
            return this;
        }

        /**
         * Incoming drawable will be displayed in {@link com.nostra13.universalimageloader.core.imageaware.ImageAware
         * image aware view}* if empty URI (null or empty
         * string) will be passed to <b>ImageLoader.displayImage(...)</b> method.
         * This option will be ignored if {@link Builder#showImageForEmptyUri(int)} is set.
         *
         * @param drawable drawable
         * @return the builder
         */
        public Builder showImageForEmptyUri(Element drawable) {
            imageForEmptyUri = drawable;
            return this;
        }

        /**
         * Incoming image will be displayed in {@link com.nostra13.universalimageloader.core.imageaware.ImageAware
         * image aware view}* if some error occurs during
         * requested image loading/decoding.
         *
         * @param imageRes Image resource
         * @return the builder
         */
        public Builder showImageOnFail(int imageRes) {
            imageResOnFail = imageRes;
            return this;
        }

        /**
         * Incoming drawable will be displayed in {@link com.nostra13.universalimageloader.core.imageaware.ImageAware
         * image aware view}* if some error occurs during
         * requested image loading/decoding.
         * This option will be ignored if {@link Builder#showImageOnFail(int)} is set.
         *
         * @param drawable drawable
         * @return the builder
         */
        public Builder showImageOnFail(Element drawable) {
            imageOnFail = drawable;
            return this;
        }

        /**
         * {@link com.nostra13.universalimageloader.core.imageaware.ImageAware
         * image aware view}* will be reset (set <b>null</b>) before image loading start
         *
         * @return the builder
         * @deprecated Use {@link #resetViewBeforeLoading(boolean) resetViewBeforeLoading(true)} instead
         */
        public Builder resetViewBeforeLoading() {
            resetViewBeforeLoading = true;
            return this;
        }

        /**
         * Sets whether {@link com.nostra13.universalimageloader.core.imageaware.ImageAware
         * image aware view}* will be reset (set <b>null</b>) before image loading start
         *
         * @param resetViewBeforeLoading reset view before loading
         * @return the builder
         */
        public Builder resetViewBeforeLoading(boolean resetViewBeforeLoading) {
            this.resetViewBeforeLoading = resetViewBeforeLoading;
            return this;
        }

        /**
         * Loaded image will be cached in memory
         *
         * @return the builder
         * @deprecated Use {@link #cacheInMemory(boolean) cacheInMemory(true)} instead
         */
        @Deprecated
        public Builder cacheInMemory() {
            cacheInMemory = true;
            return this;
        }

        /**
         * Sets whether loaded image will be cached in memory
         *
         * @param cacheInMemory cache in memory
         * @return the builder
         */
        public Builder cacheInMemory(boolean cacheInMemory) {
            this.cacheInMemory = cacheInMemory;
            return this;
        }

        /**
         * Loaded image will be cached on disk
         *
         * @return the builder
         * @deprecated Use {@link #cacheOnDisk(boolean) cacheOnDisk(true)} instead
         */
        @Deprecated
        public Builder cacheOnDisc() {
            return cacheOnDisk(true);
        }

        /**
         * Sets whether loaded image will be cached on disk
         *
         * @param cacheOnDisk cache on disk
         * @return the builder
         * @deprecated Use {@link #cacheOnDisk(boolean)} instead
         */
        @Deprecated
        public Builder cacheOnDisc(boolean cacheOnDisk) {
            return cacheOnDisk(cacheOnDisk);
        }

        /**
         * Sets whether loaded image will be cached on disk
         *
         * @param cacheOnDisk cache on disk
         * @return the builder
         */
        public Builder cacheOnDisk(boolean cacheOnDisk) {
            this.cacheOnDisk = cacheOnDisk;
            return this;
        }

        /**
         * Sets {@linkplain ImageScaleType scale type} for decoding image. This parameter is used while define scale
         * size for decoding image to PixelMap. Default value - {@link ImageScaleType#IN_SAMPLE_POWER_OF_2}
         *
         * @param imageScaleType image scale type
         * @return the builder
         */
        public Builder imageScaleType(ImageScaleType imageScaleType) {
            this.imageScaleType = imageScaleType;
            return this;
        }

        /**
         * Bitmap config builder
         *
         * @param bitmapConfig bitmap config
         * @return the builder
         */
        public Builder bitmapConfig(PixelFormat bitmapConfig) {
            if (bitmapConfig == null) throw new IllegalArgumentException("bitmapConfig can't be null");
            decodingOptions.desiredPixelFormat = bitmapConfig;
            return this;
        }

        /**
         * Sets options for image decoding.<br />
         * calculate the most appropriate sample size itself according yo {@link #imageScaleType(ImageScaleType)}
         * options.<br />
         * option.
         *
         * @param decodingOptions decoding options
         * @return the builder
         */
        public Builder decodingOptions(DecodingOptions decodingOptions) {
            if (decodingOptions == null) throw new IllegalArgumentException("decodingOptions can't be null");
            this.decodingOptions = decodingOptions;
            return this;
        }

        /**
         * Sets delay time before starting loading task. Default - no delay.
         *
         * @param delayInMillis delay in millis
         * @return the builder
         */
        public Builder delayBeforeLoading(int delayInMillis) {
            this.delayBeforeLoading = delayInMillis;
            return this;
        }

        /**
         * Sets auxiliary object which will be passed to {@link ImageDownloader#getStream(String, Object)}
         *
         * @param extra extra
         * @return the builder
         */
        public Builder extraForDownloader(Object extra) {
            this.extraForDownloader = extra;
            return this;
        }

        /**
         * Sets whether ImageLoader will consider EXIF parameters of JPEG image (rotate, flip)
         *
         * @param considerExifParams consider exif params
         * @return the builder
         */
        public Builder considerExifParams(boolean considerExifParams) {
            this.considerExifParams = considerExifParams;
            return this;
        }

        /**
         * Sets bitmap processor which will be process bitmaps before they will be cached in memory. So memory cache
         * will contain bitmap processed by incoming preProcessor.<br />
         * Image will be pre-processed even if caching in memory is disabled.
         *
         * @param preProcessor pre processor
         * @return the builder
         */
        public Builder preProcessor(BitmapProcessor preProcessor) {
            this.preProcessor = preProcessor;
            return this;
        }

        /**
         * Sets bitmap processor which will be process bitmaps before they will be displayed in
         * {@link com.nostra13.universalimageloader.core.imageaware.ImageAware image aware view} but
         * after they'll have been saved in memory cache.
         *
         * @param postProcessor post processor
         * @return the builder
         */
        public Builder postProcessor(BitmapProcessor postProcessor) {
            this.postProcessor = postProcessor;
            return this;
        }

        /**
         * Sets custom {@link BitmapDisplayer displayer} for image loading task. Default value -
         * {@link DefaultConfigurationFactory#createBitmapDisplayer()}
         *
         * @param displayer displayer
         * @return the builder
         */
        public Builder displayer(BitmapDisplayer displayer) {
            if (displayer == null) throw new IllegalArgumentException("displayer can't be null");
            this.displayer = displayer;
            return this;
        }

        /**
         * Sync loading builder
         *
         * @param isSyncLoading is sync loading
         * @return the builder
         */
        Builder syncLoading(boolean isSyncLoading) {
            this.isSyncLoading = isSyncLoading;
            return this;
        }

        /**
         * listener} events.
         *
         * @param handler handler
         * @return the builder
         */
        public Builder handler(EventHandler handler) {
            this.handler = handler;
            return this;
        }

        /**
         * Sets all options equal to incoming options
         *
         * @param options options
         * @return the builder
         */
        public Builder cloneFrom(DisplayImageOptions options) {
            imageResOnLoading = options.imageResOnLoading;
            imageResForEmptyUri = options.imageResForEmptyUri;
            imageResOnFail = options.imageResOnFail;
            imageOnLoading = options.imageOnLoading;
            imageForEmptyUri = options.imageForEmptyUri;
            imageOnFail = options.imageOnFail;
            resetViewBeforeLoading = options.resetViewBeforeLoading;
            cacheInMemory = options.cacheInMemory;
            cacheOnDisk = options.cacheOnDisk;
            imageScaleType = options.imageScaleType;
            decodingOptions = options.decodingOptions;
            delayBeforeLoading = options.delayBeforeLoading;
            considerExifParams = options.considerExifParams;
            extraForDownloader = options.extraForDownloader;
            preProcessor = options.preProcessor;
            postProcessor = options.postProcessor;
            displayer = options.displayer;
            handler = options.handler;
            isSyncLoading = options.isSyncLoading;
            return this;
        }

        /**
         * Builds configured {@link DisplayImageOptions} object
         *
         * @return the display image options
         */
        public DisplayImageOptions build() {
            return new DisplayImageOptions(this);
        }
    }

    /**
     * Creates options appropriate for single displaying:
     * <ul>
     * <li>View will <b>not</b> be reset before loading</li>
     * <li>Loaded image will <b>not</b> be cached in memory</li>
     * <li>Loaded image will <b>not</b> be cached on disk</li>
     * <li>{@link ImageScaleType#IN_SAMPLE_POWER_OF_2} decoding type will be used</li>
     * <li>{@link SimpleBitmapDisplayer} will be used for image displaying</li>
     * </ul>
     * <p/>
     * These option are appropriate for simple single-use image (from drawables or from Internet) displaying.
     *
     * @return the display image options
     */
    public static DisplayImageOptions createSimple() {
        return new Builder().build();
    }
}
