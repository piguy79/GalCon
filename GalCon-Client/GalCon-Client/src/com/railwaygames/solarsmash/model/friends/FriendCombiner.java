package com.railwaygames.solarsmash.model.friends;

import java.util.ArrayList;
import java.util.List;

import com.railwaygames.solarsmash.model.Friend;
import com.railwaygames.solarsmash.model.MinifiedGame.MinifiedPlayer;

public class FriendCombiner {
	
	public static List<CombinedFriend> combineFriends(List<Friend> socialFriends, List<MinifiedPlayer> players, String authProvider){
		List<CombinedFriend> combinedFriends = new ArrayList<CombinedFriend>();
		List<String> playersAlreadyMatchingASocialFriend = new ArrayList<String>();
				
		for(Friend friend : socialFriends){
			boolean matchForFriend = false;
			for(MinifiedPlayer player : players){
				if(samePerson(friend, player, authProvider)){
					GalconSocialUser galConFriend = new GalconSocialUser(player.auth.getID(authProvider), friend.image, player.handle, player.xp, friend.name);
					combinedFriends.add(galConFriend);
					matchForFriend = true;
					playersAlreadyMatchingASocialFriend.add(player.auth.getID(authProvider));
				}
			}
			
			if(!matchForFriend){
				SocialOnlyFriend socilaOnlyFriend = new SocialOnlyFriend(friend.id, friend.image, friend.name);
				combinedFriends.add(socilaOnlyFriend);
			}
		}
		
		for(MinifiedPlayer player : players){
			if(!playersAlreadyMatchingASocialFriend.contains(player.auth.getID(authProvider))){
				GalConFriend galconFriend = new GalConFriend(player.auth.getID(authProvider), "", player.handle, player.xp);
				combinedFriends.add(galconFriend);
			}
		}
		
		return combinedFriends;
	}
	
	public static List<CombinedFriend> combineFriends(List<MinifiedPlayer> players){
		List<CombinedFriend> combinedFriends = new ArrayList<CombinedFriend>();
				
		
		for(MinifiedPlayer player : players){
			GalConFriend galconFriend = new GalConFriend(player.auth.getID(player.auth.defaultAuth), "", player.handle, player.xp);
			combinedFriends.add(galconFriend);
		}
		
		return combinedFriends;
	}
	
	private static boolean samePerson(Friend friend, MinifiedPlayer player, String authProvider){
		return friend.id.equals(player.auth.getID(authProvider));
	}

}
