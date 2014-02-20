package com.xxx.galcon;

import java.util.HashMap;
import java.util.Map;

public abstract class Config {
	public static final String HOST = "host";
	public static final String PORT = "port";
	public static final String PROTOCOL = "protocol";

	protected final Map<String, String> LOCALHOST_VALUES = new HashMap<String, String>() {
		{
			put(HOST, "localhost");
			put(PORT, "3000");
			put(PROTOCOL, "http");
		}
	};

	protected final Map<String, String> DEV_VALUES = new HashMap<String, String>() {
		{
			put(HOST, "stormy-sands-7424.herokuapp.com");
			put(PORT, "443");
			put(PROTOCOL, "https");
		}
	};

	protected final Map<String, String> PROD_VALUES = new HashMap<String, String>() {
		{
			put(HOST, "damp-crag-7750.herokuapp.com");
			put(PORT, "443");
			put(PROTOCOL, "https");
		}
	};

	public abstract String getValue(String key);
}
