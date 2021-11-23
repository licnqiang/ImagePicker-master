/*******************************************************************************
 * Copyright 2015 Sergey Tarasevich
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
package com.nostra13.universalimageloader.core.display;



import com.nostra13.universalimageloader.utils.L;
import com.nostra13.universalimageloader.utils.PixelMapUtils;
import ohos.agp.render.*;
import ohos.agp.utils.Color;
import ohos.media.image.PixelMap;
import ohos.agp.utils.RectFloat;
import ohos.media.image.*;
import ohos.agp.utils.Rect;
import ohos.agp.utils.Matrix;
import ohos.agp.components.element.Element;


import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

/**
 * Can display bitmap cropped by a circle. This implementation works only with ImageViews wrapped
 * in ImageViewAware.
 * <br />
 * If this implementation doesn't meet your needs then consider
 * <a href="https://github.com/vinc3m1/RoundedImageView">RoundedImageView</a> or
 * <a href="https://github.com/Pkmmte/CircularImageView">CircularImageView</a> projects for usage.
 *
 * @author Qualtagh, Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @since 1.9.5
 */
public class CircleBitmapDisplayer implements BitmapDisplayer {

	protected final Integer strokeColor;
	protected final float strokeWidth;

	public CircleBitmapDisplayer() {
		this(null);
	}

	public CircleBitmapDisplayer(Integer strokeColor) {
		this(strokeColor, 0);
	}

	public CircleBitmapDisplayer(Integer strokeColor, float strokeWidth) {
		this.strokeColor = strokeColor;
		this.strokeWidth = strokeWidth;
	}

	@Override
	public void display(PixelMap bitmap, ImageAware imageAware, LoadedFrom loadedFrom) {
		if (!(imageAware instanceof ImageViewAware)) {
			throw new IllegalArgumentException("ImageAware should wrap ImageView. ImageViewAware is expected.");
		}
//		L.info("PixelMap:" + bitmap);
//		imageAware.setImageDrawable(new CircleDrawable(bitmap, strokeColor, strokeWidth));

//		imageAware.setImageBitmap(bitmap);

		imageAware.setImageBitmap(PixelMapUtils.cropBitmap(bitmap),100);
	}

	public static class CircleDrawable extends Element {

		protected float radius;

		protected final RectFloat mRect = new RectFloat();
		protected final RectFloat mBitmapRect;
		protected final PixelMapShader bitmapShader;
		protected final Paint paint;
		protected final Paint strokePaint;
		protected final float strokeWidth;
		protected float strokeRadius;

		public CircleDrawable(PixelMap bitmap, Integer strokeColor, float strokeWidth) {
			int diameter = Math.min(bitmap.getImageInfo().size.width, bitmap.getImageInfo().size.height);
			radius = diameter / 2f;

			bitmapShader = new PixelMapShader(new PixelMapHolder(bitmap), Shader.TileMode.CLAMP_TILEMODE, Shader.TileMode.CLAMP_TILEMODE);

			float left = (bitmap.getImageInfo().size.width - diameter) / 2f;
			float top = (bitmap.getImageInfo().size.height - diameter) / 2f;
			mBitmapRect = new RectFloat((int)left, (int)top, diameter, diameter);

			paint = new Paint();
			paint.setAntiAlias(true);
			paint.setShader(bitmapShader,Paint.ShaderType.PIXELMAP_SHADER);
			paint.setFilterBitmap(true);
			paint.setDither(true);

			if (strokeColor == null) {
				strokePaint = null;
			} else {
				strokePaint = new Paint();
				strokePaint.setStyle(Paint.Style.STROKE_STYLE);
				strokePaint.setColor(new Color(strokeColor));
				strokePaint.setStrokeWidth(strokeWidth);
				strokePaint.setAntiAlias(true);
			}
			this.strokeWidth = strokeWidth;
			strokeRadius = radius - strokeWidth / 2;
		}

		/*@Override
		protected void onBoundsChange(Rect bounds) {
			super.onBoundsChange(bounds);
			mRect.modify(0, 0, bounds.getWidth(), bounds.getHeight());
			radius = Math.min(bounds.getWidth(), bounds.getHeight()) / 2;
			strokeRadius = radius - strokeWidth / 2;

			// Resize the original bitmap to fit the new bound
			Matrix shaderMatrix = new Matrix();
			shaderMatrix.setRectToRect(mBitmapRect, mRect, Matrix.ScaleToFit.FILL);
			bitmapShader.setShaderMatrix(shaderMatrix);
		}*/

	/*	@Override
		public void draw(Canvas canvas) {
			canvas.drawCircle(radius, radius, radius, paint);
			if (strokePaint != null) {
				canvas.drawCircle(radius, radius, strokeRadius, strokePaint);
			}
		}*/
/*
		@Override
		public int getOpacity() {
			return PixelFormat.TRANSLUCENT;
		}*/

		@Override
		public void setAlpha(int alpha) {
			paint.setAlpha(alpha);
		}

	/*	@Override
		public void setColorFilter(ColorFilter cf) {
			paint.setColorFilter(cf);
		}*/
	}
}
