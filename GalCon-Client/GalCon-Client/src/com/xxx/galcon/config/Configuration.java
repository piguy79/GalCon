package com.xxx.galcon.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.xxx.galcon.Constants;
import com.xxx.galcon.model.Player;
import com.xxx.galcon.model.Rank;
import com.xxx.galcon.model.base.JsonConvertible;

public class Configuration extends JsonConvertible {

	public Long version;
	public String type;
	public Map<String, String> configValues;
	public List<Rank> ranks;

	// TODO ADD a defaults map for when no config values exist on load

	@Override
	protected void doConsume(JSONObject jsonObject) throws JSONException {
		this.configValues = new HashMap<String, String>();
		
		JSONObject configJsonObject = jsonObject.getJSONObject(Constants.CONFIG);

		this.version = configJsonObject.getLong(Constants.VERSION);
		this.type = configJsonObject.getString(Constants.TYPE);

		JSONObject configValues = configJsonObject.getJSONObject(Constants.VALUES);

		for (Iterator<String> i = configValues.keys(); i.hasNext();) {
			ConfigValue conf = extractConfig((String) i.next(), configValues);
			this.configValues.put(conf.name, conf.value);
		}
		
		ranks = new ArrayList<Rank>();
		JSONArray ranksJson = jsonObject.getJSONArray(Constants.RANKS);
		for (int i = 0; i < ranksJson.length(); i++) {
			Rank rank = new Rank();
			rank.consume(ranksJson.getJSONObject(i));
			this.ranks.add(rank);
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
	
	protected Rank getRankForXp(Integer xp){
		for(Rank rank : ranks){
			if(rank.startFrom <= xp && rank.endAt > xp){
				return rank;
			}
		}
		return null;
	}

	protected String getConfigValue(String configKey) {
		return this.configValues.get(configKey);
	}

	class ConfigValue {
		public String name;
		public String value;

		public ConfigValue(String name, String value) {
			super();
			this.name = name;
			this.value = value;
		}

	}

}
