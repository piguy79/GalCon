package com.railwaygames.solarsmash.social;

import java.util.ArrayList;
import java.util.List;

import org.robovm.apple.foundation.NSArray;
import org.robovm.apple.foundation.NSError;
import org.robovm.apple.foundation.NSObject;
import org.robovm.apple.foundation.NSString;
import org.robovm.apple.foundation.NSURL;
import org.robovm.bindings.gpp.GPPShare;
import org.robovm.bindings.gpp.GPPShareDelegate;
import org.robovm.bindings.gpp.GPPSignInDelegate;
import org.robovm.bindings.gt.GTMOAuth2Authentication;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.GameLoop;
import com.railwaygames.solarsmash.http.AuthenticationListener;
import com.railwaygames.solarsmash.http.FriendPostListener;
import com.railwaygames.solarsmash.http.FriendsListener;
import com.railwaygames.solarsmash.model.Friend;
import com.railwaygames.solarsmash.social.google.GPPSignIn;
import com.railwaygames.solarsmash.social.google.GTLPlusPeopleFeed;
import com.railwaygames.solarsmash.social.google.GTLPlusPerson;
import com.railwaygames.solarsmash.social.google.GTLQueryPlus;
import com.railwaygames.solarsmash.social.google.GTLServicePlus;
import com.railwaygames.solarsmash.social.google.GTLServiceTicket;

public class GooglePlusAuthorization implements Authorizer {
	private GPPSignIn gppSignIn;
	private String clientId = "1066768766862-7jltmge7ot8nje89hd47qu45jeir4dij.apps.googleusercontent.com";
	private NSString scopes = new NSString("https://www.googleapis.com/auth/plus.login");

	private AuthenticationListener listener;

	private GPPShareDelegate delegate;

	public class SignInDelegate extends GPPSignInDelegate.Adapter {
		@Override
		public void finishedWithAuth(GTMOAuth2Authentication auth, NSError error) {
			if (error != null) {
				listener.onSignInFailed(error.description());
			} else {
				GameLoop.USER.addAuthProvider(Constants.Auth.SOCIAL_AUTH_PROVIDER_GOOGLE, gppSignIn.getUserId()
						.toString());

				Preferences prefs = Gdx.app.getPreferences(Constants.GALCON_PREFS);
				prefs.putString(Constants.Auth.SOCIAL_AUTH_PROVIDER_GOOGLE + Constants.ID,
						GameLoop.USER.auth.getID(Constants.Auth.SOCIAL_AUTH_PROVIDER_GOOGLE));
				prefs.flush();

				// hacky, but parse the access token out as it's not exposed
				String t = auth.description();
				int first = t.indexOf("\"");
				String token = t.substring(first + 1, t.indexOf("\"", first + 1));

				listener.onSignInSucceeded(Constants.Auth.SOCIAL_AUTH_PROVIDER_GOOGLE, token);
			}
		}
	}

	public GooglePlusAuthorization() {
		gppSignIn = GPPSignIn.sharedInstance();
		gppSignIn.setShouldFetchGooglePlusUser(true);
		gppSignIn.setShouldFetchGoogleUserID(true);
		gppSignIn.setClientID(clientId);
		gppSignIn.setScopes(new NSArray<NSString>(scopes));
		gppSignIn.setDelegate(new SignInDelegate());
	}

	@Override
	public void signIn(AuthenticationListener listener) {
		this.listener = listener;
		gppSignIn.authenticate();
	}

	@Override
	public void getToken(AuthenticationListener listener) {
		this.listener = listener;
		gppSignIn.authenticate();
	}

	@Override
	public void onActivityResult(int responseCode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void getFriends(final FriendsListener listener) {
		GTLServicePlus plusService = gppSignIn.plusService();

		GTLQueryPlus query = GTLQueryPlus.queryForPeopleListWithUserId("me", "visible");
		query.setMaxResults(100);

		plusService.executeQuery(query, QueryResultsBlock.Marshaler.toObjCBlock(new QueryResultsBlock() {
			public void invoke(GTLServiceTicket ticket, GTLPlusPeopleFeed peopleFeed, NSError error) {
				Gdx.app.log("PEOPLE", "In invoke()");
				if (error != null) {
					Gdx.app.log("PEOPLE_ERROR", error.localizedDescription());
					listener.onFriendsLoadedFail(error.localizedDescription());
					return;
				}

				List<Friend> friends = new ArrayList<Friend>();
				if (peopleFeed != null && peopleFeed.getItems() != null) {
					for (NSObject oPerson : peopleFeed.getItems()) {
						GTLPlusPerson person = (GTLPlusPerson) oPerson;
						friends.add(new Friend(person.getIdentifier(), person.getDisplayName(), ""));
					}
				}

				listener.onFriendsLoadedSuccess(friends, Constants.Auth.SOCIAL_AUTH_PROVIDER_GOOGLE);
			}
		}));
	}

	@Override
	public void postToFriend(final FriendPostListener listener, String id) {
		GPPShare share = GPPShare.sharedInstance();

		delegate = new GPPShareDelegate.Adapter() {

			@Override
			public void finishedSharingWithError(NSError error) {
				if (error != null) {
					Gdx.app.log("GOOGLE", "Finished sharing with error: " + error.localizedDescription());
					listener.onPostFails(error.localizedDescription());
				} else {
					Gdx.app.log("GOOGLE", "Finished sharing");
					listener.onPostSucceeded();
				}
			}
		};
		share.setDelegate(delegate);

		List<NSString> ids = new ArrayList<NSString>();
		ids.add(new NSString(id));
		share.getNativeShareDialog()
				.setPreselectedPeopleIDs(new NSArray<NSString>(ids))
				.setPrefillText(
						new NSString("Hey, come play me in Solar Smash. Invite me using the handle \""
								+ GameLoop.USER.handle + "\". Download from http://www.railwaygames.mobi/ "))
				.setURLToShare(new NSURL("http://www.railwaygames.mobi/")).open();

	}
}
