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
package com.nostra13.universalimageloader.core.decode;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.ViewScaleType;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import ohos.media.image.ImageSource.DecodingOptions;

/**
 * Contains needed information for decoding image to Bitmap
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @since 1.8.3
 */
public class ImageDecodingInfo {

	private final String imageKey;
	private final String imageUri;
	private final String originalImageUri;
	private final ImageSize targetSize;

	private final ImageScaleType imageScaleType;
	private final ViewScaleType viewScaleType;

	private final ImageDownloader downloader;
	private final Object extraForDownloader;

	private final boolean considerExifParams;
	private final DecodingOptions decodingOptions;

	/**
	 * Image decoding info
	 *
	 * @param imageKey         image key
	 * @param imageUri         image uri
	 * @param originalImageUri original image uri
	 * @param targetSize       target size
	 * @param viewScaleType    view scale type
	 * @param downloader       downloader
	 * @param displayOptions   display options
	 */
	public ImageDecodingInfo(String imageKey, String imageUri, String originalImageUri, ImageSize targetSize, ViewScaleType viewScaleType,
							 ImageDownloader downloader, DisplayImageOptions displayOptions) {
		this.imageKey = imageKey;
		this.imageUri = imageUri;
		this.originalImageUri = originalImageUri;
		this.targetSize = targetSize;

		this.imageScaleType = displayOptions.getImageScaleType();
		this.viewScaleType = viewScaleType;

		this.downloader = downloader;
		this.extraForDownloader = displayOptions.getExtraForDownloader();

		considerExifParams = displayOptions.isConsiderExifParams();
		decodingOptions = new DecodingOptions();
		copyOptions(displayOptions.getDecodingOptions(), decodingOptions);
	}

	private void copyOptions(DecodingOptions srcOptions, DecodingOptions destOptions) {
		destOptions.allowPartialImage = srcOptions.allowPartialImage;
		destOptions.desiredColorSpace = srcOptions.desiredColorSpace;
		destOptions.desiredPixelFormat = srcOptions.desiredPixelFormat;
		destOptions.desiredRegion = srcOptions.desiredRegion;
		destOptions.desiredSize = srcOptions.desiredSize;
		destOptions.editable = srcOptions.editable;
		destOptions.rotateDegrees = srcOptions.rotateDegrees;
		destOptions.sampleSize = srcOptions.sampleSize;

	}

/*	@TargetApi(10)
	private void copyOptions10(Options srcOptions, Options destOptions) {
		destOptions.inPreferQualityOverSpeed = srcOptions.inPreferQualityOverSpeed;
	}*/

/*	@TargetApi(11)
	private void copyOptions11(Options srcOptions, Options destOptions) {
		destOptions.inBitmap = srcOptions.inBitmap;
		destOptions.inMutable = srcOptions.inMutable;
	}*/

	/**
	 * Gets image key *
	 *
	 * @return Original {@linkplain com.nostra13.universalimageloader.utils.MemoryCacheUtils#generateKey(String, ImageSize) image key} (used in memory cache).
	 */
	public String getImageKey() {
		return imageKey;
	}

	/**
	 * Gets image uri *
	 *
	 * @return Image URI for decoding (usually image from disk cache)
	 */
	public String getImageUri() {
		return imageUri;
	}

	/**
	 * Gets original image uri *
	 *
	 * @return The original image URI which was passed to ImageLoader
	 */
	public String getOriginalImageUri() {
		return originalImageUri;
	}

	/**
	 * Gets target size *
	 *
	 * @return Target size for image. Decoded bitmap should close to this size according to {@linkplain ImageScaleType
	 * image scale type} and {@linkplain ViewScaleType view scale type}.
	 */
	public ImageSize getTargetSize() {
		return targetSize;
	}

	/**
	 * Gets image scale type *
	 *
	 * @return {@linkplain ImageScaleType Scale type for image sampling and scaling}. This parameter affects result size of decoded bitmap.
	 */
	public ImageScaleType getImageScaleType() {
		return imageScaleType;
	}

	/**
	 * Gets view scale type *
	 *
	 * @return {@linkplain ViewScaleType View scale type}. This parameter affects result size of decoded bitmap.
	 */
	public ViewScaleType getViewScaleType() {
		return viewScaleType;
	}

	/**
	 * Gets downloader *
	 *
	 * @return Downloader for image loading
	 */
	public ImageDownloader getDownloader() {
		return downloader;
	}

	/**
	 * Gets extra for downloader *
	 *
	 * @return Auxiliary object for downloader
	 */
	public Object getExtraForDownloader() {
		return extraForDownloader;
	}

	/**
	 * Should consider exif params boolean
	 *
	 * @return <b>true</b> - if EXIF params of image should be considered; <b>false</b> - otherwise
	 */
	public boolean shouldConsiderExifParams() {
		return considerExifParams;
	}

	/**
	 * Gets decoding options *
	 *
	 * @return Decoding options
	 */
	public DecodingOptions getDecodingOptions() {
		return decodingOptions;
	}
}