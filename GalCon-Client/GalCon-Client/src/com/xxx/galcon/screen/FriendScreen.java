package com.xxx.galcon.screen;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.forever;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.rotateBy;
import static com.xxx.galcon.Constants.GALCON_PREFS;
import static com.xxx.galcon.Util.createShader;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.xxx.galcon.Constants;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.ScreenFeedback;
import com.xxx.galcon.UIConnectionWrapper;
import com.xxx.galcon.UISkin;
import com.xxx.galcon.http.FriendsListener;
import com.xxx.galcon.http.GameAction;
import com.xxx.galcon.http.SocialAction;
import com.xxx.galcon.http.UIConnectionResultCallback;
import com.xxx.galcon.model.Friend;
import com.xxx.galcon.model.GameInviteRequest;
import com.xxx.galcon.model.People;
import com.xxx.galcon.model.Point;
import com.xxx.galcon.model.friends.CombinedFriend;
import com.xxx.galcon.model.friends.FriendCombiner;
import com.xxx.galcon.model.friends.GalConFriend;
import com.xxx.galcon.screen.widget.ActionButton;
import com.xxx.galcon.screen.widget.ScrollList;
import com.xxx.galcon.screen.widget.ShaderLabel;
import com.xxx.galcon.screen.widget.ShaderTextField;
import com.xxx.galcon.screen.widget.WaitImageButton;

public class FriendScreen implements ScreenFeedback {
	
	private MenuScreenContainer previousScreen;
	private InputProcessor oldInputProcessor;


	private AssetManager assetManager;
	private UISkin skin;
	private ShaderProgram fontShader;
	
	private Stage stage;
	private TextureAtlas menusAtlas;
	
	private WaitImageButton waitImage;
	private ActionButton backButton;
	private ShaderTextField searchBox;
	private ShaderLabel noResultsFound;
	private ActionButton searchButton;
	private ImageButton galButton;
	private ImageButton fbButton;
	private ImageButton gpButton;

	
	private Group searchLabelGroup;
	private ScrollList<CombinedFriend> scrollList;
	private List<CombinedFriend> loadedFriends = new ArrayList<CombinedFriend>();
	private int screenState = 1;
	
	
	private GameInviteRequest gameInviteRequest;
	private String returnCode = null;
	private Long mapKey;
	
	private SocialAction socialAction;
	private GameAction gameAction;


	
	public FriendScreen(UISkin skin, AssetManager assetManager, SocialAction socialAction, GameAction gameAction) {
		this.assetManager = assetManager;
		this.skin = skin;
		this.socialAction = socialAction;
		this.gameAction = gameAction;

		fontShader = createShader("data/shaders/font-vs.glsl", "data/shaders/font-fs.glsl");
		menusAtlas = assetManager.get("data/images/menus.atlas", TextureAtlas.class);

	}

	private void initialize() {
		createBg();
		createWaitImage();
		createBackButton();
		createSearchBox();
		createSearchButton();
		createSearchLabels();
		createScrollList();
		createNoResultsFound();
		showFriends();
		showFbButton();
		showGpButton();
		showGalconButton();
		
		Gdx.input.setInputProcessor(stage);
	}


	private void showGalconButton() {
		galButton = new ImageButton(skin, Constants.UI.GALCON_SEARCH_IMAGE);
		galButton.setWidth(Gdx.graphics.getWidth() * 0.2f);
		galButton.setHeight(Gdx.graphics.getHeight() * 0.15f);
		galButton.setX(gpButton.getX() - (galButton.getWidth() * 1.1f));
		galButton.setY(backButton.getY() - (galButton.getHeight() * 0.25f));
		
		stage.addActor(galButton);
		
		galButton.addListener(allClickListener);
		
	}

	private void showGpButton() {
		gpButton = new ImageButton(skin, Constants.UI.GOOGLE_PLUS_SIGN_IN_NORMAL);
		gpButton.setWidth(Gdx.graphics.getWidth() * 0.2f);
		gpButton.setHeight(Gdx.graphics.getHeight() * 0.15f);
		gpButton.setX(fbButton.getX() - (gpButton.getWidth() * 1.1f));
		gpButton.setY(backButton.getY() - (gpButton.getHeight() * 0.25f));
		
		stage.addActor(gpButton);
		
		gpButton.addListener(gpButtonListener);
		
	}

