package com.game.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import com.game.SysConfig;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.server.util.ServerLogger;

/**
 *
 * 缓存管理类。在google guava cache的基础上进行封装，提供了简单的增删改查的功能。
 * 在每个模块初始化时先初始化缓存实例。 使用guava
 * cache的好处是自动管理缓存内存，当超过缓存数量时，会基于LRU删除部分失效的cache
 * 
 * @author ken@iamcoding.com
 */
public class CacheManager {

	@SuppressWarnings("rawtypes")
	private static final Map<String, LoadingCache> caches = new ConcurrentHashMap<String, LoadingCache>();

	/**
	 * 初始化缓存
	 */
	public static <K, V> void initCache(Class<K> k, Class<V> v, int max, CacheLoader<K, V> loader) {
		LoadingCache<K, V> cache = CacheBuilder.newBuilder().maximumSize(max).build(loader);
		caches.put(v.getSimpleName(), cache);
	}

	/**
	 * 初始化缓存
	 */
	public static <K, V> void initCache(Class<K> k, Class<V> v, CacheLoader<K, V> loader) {
		initCache(k, v, SysConfig.cacheCount, loader);
	}

	/**
	 * 获取缓存
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> V get(K k, Class<V> v) {
		try {
			return (V) (caches.get(v.getSimpleName()).get(k));
		} catch (ExecutionException e) {
			ServerLogger.err(e, "Get cache err!");
			return null;
		}
	}

	/**
	 * 更新缓存
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> void put(K k, V v) {
		caches.get(v.getClass().getSimpleName()).put(k, v);
	}

	/**
	 * 移除缓存
	 */
	public static <K, V> void remove(K k, Class<V> v) {
		caches.get(v.getClass().getSimpleName()).invalidate(k);
	}
}
