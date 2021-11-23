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

import ohos.media.image.PixelMap;
import com.nostra13.universalimageloader.cache.memory.LimitedMemoryCache;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Limited {@link PixelMap PixelMap} cache. Provides {@link PixelMap PixelMaps} storing. Size of all stored PixelMaps will not to
 * exceed size limit. When cache reaches limit size then the least recently used PixelMap is deleted from cache.<br />
 * <br />
 * <b>NOTE:</b> This cache uses strong and weak references for stored PixelMaps. Strong references - for limited count of
 * PixelMaps (depends on cache size), weak references - for all other cached PixelMaps.
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @since 1.3.0
 */
public class LRULimitedMemoryCache extends LimitedMemoryCache {

	private static final int INITIAL_CAPACITY = 10;
	private static final float LOAD_FACTOR = 1.1f;

	/** Cache providing Least-Recently-Used logic */
	private final Map<String, PixelMap> lruCache = Collections.synchronizedMap(new LinkedHashMap<String, PixelMap>(INITIAL_CAPACITY, LOAD_FACTOR, true));

	/** @param maxSize Maximum sum of the sizes of the PixelMaps in this cache */
	public LRULimitedMemoryCache(int maxSize) {
		super(maxSize);
	}

	@Override
	public boolean put(String key, PixelMap value) {
		if (super.put(key, value)) {
			lruCache.put(key, value);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public PixelMap get(String key) {
		lruCache.get(key); // call "get" for LRU logic
		return super.get(key);
	}

	@Override
	public PixelMap remove(String key) {
		lruCache.remove(key);
		return super.remove(key);
	}

	@Override
	public void clear() {
		lruCache.clear();
		super.clear();
	}

	@Override
	protected int getSize(PixelMap value) {
		return value.getBytesNumberPerRow() * value.getImageInfo().size.height;
	}

	@Override
	protected PixelMap removeNext() {
		PixelMap mostLongUsedValue = null;
		synchronized (lruCache) {
			Iterator<Entry<String, PixelMap>> it = lruCache.entrySet().iterator();
			if (it.hasNext()) {
				Entry<String, PixelMap> entry = it.next();
				mostLongUsedValue = entry.getValue();
				it.remove();
			}
		}
		return mostLongUsedValue;
	}

	@Override
	protected Reference<PixelMap> createReference(PixelMap value) {
		return new WeakReference<PixelMap>(value);
	}
}
