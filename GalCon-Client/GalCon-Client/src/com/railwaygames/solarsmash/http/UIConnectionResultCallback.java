package com.railwaygames.solarsmash.http;

/**
 * All network activity should happen in a background thread. When the request
 * is complete, the result will be sent back here.
 */
public interface UIConnectionResultCallback<T> {

	public void onConnectionResult(T result);

	public void onConnectionError(String msg);
}
