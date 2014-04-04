package com.xxx.galcon.config;

import java.util.HashMap;
import java.util.Map;

import com.xxx.galcon.GameLoop;
import com.xxx.galcon.model.Rank;

public class ConfigResolver {
	
	private static final Map<String, String> defaultConfigs = new HashMap<String, String>(){
		{
			put(ConfigConstants.TIME_LAPSE_FOR_NEW_COINS,"1200000");
		}
	};
	
	public static String getByConfigKey(String configKey){
		String value = GameLoop.CONFIG.getConfigValue(configKey);
		if(value == null){
			value = defaultConfigs.get(configKey);
		}
		
		return value;
	}
	
	public static Rank getRankForXp(Integer xp){
		return GameLoop.CONFIG.getRankForXp(xp);
	}

}
