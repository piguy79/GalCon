package com.railwaygames.solarsmash.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.railwaygames.solarsmash.GameLoop;
import com.railwaygames.solarsmash.model.base.JsonConvertible;

public class Leaderboards extends JsonConvertible {

	public java.util.Map<String, List<LeaderboardEntry>> leaderboards = new HashMap<String, List<LeaderboardEntry>>();

	@Override
	protected void doConsume(JSONObject jsonObject) throws JSONException {
		JSONArray boards = jsonObject.optJSONArray("boards");
		if (boards != null && boards.length() > 0) {
			for (int i = 0; i < boards.length(); ++i) {
				JSONArray entriesForBoard = boards.getJSONArray(i);
				if (entriesForBoard != null && entriesForBoard.length() > 0) {
					List<LeaderboardEntry> entries = new ArrayList<LeaderboardEntry>();
					for (int j = 0; j < entriesForBoard.length(); ++j) {
						JSONObject entry = entriesForBoard.getJSONObject(j);
						LeaderboardEntry lb = new LeaderboardEntry();
						lb.id = entry.getString("id");
						lb.score = (float) entry.getDouble("score");

						JSONObject user = entry.getJSONObject("user");
						lb.handle = user.getString("handle");
						lb.userId = user.getString("id");

						JSONObject record = entry.getJSONObject("record");
						lb.wins = record.getInt("w");
						lb.losses = record.getInt("l");

						lb.rank = entry.optInt("rank", -1);

						entries.add(lb);
					}
					Collections.sort(entries, new Comparator<LeaderboardEntry>() {
						@Override
						public int compare(LeaderboardEntry o1, LeaderboardEntry o2) {
							if (o1.score < o2.score) {
								return 1;
							}

							if (o2.score < o1.score) {
								return -1;
							}

							return 0;
						}
					});
					leaderboards.put(entries.get(0).id, entries);
				}
			}
		}
	}

	public static class LeaderboardEntry {
		public float score;
		public String handle;
		public String userId;
		public String id;
		public int wins;
		public int losses;
		public int rank;
	}

	public void merge(Leaderboards otherBoards) {
		if (otherBoards == null) {
			return;
		}

		for (java.util.Map.Entry<String, List<LeaderboardEntry>> otherEntriesSet : otherBoards.leaderboards.entrySet()) {
			String otherId = otherEntriesSet.getKey();

			List<LeaderboardEntry> mergedEntries = new ArrayList<LeaderboardEntry>();
			List<LeaderboardEntry> thisEntries = leaderboards.get(otherId);
			List<LeaderboardEntry> otherEntries = otherEntriesSet.getValue();

			if (thisEntries == null || thisEntries.isEmpty()) {
				mergedEntries = otherEntries;
			} else if (otherEntries.isEmpty()) {
				mergedEntries = thisEntries;
			} else {
				int iOther = 0;
				int iThis = 0;
				int count = -1;
				while (count++ < 11 && (iOther < otherEntries.size() || iThis < thisEntries.size())) {
					if (iOther == otherEntries.size()) {
						mergedEntries.add(thisEntries.get(iThis));
						iThis += 1;
						continue;
					} else if (iThis == thisEntries.size()) {
						mergedEntries.add(otherEntries.get(iOther));
						iOther += 1;
						continue;
					}

					LeaderboardEntry otherEntry = otherEntries.get(iOther);
					LeaderboardEntry thisEntry = thisEntries.get(iThis);

					// look for friend duplicates
					if (otherEntry.handle.equals(thisEntry.handle)) {
						mergedEntries.add(otherEntry);
						iOther += 1;
						iThis += 1;
						continue;
					}

					if (otherEntry.rank < thisEntry.rank) {
						mergedEntries.add(otherEntry);
						iOther += 1;
					} else {
						mergedEntries.add(thisEntry);
						iThis += 1;
					}
				}

				boolean foundMe = false;
				for (ListIterator<LeaderboardEntry> iter = mergedEntries.listIterator(); iter.hasNext();) {
					if (iter.next().handle.equals(GameLoop.getUser().handle)) {
						if (foundMe) {
							iter.remove();
						} else {
							foundMe = true;
						}
					}
				}

				if (!foundMe) {
					for (LeaderboardEntry entry : thisEntries) {
						if (entry.handle.equals(GameLoop.getUser().handle)) {
							mergedEntries.add(entry);
							break;
						}
					}
				}
			}

			leaderboards.put(otherId, mergedEntries);
		}
	}
}
