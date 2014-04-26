/**
 * 
 */
package com.railwaygames.solarsmash.model.base;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
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
	public boolean valid = true;
	public String reason;

	public boolean sessionExpired = false;

	/**
	 * This method is used to add attributs to a class from a JsonObject.
	 * 
	 * @param jsonObject
	 * @throws JSONException
	 */
	public void consume(JSONObject jsonObject) throws JSONException {
		if (jsonObject != null) {
			String session = jsonObject.optString("session", "");
			errorMessage = jsonObject.optString("error");
			if (session.equals("expired")) {
				sessionExpired = true;
			} else {
				if (jsonObject.has("reason")) {
					valid = false;
				}
				reason = jsonObject.optString("reason");
				doConsume(jsonObject);
			}

		}
	}

	abstract protected void doConsume(JSONObject jsonObject) throws JSONException;

	protected DateTime formatDate(JSONObject jsonObject, String field) {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

		String sDate = jsonObject.optString(field);
		if (sDate != null && sDate.length() > 0) {
			DateTime date = formatter.parseDateTime(sDate);
			return date;
		}

		return null;
	}
}