	private void showFbButton() {
		fbButton = new ImageButton(skin, Constants.UI.FACEBOOK_SIGN_IN_BUTTON);
		fbButton.setWidth(Gdx.graphics.getWidth() * 0.2f);
		fbButton.setHeight(Gdx.graphics.getHeight() * 0.15f);
		fbButton.setX(Gdx.graphics.getWidth() - fbButton.getWidth());
		fbButton.setY(backButton.getY() - (fbButton.getHeight() * 0.25f));
		
		stage.addActor(fbButton);
		
		fbButton.addListener(fbButtonListener);
	}

	private void createSearchLabels() {
		float height = Gdx.graphics.getHeight();
		
		searchLabelGroup = new Group();
		searchLabelGroup.setX(5);
		searchLabelGroup.setY(searchBox.getY() - (height * 0.05f));
		
		stage.addActor(searchLabelGroup);
		
	}

	private void createNoResultsFound() {
		float width = Gdx.graphics.getWidth();
		float height = Gdx.graphics.getHeight();
		
		noResultsFound = new ShaderLabel(fontShader, "Unable to find a Match ", skin, Constants.UI.DEFAULT_FONT);
		noResultsFound.setAlignment(Align.center);
		noResultsFound.setWidth(width);
		noResultsFound.setY(height / 2);
		noResultsFound.setVisible(false);
		
		stage.addActor(noResultsFound);
		
	}

	private void createScrollList() {
		float width = Gdx.graphics.getWidth();
		float height = Gdx.graphics.getHeight();
		
		final float tableHeight = height - (height - searchLabelGroup.getY());
		scrollList = new ScrollList<CombinedFriend>(skin) {
			@Override
			public void buildCell(CombinedFriend item, Group group) {
				createPlayerEntry(item, group);
			}
		};
		scrollList.setX(0);
		scrollList.setY(0);
		scrollList.setWidth(width);
		scrollList.setHeight(tableHeight);

		stage.addActor(scrollList);
		
	}
	
	private void createPlayerEntry(CombinedFriend item, Group group) {
		float width = Gdx.graphics.getWidth();
		
		ShaderLabel playerLabel = new ShaderLabel(fontShader, item.getDisplay(), skin, Constants.UI.DEFAULT_FONT);
		playerLabel.setAlignment(Align.center);
		playerLabel.setWidth(width);
		playerLabel.setY(group.getHeight() * 0.4f);
		
		group.addActor(playerLabel);

	}


	private void createSearchButton() {
		Point position = new Point(searchBox.getX() + searchBox.getWidth() + (GraphicsUtils.actionButtonSize * 0.25f), searchBox.getY());
		searchButton = new ActionButton(skin, "okButton", position);
		//searchButton.setDisabled(true);
		
		searchButton.addListener(new ClickListener(){@Override
		public void clicked(InputEvent event, float x, float y) {
			if(!searchButton.isDisabled()){
				if(screenState == 1){
					searchAllUsers();
				}else{
					filterUser();
				}
			}
		}

		private void filterUser() {
			searchBox.getOnscreenKeyboard().show(false);
			if(searchBox.getText().isEmpty()){
				displayPeople(loadedFriends);
			}else{
				List<CombinedFriend> filteredFriends = new ArrayList<CombinedFriend>();
				for(CombinedFriend friend : loadedFriends){
					if(friend.getDisplay().toLowerCase().contains(searchBox.getText().toLowerCase())){
						filteredFriends.add(friend);
					}
				}
				
				displayPeople(filteredFriends);
				
				ShaderLabel label = new ShaderLabel(fontShader, "Filtered Friends: ", skin, Constants.UI.DEFAULT_FONT);
				populateSearchLabelGroup(label);
			}
			
			
			
		}

		private void searchAllUsers() {
			waitImage.setVisible(true);
			scrollList.clearRows();
			UIConnectionWrapper.searchForPlayers(new UIConnectionResultCallback<People>() {
				
				@Override
				public void onConnectionResult(People result) {
					waitImage.setVisible(false);
					if(result == null || result.people.size() == 0){
						noResultsFound.setText("No results found.");
						noResultsFound.setVisible(true);
						ShaderLabel label = new ShaderLabel(fontShader, "", skin, Constants.UI.DEFAULT_FONT);
						populateSearchLabelGroup(label);
					}else{
						ShaderLabel label = new ShaderLabel(fontShader, "Search Results: ", skin, Constants.UI.DEFAULT_FONT);
						populateSearchLabelGroup(label);
						noResultsFound.setVisible(false);
					}
					
					displayPeople(FriendCombiner.combineFriends(new ArrayList<Friend>(), result.people));
					searchBox.getOnscreenKeyboard().show(false);
				}		
				
				@Override
				public void onConnectionError(String msg) {
					waitImage.setVisible(false);
					noResultsFound.setText(msg);
				}
			}, searchBox.getText());
		}});
		
		stage.addActor(searchButton);
		
	}
	
