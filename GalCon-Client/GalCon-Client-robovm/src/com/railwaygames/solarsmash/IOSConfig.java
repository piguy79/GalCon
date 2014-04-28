package com.railwaygames.solarsmash;

public class IOSConfig extends Config {

	@Override
	public String getValue(String key) {
		return IOS_DEV_VALUES.get(key);
//		return LOCALHOST_VALUES.get(key);
	}

}
