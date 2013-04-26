package com.xxx.galcon;

import java.util.HashMap;
import java.util.Map;

public class Config {
	public static final String HOST = "host";
	public static final String PORT = "port";

	private static final Map<String, String> DEV_VALUES = new HashMap<String, String>() {
		{
			put(HOST, "stormy-sands-7424.herokuapp.com");
			put(PORT, "80");
		}
	};

	private static final Map<String, String> PROD_VALUES = new HashMap<String, String>() {
		{
			put(HOST, "damp-crag-7750.herokuapp.com");
			put(PORT, "80");
		}
	};

	public static String getValue(String key) {
		if (BuildConfig.DEBUG) {
			return DEV_VALUES.get(key);
		}

		return PROD_VALUES.get(key);
	}
}
