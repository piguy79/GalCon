package com.xxx.galcon;

import com.xxx.galcon.http.UIConnectionResultCallback;
import com.xxx.galcon.model.Maps;

public class AndroidGameActionCache {
	public static class MapsCache implements UIConnectionResultCallback<Maps> {
		private UIConnectionResultCallback<Maps> delegate;
		private Maps cachedMaps;

		public void setDelegate(UIConnectionResultCallback<Maps> delegate) {
			this.delegate = delegate;
		}

		@Override
		public void onConnectionResult(Maps result) {
			cachedMaps = result;
			delegate.onConnectionResult(result);
			this.delegate = null;
		}

		@Override
		public void onConnectionError(String msg) {
			delegate.onConnectionError(msg);
			this.delegate = null;
		}

		public Maps getCachedMaps() {
			return cachedMaps;
		}

	};
}
