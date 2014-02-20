package com.xxx.galcon;

public class AndroidConfig extends Config {

	@Override
	public String getValue(String key) {
		if (BuildConfig.DEBUG) {
			return DEV_VALUES.get(key);
		}

		return PROD_VALUES.get(key);
	}
}
