package com.railwaygames.solarsmash.social;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Request;
import com.facebook.Request.GraphUserListCallback;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.GameLoop;
import com.railwaygames.solarsmash.MainActivity;
import com.railwaygames.solarsmash.http.AuthenticationListener;
import com.railwaygames.solarsmash.http.FriendPostListener;
import com.railwaygames.solarsmash.http.FriendsListener;
import com.railwaygames.solarsmash.model.Friend;

public class FacebookAuthorization implements Authorizer {

	private Activity activity;
	private AuthenticationListener listener;
	private Session.StatusCallback statusCallback = new SessionStatusCallback();

	public FacebookAuthorization(Activity activity) {
		this.activity = activity;
	}

	@Override
	public void signIn(AuthenticationListener listener) {
		this.listener = listener;

		Session current = Session.getActiveSession();
		if (current != null && current.isOpened()) {
			populateAuthIdAndSucceed();
		} else {
			Session session = createSession();
			session.openForRead(createRequest().setCallback(statusCallback));
		}
	}

	private Session createSession() {
		Session activeSession = Session.getActiveSession();
		if (activeSession == null || activeSession.getState().isClosed()) {
			activeSession = new Session.Builder(activity).build();
			Session.setActiveSession(activeSession);
		}
		return activeSession;
	}

	private Session.OpenRequest createRequest() {
		Session.OpenRequest request = new Session.OpenRequest(activity);
		request.setRequestCode(MainActivity.FACEBOOK_SIGN_IN_ACTIVITY_RESULT_CODE);
		return request;
	}

	@Override
	public void onActivityResult(int responseCode) {
	}

	private void populateAuthIdAndSucceed() {
		if (GameLoop.getUser().auth != null
				&& GameLoop.getUser().auth.hasAuth(Constants.Auth.SOCIAL_AUTH_PROVIDER_FACEBOOK)) {
			listener.onSignInSucceeded(Constants.Auth.SOCIAL_AUTH_PROVIDER_FACEBOOK, Session.getActiveSession()
					.getAccessToken());
		} else {
			Request.newGraphPathRequest(Session.getActiveSession(), "/me", new Request.Callback() {

				@Override
				public void onCompleted(Response response) {
					if (response.getError() != null) {
						Gdx.app.postRunnable(new Runnable() {
							public void run() {
								listener.onSignInFailed("Unable to connect to FB.");
							}
						});
					} else {
						GraphObject user = response.getGraphObject();
						GameLoop.getUser().addAuthProvider(Constants.Auth.SOCIAL_AUTH_PROVIDER_FACEBOOK,
								user.getProperty("id").toString());

						Preferences prefs = Gdx.app.getPreferences(Constants.GALCON_PREFS);
						prefs.putString(Constants.Auth.SOCIAL_AUTH_PROVIDER_FACEBOOK + Constants.ID,
								GameLoop.getUser().auth.getID(Constants.Auth.SOCIAL_AUTH_PROVIDER_FACEBOOK));
						prefs.flush();

						Gdx.app.postRunnable(new Runnable() {
							public void run() {
								listener.onSignInSucceeded(Constants.Auth.SOCIAL_AUTH_PROVIDER_FACEBOOK, Session
										.getActiveSession().getAccessToken());
							}
						});
					}

				}
			}).executeAsync();
		}
	}

	@Override
	public void getToken(final AuthenticationListener listener) {
		this.listener = listener;

		Session session = Session.getActiveSession();
		if (session == null || session.isClosed()) {
			session = createSession();
			session.openForRead(createRequest().setCallback(statusCallback));
		} else if (session.isOpened()) {
			Gdx.app.postRunnable(new Runnable() {
				public void run() {
					listener.onSignInSucceeded(Constants.Auth.SOCIAL_AUTH_PROVIDER_FACEBOOK, Session.getActiveSession()
							.getAccessToken());
				}
			});
		}
	}

