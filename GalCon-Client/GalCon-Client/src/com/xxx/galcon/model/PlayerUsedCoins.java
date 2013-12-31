package com.xxx.galcon.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.xxx.galcon.Constants;
import com.xxx.galcon.model.base.JsonConvertible;

public class PlayerUsedCoins extends JsonConvertible {

	public Long usedCoins;

	@Override
	protected void doConsume(JSONObject jsonObject) throws JSONException {
		usedCoins = jsonObject.optLong(Constants.USED_COINS);
	}

}
