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
package com.nostra13.universalimageloader.core.display;


import com.nostra13.universalimageloader.utils.L;
import ohos.agp.render.*;
import ohos.agp.utils.RectFloat;
import ohos.media.image.*;
import ohos.agp.components.element.Element;

import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

/**
 * Can display bitmap with rounded corners. This implementation works only with ImageViews wrapped
 * in ImageViewAware.
 * <br />
 * This implementation is inspired by
 * <a href="http://www.curious-creature.org/2012/12/11/android-recipe-1-image-with-rounded-corners/">
 * Romain Guy's article</a>. It rounds images using custom drawable drawing. Original bitmap isn't changed.
 * <br />
 * <br />
 * If this implementation doesn't meet your needs then consider
 * <a href="https://github.com/vinc3m1/RoundedImageView">RoundedImageView</a> or
 * <a href="https://github.com/Pkmmte/CircularImageView">CircularImageView</a> projects for usage.
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @since 1.5.6
 */
public class RoundedBitmapDisplayer implements BitmapDisplayer {

    protected final int cornerRadius;
    protected final int margin;

    public RoundedBitmapDisplayer(int cornerRadiusPixels) {
        this(cornerRadiusPixels, 0);
    }

    public RoundedBitmapDisplayer(int cornerRadiusPixels, int marginPixels) {
        this.cornerRadius = cornerRadiusPixels;
        this.margin = marginPixels;
    }

    @Override
    public void display(PixelMap bitmap, ImageAware imageAware, LoadedFrom loadedFrom) {
        if (!(imageAware instanceof ImageViewAware)) {
            throw new IllegalArgumentException("ImageAware should wrap ImageView. ImageViewAware is expected.");
        }
//        L.info("bitmap:" + bitmap);
//		imageAware.setImageDrawable(new RoundedDrawable(bitmap, cornerRadius, margin));

        imageAware.setImageBitmap(bitmap, 20);
    }

    public static class RoundedDrawable extends Element {

        protected final float cornerRadius;
        protected final int margin;

        protected final RectFloat mRect = new RectFloat(),
                mBitmapRect;
        protected final PixelMapShader bitmapShader;
        protected final Paint paint;

        public RoundedDrawable(PixelMap bitmap, int cornerRadius, int margin) {
            this.cornerRadius = cornerRadius;
            this.margin = margin;

            bitmapShader = new PixelMapShader(new PixelMapHolder(bitmap), Shader.TileMode.CLAMP_TILEMODE, Shader.TileMode.CLAMP_TILEMODE);
            mBitmapRect = new RectFloat(margin, margin, bitmap.getImageInfo().size.width - margin, bitmap.getImageInfo().size.height - margin);

            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setShader(bitmapShader, Paint.ShaderType.PIXELMAP_SHADER);
            paint.setFilterBitmap(true);
            paint.setDither(true);
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
