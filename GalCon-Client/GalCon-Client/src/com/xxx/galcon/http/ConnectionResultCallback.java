package com.xxx.galcon.http;

/**
 * All network activity should happen in a background thread. When the request
 * is complete, the result will be sent back here.
 */
public interface ConnectionResultCallback<T> {

	public void result(T result);
}
