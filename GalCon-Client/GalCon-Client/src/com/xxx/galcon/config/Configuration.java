package com.xxx.galcon.config;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.xxx.galcon.Constants;
import com.xxx.galcon.model.base.JsonConvertible;

public class Configuration extends JsonConvertible {
	
	public Long version;
	public String type;
	public Map<String, String> configValues;
	
	
	@Override
	public void consume(JSONObject jsonObject) throws JSONException {
		this.configValues = new HashMap<String, String>();
		
		this.version = jsonObject.getLong(Constants.VERSION);
		this.type = jsonObject.getString(Constants.TYPE);
		
		JSONObject configValues = jsonObject.getJSONObject(Constants.VALUES);
		
		
		for(Iterator<String> i = configValues.keys(); i.hasNext();){
			ConfigValue conf = extractConfig((String)i.next(), configValues);
			this.configValues.put(conf.name, conf.value);
		}
	}
	
	
	private ConfigValue extractConfig(String key, JSONObject values) {
		
		ConfigValue conf = null;
		try {
			conf = new ConfigValue(key, values.getString(key));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return conf;
	}
	
	protected String getConfigValue(String configKey){
		return this.configValues.get(configKey);
	}


	class ConfigValue{
		public String name;
		public String value;
		
		public ConfigValue(String name, String value) {
			super();
			this.name = name;
			this.value = value;
		}
		
		
	}
	
}


