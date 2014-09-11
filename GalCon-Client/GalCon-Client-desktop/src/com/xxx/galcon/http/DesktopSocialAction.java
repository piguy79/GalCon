package com.xxx.galcon.http;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.GameLoop;
import com.railwaygames.solarsmash.http.AuthenticationListener;
import com.railwaygames.solarsmash.http.FriendPostListener;
import com.railwaygames.solarsmash.http.FriendsListener;
import com.railwaygames.solarsmash.http.SocialAction;
import com.railwaygames.solarsmash.model.Friend;

public class DesktopSocialAction implements SocialAction {

	private AuthenticationListener listener;

	@Override
	public void signIn(AuthenticationListener signInListener, final String provider) {
		this.listener = signInListener;
		(new Thread() {
			@Override
			public void run() {
				Random rand = new Random();
				try {
					Thread.sleep(Math.abs((rand.nextInt() % 2 + 1) * 1000));
				} catch (InterruptedException e) {

				}

				Gdx.app.postRunnable(new Runnable() {
					public void run() {
						int rand = (int) (Math.random() * 10000);
						String id = "me" + rand + ":google";
						GameLoop.getUser().addAuthProvider(provider, id);

						Preferences prefs = Gdx.app.getPreferences(Constants.GALCON_PREFS);
						prefs.putString(provider + Constants.ID, GameLoop.getUser().auth.getID(provider));
						prefs.flush();

						listener.onSignInSucceeded(provider, "FAKE_TOKEN");
					}
				});
			}
		}).start();
	}

	@Override
	public void onActivityResult(int responseCode) {
		listener.onSignInSucceeded("google", "");
	}

	@Override
	public void getToken(AuthenticationListener listener) {
		listener.onSignInSucceeded(Constants.Auth.SOCIAL_AUTH_PROVIDER_GOOGLE, "FAKE_TOKEN");
	}

	@Override
	public void getFriends(FriendsListener listener, String authProvider) {
		List<Friend> friends = new ArrayList<Friend>();
		Friend friend = new Friend("115260459815431014313", "Pal", "url");
		friends.add(friend);
		friend = new Friend("104440888501408192907", "Pal", "url");
		friends.add(friend);
		friend = new Friend("117137272197278398355", "Pal", "url");
		friends.add(friend);
		friend = new Friend("104188068991971481622", "Pal", "url");
		friends.add(friend);
		friend = new Friend("113372054470739143966", "Pal", "url");
		friends.add(friend);
		friend = new Friend("103272367747992992405", "Pal", "url");
		friends.add(friend);
		friend = new Friend("102611137112953475527", "Pal", "url");
		friends.add(friend);
		friend = new Friend("110314681094201302715", "Pal", "url");
		friends.add(friend);
		friend = new Friend("115959516283320282628", "Pal", "url");
		friends.add(friend);
		friend = new Friend("106662151428691330072", "Pal", "url");
		friends.add(friend);
		friend = new Friend("105039084635658744663", "Pal", "url");
		friends.add(friend);
		friend = new Friend("109286347620310178541", "Pal", "url");
		friends.add(friend);
		friend = new Friend("113728201580287827922", "Pal", "url");
		friends.add(friend);
		friend = new Friend("107893693724421468455", "Pal", "url");
		friends.add(friend);
		friend = new Friend("108251213289246211247", "Pal", "url");
		friends.add(friend);
		friend = new Friend("111626127367496192147", "Pal", "url");
		friends.add(friend);
		friend = new Friend("116681200248484300941", "Pal", "url");
		friends.add(friend);
		friend = new Friend("106101947971011049416", "Pal", "url");
		friends.add(friend);
		friend = new Friend("116894386973503829465", "Pal", "url");
		friends.add(friend);
		friend = new Friend("104198860246882311312", "Pal", "url");
		friends.add(friend);
		friend = new Friend("104516729913236384126", "Pal", "url");
		friends.add(friend);
		friend = new Friend("112267848864578808144", "Pal", "url");
		friends.add(friend);
		friend = new Friend("115309339820859194432", "Pal", "url");
		friends.add(friend);
		friend = new Friend("107411422944314880504", "Pal", "url");
		friends.add(friend);
		friend = new Friend("115753340018567471435", "Pal", "url");
		friends.add(friend);
		friend = new Friend("109615986770522229394", "Pal", "url");
		friends.add(friend);
		friend = new Friend("107489039742116100814", "Pal", "url");
		friends.add(friend);
		friend = new Friend("101514892671688736089", "Pal", "url");
		friends.add(friend);
		friend = new Friend("117932778149239258065", "Pal", "url");
		friends.add(friend);

		listener.onFriendsLoadedSuccess(friends, authProvider);
	}

	@Override
	public void postToFriends(FriendPostListener listener, String authProvider, String id) {
		listener.onPostSucceeded();
	}

	@Override
	public boolean enableGoogle() {
		return true;
	}

}
