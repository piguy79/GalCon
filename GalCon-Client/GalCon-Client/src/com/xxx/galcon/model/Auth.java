package com.xxx.galcon.model;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.xxx.galcon.Constants;
import com.xxx.galcon.model.base.JsonConvertible;


public class Auth extends JsonConvertible {
	
	public Map<String, String> auth;

	@Override
	protected void doConsume(JSONObject jsonObject) throws JSONException {
		auth = new HashMap<String, String>();
		putSocialProvider(jsonObject, Constants.Auth.SOCIAL_AUTH_PROVIDER_GOOGLE);
		putSocialProvider(jsonObject, Constants.Auth.SOCIAL_AUTH_PROVIDER_FACEBOOK);
		
	}

	private void putSocialProvider(JSONObject jsonObject, String authProvider) throws JSONException {
		if(jsonObject.has(authProvider)){
			auth.put(authProvider, jsonObject.getString(authProvider));
		}
	}
	
	
	public String getID(String authProvider){
		return auth.get(authProvider);
	}

}