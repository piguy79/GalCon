package com.railwaygames.solarsmash.model.display;

import java.util.HashMap;
import java.util.Map;

public class AbilityDisplay {
	
	public static Map<String, String> abilityDisplayNames = new HashMap<String, String>(){
		{
			put("attackModifier", "Attack");
			put("speedModifier","Speed");
			put("defenseModifier","Defence");
		}
	};

}