	private void displayPeople(List<CombinedFriend> friends) {
		waitImage.setVisible(false);
		scrollList.clearRows();
		for(final CombinedFriend friend: friends){
			scrollList.addRow(friend, new ClickListener(){@Override
				public void clicked(InputEvent event, float x,
						float y) {
					if(friend.hasGalconAccount()){
						gameInviteRequest = new GameInviteRequest(GameLoop.USER.handle, ((GalConFriend)friend).handle, mapKey);
						returnCode = Action.INVITE_PLAYER;
					}
				}});
		}
	}

	private void createSearchBox() {
		float width = Gdx.graphics.getWidth();
		float height = Gdx.graphics.getHeight();
		
		searchBox = new ShaderTextField(fontShader, "", skin, Constants.UI.TEXT_FIELD);
		searchBox.setMessageText("Search by handle...");
		searchBox.setWidth(width * 0.75f);
		searchBox.setHeight(height * .08f);
		searchBox.setX(width * 0.5f - searchBox.getWidth() * 0.6f);
		searchBox.setY(backButton.getY() - (height * 0.1f));
		searchBox.setOnscreenKeyboard(new ShaderTextField.DefaultOnscreenKeyboard());
		
		searchBox.addListener(new ChangeListener() {
			
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				searchButton.setDisabled(searchBox.getText().length() >= 3);
			}
		});
		
