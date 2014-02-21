package com.xxx.galcon.model.friends;

import java.util.ArrayList;
import java.util.List;

import com.xxx.galcon.model.Friend;
import com.xxx.galcon.model.MinifiedGame.MinifiedPlayer;

public class FriendCombiner {
	
	public static List<CombinedFriend> combineFriends(List<Friend> socialFriends, List<MinifiedPlayer> players){
		List<CombinedFriend> combinedFriends = new ArrayList<CombinedFriend>();
		List<String> playersAlreadyMatchingASocialFriend = new ArrayList<String>();
				
		for(Friend friend : socialFriends){
			boolean matchForFriend = false;
			for(MinifiedPlayer player : players){
				if(samePerson(friend, player)){
					GalconSocialUser galConFriend = new GalconSocialUser(player.authId, friend.image, player.handle, player.rank, friend.name);
					combinedFriends.add(galConFriend);
					matchForFriend = true;
					playersAlreadyMatchingASocialFriend.add(player.authId);
				}
			}
			
			if(!matchForFriend){
				SocialOnlyFriend socilaOnlyFriend = new SocialOnlyFriend(friend.id, friend.image, friend.name);
				combinedFriends.add(socilaOnlyFriend);
			}
		}
		
		for(MinifiedPlayer player : players){
			if(!playersAlreadyMatchingASocialFriend.contains(player.authId)){
				GalConFriend galconFriend = new GalConFriend(player.authId, "", player.handle, player.rank);
				combinedFriends.add(galconFriend);
			}
		}
		
		return combinedFriends;
	}
	
	private static boolean samePerson(Friend friend, MinifiedPlayer player){
		return friend.id.equals(player.authId);
	}

}
