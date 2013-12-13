/**
 * 
 */
package com.xxx.galcon.model.base;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This interface defines a set of methods which make a class convertible from a
 * JSONObject.
 * 
 * @author conormullen
 * 
 */
public abstract class JsonConvertible {

	public String errorMessage;

	public boolean sessionExpired = false;
	public boolean sessionInvalid = false;

	/**
	 * This method is used to add attributs to a class from a JsonObject.
	 * 
	 * @param jsonObject
	 * @throws JSONException
	 */
	public void consume(JSONObject jsonObject) throws JSONException {
		if(jsonObject != null){
			String session = jsonObject.optString("session", "");
			if (session.equals("expired")) {
				sessionExpired = true;
			} else if (session.equals("invalid")) {
				sessionInvalid = true;
			} else {
				doConsume(jsonObject);
			}
		}
	}

	abstract protected void doConsume(JSONObject jsonObject) throws JSONException;

	protected Date formatDate(JSONObject jsonObject, String field) {
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.mmm'Z'");
		try {
			String date = jsonObject.optString(field);
			if (date != null && date.length() > 0) {
				return format.parse(date);
			}

		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
}
