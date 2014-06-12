package com.railwaygames.solarsmash.http;

public class GameActionCache<T> implements UIConnectionResultCallback<T> {
	private UIConnectionResultCallback<T> delegate;
	private T cache;
	private final long expireTime;

	public GameActionCache(long expireInMilliseconds) {
		expireTime = System.currentTimeMillis() + expireInMilliseconds;
	}

	public void setDelegate(UIConnectionResultCallback<T> delegate) {
		this.delegate = delegate;
	}

	@Override
	public void onConnectionResult(T result) {
		cache = result;
		delegate.onConnectionResult(result);
		this.delegate = null;
	}

	@Override
	public void onConnectionError(String msg) {
		delegate.onConnectionError(msg);
		this.delegate = null;
	}

	public T getCache() {
		if (System.currentTimeMillis() > expireTime) {
			cache = null;
		}
		return cache;
	}
}