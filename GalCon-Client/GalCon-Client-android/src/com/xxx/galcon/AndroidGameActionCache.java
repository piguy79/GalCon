package com.xxx.galcon;

import com.xxx.galcon.http.UIConnectionResultCallback;
import com.xxx.galcon.model.Inventory;
import com.xxx.galcon.model.Maps;

public class AndroidGameActionCache {
	public static class MapsCache implements UIConnectionResultCallback<Maps> {
		private UIConnectionResultCallback<Maps> delegate;
		private Maps cache;

		public void setDelegate(UIConnectionResultCallback<Maps> delegate) {
			this.delegate = delegate;
		}

		@Override
		public void onConnectionResult(Maps result) {
			cache = result;
			delegate.onConnectionResult(result);
			this.delegate = null;
		}

		@Override
		public void onConnectionError(String msg) {
			delegate.onConnectionError(msg);
			this.delegate = null;
		}

		public Maps getCache() {
			return cache;
		}
	};

	public static class InventoryCache implements UIConnectionResultCallback<Inventory> {
		private UIConnectionResultCallback<Inventory> delegate;
		private Inventory cache;

		public void setDelegate(UIConnectionResultCallback<Inventory> delegate) {
			this.delegate = delegate;
		}

		@Override
		public void onConnectionResult(Inventory result) {
			cache = result;
			delegate.onConnectionResult(result);
			this.delegate = null;
		}

		@Override
		public void onConnectionError(String msg) {
			delegate.onConnectionError(msg);
			this.delegate = null;
		}

		public Inventory getCache() {
			return cache;
		}
	};
}