	public class SessionStatusCallback implements Session.StatusCallback {
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			if (SessionState.CLOSED_LOGIN_FAILED == state) {
				Gdx.app.postRunnable(new Runnable() {
					public void run() {
						listener.onSignInFailed("Unable to connect to Facebook.");
					}
				});
			} else if (SessionState.OPENED == state) {
				populateAuthIdAndSucceed();
			}
		}
	}

	@Override
	public void getFriends(final FriendsListener listener) {
		if (Session.getActiveSession() == null || !Session.getActiveSession().isOpened()) {
			signIn(new AuthenticationListener() {

				@Override
				public void onSignOut() {
					// TODO Auto-generated method stub

				}

				@Override
				public void onSignInSucceeded(String authProvider, String token) {
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							requestFriendInfo(listener, Session.getActiveSession());
						}
					});
				}

				@Override
				public void onSignInFailed(String failureMessage) {
					Gdx.app.postRunnable(new Runnable() {
						public void run() {
							listener.onFriendsLoadedFail("Unable to connect to FB.");
						}
					});
				}
			});
		} else {
			requestFriendInfo(listener, Session.getActiveSession());
		}
	}

	private void requestFriendInfo(final FriendsListener listener, Session session) {
		Bundle parameters = new Bundle();
		parameters.putString("fields", "id, name, picture");

		Request friendRequest = Request.newMyFriendsRequest(session, new GraphUserListCallback() {
			@Override
			public void onCompleted(List<GraphUser> users, Response response) {
				if (users == null) {
					Gdx.app.postRunnable(new Runnable() {
						public void run() {
							listener.onFriendsLoadedFail("Could not find users");
						}
					});
					return;
				}
				final List<Friend> friends = new ArrayList<Friend>();
				for (GraphUser user : users) {
					String imageUrl = "";
					JSONObject jsonObject = user.getInnerJSONObject();
					try {
						JSONObject picture = jsonObject.getJSONObject("picture").getJSONObject("data");
						imageUrl = picture.getString("url");
					} catch (JSONException e) {

					}
					Friend friend = new Friend(user.getId(), user.getName(), imageUrl);
					friends.add(friend);
				}
				Gdx.app.postRunnable(new Runnable() {
					public void run() {
						listener.onFriendsLoadedSuccess(friends, Constants.Auth.SOCIAL_AUTH_PROVIDER_FACEBOOK);
					}
				});
			}
		});
		friendRequest.setParameters(parameters);
		friendRequest.executeAsync();
	}

	@Override
	public void postToFriend(final FriendPostListener listener, final String id) {

		signIn(new AuthenticationListener() {

			@Override
			public void onSignOut() {
			}

			@Override
			public void onSignInSucceeded(String authProvider, String token) {
				showPostDialog(listener, id);
			}

			@Override
			public void onSignInFailed(String failureMessage) {
				Gdx.app.postRunnable(new Runnable() {
					public void run() {
						listener.onPostFails("Unable to connect to FB.");
					}
				});
			}
		});
	}

	private void showPostDialog(final FriendPostListener listener, String id) {
		Bundle params = new Bundle();
		params.putString("name", "Solar Smash invite");
		params.putString("caption", "Download Solar Smash now");
		params.putString("description",
				"Come conquer the galaxy in this addictive multiplayer strategy game. Invite me using the handle \""
						+ GameLoop.getUser().handle + "\"");
		params.putString("link", "http://www.railwaygames.mobi/");
		params.putString("to", id);
		params.putString("from", GameLoop.getUser().auth.getID(Constants.Auth.SOCIAL_AUTH_PROVIDER_FACEBOOK));
		params.putString("picture", "http://www.railwaygames.mobi/assets/images/logo/android_icon.png");

		WebDialog feedDialog = new WebDialog.FeedDialogBuilder(activity, Session.getActiveSession(), params)
				.setOnCompleteListener(new OnCompleteListener() {

					@Override
					public void onComplete(Bundle values, FacebookException error) {
						if (error != null) {
							if (error instanceof FacebookOperationCanceledException) {
								Gdx.app.postRunnable(new Runnable() {
									public void run() {
										listener.onPostCancelled();
									}
								});
							} else {
								Gdx.app.postRunnable(new Runnable() {
									public void run() {
										listener.onPostFails("Unable to post to FB.");
									}
								});
							}
						} else {
							Gdx.app.postRunnable(new Runnable() {
								public void run() {
									listener.onPostSucceeded();
								}
							});
						}

					}
				}).build();
		feedDialog.show();
	}
}