		stage.addActor(searchBox);
		
	}

	private void createBackButton() {
		Point position = new Point(10, 0);
		backButton = new ActionButton(skin, "backButton", position);
		GraphicsUtils.setCommonButtonSize(backButton);
		backButton.setX(position.x);
		backButton.setY(Gdx.graphics.getHeight() - backButton.getHeight() - 5);
		backButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				searchBox.getOnscreenKeyboard().show(false);
				stage.dispose();
				returnCode = Action.BACK;
			}
		});
		stage.addActor(backButton);
	}

	private void createWaitImage() {
		float width = Gdx.graphics.getWidth();
		float height = Gdx.graphics.getHeight();

		waitImage = new WaitImageButton(skin);
		float buttonWidth = .25f * (float) width;
		waitImage.setWidth(buttonWidth);
		waitImage.setHeight(buttonWidth);
		waitImage.setX(width / 2 - buttonWidth / 2);
		waitImage.setY(height / 2 - buttonWidth / 2);
		stage.addActor(waitImage);

		waitImage.start();
		
	}

	private void createBg() {
		float width = Gdx.graphics.getWidth();
		float height = Gdx.graphics.getHeight();
		
		Image bgImage = new Image(menusAtlas.findRegion("bg"));
		bgImage.setX(-2 * width);
		bgImage.setWidth(width * 4);
		bgImage.setY(-0.5f * height);
		bgImage.setHeight(height * 2f);
		bgImage.setColor(0.0f, 0.7f, 0.7f, 0.6f);
		bgImage.setOrigin((float) width * 2.0f, (float) height * 1.0f);
		bgImage.addAction(forever(rotateBy(360, 150)));
		stage.addActor(bgImage);
		
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		stage.act(delta);
		stage.draw();
	}

	@Override
	public void resize(int width, int height) {
		
	}

	@Override
	public void show() {
		stage = new Stage();
		initialize();
		oldInputProcessor = Gdx.input.getInputProcessor();
		Gdx.input.setInputProcessor(stage);
	}

	@Override
	public void hide() {
		Gdx.input.setInputProcessor(oldInputProcessor);
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object getRenderResult() {
		return returnCode;
	}

	@Override
	public void resetState() {
		returnCode = null;
		gameInviteRequest = null;
		mapKey = null;
	}
	
	public MenuScreenContainer getPreviousScreen() {
		return previousScreen;
	}
	
	public GameInviteRequest getGameInviteRequest(){
		return gameInviteRequest;
	}

	public void setPreviousScreen(MenuScreenContainer previousScreen) {
		this.previousScreen = previousScreen;
	}

	public void setMapType(String mapKey) {
		this.mapKey = Long.valueOf(mapKey);
	}
	
	private void populateSearchLabelGroup(Actor actor){
		searchLabelGroup.clearChildren();
		searchLabelGroup.addActor(actor);
	}

	private void showFriends() {
		waitImage.setVisible(true);
		
		ShaderLabel label = new ShaderLabel(fontShader, "Recent Opponents: ", skin, Constants.UI.DEFAULT_FONT);
		populateSearchLabelGroup(label);
		
		UIConnectionWrapper.findFriends(new UIConnectionResultCallback<People>() {
			@Override
			public void onConnectionResult(People result) {
				waitImage.setVisible(false);
				displayPeople(FriendCombiner.combineFriends(new ArrayList<Friend>(), result.people));
			}
			
			@Override
			public void onConnectionError(String msg) {
				waitImage.setVisible(false);				
			}
		}, GameLoop.USER.handle);
	}
	
	private ClickListener allClickListener = new ClickListener(){
		public void clicked(InputEvent event, float x, float y) {
			clearActiveTab("Search by handle.", 1);
			showFriends();
		}
	};
	
	private ClickListener fbButtonListener = new ClickListener(){
		public void clicked(InputEvent event, float x, float y) {
			loadFriends(Constants.Auth.SOCIAL_AUTH_PROVIDER_FACEBOOK);
		};
	};
	
	private ClickListener gpButtonListener = new ClickListener(){
		public void clicked(InputEvent event, float x, float y) {
			loadFriends(Constants.Auth.SOCIAL_AUTH_PROVIDER_GOOGLE);
		};
	};
	
	private void clearActiveTab(String searchMessageText, int state) {
		searchBox.setMessageText(searchMessageText);
		searchBox.setText("");
		searchBox.getOnscreenKeyboard().show(false);
		screenState = state;
		noResultsFound.setVisible(false);
		scrollList.clearRows();
	};
	
	private ClickListener friendClickListener = new ClickListener(){
		public void clicked(InputEvent event, float x, float y) {
			Preferences prefs = Gdx.app.getPreferences(GALCON_PREFS);
			String socialAuthProvider = prefs.getString(Constants.Auth.SOCIAL_AUTH_PROVIDER);
			
			loadFriends(socialAuthProvider);
		}

		
	};
	
	public void loadFriends(String authProvider) {
		waitImage.setVisible(true);
		clearActiveTab("Filter...", 2);
		ShaderLabel label = new ShaderLabel(fontShader, "Friends: ", skin, Constants.UI.DEFAULT_FONT);
		populateSearchLabelGroup(label);

		socialAction.getFriends(new FriendsListener() {
			
			@Override
			public void onFriendsLoadedSuccess(final List<Friend> friends, final String authProviderUsed) {
				
				List<String> authIds = createAuthIdList(friends);
				
				gameAction.findMatchingFriends(new UIConnectionResultCallback<People>() {
					public void onConnectionResult(People result) {
						List<CombinedFriend> combinedFriends = FriendCombiner.combineFriends(friends, result.people);
						loadedFriends = combinedFriends;
						displayPeople(combinedFriends);
					};
					
					public void onConnectionError(String msg) {
						waitImage.setVisible(false);
						noResultsFound.setVisible(true);
						noResultsFound.setText("Failed loading friends.");
					};
				}, authIds, GameLoop.USER.handle, authProviderUsed);
				
			}
			
			@Override
			public void onFriendsLoadedFail(String error) {
				noResultsFound.setText("Unable to load friends.");
				noResultsFound.setVisible(true);
			}
		}, authProvider);
	};
	
	private List<String> createAuthIdList(List<Friend> friends) {
		List<String> authIds = new ArrayList<String>();
		
		for(Friend friend : friends){
			authIds.add(friend.id);
		}
		return authIds;
	}

}
