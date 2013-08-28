package com.xxx.galcon.http;

import com.xxx.galcon.model.Configuration;

public class SetConfigurationResultHandler implements UIConnectionResultCallback<Configuration> {
	
	private Configuration config;

	public SetConfigurationResultHandler(Configuration config) {
		super();
		this.config = config;
	}

	@Override
	public void onConnectionResult(Configuration result) {
		this.config.version = result.version;
		this.config.type = result.type;
		this.config.configValues = result.configValues;
	}

	@Override
	public void onConnectionError(String msg) {		
	}

}
