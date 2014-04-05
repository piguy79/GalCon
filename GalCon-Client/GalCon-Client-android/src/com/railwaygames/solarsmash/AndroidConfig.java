package com.railwaygames.solarsmash;

import com.railwaygames.solarsmash.Config;

public class AndroidConfig extends Config {

	@Override
	public String getValue(String key) {
		if (BuildConfig.DEBUG) {
			return DEV_VALUES.get(key);
		}

		return PROD_VALUES.get(key);
	}
}
