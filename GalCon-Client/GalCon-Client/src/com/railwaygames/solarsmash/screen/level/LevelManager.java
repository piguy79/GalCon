package com.railwaygames.solarsmash.screen.level;

import static com.railwaygames.solarsmash.Constants.GALCON_PREFS;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.config.ConfigResolver;
import com.railwaygames.solarsmash.model.Player;
import com.railwaygames.solarsmash.model.Rank;

public class LevelManager {

	public static void storeLevel(Player player) {
		Preferences prefs = Gdx.app.getPreferences(GALCON_PREFS);
		prefs.putString(Constants.CURRENT_RANK, ConfigResolver.getRankForXp(player.xp).level.toString());
		prefs.flush();
	}

	public static boolean shouldShowLevelUp(Player player) {
		Preferences prefs = Gdx.app.getPreferences(GALCON_PREFS);
		String currentRank = prefs.getString(Constants.CURRENT_RANK);

		if (currentRank == null || currentRank.isEmpty()) {
			storeLevel(player);
			return false;
		}

		Rank rank = ConfigResolver.getRankForXp(player.xp);
		if (rank == null) {
			return false;
		}

		return rank.level > Long.parseLong(currentRank);
	}

}
