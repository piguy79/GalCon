package com.railwaygames.solarsmash.social;

import java.util.ArrayList;
import java.util.List;

import org.robovm.apple.foundation.Foundation;
import org.robovm.apple.foundation.NSObject;
import org.robovm.apple.foundation.NSURL;
import org.robovm.apple.uikit.UIApplication;
import org.robovm.apple.uikit.UIApplicationDelegateAdapter;
import org.robovm.bindings.facebook.manager.FBPermission;
import org.robovm.bindings.facebook.manager.FacebookLoginListener;
import org.robovm.bindings.facebook.manager.FacebookRequestListener;
import org.robovm.bindings.facebook.manager.GraphObject;
import org.robovm.bindings.facebook.session.FBSession;
import org.robovm.bindings.facebook.session.FBSessionDefaultAudience;
import org.robovm.bindings.facebook.session.FBSessionLoginBehavior;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.GameLoop;
import com.railwaygames.solarsmash.http.AuthenticationListener;
import com.railwaygames.solarsmash.http.FriendPostListener;
import com.railwaygames.solarsmash.http.FriendsListener;
import com.railwaygames.solarsmash.model.Friend;
import com.railwaygames.solarsmash.social.facebook.CommonFacebookRequests;
import com.railwaygames.solarsmash.social.facebook.FacebookConfiguration;
import com.railwaygames.solarsmash.social.facebook.FacebookManager;

public class FacebookAuthorization extends UIApplicationDelegateAdapter implements Authorizer {

	private static final String APP_ID = "257147704459293";
	private static final String APP_NAMESPACE = "com.railwaygames.solarsmash";
	static final String TAG = "[FacebookAuth] ";

	private FacebookManager facebook;

	@Override
	public void signIn(final AuthenticationListener listener) {
		setupManager();
		if (!facebook.isLoggedIn() || !FBSession.getActiveSession().isOpen()) {
			Foundation.log("FB: LOGGING IN TO FB");
			facebook.login(new FacebookLoginListener() {

				@Override
				public void onSuccess(GraphObject user) {
					Foundation.log("FB_USER_LOADED: " + user.getString("id"));
					GameLoop.USER.addAuthProvider(Constants.Auth.SOCIAL_AUTH_PROVIDER_FACEBOOK, user.getString("id")
							.toString());

					Preferences prefs = Gdx.app.getPreferences(Constants.GALCON_PREFS);
					prefs.putString(Constants.Auth.SOCIAL_AUTH_PROVIDER_FACEBOOK + Constants.ID,
							GameLoop.USER.auth.getID(Constants.Auth.SOCIAL_AUTH_PROVIDER_FACEBOOK));
					prefs.flush();
					listener.onSignInSucceeded(Constants.Auth.SOCIAL_AUTH_PROVIDER_FACEBOOK, FBSession
							.getActiveSession().getAccessTokenData().getAccessToken());
				}

				@Override
				public void onError(String error) {
					Foundation.log("FB_LOGIN: Failed: " + error);
					listener.onSignInFailed("Unable to sign in");
				}

				@Override
				public void onCancel() {
					Foundation.log("FB: Login cancelled.");
					listener.onSignInFailed("");
				}
			});
		} else {
			listener.onSignInSucceeded(Constants.Auth.SOCIAL_AUTH_PROVIDER_FACEBOOK, FBSession.getActiveSession()
					.getAccessTokenData().getAccessToken());
		}

	}

	@Override
	public void onActivityResult(int responseCode) {
	}

	@Override
	public void getToken(final AuthenticationListener listener) {
		signIn(listener);
	}

	@Override
	public void getFriends(final FriendsListener listener) {
		signIn(new AuthenticationListener() {

			@Override
			public void onSignOut() {
			}

			@Override
			public void onSignInSucceeded(String authProvider, String token) {
				facebook.request(CommonFacebookRequests.getFriends(new FacebookRequestListener() {

					@Override
					public void onSuccess(GraphObject result) {
						List<Friend> friends = new ArrayList<Friend>();
						for (GraphObject graphObject : result.getChildren()) {
							Friend friend = new Friend(graphObject.getString("id"), graphObject.getString("name"), "");
							friends.add(friend);
						}

						listener.onFriendsLoadedSuccess(friends, Constants.Auth.SOCIAL_AUTH_PROVIDER_FACEBOOK);
					}

					@Override
					public void onError(String error) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onCancel() {
						// TODO Auto-generated method stub

					}
				}));

			}

			@Override
			public void onSignInFailed(String failureMessage) {
				listener.onFriendsLoadedFail("Unable to load friends");
			}
		});
	}

	@Override
	public void postToFriend(final FriendPostListener listener, final String id) {
		signIn(new AuthenticationListener() {

			@Override
			public void onSignOut() {
			}

			@Override
			public void onSignInSucceeded(String authProvider, String token) {
				Foundation.log(TAG + "Session state: " + FBSession.getActiveSession().getState());
				facebook.request(CommonFacebookRequests
						.publishFeedToFriend(id, "Solar Smash", "Download Solar Smash now.", "",
								"Hey, come play me in Solar Smash. Invite me using the handle \""
										+ GameLoop.USER.handle + "\"",
								"http://www.railwaygames.mobi/assets/images/logo/android_icon.png",
								"http://www.railwaygames.mobi", FBSession.getActiveSession().getAccessTokenData()
										.getAccessToken(), true, new FacebookRequestListener() {

									@Override
									public void onSuccess(GraphObject result) {
										Foundation.log(TAG + "Result: " + result);
										listener.onPostSucceeded();

									}

									@Override
									public void onError(String error) {
										Foundation.log(TAG + "Error occured: " + error);
										listener.onPostFails("Unable to post to FB");
									}

									@Override
									public void onCancel() {
										listener.onPostCancelled();
									}
								}));
			}

			@Override
			public void onSignInFailed(String failureMessage) {
				listener.onPostFails("Unable to post to friend.");
			}
		});

	}

	private void setupManager() {
		if (facebook == null) {
			facebook = FacebookManager.getInstance();

			FBPermission[] fbPermissions = new FBPermission[] { FBPermission.PUBLIC_PROFILE, FBPermission.USER_FRIENDS };

			FacebookConfiguration fbConfiguration = new FacebookConfiguration.Builder().setAppId(APP_ID)
					.setNamespace(APP_NAMESPACE).setPermissions(fbPermissions)
					.setDefaultAudience(FBSessionDefaultAudience.Everyone)
					.setLoginBehavior(FBSessionLoginBehavior.UseSystemAccountIfPresent).build();
			facebook.setConfiguration(fbConfiguration);
		}
	}

	@Override
	public void didBecomeActive(UIApplication application) {
		facebook.didBecomeActive(application);
	}

	@Override
	public boolean openURL(UIApplication application, NSURL url, String sourceApplication, NSObject annotation) {
		return facebook.openURL(application, url, sourceApplication, annotation);
	}

	@Override
	public void willTerminate(UIApplication application) {
		facebook.willTerminate(application);
	}

}
