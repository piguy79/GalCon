/**
 * 
 */
package com.xxx.galcon.model.base;

import org.json.JSONObject;

/**
 * This interface defines a set of methods which make a class convertible from a JSONObject.
 * 
 * @author conormullen
 *
 */
public interface JsonConvertible {
	
	/**
	 * This method is used to add attributs to a class from a JsonObject.
	 * 
	 * @param jsonObject
	 */
	void consume(JSONObject jsonObject);

}
