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
package com.nostra13.universalimageloader.cache.memory.impl;

import com.nostra13.universalimageloader.cache.memory.LimitedMemoryCache;
import ohos.media.image.PixelMap;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Limited {@link PixelMap PixelMap} cache. Provides {@link PixelMap PixelMaps} storing. Size of all stored PixelMaps will not to
 * exceed size limit. When cache reaches limit size then cache clearing is processed by FIFO principle.<br />
 * <br />
 * <b>NOTE:</b> This cache uses strong and weak references for stored PixelMaps. Strong references - for limited count of
 * PixelMaps (depends on cache size), weak references - for all other cached PixelMaps.
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @since 1.0.0
 */
public class FIFOLimitedMemoryCache extends LimitedMemoryCache {

	private final List<PixelMap> queue = Collections.synchronizedList(new LinkedList<PixelMap>());

	public FIFOLimitedMemoryCache(int sizeLimit) {
		super(sizeLimit);
	}

	@Override
	public boolean put(String key, PixelMap value) {
		if (super.put(key, value)) {
			queue.add(value);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public PixelMap remove(String key) {
		PixelMap value = super.get(key);
		if (value != null) {
			queue.remove(value);
		}
		return super.remove(key);
	}

	@Override
	public void clear() {
		queue.clear();
		super.clear();
	}

	@Override
	protected int getSize(PixelMap value) {
		return value.getBytesNumberPerRow() * value.getImageInfo().size.height;
	}

	@Override
	protected PixelMap removeNext() {
		return queue.remove(0);
	}

	@Override
	protected Reference<PixelMap> createReference(PixelMap value) {
		return new WeakReference<PixelMap>(value);
	}
}
