/*******************************************************************************
 * Copyright 2014 Sergey Tarasevich
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
import ohos.eventhandler.EventRunner;
import ohos.agp.components.Component;
import ohos.agp.components.ComponentContainer;
import com.nostra13.universalimageloader.core.assist.ViewScaleType;
import com.nostra13.universalimageloader.utils.L;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

/**
 * Wrapper for Android {@link ohos.agp.components.Component View}. Keeps weak reference of View to prevent memory leaks.
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @since 1.9.2
 */
public abstract class ViewAware implements ImageAware {

	/**
	 * WARN_CANT_SET_DRAWABLE
	 */
	public static final String WARN_CANT_SET_DRAWABLE = "Can't set a drawable into view. You should call ImageLoader on UI thread for it.";
	/**
	 * WARN_CANT_SET_BITMAP
	 */
	public static final String WARN_CANT_SET_BITMAP = "Can't set a bitmap into view. You should call ImageLoader on UI thread for it.";

	/**
	 * View ref
	 */
	protected Reference<Component> viewRef;
	/**
	 * Check actual view size
	 */
	protected boolean checkActualViewSize;

	/**
	 * Constructor. <br />
	 * References {@link #ViewAware(ohos.agp.components.Component, boolean) ImageViewAware(imageView, true)}.
	 *
	 * @param view {@link ohos.agp.components.Component View} to work with
	 */
	public ViewAware(Component view) {
		this(view, true);
	}

	/**
	 * Constructor
	 *
	 * @param view                {@link ohos.agp.components.Component View} to work with
	 * @param checkActualViewSize <b>true</b> - then {@link #getWidth()} and {@link #getHeight()} will check actual                            size of View. It can cause known issues like                            <a href="https://github.com/nostra13/Android-Universal-Image-Loader/issues/376">this</a>.                            But it helps to save memory because memory cache keeps bitmaps of actual (less in                            general) size.                            <p/>                            <b>false</b> - then {@link #getWidth()} and {@link #getHeight()} will <b>NOT</b>                            consider actual size of View, just layout parameters. <br /> If you set 'false'                            it's recommended 'android:layout_width' and 'android:layout_height' (or                            'android:maxWidth' and 'android:maxHeight') are set with concrete values. It helps to                            save memory.
	 */
	public ViewAware(Component view, boolean checkActualViewSize) {
		if (view == null) throw new IllegalArgumentException("view must not be null");

		this.viewRef = new WeakReference<Component>(view);
		this.checkActualViewSize = checkActualViewSize;
	}


	@Override
	public int getWidth() {
		Component view = viewRef.get();
		if (view != null) {
			final ComponentContainer.LayoutConfig params = view.getLayoutConfig();
			int width = 0;
			if (checkActualViewSize && params != null && params.width != ComponentContainer.LayoutConfig.MATCH_CONTENT) {
				width = view.getWidth(); // Get actual image width
			}
			if (width <= 0 && params != null) width = params.width; // Get layout width parameter
			return width;
		}
		return 0;
	}


	@Override
	public int getHeight() {
		Component view = viewRef.get();
		if (view != null) {
			final ComponentContainer.LayoutConfig params = view.getLayoutConfig();
			int height = 0;
			if (checkActualViewSize && params != null && params.height != ComponentContainer.LayoutConfig.MATCH_CONTENT) {
				height = view.getHeight(); // Get actual image height
			}
			if (height <= 0 && params != null) height = params.height; // Get layout height parameter
			return height;
		}
		return 0;
	}

	@Override
	public ViewScaleType getScaleType() {
		return ViewScaleType.CROP;
	}

	@Override
	public Component getWrappedView() {
		return viewRef.get();
	}

	@Override
	public boolean isCollected() {
		return viewRef.get() == null;
	}

	@Override
	public int getId() {
		Component view = viewRef.get();
		return view == null ? super.hashCode() : view.hashCode();
	}

	@Override
	public boolean setImageDrawable(Element drawable) {
		if (EventRunner.current() == EventRunner.getMainEventRunner()) {
			Component view = viewRef.get();
			if (view != null) {
				setImageDrawableInto(drawable, view);
				return true;
			}
		} else {
			L.w(WARN_CANT_SET_DRAWABLE);
		}
		return false;
	}

	@Override
	public boolean setImageBitmap(PixelMap bitmap) {
		if (EventRunner.current() == EventRunner.getMainEventRunner()) {
			Component view = viewRef.get();
			if (view != null) {
				setImageBitmapInto(bitmap, view);
				return true;
			}
		} else {
			L.w(WARN_CANT_SET_BITMAP);
		}
		return false;
	}

	@Override
	public boolean setImageBitmap(PixelMap bitmap,float corner) {
		if (EventRunner.current() == EventRunner.getMainEventRunner()) {
			Component view = viewRef.get();
			if (view != null) {
				setImageBitmapInto(bitmap, view,corner);
				return true;
			}
		} else {
			L.w(WARN_CANT_SET_BITMAP);
		}
		return false;
	}

	/**
	 * Should set drawable into incoming view. Incoming view is guaranteed not null.<br />
	 * This method is called on UI thread.
	 *
	 * @param drawable drawable
	 * @param view     view
	 */
	protected abstract void setImageDrawableInto(Element drawable, Component view);

	/**
	 * Should set Bitmap into incoming view. Incoming view is guaranteed not null.< br />
	 * This method is called on UI thread.
	 *
	 * @param bitmap bitmap
	 * @param view   view
	 */
	protected abstract void setImageBitmapInto(PixelMap bitmap, Component view);

	/**
	 * Sets image bitmap into *
	 *
	 * @param bitmap bitmap
	 * @param view   view
	 * @param corner corner
	 */
	protected abstract void setImageBitmapInto(PixelMap bitmap, Component view,float corner);
}
